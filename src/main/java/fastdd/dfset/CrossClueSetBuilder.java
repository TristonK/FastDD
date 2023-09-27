package fastdd.dfset;

import ch.javasoft.bitset.LongBitSet;
import fastdd.pli.DoublePli;
import fastdd.pli.IPli;
import fastdd.pli.PliShard;
import fastdd.differentialfunction.DifferentialFunctionBuilder;
import fastdd.utils.DistanceCalculation;

import java.util.HashMap;
import java.util.List;

/**
 * To build the clue set of two Pli shards
 */
public class CrossClueSetBuilder extends ClueSetBuilder {

    private final List<IPli> plis1, plis2;
    private final int evidenceCount;

    private final double ERR = 0.000000001;
    private long[] forwardClues;//用来代替longbitset，实验测试
    private long[] bases;
    private IClueOffset calUtils;

    public CrossClueSetBuilder(PliShard shard1, PliShard shard2, IClueOffset calUtils) {
        plis1 = shard1.plis;
        plis2 = shard2.plis;
        evidenceCount = (shard1.end - shard1.beg) * (shard2.end - shard2.beg);
        this.calUtils = calUtils;
    }

    public HashMap<LongBitSet, Long> buildClueSet() {
        LongBitSet[] forwardClues = new LongBitSet[evidenceCount];   // plis1 -> plis2
        for (int i = 0; i < evidenceCount; i++) {
            forwardClues[i] = new LongBitSet(DifferentialFunctionBuilder.getIntervalCnt());//TODO: n个阈值有n+1个interval，这里需要修改（eg:54 => 45）
        }
        for (PredicatePack strPack : strPacks) {
            correctStr(forwardClues, plis1.get(strPack.colIndex), plis2.get(strPack.colIndex), strPack.pos, strPack.thresholds);
        }
        for (PredicatePack intPack : intPacks) {
            linerCorrectNum(forwardClues, plis1.get(intPack.colIndex), plis2.get(intPack.colIndex), intPack.pos, intPack.thresholds);
//            correctInteger(forwardClues, plis1.get(intPack.colIndex), plis2.get(intPack.colIndex), intPack.pos, intPack.thresholds);
        }
        for (PredicatePack numPack : doublePacks) {
            linerCorrectNum(forwardClues, plis1.get(numPack.colIndex), plis2.get(numPack.colIndex), numPack.pos, numPack.thresholds);
//            correctNum(forwardClues, plis1.get(numPack.colIndex), plis2.get(numPack.colIndex), numPack.pos, numPack.thresholds);
        }


        return accumulateClues(forwardClues);
    }


    static public long timeCnt = 0;

    private void setNumMask(LongBitSet[] clues1, IPli pli1, int i, IPli pli2, int j, int pos) {

        int beg1 = pli1.getPliShard().beg, beg2 = pli2.getPliShard().beg;
        int range2 = pli2.getPliShard().end - beg2;
        for (int tid1 : pli1.get(i).getRawCluster()) {
            int t1 = tid1 - beg1;
            int r1 = t1 * range2 - beg2;
            for (int tid2 : pli2.get(j).getRawCluster()) {
                clues1[r1 + tid2].set(pos);
                //clues2[(tid2 - beg2) * range1 + t1].set(pos);
            }
        }

    }

    private void correctStr(LongBitSet[] clues1, IPli pivotPli, IPli probePli, int pos, List<Double> thresholds) {
        final String[] pivotKeys = (String[]) pivotPli.getKeys();
        final String[] probeKeys = (String[]) probePli.getKeys();
        for (int i = 0; i < pivotKeys.length; i++) {
            for (int j = 0; j < probeKeys.length; j++) {
                int diff = DistanceCalculation.StringDistance(pivotKeys[i], probeKeys[j]);
                int c = 0;
                if (diff < ERR + thresholds.get(0)) {
                    c = 0;
                } else if (diff > ERR + thresholds.get(thresholds.size() - 1)) {
                    c = thresholds.size();
                } else {
                    while (c < thresholds.size() - 1) {
                        if (diff > thresholds.get(c) + ERR && diff < ERR + thresholds.get(c + 1)) {
                            c++;
                            break;
                        }
                        c++;
                    }
                }
                setNumMask(clues1, pivotPli, i, probePli, j, pos + c);
            }
        }
    }

