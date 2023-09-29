package fastdd.dfset;

import fastdd.dfset.isnimpl.ISNBuilder;
import fastdd.pli.PliShard;
import fastdd.differentialfunction.DifferentialFunctionBuilder;

import java.util.HashMap;
import java.util.Set;

/**
 * @author tristonK 2022/12/31
 */
public class DFSetBuilder {
    private final DFSet dfSet;

    public DFSetBuilder(DifferentialFunctionBuilder differentialFunctionBuilder) {
        ISNBuilder.configure(differentialFunctionBuilder);
        dfSet = new DFSet(differentialFunctionBuilder);
    }

    public Set<Long> buildDifferentialSetFromISN(PliShard[] pliShards){
        long t2 = System.currentTimeMillis();
        HashMap<Long, Long> longClueSet = buildISNs(pliShards);
        System.out.println("[ISN] build cost: " + (System.currentTimeMillis() - t2) + " ms");
        System.out.println("[ISN] # ISN size: " + longClueSet.size());
        dfSet.buildFromLong(longClueSet);
        return longClueSet.keySet();
    }

    public DFSet getDFSet() {
        return dfSet;
    }

    private HashMap<Long, Long> buildISNs(PliShard[] pliShards) {
        int taskCount = (pliShards.length * (pliShards.length + 1)) / 2;
        System.out.println("  [ISN] task count: " + taskCount);
        var exec = new Executor(pliShards);
        return exec.res;
    }
}