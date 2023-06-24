package ddfinder.evidence.longclueimpl;

import ddfinder.evidence.IClueOffset;
import ddfinder.pli.DoublePli;
import ddfinder.pli.IPli;
import ddfinder.pli.PliShard;
import ddfinder.utils.StringCalculation;

import java.util.HashMap;
import java.util.List;

/**
 * @author tristonK 2023/6/2
 */
public class LongCrossClueSetBuilder extends LongClueSetBuilder {
    private final List<IPli> plis1, plis2;
    private final int evidenceCount;

    private final double ERR = 0.000000001;
    private long[] forwardClues;//用来代替longbitset，实验测试
    private IClueOffset calUtils;

    public LongCrossClueSetBuilder(PliShard shard1, PliShard shard2, IClueOffset calUtils) {
        plis1 = shard1.plis;
        plis2 = shard2.plis;
        evidenceCount = (shard1.end - shard1.beg) * (shard2.end - shard2.beg);
        this.calUtils = calUtils;
    }

    public HashMap<Long, Long> buildClueSet() {
        forwardClues = new long[evidenceCount];   // plis1 -> plis2
        for (PredicatePack strPack : strPacks) {
            correctStr(plis1.get(strPack.colIndex), plis2.get(strPack.colIndex), strPack.base, strPack.thresholds);
        }
        for (PredicatePack intPack : intPacks) {
            linerCorrectNum(plis1.get(intPack.colIndex), plis2.get(intPack.colIndex), intPack.base, intPack.thresholds);
        }
        for (PredicatePack numPack : doublePacks) {
            linerCorrectNum(plis1.get(numPack.colIndex), plis2.get(numPack.colIndex), numPack.base, numPack.thresholds);
        }
        return accumulateClues(forwardClues);
    }


    private void setNumMask(IPli pli1, int i, IPli pli2, int j, long base, int offset) {
        int beg1 = pli1.getPliShard().beg, beg2 = pli2.getPliShard().beg;
        int range2 = pli2.getPliShard().end - beg2;
        for (int tid1 : pli1.get(i).getRawCluster()) {
            int t1 = tid1 - beg1;
            int r1 = t1 * range2 - beg2;
            for (int tid2 : pli2.get(j).getRawCluster()) {
                forwardClues[r1 + tid2] += base * offset;
            }
        }
    }

    private void correctStr(IPli pivotPli, IPli probePli, long base, List<Double> thresholds) {
        final String[] pivotKeys = (String[]) pivotPli.getKeys();
        final String[] probeKeys = (String[]) probePli.getKeys();
        for (int i = 0; i < pivotKeys.length; i++) {
            for (int j = 0; j < probeKeys.length; j++) {
                int diff = StringCalculation.getDistance(pivotKeys[i], probeKeys[j]);
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
                setNumMask(pivotPli, i, probePli, j, base, c);
            }
        }
    }

    private void linerCorrectNum(IPli pivotPli, IPli probePli, long base, List<Double> thresholds) {
        //IClueOffset bruteCalOffsetUtils = new BruteCalOffset();
        //IClueOffset binaryCalOffsetUtils = new BinaryCalOffset();
        for (int i = 0; i < pivotPli.size(); i++) {
            int[] offsets;
            if (pivotPli.getClass() == DoublePli.class) {
                offsets = calUtils.countDouble(probePli, 0, (Double[]) probePli.getKeys(), 0, (Double) pivotPli.getKeys()[i], thresholds);
                /*int[] offsets2 = bruteCalOffsetUtils.countDouble(probePli, 0, (Double[]) probePli.getKeys(), 0, (Double) pivotPli.getKeys()[i], thresholds);
                if(!offsets.equals(offsets2)){
                    System.out.println("Index " + i + "Pivot = " + pivotPli.getKeys()[i]);
                    System.out.println(probePli.getKeys().toString());
                    System.out.println("offset for linear: " + offsets.toString());
                    System.out.println("offset for brute: " + offsets2.toString());
                }*/
            } else {
                offsets = calUtils.countInt(probePli, 0, (Integer[]) probePli.getKeys(), 0, (Integer) pivotPli.getKeys()[i], thresholds);
                /*int[] offsets2 = bruteCalOffsetUtils.countInt(probePli, 0, (Integer[]) probePli.getKeys(), 0, (Integer) pivotPli.getKeys()[i], thresholds);
                if(!offsets.equals(offsets2)){
                    System.out.println("Index " + i + "Pivot = " + pivotPli.getKeys()[i]);
                    System.out.println(probePli.getKeys().toString());
                    System.out.println("offset for linear: " + offsets.toString());
                    System.out.println("offset for brute: " + offsets2.toString());
                }*/
            }
            for (int j = 0; j < probePli.size(); j++) {
                setNumMask(pivotPli, i, probePli, j, base, offsets[j]);
            }
        }
    }
}

