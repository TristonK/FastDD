package fastdd;

import bruteforce.DifferentialSetCount;
import ch.javasoft.bitset.LongBitSet;
import de.metanome.algorithms.dcfinder.helpers.IndexProvider;
import de.metanome.algorithms.dcfinder.input.Input;
import fastdd.dfset.DFSet;
import fastdd.dfset.DFSetBuilder;
import fastdd.dfset.MatchDF;
import fastdd.dfset.isnimpl.ISNBuilder;
import fastdd.differentialdependency.DifferentialDependency;
import fastdd.differentialdependency.DifferentialDependencySet;
import fastdd.differentialfunction.DifferentialFunction;
import fastdd.differentialfunction.DifferentialFunctionBuilder;
import fastdd.enumeration.Enumeration;
import fastdd.enumeration.HybridEvidenceInversion;
import fastdd.pli.PliShard;
import fastdd.pli.PliShardBuilder;
import fastdd.utils.PrintResult;
import ie.hybrid.Analyzer;
import thresholds.Determination;
import thresholds.ExtremaStrategy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author tristonk
 */
public class FastDD {
    private final Input input;
    private final DifferentialFunctionBuilder differentialFunctionBuilder;

    public FastDD(Input input, String predicatesPath) throws IOException {
        this.input = input;

        long t0 = System.currentTimeMillis();
        if (Objects.equals(predicatesPath, "")) {
            ExtremaStrategy strategy = new ExtremaStrategy();
            int sampleNum = Math.min(input.getRowCount() / 5, 200);
            Determination determination = new Determination(input.getRowCount(), 5, 3, this.input.getColCount(), strategy);
            determination.sampleAndCalculate(this.input);
            long sampleAndCalculateTime = System.currentTimeMillis() - t0;
            System.out.println("sample Size: " + sampleNum);
            System.out.println("[Time] sampleAndCalculate cost: " + sampleAndCalculateTime + "ms");
            long t1 = System.currentTimeMillis();
            List<List<List<Double>>> thresholds = determination.determine();
            long determineTime = System.currentTimeMillis() - t1;
            System.out.println("[Time] determine cost: " + determineTime + "ms");
            this.differentialFunctionBuilder = new DifferentialFunctionBuilder(input, thresholds);
        } else {
            this.differentialFunctionBuilder = new DifferentialFunctionBuilder(new File(predicatesPath), input);
        }
        long buildPredicateTime = System.currentTimeMillis() - t0;
        System.out.println("[Time] build predicates cost: " + buildPredicateTime + " ms");
        System.out.println("Predicates Size: " + differentialFunctionBuilder.size());
    }

    public DifferentialDependencySet buildDDs() {
        long t0 = System.currentTimeMillis();
        PliShardBuilder pliShardBuilder = new PliShardBuilder(Config.PliShardLength, input.getParsedColumns());
        PliShard[] pliShards = pliShardBuilder.buildPliShards(input.getDoubleInput(), input.getLongInput(), input.getStringInput());
        long buildPliTime = System.currentTimeMillis() - t0;
        System.out.println("[PLIs] build PLIs cost: " + buildPliTime + "ms");

        t0 = System.currentTimeMillis();

        List<LongBitSet> differentialSet = new ArrayList<>(new DifferentialSetCount().calculateEvidence(input, differentialFunctionBuilder));
        System.out.println("[DifferentialSet] naive build differential set cost: " + (System.currentTimeMillis() - t0) + " ms");

        long t1 = System.currentTimeMillis();
        DifferentialDependencySet ies = new Analyzer(differentialSet, differentialFunctionBuilder).run(differentialFunctionBuilder.getFullDFBitSet());
        System.out.println("ie use time : " + (System.currentTimeMillis() - t1));
        System.out.println("ie #dd : " + ies.size());

        return ies;
    }
}
