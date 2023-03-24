package ddfinder.evidence;

import ch.javasoft.bitset.LongBitSet;
import ddfinder.pli.*;
import ddfinder.predicate.PredicateBuilder;
import ddfinder.utils.StringCalculation;
import org.roaringbitmap.RoaringBitmap;

import java.util.*;

/**
 * @author tristonK 2023/3/13
 */
public class ClueContextSetBuilder extends ClueSetBuilder{
    private final List<IPli> plis;
    private final int tidBeg, tidRange;

    private final double ERR = 0.000000001;
    LongBitSet allSameClue;

    public ClueContextSetBuilder(PliShard shard) {
        plis = shard.plis;
        if(shard.beg != 0){
            throw new IllegalArgumentException("Not Support, please input a full PLI");
        }
        tidBeg = shard.beg;
        tidRange = shard.end - shard.beg;
        allSameClue = new LongBitSet(PredicateBuilder.getIntervalCnt());
        for(Integer start: startPositions){
            allSameClue.set(start);
        }
    }

    public HashMap<LongBitSet, Long> buildClueSet() {
        HashMap<LongBitSet, Long> ret = new HashMap<>();
        for(int i = 0; i < tidRange; i++){
            //System.out.println("check "+ i);
            caclulateTid(i).forEach(clueContext -> ret.merge(clueContext.getClue(), clueContext.getRightSide().getLongCardinality(), Long::sum));
        }
        return ret;
    }

    private Set<clueContext> caclulateTid(int tid){
        Set<clueContext> clueContextSet = new HashSet<>();
        clueContextSet.add(new clueContext(tid, tidRange, allSameClue));
        for(ClueSetBuilder.PredicatePack intPack: intPacks){
            correctInterger(plis.get(intPack.colIndex), intPack.pos, intPack.thresholds, clueContextSet, tid);
        }
        for(ClueSetBuilder.PredicatePack doublePack: doublePacks){
            correctDouble(plis.get(doublePack.colIndex), doublePack.pos, doublePack.thresholds, clueContextSet, tid);
        }
        for(ClueSetBuilder.PredicatePack strPack : strPacks){
            correctStr(plis.get(strPack.colIndex), strPack.pos, strPack.thresholds, clueContextSet, tid);
        }
        return clueContextSet;
    }

    private void setNumMask(RoaringBitmap roaringBitmap, int pos, int startPos, Set<clueContext> clueContextSet) {
        if(roaringBitmap == null) {return;}
        Set<clueContext> toAdd = new HashSet<>();
        for(clueContext clueContext: clueContextSet){
            clueContext newClueContext = clueContext.split(roaringBitmap, startPos, pos);
            if(newClueContext  != null){toAdd.add(newClueContext);}
        }
        clueContextSet.addAll(toAdd);

    }

    private void correctStr(IPli pli, int pos, List<Double>thresholds, Set<clueContext> clueContextSet, int tid) {
        int pliId = pli.getClusterIdByRow(tid);
        String[] probeKeys = (String[])  pli.getKeys();
        String key = probeKeys[pliId];
        for (int i = 0; i < pli.size(); i++) {
            if(i == pliId){continue;}
            if(pli.getThresholdsBetween(i, pliId) != -1){
                setNumMask(pli.get(i).getRbm(), pos + pli.getThresholdsBetween(i, pliId), pos, clueContextSet);
                continue;
            }
            int diff = StringCalculation.getDistance(key, probeKeys[i]);
            int c = 0;
            if(diff < ERR + thresholds.get(0)){
                c = 0;
            } else if (diff > ERR + thresholds.get(thresholds.size()-1)) {
                c = thresholds.size();
            }else{
                while(c < thresholds.size()-1){
                    if(diff > thresholds.get(c) + ERR && diff < ERR + thresholds.get(c+1)){
                        c++;
                        break;
                    }
                    c++;
                }
            }
            setNumMask(pli.get(i).getRbm(), pos + c, pos, clueContextSet);
            pli.setThresholdsBetween(pliId, i, c);
        }
    }

