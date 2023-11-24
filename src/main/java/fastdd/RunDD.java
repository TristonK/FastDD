package fastdd;

import bruteforce.DifferentialSetCount;
import ch.javasoft.bitset.LongBitSet;
import dd.topdownPositive.Analyzer;
import de.metanome.algorithms.dcfinder.input.Input;
import fastdd.differentialdependency.DifferentialDependencySet;
import fastdd.differentialfunction.DifferentialFunctionBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tristonk
 */
public class RunDD {
    private final Input input;
    private final DifferentialFunctionBuilder differentialFunctionBuilder;

    public RunDD(Input input, String predicatesPath) throws IOException {
        this.input = input;
        long t0 = System.currentTimeMillis();
        this.differentialFunctionBuilder = new DifferentialFunctionBuilder(new File(predicatesPath), input);
        long buildDFsTime = System.currentTimeMillis() - t0;
        System.out.println("[Time] build differential functions cost: " + buildDFsTime + " ms");
        System.out.println("Differential functions Size: " + differentialFunctionBuilder.size());
    }

    public DifferentialDependencySet buildDDs(){
        long t0 = System.currentTimeMillis();

        List<LongBitSet> differentialSet = new ArrayList<>(new DifferentialSetCount().calculateDFSets(input, differentialFunctionBuilder));
        System.out.println("[DifferentialSet] naive build differential set cost: " + (System.currentTimeMillis() - t0) + " ms");

        long t1 = System.currentTimeMillis();
        DifferentialDependencySet dds = new Analyzer(differentialSet, differentialFunctionBuilder).run(differentialFunctionBuilder.getFullDFBitSet());
        System.out.println(Config.method + " use time : " + (System.currentTimeMillis() - t1) + "ms");
        System.out.println(Config.method + " #dd : " + dds.size());
        return dds;
    }
}
