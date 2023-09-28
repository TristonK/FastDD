package fastdd.dfset;

import com.koloboke.collect.map.hash.HashLongLongMap;
import com.koloboke.collect.map.hash.HashLongLongMaps;
import fastdd.dfset.isnimpl.ISNBuilder;
import fastdd.dfset.isnimpl.CrossISNBuilder;
import fastdd.dfset.isnimpl.SingleISNBuilder;
import fastdd.dfset.offsetimpl.BinaryCalOffset;
import fastdd.pli.PliShard;
import fastdd.differentialfunction.DifferentialFunctionBuilder;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CountedCompleter;

/**
 * @author tristonK 2022/12/31
 */
public class DFSetBuilder {
    private final DFSet dfSet;

    public DFSetBuilder(DifferentialFunctionBuilder differentialFunctionBuilder) {
        ISNBuilder.configure(differentialFunctionBuilder);
        dfSet = new DFSet(differentialFunctionBuilder);
    }

    public Set<Long> buildDifferentialSetFromLongClue(PliShard[] pliShards){
        long t2 = System.currentTimeMillis();
        HashMap<Long, Long> longClueSet = buildLongClueSet(pliShards);
        System.out.println("[LongClueSet] build cost: " + (System.currentTimeMillis() - t2) + " ms");
        System.out.println("[LongClueSet] # clueSet size: " + longClueSet.size());
        dfSet.buildFromLong(longClueSet);
        return longClueSet.keySet();
    }

    public DFSet getDFSet() {
        return dfSet;
    }

    private HashMap<Long, Long> buildLongClueSet(PliShard[] pliShards) {
        int taskCount = (pliShards.length * (pliShards.length + 1)) / 2;
        System.out.println("  [CLUE] task count: " + taskCount);
        var exec = new Executor(pliShards);
        return exec.res;
    }
}