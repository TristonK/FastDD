package ddfinder.dfset;

import ch.javasoft.bitset.LongBitSet;
import ddfinder.Config;
import ddfinder.dfset.longclueimpl.LongClueSetBuilder;
import ddfinder.dfset.longclueimpl.LongCrossClueSetBuilder;
import ddfinder.dfset.longclueimpl.LongSingleClueSetBuilder;
import ddfinder.dfset.offsetimpl.BinaryCalOffset;
import ddfinder.pli.PliShard;
import ddfinder.differentialfunction.DifferentialFunctionBuilder;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CountedCompleter;

/**
 * @author tristonK 2022/12/31
 */
public class EvidenceSetBuilder {
    private final EvidenceSet evidenceSet;

    public EvidenceSetBuilder(DifferentialFunctionBuilder differentialFunctionBuilder) {
        ClueSetBuilder.configure(differentialFunctionBuilder);
        LongClueSetBuilder.configure(differentialFunctionBuilder);
        evidenceSet = new EvidenceSet(differentialFunctionBuilder);
    }

    public Set<LongBitSet> buildEvidenceSet(PliShard[] pliShards) {
        if (pliShards.length != 0) {
            long t1 = System.currentTimeMillis();
            HashMap<LongBitSet, Long> clueSet = linearBuildClueSet(pliShards);
            System.out.println("[ClueSet] build cost: " + (System.currentTimeMillis() - t1) + " ms");
            System.out.println("[ClueSet] # clueSet size: " + clueSet.size());
            evidenceSet.build(clueSet);
            return clueSet.keySet();
        }
        return null;
    }

    public Set<Long> buildEvidenceSetFromLongClue(PliShard[] pliShards){
        long t2 = System.currentTimeMillis();
        HashMap<Long, Long> longClueSet = buildLongClueSet(pliShards);
        System.out.println("[LongClueSet] build cost: " + (System.currentTimeMillis() - t2) + " ms");
        System.out.println("[LongClueSet] # clueSet size: " + longClueSet.size());
        evidenceSet.buildFromLong(longClueSet);
        return longClueSet.keySet();
    }

    private HashMap<LongBitSet, Long> linearBuildClueSet(PliShard[] pliShards) {
        int taskCount = (pliShards.length * (pliShards.length + 1)) / 2;
        System.out.println("  [CLUE] task count: " + taskCount);

        HashMap<LongBitSet, Long> clueSet = new HashMap<>();

        IClueOffset binaryCalOffsetUtils = new BinaryCalOffset();
        System.out.println("[ClueOffset] Using Strategy: " + binaryCalOffsetUtils.getClass().getSimpleName());

        for (int i = 0; i < pliShards.length; i++) {
            for (int j = i; j < pliShards.length; j++) {
                ClueSetBuilder builderLinear = i == j ? new SingleClueSetBuilder(pliShards[i], binaryCalOffsetUtils) : new CrossClueSetBuilder(pliShards[i], pliShards[j], binaryCalOffsetUtils);
                HashMap<LongBitSet, Long> partialClueSet =builderLinear.buildClueSet();
                partialClueSet.forEach((k, v) -> clueSet.merge(k, v, Long::sum));
            }
        }
        return clueSet;
    }


    public EvidenceSet getEvidenceSet() {
        return evidenceSet;
    }

    private HashMap<Long, Long> buildLongClueSet(PliShard[] pliShards) {
        int taskCount = (pliShards.length * (pliShards.length + 1)) / 2;
        System.out.println("  [CLUE] task count: " + taskCount);
        return Config.TestMultiThread? new ClueSetTask(null, pliShards, 0, taskCount).invoke() : linerBuild(pliShards);
    }

    private HashMap<Long, Long> linerBuild(PliShard[] pliShards){
        HashMap<Long, Long> clueSet = new HashMap<>();

        IClueOffset binaryCalOffsetUtils = new BinaryCalOffset();

        System.out.println("[LongClueOffset] Using Strategy: " + binaryCalOffsetUtils.getClass().getSimpleName());

        for (int i = 0; i < pliShards.length; i++) {
            for (int j = i; j < pliShards.length; j++) {
                LongClueSetBuilder builder = i == j ? new LongSingleClueSetBuilder(pliShards[i], binaryCalOffsetUtils) : new LongCrossClueSetBuilder(pliShards[i], pliShards[j], binaryCalOffsetUtils);
                HashMap<Long, Long> partialClueSet = builder.buildClueSet();
                partialClueSet.forEach((k, v) -> clueSet.merge(k, v, Long::sum));
            }
        }
        return clueSet;
    }
}


class ClueSetTask extends CountedCompleter<HashMap<Long, Long>> {

    private static int[] searchIndexes;

    private static void buildSearchIndex(int count) {
        if (searchIndexes == null || searchIndexes[searchIndexes.length - 1] < count) {
            int n = (int) Math.sqrt(2 * count + 2) + 3;
            searchIndexes = new int[n];
            for (int i = 1; i < n; i++)
                searchIndexes[i] = searchIndexes[i - 1] + i + 1;
        }
    }

    final int taskBeg, taskEnd;
    PliShard[] pliShards;

    ClueSetTask sibling;
    HashMap<Long, Long> partialClueSet;

    public ClueSetTask(ClueSetTask parent, PliShard[] _pliShards, int _beg, int _end) {
        super(parent);
        pliShards = _pliShards;
        taskBeg = _beg;
        taskEnd = _end;
        buildSearchIndex(taskEnd);
    }

    @Override
    public void compute() {
        if (taskEnd - taskBeg >= 2) {
            int mid = (taskBeg + taskEnd) >>> 1;
            ClueSetTask left = new ClueSetTask(this, pliShards, taskBeg, mid);
            ClueSetTask right = new ClueSetTask(this, pliShards, mid, taskEnd);
            left.sibling = right;
            right.sibling = left;

            setPendingCount(1);
            right.fork();

            left.compute();
        } else {
            if (taskEnd > taskBeg) {
                LongClueSetBuilder builder = getClueSetBuilder(taskBeg);
                partialClueSet = builder.buildClueSet();
            }

            tryComplete();
        }
    }

    private LongClueSetBuilder getClueSetBuilder(int taskID) {
        // taskID = i*(i+1)/2 + j
        int i = lowerBound(searchIndexes, taskID);
        int j = i - (searchIndexes[i] - taskID);
        IClueOffset binaryCalOffsetUtils = new BinaryCalOffset();
        return i == j ? new LongSingleClueSetBuilder(pliShards[i], binaryCalOffsetUtils) : new LongCrossClueSetBuilder(pliShards[i], pliShards[j], binaryCalOffsetUtils);
    }

    // return the index of the first num that's >= target, or nums.length if no such num
    private int lowerBound(int[] nums, int target) {
        int l = 0, r = nums.length;
        while (l < r) {
            int m = l + ((r - l) >>> 1);
            if (nums[m] >= target) r = m;
            else l = m + 1;
        }
        return l;
    }

    @Override
    public void onCompletion(CountedCompleter<?> caller) {
        if (caller != this) {
            ClueSetTask child = (ClueSetTask) caller;
            ClueSetTask childSibling = child.sibling;

            partialClueSet = child.partialClueSet;
            if (childSibling != null && childSibling.partialClueSet != null) {
                for (var e : childSibling.partialClueSet.entrySet())
                    partialClueSet.put(e.getKey(), e.getValue()+partialClueSet.getOrDefault(e.getKey(),0L));
            }
        }
    }

    @Override
    public HashMap<Long, Long> getRawResult() {
        return partialClueSet == null ? new HashMap<>() : partialClueSet;
    }
}
