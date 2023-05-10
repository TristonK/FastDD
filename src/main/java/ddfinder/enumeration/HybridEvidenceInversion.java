package ddfinder.enumeration;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.LongBitSet;
import ch.javasoft.bitset.search.NTreeSearch;
import ch.javasoft.bitset.search.TranslatingTreeSearch;
import ddfinder.differentialdependency.DifferentialDependencySet;
import ddfinder.evidence.Evidence;
import ddfinder.evidence.EvidenceSet;
import ddfinder.predicate.Predicate;
import ddfinder.predicate.PredicateBuilder;
import ddfinder.predicate.PredicateSet;
import ddfinder.predicate.PredicateSpace;
import ddfinder.search.TranslatingMinimizeTree;
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
    Set<IBitSet> covers;
    EvidenceSet evidenceSet;
    Map<Integer, LongBitSet> predSatisfiedEvidenceSet;

    LongBitSet evidenceBitSet;

    IndexProvider<Predicate> predicateIndexProvider;

    LongBitSet pivotPredicates;

    List<Integer> acceptedPredicates;

    List<BitSet> colToPredicatesGroup;
    public HybridEvidenceInversion(EvidenceSet evidenceSet, PredicateBuilder predicateBuilder){
        this.predicates = new PredicateSet(predicateBuilder.getPredicates().size());
        this.pred2PredGroupMap = new HashMap<>();
        this.covers = new HashSet<>();
        this.evidenceSet = evidenceSet;
        this.colToPredicatesGroup = new ArrayList<>();
        for(BitSet bs: predicateBuilder.getColPredicateGroup()){
            colToPredicatesGroup.add((BitSet) bs.clone());
        }
        predicateIndexProvider = predicateBuilder.getPredicateIdProvider();
        LongBitSet acceptPredicatesBitSet = predicateBuilder.getAcceptPredicatesBitSet();
        this.acceptedPredicates = new ArrayList<>();
        for(int i = acceptPredicatesBitSet.nextSetBit(0); i >= 0; i = acceptPredicatesBitSet.nextSetBit(i + 1)){
            this.acceptedPredicates.add(i);
        }
        pivotPredicates = predicateBuilder.getRejectPredicatesBitSet();
        for (BitSet bitSet : colToPredicatesGroup) {
            HashSet<Integer> pids = new HashSet<>();
            for (int i = bitSet.nextSetBit(0); i >= 0; i = bitSet.nextSetBit(i + 1)) {
                pids.add(i);
            }
            for (int pid : pids) {
                pred2PredGroupMap.put(pid, pids);
            }
        }
        //System.out.println(pivotPredicates.toString());
        for(BitSet bs: colToPredicatesGroup){
            bs.andNot(pivotPredicates.toBitSet());
        }
        constructMinimizeTree(predicateBuilder);
    }

    /**
     * @return dds
     */
    @Override
    public DifferentialDependencySet buildDifferentialDenpendency() {
        buildClueIndexes();

        List<Integer> preds = new ArrayList<>();
        for(int i = pivotPredicates.nextSetBit(0); i >= 0; i = pivotPredicates.nextSetBit(i + 1)){
            preds.add(i);
        }
        //TODO: 可能需要修改排序规则，这里是右侧的选择
        preds.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return predSatisfiedEvidenceSet.get(o1).cardinality() - predSatisfiedEvidenceSet.get(o2).cardinality();
            }
        });

        int minimizeBefore = 0;

        for(int i = 0; i < preds.size(); i++){
            int satisfiedPid = preds.get(i);
            if (predSatisfiedEvidenceSet.get(satisfiedPid).cardinality() == 0){
                // dd must have at least two predicates
                continue;
            }
            //List<Integer> currPredicateSpace = new ArrayList<>(predicates);
            List<Integer> currPredicateSpace = new ArrayList<>(acceptedPredicates);
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

            int satisfiedCol = predicateIndexProvider.getObject(satisfiedPid).getOperand().getColumn().getIndex();
            Set<IBitSet> partialCovers =
                new EvidenceInversion(satisfiedCol, colToPredicatesGroup, predsNotSatisfied, currEvidences, predicates.size()).getCovers();

            //minimizeBefore += partialCovers.size();


            for(IBitSet partialCover : partialCovers){
                partialCover.set(satisfiedPid);
                covers.add((LongBitSet) partialCover);
            }
        }

        System.out.println("[Minimize] # before: " + covers.size());
        long minimizeTime = System.currentTimeMillis();
        TranslatingMinimizeTree minimizeTree = new TranslatingMinimizeTree(intervalSize,predicateId2NodeId, col2Interval,
                col2PredicateId, colSize, intervalLength);
        covers = minimizeTree.minimize(new ArrayList<>(covers));
        System.out.println("[Minimize TIME]: " + (System.currentTimeMillis() - minimizeTime));
        System.out.println("[Minimize] # after: " + covers.size());

        //covers = minimize();

        //System.out.println("[Minimize] # after: " + covers.size());

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

   /* private Set<LongBitSet> minimize(){
        long t1 = System.currentTimeMillis();

        NTreeSearch nt = new NTreeSearch();
        for (LongBitSet key : covers) {
            nt.add(LongBitSet.FACTORY.create(key));
        }

        Set<LongBitSet> nonGeneralized = new HashSet<>();

        for (LongBitSet key : covers) {

            IBitSet bs = LongBitSet.FACTORY.create(key);

            nt.remove(bs);

            if (!nt.containsSubset(bs)) {
                nonGeneralized.add(key);
            }

            nt.add(bs);

        }
        covers = nonGeneralized;

        Set<LongBitSet> minimizeCovers = new HashSet<>();
        LongBitSet[] coversArray = covers.toArray(new LongBitSet[0]);
        boolean[] flags = new boolean[coversArray.length];
        Arrays.fill(flags, true);
        for(int i = 0; i < coversArray.length; i++){
            for(int j = i+1; j< coversArray.length; j++){
                LongBitSet bs1 = coversArray[i].clone();
                bs1.xor(coversArray[j]);
                if(bs1.cardinality() == 2){
                    int pid1 = bs1.nextSetBit(0);
                    int pid2 = bs1.nextSetBit(pid1+1);
                    int hasPid1Index = i, hasPid2Index = j;
                    if(!coversArray[i].get(pid1)){hasPid2Index = i; hasPid1Index = j;}
                    int cmpAns = predicateIndexProvider.getObject(pid1).comparePredicate(predicateIndexProvider.getObject(pid2));
                    // save pid1
                    if(cmpAns == 1){
                        flags[hasPid2Index] = false;
                    }
                    // save pid2
                    else if(cmpAns == -1){
                        flags[hasPid1Index] = false;
                    }
                }
            }
        }
        for(int i = 0; i < coversArray.length; i++){
            if(flags[i]){minimizeCovers.add(coversArray[i]);}
        }

        System.out.println("[Minimize] cost "+ (System.currentTimeMillis() - t1));
        return minimizeCovers;
    }*/

    //所有的interval个数
    int intervalSize;
    // predicate的id对应到node的id，先所有属性的的小于等于，再所有属性的大于
    int[] predicateId2NodeId;
    // 列对应的interval起始点，也就是从这一位开始设置
    int[] col2Interval;
    // 列对应的第一个谓词的id
    int[] col2PredicateId;
    int colSize;
    // 列i对应的interval个数
    int[] intervalLength;
    private void constructMinimizeTree(PredicateBuilder predicateBuilder){
        colSize = predicateBuilder.getColSize();
        intervalSize = 0;
        predicateId2NodeId = new int[predicateBuilder.size()];
        col2Interval = new int[colSize];
        col2PredicateId = new int[colSize];
        intervalLength = new int[colSize];
        List<BitSet>  colPredicateGroup = predicateBuilder.getColPredicateGroup();
        for(int i = 0; i < colSize; i++){
            BitSet bs = colPredicateGroup.get(i);
            //System.out.println(bs.toString());
            col2Interval[i] = intervalSize;
            intervalLength[i] = bs.cardinality()/2;
            intervalSize += intervalLength[i];
            int cnt = 0;
            for(int j = bs.nextSetBit(0); j >= 0; j = bs.nextSetBit(j + 1)){
                if(cnt == 0){
                    col2PredicateId[i] = j;
                }
                if(cnt < intervalLength[i]){
                    predicateId2NodeId[j] = i;
                }else{
                    predicateId2NodeId[j] = i + colSize;
                }
                cnt++;
            }
        }
    }



}