    private void correctNum(LongBitSet[] forwardArray, IPli pivotPli, IPli probePli, int pos, List<Double> thresholds) {
        final Double[] pivotKeys = (Double[]) pivotPli.getKeys();
        final Double[] probeKeys = (Double[]) probePli.getKeys();

        for (int i = 0; i < pivotKeys.length; i++) {
            int start = 0;
            for (int index = thresholds.size() - 1; index >= 0 && start < probeKeys.length; index--) {
                int end = probePli.getFirstIndexWhereKeyIsLT(pivotKeys[i] + thresholds.get(index), start, 0);
                for (int j = start; j < end; j++) {
                    setNumMask(forwardArray, pivotPli, i, probePli, j, pos + index + 1);
                }
                start = end;
            }
            if (start >= probeKeys.length) {
                continue;
            }
            if (Math.abs(probeKeys[start] - pivotKeys[i]) < ERR) {
                setNumMask(forwardArray, pivotPli, i, probePli, start, pos);
                start++;
            }
            for (int index = 1; index < thresholds.size() && start < probeKeys.length; index++) {
                int end = probePli.getFirstIndexWhereKeyIsLT(pivotKeys[i] - thresholds.get(index), start, 1);//LT：less than 获取probePli.keys[]中 key小于的首个元素的位置
                for (int j = start; j < end; j++) {
                    setNumMask(forwardArray, pivotPli, i, probePli, j, pos + index);
                }
                start = end;
            }
            if (start < probeKeys.length) {
                for (int j = start; j < probeKeys.length; j++) {
                    setNumMask(forwardArray, pivotPli, i, probePli, j, pos + thresholds.size());
                }
            }

        }
    }

    private void correctInteger(LongBitSet[] forwardArray, IPli pivotPli, IPli probePli, int pos, List<Double> thresholds) {
        final Integer[] pivotKeys = (Integer[]) pivotPli.getKeys();
        final Integer[] probeKeys = (Integer[]) probePli.getKeys();

        for (int i = 0; i < pivotKeys.length; i++) {
            int start = 0;
            for (int index = thresholds.size() - 1; index >= 0 && start < probeKeys.length; index--) {
                //keys降序排列
                //end得到的返回值是比目标key值+指定阈值的和小或相等的第一个下标i，即keys[i]<key+th[index]
                int end = probePli.getFirstIndexWhereKeyIsLT(pivotKeys[i] + thresholds.get(index).intValue(), start, 0);
                for (int j = start; j < end; j++) {
                    setNumMask(forwardArray, pivotPli, i, probePli, j, pos + index + 1);//给定偏移为index+1，即keys[start] to keys[end]的值与目标key的差值都在此范围内
                }
                start = end;
            }
            if (start >= probeKeys.length) {
                continue;
            }
            if (probeKeys[start].equals(pivotKeys[i])) {
                setNumMask(forwardArray, pivotPli, i, probePli, start, pos);
                start++;
            }
            for (int index = 1; index < thresholds.size() && start < probeKeys.length; index++) {
                int end = probePli.getFirstIndexWhereKeyIsLT(pivotKeys[i] - thresholds.get(index).intValue(), start, 1);
                for (int j = start; j < end; j++) {
                    setNumMask(forwardArray, pivotPli, i, probePli, j, pos + index);
                }
                start = end;
            }
            if (start < probeKeys.length) {
                for (int j = start; j < probeKeys.length; j++) {
                    setNumMask(forwardArray, pivotPli, i, probePli, j, pos + thresholds.size());
                }
            }
        }
    }

    private void linerCorrectNum(LongBitSet[] forwardArray, IPli pivotPli, IPli probePli, int pos, List<Double> thresholds) {
        for (int i = 0; i < pivotPli.size(); i++) {
            int[] offsets;
            if (pivotPli.getClass() == DoublePli.class) {
                offsets = calUtils.countDouble(probePli, 0, (Double[]) probePli.getKeys(), 0, (Double) pivotPli.getKeys()[i], thresholds);
            } else {
                offsets = calUtils.countInt(probePli, 0, (Long[])  probePli.getKeys(), 0, (Long) pivotPli.getKeys()[i], thresholds);
            }
            for (int j = 0; j < probePli.size(); j++) {
                setNumMask(forwardArray, pivotPli, i, probePli, j, pos + offsets[j]);
            }
        }
    }


}