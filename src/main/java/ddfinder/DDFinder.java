package ddfinder;

import ddfinder.differentialdependency.DifferentialDependency;
import ddfinder.differentialdependency.DifferentialDependencySet;
import ddfinder.enumeration.Enumeration;
import ddfinder.enumeration.HybridEvidenceInversion;
import ddfinder.evidence.*;
import ddfinder.pli.PliShard;
import ddfinder.pli.PliShardBuilder;
import ddfinder.predicate.PredicateBuilder;
import de.metanome.algorithms.dcfinder.input.Input;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

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

    public DifferentialDependencySet buildDDs(){ //DifferentialDependencySet
        long t0 = System.currentTimeMillis();
        pliShardBuilder = new PliShardBuilder(350, input.getParsedColumns());
        PliShard[] pliShards = pliShardBuilder.buildPliShards(input.getDoubleInput(), input.getIntInput(), input.getStringInput());
        long buildPliTime = System.currentTimeMillis() - t0;
        System.out.println("[PLIs] build PLIs cost: " + buildPliTime + "ms");

        // test bitset
        /* t0 = System.currentTimeMillis();
        EvidenceSetBuilder evidenceSetBuilder = new EvidenceSetBuilder(predicateBuilder);
        evidenceSetBuilder.buildEvidenceSet(pliShards);
        EvidenceSet evidenceSet = evidenceSetBuilder.getEvidenceSet();
        System.out.println("[EvidenceSet] build clueSet and evidence set cost: " + (System.currentTimeMillis()-t0) + " ms");
        System.out.println("COUNT: " + evidenceSet.size());*/
        // test Long
        t0 = System.currentTimeMillis();
        EvidenceSetBuilder evidenceSetBuilder = new EvidenceSetBuilder(predicateBuilder);
        evidenceSetBuilder.buildEvidenceSetFromLongClue(pliShards);
        EvidenceSet evidenceSet = evidenceSetBuilder.getEvidenceSet();
        System.out.println("[EvidenceSet] build long clueSet and evidence set cost: " + (System.currentTimeMillis()-t0) + " ms");
        long enmurationTime = System.currentTimeMillis();
        Enumeration ddfinder = new HybridEvidenceInversion(evidenceSet, predicateBuilder);
        DifferentialDependencySet dds = ddfinder.buildDifferentialDenpendency();
        System.out.println("[Enumeration] cost: " + (System.currentTimeMillis() - enmurationTime));
        System.out.println("[Enumeration] # dds: " + dds.size());
        for(DifferentialDependency dd: dds){System.out.println(dd.toString());}
        return dds;
    }
}
