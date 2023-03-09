package ddfinder.enumeration;

import ch.javasoft.bitset.LongBitSet;
import ch.javasoft.bitset.search.TranslatingTreeSearch;
import ddfinder.differentialdependency.DifferentialDependencySet;
import ddfinder.evidence.Evidence;
import ddfinder.evidence.EvidenceSet;
import ddfinder.predicate.Predicate;
import ddfinder.predicate.PredicateBuilder;
import ddfinder.predicate.PredicateSet;
import ddfinder.utils.ObjectIndexBijection;
import de.metanome.algorithms.dcfinder.helpers.IndexProvider;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author tristonK 2023/2/27
 */
public class HybridEvidenceInversion implements Enumeration{
    PredicateSet predicates;
    HashMap<Integer, Set<Integer>> pred2PredGroupMap;
    Set<LongBitSet> covers;
    EvidenceSet evidenceSet;
    Map<Integer, LongBitSet> predSatisfiedEvidenceSet;

    LongBitSet evidenceBitSet;

    IndexProvider<Predicate> predicateIndexProvider;
    public HybridEvidenceInversion(EvidenceSet evidenceSet, PredicateBuilder predicateBuilder){
        this.predicates = new PredicateSet(predicateBuilder.getPredicates().size());
        this.pred2PredGroupMap = new HashMap<>();
        this.covers = new HashSet<>();
        this.evidenceSet = evidenceSet;
        predicateIndexProvider = predicateBuilder.getPredicateIdProvider();
        for(int col = 0; col < predicateBuilder.getColSize(); col++){
            HashSet<Integer> pids = new HashSet<>();
            for(Predicate predicate: predicateBuilder.getColPredicates(col)){
                pids.add(predicateBuilder.getPredicateId(predicate));
            }
            for(int pid: pids){
                pred2PredGroupMap.put(pid, pids);
            }
        }
    }

    /**
     * @return dds
     */
    @Override
    public DifferentialDependencySet buildDifferentialDenpendency() {
        buildClueIndexes();

        List<Integer> preds = new ArrayList<>();
        for(int i = 0; i< predicates.size();i++){
            preds.add(i);
        }
        preds.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return predSatisfiedEvidenceSet.get(o1).cardinality() - predSatisfiedEvidenceSet.get(o2).cardinality();
            }
        });

        for(int i = 0; i < preds.size(); i++){
            int satisfiedPid = preds.get(i);
            if (predSatisfiedEvidenceSet.get(satisfiedPid).cardinality() == 0){
                // dd must have at least two predicates
                continue;
            }
            List<Integer> currPredicateSpace = new ArrayList<>(preds);
            currPredicateSpace = currPredicateSpace.subList(i+1, currPredicateSpace.size());
            currPredicateSpace.removeAll(pred2PredGroupMap.get(satisfiedPid));

            Set<Integer> predsNotSatisfied = new HashSet<>();
            for(int j = 0; j < i; j++){predsNotSatisfied.add(preds.get(j));}

            LongBitSet currEvidenceSet = predSatisfiedEvidenceSet.get(satisfiedPid);
            List<LongBitSet> currEvidences = new ArrayList<>();
            for(int eviId = currEvidenceSet.nextSetBit(0); eviId >= 0; eviId = currEvidenceSet.nextSetBit(eviId + 1)){
                LongBitSet bs = evidenceSet.getEvidenceById(eviId).getBitset().clone();
                pred2PredGroupMap.get(satisfiedPid).forEach(bs::clear);
                predsNotSatisfied.forEach(bs::clear);
                currEvidences.add(bs);
            }

            Set<LongBitSet> partialCovers =  hybridEI(currPredicateSpace, predsNotSatisfied, currEvidences);
            for(LongBitSet partialCover : partialCovers){
                partialCover.set(satisfiedPid);
                covers.add(partialCover);
            }
        }

        return new DifferentialDependencySet(covers, predicateIndexProvider);

    }

    private void buildClueIndexes() {

        predSatisfiedEvidenceSet = new HashMap<>(predicates.size());
        for(int i = 0; i < predicates.size(); i++){
            predSatisfiedEvidenceSet.put(i, new LongBitSet());
        }
        evidenceBitSet = new LongBitSet();
        for(int evidenceId = 0; evidenceId <evidenceSet.size(); evidenceId++){
            evidenceBitSet.set(evidenceId);
            LongBitSet bs = evidenceSet.getEvidenceById(evidenceId).getBitset();
            for(int pid = bs.nextSetBit(0); pid >= 0; pid = bs.nextSetBit(pid+1)){
                predSatisfiedEvidenceSet.get(pid).set(evidenceId);
            }
        }


    }

    private Set<LongBitSet> hybridEI(List<Integer> predicateSpace, Set<Integer> notMatchPredicates, List<LongBitSet> evidenceBitSets){
        Set<LongBitSet> evidenceInversionCovers = new HashSet<>();
        for(int predicate : predicateSpace){
            LongBitSet bs = new LongBitSet();
            bs.set(predicate);
            evidenceInversionCovers.add(bs);
        }
        for(LongBitSet evidenceBitSet: evidenceBitSets){
            handleEvidence(evidenceBitSet, evidenceInversionCovers, predicateSpace);
        }
        return evidenceInversionCovers;
    }

    private void handleEvidence(LongBitSet evidenceBitSet, Set<LongBitSet> covers, List<Integer> predicateSpace){
        Set<LongBitSet> coversMinus = new HashSet<>();
        for(LongBitSet cover: covers){
            if(cover.isSubSetOf(evidenceBitSet)) {
                coversMinus.add(cover);
            }
        }
        covers.removeAll(coversMinus);
        List<Integer> predsInEvidence = new ArrayList<>();
        for(int i = evidenceBitSet.nextSetBit(0); i >= 0; i = evidenceBitSet.nextSetBit(i+1)){
            predsInEvidence.add(i);
        }
        List<Integer> pspace = new ArrayList<>(predicateSpace);
        pspace.removeAll(predsInEvidence);
        for(LongBitSet coverMinus: coversMinus){
            for(int pred : pspace){
                boolean flag = true;
                for(int related: pred2PredGroupMap.get(pred)){
                    if(coverMinus.get(related)){flag = false; break;}
                }
                if(!flag){continue;}
                coverMinus.set(pred);
                boolean exist = false;
                for(LongBitSet bs : covers){
                    if(bs.isSubSetOf(coverMinus)){exist = true; coverMinus.clear(pred); break;}
                }
                if(!exist){
                    LongBitSet nbs = coverMinus.clone();
                    covers.add(nbs);
                }
                coverMinus.clear(pred);
            }
        }
    }

    void minimize(){
        Set<LongBitSet> minimizeCovers = new HashSet<>();
    }



}