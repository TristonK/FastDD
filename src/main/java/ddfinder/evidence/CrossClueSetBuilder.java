package ddfinder.evidence;

import ch.javasoft.bitset.LongBitSet;
import ddfinder.evidence.ClueSetBuilder;
import ddfinder.pli.Cluster;
import ddfinder.pli.DoublePli;
import ddfinder.pli.IPli;
import ddfinder.pli.PliShard;
import ddfinder.predicate.PredicateBuilder;
import ddfinder.utils.StringCalculation;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * To build the clue set of two Pli shards
 */
public class CrossClueSetBuilder extends ClueSetBuilder {

    private final List<IPli> plis1, plis2;
    private final int evidenceCount;

    private final double ERR = 0.000000001;
    private long[] forwardClues;
    private long[] bases;
    private IClueOffset calUtils;


    public CrossClueSetBuilder(PliShard shard1, PliShard shard2, IClueOffset calUtils) {
        plis1 = shard1.plis;
        plis2 = shard2.plis;
        evidenceCount = (shard1.end - shard1.beg) * (shard2.end - shard2.beg);
        this.calUtils = calUtils;
    }
    private void tempCalBase(){
        long[] temp = new long[bases.length];
        for (PredicatePack intPack: intPacks){
            temp[intPack.colIndex] = intPack.thresholds.size()+1;
        }
        for (PredicatePack numPack: doublePacks){
            temp[numPack.colIndex] = numPack.thresholds.size()+1;
        }
        for(PredicatePack strPack: strPacks){
            temp[strPack.colIndex] = strPack.thresholds.size()+1;
        }
        bases[0] = 0;
        bases[1] = temp[0];
        for(int i = 2; i < bases.length; i++){
            bases[i] = bases[i - 1] * temp[i - 1];
        }
    }
    public HashMap<LongBitSet, Long> linearBuildClueSet() {
        forwardClues = new long[evidenceCount];
        bases = new long[strPacks.size()+ intPacks.size()+doublePacks.size()];
        tempCalBase();
        for(PredicatePack strPack: strPacks){
            correctStr(plis1.get(strPack.colIndex), plis2.get(strPack.colIndex), strPack.colIndex, strPack.thresholds);
        }
        for (PredicatePack intPack: intPacks){
            linerCorrectNum(plis1.get(intPack.colIndex), plis2.get(intPack.colIndex), intPack.colIndex, intPack.thresholds);
        }
        for (PredicatePack numPack: doublePacks){
            linerCorrectNum(plis1.get(numPack.colIndex), plis2.get(numPack.colIndex), numPack.colIndex, numPack.thresholds);
        }
        //TODO
        return null;
        //return accumulateClues(forwardClues);
    }


    private void setNumMask(IPli pli1, int i, IPli pli2, int j, int colIndex, int offset) {

        int beg1 = pli1.getPliShard().beg, beg2 = pli2.getPliShard().beg;
        int range2 = pli2.getPliShard().end - beg2;

        for (int tid1 : pli1.get(i).getRawCluster()) {
            int t1 = tid1 - beg1;
            int r1 = t1 * range2 - beg2;
            for (int tid2 : pli2.get(j).getRawCluster()) {
                forwardClues[r1 + tid2] *= bases[colIndex] * offset;
                //clues2[(tid2 - beg2) * range1 + t1].set(pos);
            }
        }

    }

    private void correctStr(IPli pivotPli, IPli probePli, int colIndex, List<Double>thresholds) {
        final String[] pivotKeys = (String[]) pivotPli.getKeys();
        final String[] probeKeys = (String[]) probePli.getKeys();
        for(int i = 0; i < pivotKeys.length; i++){
            for(int j = 0; j < probeKeys.length; j++){
                int diff = StringCalculation.getDistance(pivotKeys[i], probeKeys[j]);
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
                setNumMask(pivotPli, i, probePli, j, colIndex, c);
            }
        }
    }

    private void linerCorrectNum(IPli pivotPli, IPli probePli, int colIndex, List<Double>thresholds){
        for(int i = 0; i < pivotPli.size(); i++){
            int[] offsets;
            if(pivotPli.getClass() == DoublePli.class){
                offsets = calUtils.linerCountDouble((Double[]) probePli.getKeys(), 0 , (Double) pivotPli.getKeys()[i], thresholds);
            }else{
                offsets = calUtils.linerCountInt((Integer[]) probePli.getKeys(), 0, (Integer) pivotPli.getKeys()[i], thresholds);
            }
            for(int j = 0; j < probePli.size(); j++){
                setNumMask(pivotPli, i, probePli, j, colIndex, offsets[j]);
            }
        }
    }

}