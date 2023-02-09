package ddfinder;


import bruteforce.EvidenceCount;
import ddfinder.differentialdependency.DifferentialDependencySet;
import ddfinder.evidence.Evidence;
import ddfinder.evidence.EvidenceSetBuilder;
import ddfinder.pli.PliShard;
import ddfinder.pli.PliShardBuilder;
import ddfinder.predicate.Predicate;
import ddfinder.predicate.PredicateBuilder;
import de.metanome.algorithms.dcfinder.input.Input;

import java.io.File;
import java.util.Objects;

/**
 * @author tristonk
 */
public class DDFinder {

    private final int rowLimit;
    private Input input;
    private PredicateBuilder predicateBuilder;
    private PliShardBuilder pliShardBuilder;
    public DDFinder(int rowLimit, Input input, String predicatesPath){
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
        long t0 = System.currentTimeMillis();
        pliShardBuilder = new PliShardBuilder(350, input.getParsedColumns());
        PliShard[] pliShards = pliShardBuilder.buildPliShards(input.getDoubleInput());
        long buildPliTime = System.currentTimeMillis() - t0;
        System.out.println("[Time] build PLIs cost: " + buildPliTime + "ms");
        DifferentialDependencySet dds = new DifferentialDependencySet();
        t0 = System.currentTimeMillis();
        EvidenceSetBuilder evidenceSetBuilder = new EvidenceSetBuilder(predicateBuilder);
        evidenceSetBuilder.buildEvidenceSet(pliShards);
        System.out.println("[Time] build clueSet and evidence set " + (System.currentTimeMillis()-t0) + " ms");
        System.out.println("[EvidenceSet] # size is " + evidenceSetBuilder.getEvidenceSet().size());
        System.out.println("[ClueSet]Brute Force # size is "+ EvidenceCount.calculate(input));
        return dds;
    }
}