    private void correctDouble(IPli pli, int pos, List<Double>thresholds, Set<clueContext> clueContextSet, int tid) {
        int pliId = pli.getClusterIdByRow(tid);
        Double[] probeKeys = (Double[]) pli.getKeys();
        Double key = probeKeys[pliId];
        for (int i = 0; i < pli.size(); i++) {
            if(i == pliId){continue;}
            if(pli.getThresholdsBetween(i, pliId) != -1){
                setNumMask(pli.get(i).getRbm(), pos + pli.getThresholdsBetween(i, pliId), pos, clueContextSet);
                continue;
            }
            double diff = Math.abs(key - probeKeys[i]);
            int c = 0;
            if(diff < ERR + thresholds.get(0)){
                c = 0;
            } else if (diff > ERR + thresholds.get(thresholds.size()-1)) {
                c = thresholds.size();
            }else{
                while(c < thresholds.size()-1){
                    if(diff > thresholds.get(c) + ERR && diff < ERR + thresholds.get(c+1)){
                        c++;
                        break;
                    }
                    c++;
                }
            }
            setNumMask(pli.get(i).getRbm(), pos + c, pos, clueContextSet);
            pli.setThresholdsBetween(pliId, i, c);
        }
        /*int start = 0;
        for(int index = thresholds.size()-1; index >= 0 && start < pli.size(); index--){
            int end = pli.getFirstIndexWhereKeyIsLT(key+thresholds.get(index), start, 0);
            setNumMask(mergeRbm(pli, start, end), pos + index + 1, pos, clueContextSet);
            start = end;
        }
        if(start >= probeKeys.length){
            return;
        }
        start = pliId + 1;
        for(int index = 1; index < thresholds.size() && start < probeKeys.length; index++){
            int end = pli.getFirstIndexWhereKeyIsLT(key -thresholds.get(index), start, 1);
            setNumMask(mergeRbm(pli, start, end), pos + index + 1, pos, clueContextSet);
            start = end;
        }
        if(start < probeKeys.length){
            setNumMask(mergeRbm(pli, start, probeKeys.length), pos + thresholds.size(), pos, clueContextSet);
        }*/

    }

    private void correctInterger(IPli pli, int pos, List<Double>thresholds, Set<clueContext> clueContextSet, int tid) {
        int pliId = pli.getClusterIdByRow(tid);
        Integer[] probeKeys = (Integer[]) pli.getKeys();
        Integer key = probeKeys[pliId];
        for (int i = 0; i < pli.size(); i++) {
            if(i == pliId){continue;}
            if(pli.getThresholdsBetween(i, pliId) != -1){
                setNumMask(pli.get(i).getRbm(), pos + pli.getThresholdsBetween(i, pliId), pos, clueContextSet);
                continue;
            }
            int diff = Math.abs(key - probeKeys[i]);
            int c = 0;
            if(diff < ERR + thresholds.get(0)){
                c = 0;
            } else if (diff > ERR + thresholds.get(thresholds.size()-1)) {
                c = thresholds.size();
            }else{
                while(c < thresholds.size()-1){
                    if(diff > thresholds.get(c) + ERR && diff < ERR + thresholds.get(c+1)){
                        c++;
                        break;
                    }
                    c++;
                }
            }
            setNumMask(pli.get(i).getRbm(), pos + c, pos, clueContextSet);
            pli.setThresholdsBetween(pliId, i, c);
        }
        /*int start = 0;
        for(int index = thresholds.size()-1; index >= 0 && start < pli.size(); index--){
            int end = pli.getFirstIndexWhereKeyIsLT(key+thresholds.get(index).intValue(), start, 0);
            setNumMask(mergeRbm(pli, start, end), pos + index + 1, pos, clueContextSet);
            start = end;
        }
        if(pliId + 1 == probeKeys.length){
            return;
        }
        start = pliId + 1;
        for(int index = 1; index < thresholds.size() && start < probeKeys.length; index++){
            int end = pli.getFirstIndexWhereKeyIsLT(key -thresholds.get(index).intValue(), start, 1);
            setNumMask(mergeRbm(pli, start, end), pos + index + 1, pos, clueContextSet);
            start = end;
        }
        if(start < probeKeys.length){
            setNumMask(mergeRbm(pli, start, probeKeys.length), pos + thresholds.size(), pos, clueContextSet);
        }*/

    }

    /**
     * merge [start, end) rbm on pli
     * */
    RoaringBitmap mergeRbm(IPli pli, int start, int end){
        if(start >= end){return null;}
        if(start + 1 == end){return pli.get(start).getRbm();}
        RoaringBitmap ret = RoaringBitmap.or(pli.get(start).getRbm(), pli.get(start+1).getRbm());
        for(int i = start + 2; i < end; i++){
            ret.or(pli.get(i).getRbm());
        }
        return ret;
    }
}
