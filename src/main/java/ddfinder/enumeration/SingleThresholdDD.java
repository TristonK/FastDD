package ddfinder.enumeration;

import ch.javasoft.bitset.LongBitSet;
import ddfinder.differentialdependency.DifferentialDependencySet;
import ddfinder.predicate.PredicateBuilder;
import ddfinder.predicate.PredicateSet;
import ddfinder.utils.ObjectIndexBijection;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author tristonK 2023/2/20
 * Hybrid Approach
 */
public class SingleThresholdDD implements Enumeration{

    /**
     * predicate -> a bitset includes all clues that contains this predicate
    */
     private Map<Integer, LongBitSet> interval2ClueMap;

    private Set<LongBitSet> clueSet;
    private PredicateBuilder predicateBuilder;

    private Map<Integer, Set<Integer>> intervalToIntervalGroup;
    private ObjectIndexBijection<LongBitSet> clueIds;
    private LongBitSet initialClueIDs;

    private Set<LongBitSet> covers;

    public SingleThresholdDD(Set<LongBitSet> clueSet, PredicateBuilder predicateBuilder){
        this.clueSet = clueSet;
        this.predicateBuilder = predicateBuilder;
        this.covers = ConcurrentHashMap.newKeySet();
        this.intervalToIntervalGroup = predicateBuilder.getInterval2IntervalGroups();
    }

    @Override
    public DifferentialDependencySet buildDifferentialDenpendency() {

        long t1 = System.currentTimeMillis();

        buildClueIndexes();

        List<Integer> initialIntervals = new ArrayList<>();
        for(int i = 0; i < PredicateBuilder.getIntervalCnt(); i++){
            initialIntervals.add(i);
        }

        sortIntervalByCount(initialIntervals, initialClueIDs);

        for(int i = 0; i< initialIntervals.size(); i++){
            int pid = initialIntervals.get(i);
            // a valid dd must have at least two predicates
            if(interval2ClueMap.get(pid).cardinality() == 0){
                continue;
            }

            //delete related intervals and split ClueSet
            List<Integer> predicatesLeft = new ArrayList<>(initialIntervals);
            predicatesLeft = predicatesLeft.subList(i + 1, initialIntervals.size());
            predicatesLeft.removeAll(intervalToIntervalGroup.get(i));
            List<Integer> notMatchIntervals = new ArrayList<>();
            for(int j = 0; j < i; j++){
                notMatchIntervals.add(initialIntervals.get(j));
            }

            LongBitSet matchICluesets = interval2ClueMap.get(i);
            Set<LongBitSet> clueSetMatchI = new HashSet<>();
            for(int clueId = matchICluesets.nextSetBit(0); clueId >= 0; clueId = matchICluesets.nextSetBit(clueId + 1)){
                LongBitSet clue = (LongBitSet) clueIds.getObject(clueId).clone();
                notMatchIntervals.forEach(clue::clear);
                clueSetMatchI.add(clue);
            }
            Set<LongBitSet> partialCovers =  hybridEI(predicatesLeft, notMatchIntervals, clueSetMatchI);
            for(LongBitSet partialCover: partialCovers){
                partialCover.set(pid);
                covers.add(partialCover);
            }
        }
        System.out.println("[enumration] cost: " + (System.currentTimeMillis() - t1));
        System.out.println("finish finding # size " + covers.size());
        DifferentialDependencySet dds = new DifferentialDependencySet(covers, predicateBuilder.getIntervalPredicateMap());
        System.out.println("dds size # " + dds.size());
        return dds;
    }

    private void sortIntervalByCount(List<Integer> currIntervals, LongBitSet currClues){
        long[] intervalCounts = new long[PredicateBuilder.getIntervalCnt()];
        for(int iid: currIntervals){
            LongBitSet iid2Clues = (LongBitSet) currClues.clone();
            iid2Clues.and(interval2ClueMap.get(iid));
            intervalCounts[iid] = iid2Clues.cardinality();
        }
        currIntervals.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return Long.compare(intervalCounts[o1], intervalCounts[o2]);
            }
        });
    }

    private void buildClueIndexes() {

        interval2ClueMap = new HashMap<>(PredicateBuilder.getIntervalCnt());

        for (int pid = 0; pid < PredicateBuilder.getIntervalCnt(); pid++) {
            interval2ClueMap.put(pid, new LongBitSet());
        }

        initialClueIDs = new LongBitSet();
        clueIds = new ObjectIndexBijection<>();

        for(LongBitSet clue: clueSet){
            int clueId = clueIds.getIndex(clue);
            initialClueIDs.set(clueId);
            for(int iid = clue.nextSetBit(0); iid >= 0; iid = clue.nextSetBit(iid+1)){
                interval2ClueMap.get(iid).set(clueId);
            }
        }

    }

    private Set<LongBitSet> hybridEI(List<Integer> intervals, List<Integer> notMatchIntervals, Set<LongBitSet> clueSet){
        Set<LongBitSet> evidenceInversionCovers = new HashSet<>();
        for(int interval : intervals){
            LongBitSet bs = new LongBitSet();
            bs.set(interval);
            evidenceInversionCovers.add(bs);
        }
        for(LongBitSet clue: clueSet){
            handleClue(clue, evidenceInversionCovers, intervals);
        }
        return evidenceInversionCovers;
    }

    private void handleClue(LongBitSet clue, Set<LongBitSet> covers, List<Integer> intervalSpace){
        Set<LongBitSet> coversMinus = new HashSet<>();
        for(LongBitSet cover: covers){
            if(cover.isSubSetOf(clue)) {
                coversMinus.add(cover);
            }
        }
        covers.removeAll(coversMinus);
        List<Integer> clueInterval = new ArrayList<>();
        for(int i = clue.nextSetBit(0); i >= 0; i = clue.nextSetBit(i+1)){
            clueInterval.add(i);
        }
        List<Integer> ispace = new ArrayList<>(intervalSpace);
        ispace.removeAll(clueInterval);
        for(LongBitSet coverMinus: coversMinus){
            for(int interval : ispace){
                coverMinus.set(interval);
                boolean exist = false;
                for(LongBitSet bs : covers){
                    if(bs.isSubSetOf(coverMinus)){exist = true; break;}
                }
                if(!exist){
                    LongBitSet nbs = new LongBitSet(coverMinus);
                    covers.add(nbs);
                }
                coverMinus.clear(interval);
            }
        }
    }

    private void minimize(LongBitSet covers){

    }
}
