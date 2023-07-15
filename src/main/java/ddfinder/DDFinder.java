package ddfinder;

import bruteforce.EvidenceCount;
import bruteforce.TranslateRFD;
import bruteforce.ValidateDD;
import ddfinder.differentialdependency.DifferentialDependency;
import ddfinder.differentialdependency.DifferentialDependencySet;
import ddfinder.enumeration.Enumeration;
import ddfinder.enumeration.HybridEvidenceInversion;
import ddfinder.evidence.*;
import ddfinder.evidence.longclueimpl.LongCrossClueSetBuilder;
import ddfinder.evidence.longclueimpl.LongSingleClueSetBuilder;
import ddfinder.evidence.offsetimpl.BinaryCalOffset;
import ddfinder.evidence.offsetimpl.BruteCalOffset;
import ddfinder.pli.PliShard;
import ddfinder.pli.PliShardBuilder;
import ddfinder.predicate.DifferentialFunctionBuilder;
import de.metanome.algorithms.dcfinder.input.Input;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * @author tristonk
 */
public class DDFinder {
    private final Input input;
    private final DifferentialFunctionBuilder differentialFunctionBuilder;

    public DDFinder(Input input, String predicatesPath) throws IOException {
        this.input = input;

        long t0 = System.currentTimeMillis();
        if(Objects.equals(predicatesPath, "")){
            this.differentialFunctionBuilder = new DifferentialFunctionBuilder(input);
        }else{
            this.differentialFunctionBuilder = new DifferentialFunctionBuilder(new File(predicatesPath), input);
        }
        long buildPredicateTime = System.currentTimeMillis() - t0;
        System.out.println("[Time] build predicates cost: " + buildPredicateTime + " ms");
        System.out.println("Predicates Size: " + differentialFunctionBuilder.size());
    }

    public DifferentialDependencySet buildDDs(){
        long t0 = System.currentTimeMillis();
        PliShardBuilder pliShardBuilder = new PliShardBuilder(Config.PliShardLength, input.getParsedColumns());
        PliShard[] pliShards = pliShardBuilder.buildPliShards(input.getDoubleInput(), input.getLongInput(), input.getStringInput());
        long buildPliTime = System.currentTimeMillis() - t0;
        System.out.println("[PLIs] build PLIs cost: " + buildPliTime + "ms");

        t0 = System.currentTimeMillis();
        LongCrossClueSetBuilder.setMaskTimeCnt = 0;
        LongSingleClueSetBuilder.setMaskTimecnt = 0;
        LongSingleClueSetBuilder.cntStrTime =0;
        LongCrossClueSetBuilder.cntStrTime = 0;
        EvidenceSetBuilder evidenceSetBuilder = new EvidenceSetBuilder(differentialFunctionBuilder);
        evidenceSetBuilder.buildEvidenceSetFromLongClue(pliShards);
        EvidenceSet evidenceSet = evidenceSetBuilder.getEvidenceSet();
        System.out.println("[EvidenceSet] build long clueSet and evidence set cost: " + (System.currentTimeMillis()-t0) + " ms");
        System.out.println("[countOffset]: " + (BinaryCalOffset.cntTime/1000000+LongSingleClueSetBuilder.cntStrTime+LongCrossClueSetBuilder.cntStrTime/1000000) +
                "; [SetMask]: " + (LongCrossClueSetBuilder.setMaskTimeCnt + LongSingleClueSetBuilder.setMaskTimecnt)/1000000);
        long enmurationTime = System.currentTimeMillis();
        Enumeration ddfinder = new HybridEvidenceInversion(evidenceSet, differentialFunctionBuilder);
        DifferentialDependencySet dds = ddfinder.buildDifferentialDenpendency();
        System.out.println("[Enumeration] cost: " + (System.currentTimeMillis() - enmurationTime));
        System.out.println("[Enumeration] # dds: " + dds.size());

        if(Config.OutputDDFlag) {
            for (DifferentialDependency dd : dds) {
                System.out.println(dd.toString());
            }
        }
        // ValidateDD.printAllDF(differentialFunctionBuilder);
        // ValidateDD.translateRFDToDD(differentialFunctionBuilder, evidenceSet);
        if(Config.DebugFlag) {
            new ValidateDD().validate(evidenceSet, dds);
        }
        // new TranslateRFD().validatByInput(input);
        return dds;
    }
}
