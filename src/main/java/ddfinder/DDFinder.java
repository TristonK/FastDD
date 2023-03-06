package ddfinder;


import bruteforce.EvidenceCount;
import ch.javasoft.bitset.LongBitSet;
import ddfinder.differentialdependency.DifferentialDependencySet;
import ddfinder.enumeration.Enumeration;
import ddfinder.enumeration.SingleThresholdDD;
import ddfinder.evidence.CrossClueSetBuilder;
import ddfinder.evidence.Evidence;
import ddfinder.evidence.EvidenceSetBuilder;
import ddfinder.evidence.SingleClueSetBuilder;
import ddfinder.pli.PliShard;
import ddfinder.pli.PliShardBuilder;
import ddfinder.predicate.Predicate;
import ddfinder.predicate.PredicateBuilder;
import de.metanome.algorithms.dcfinder.input.Input;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author tristonk
 */
public class DDFinder {

    private final int rowLimit;
    private Input input;
    private PredicateBuilder predicateBuilder;
    private PliShardBuilder pliShardBuilder;
    public DDFinder(int rowLimit, Input input, String predicatesPath) throws IOException {
        this.rowLimit = rowLimit;
        this.input = input;
        long t0 = System.currentTimeMillis();
        if(Objects.equals(predicatesPath, "")){
            this.predicateBuilder = new PredicateBuilder(input);
        }else{
            this.predicateBuilder = new PredicateBuilder(new File(predicatesPath), input);
        }
        long buildPredicateTime = System.currentTimeMillis() - t0;
        System.out.println("[Time] build predicates cost: " + buildPredicateTime + " ms");
        System.out.println("Predicates Size: " + predicateBuilder.size());
    }

    public DifferentialDependencySet buildDDs(){
        long bfTime = System.currentTimeMillis();
        Set<LongBitSet> bf = new EvidenceCount().calculate(input);
        System.out.println("[Brute Force] cost: " + (System.currentTimeMillis()-bfTime) + " ms");
        System.out.println("[Brute Force] # clueSet Size "+ bf.size());
        long t0 = System.currentTimeMillis();
        pliShardBuilder = new PliShardBuilder(350, input.getParsedColumns());
        PliShard[] pliShards = pliShardBuilder.buildPliShards(input.getDoubleInput(), input.getIntInput(), input.getStringInput());
        long buildPliTime = System.currentTimeMillis() - t0;
        System.out.println("[Time] build PLIs cost: " + buildPliTime + "ms");
        t0 = System.currentTimeMillis();
        EvidenceSetBuilder evidenceSetBuilder = new EvidenceSetBuilder(predicateBuilder);
        Set<LongBitSet> clues = evidenceSetBuilder.buildEvidenceSet(pliShards);
        //evidenceSetBuilder.buildFullClueSet(pliShards);
        System.out.println("[Time] build clueSet and evidence set " + (System.currentTimeMillis()-t0) + " ms");
        Enumeration ddfiner = new SingleThresholdDD(clues, predicateBuilder);
        DifferentialDependencySet dds = ddfiner.buildDifferentialDenpendency();
        return dds;
    }
}
