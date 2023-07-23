package ddfinder;

import bruteforce.EvidenceCount;
import ch.javasoft.bitset.LongBitSet;
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
import ddfinder.search.MinimizeTree;
import de.metanome.algorithms.dcfinder.input.Input;
import ie.hybrid.Analyzer;
import thresholds.Determination;
import thresholds.ExtremaStrategy;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
            ExtremaStrategy strategy = new ExtremaStrategy();
            int sampleNum = Math.min(input.getRowCount() / 5, 200);
            Determination determination = new Determination(Math.min(input.getRowCount() / 5, 200), 6, 2, this.input.getColCount(), strategy);
            determination.sampleAndCalculate(this.input);
            long sampleAndCalculateTime =System.currentTimeMillis() - t0;
            System.out.println("sample Size: " + sampleNum);
            System.out.println("[Time] sampleAndCalculate cost: " + sampleAndCalculateTime + "ms");
            long t1 = System.currentTimeMillis();
            List<List<List<Double>>> thresholds = determination.determine();
            long determineTime = System.currentTimeMillis() - t1;
            System.out.println("[Time] determine cost: " + determineTime + "ms");
            this.differentialFunctionBuilder = new DifferentialFunctionBuilder(input, thresholds);
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
//        //测试暴力生成evidenceset
//        Set<LongBitSet> evidenceSetBrutal = new EvidenceCount().calculateEvidence(input, differentialFunctionBuilder);

        evidenceSetBuilder.buildEvidenceSetFromLongClue(pliShards);
        EvidenceSet evidenceSet = evidenceSetBuilder.getEvidenceSet();

//        ValidateDD.printAllDF(differentialFunctionBuilder);

        System.out.println("[EvidenceSet] build long clueSet and evidence set cost: " + (System.currentTimeMillis()-t0) + " ms");
        System.out.println("[countOffset]: " + (BinaryCalOffset.cntTime/1000000+LongSingleClueSetBuilder.cntStrTime+LongCrossClueSetBuilder.cntStrTime/1000000) +
                "; [SetMask]: " + (LongCrossClueSetBuilder.setMaskTimeCnt + LongSingleClueSetBuilder.setMaskTimecnt)/1000000);
        long enmurationTime = System.currentTimeMillis();
        Enumeration ddfinder = new HybridEvidenceInversion(evidenceSet, differentialFunctionBuilder);
        DifferentialDependencySet dds = ddfinder.buildDifferentialDenpendency();
        System.out.println("[Enumeration] cost: " + (System.currentTimeMillis() - enmurationTime)+ " ms");
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
        //long t1 = System.currentTimeMillis();
        //DifferentialDependencySet ies = new Analyzer(evidenceSet, differentialFunctionBuilder).run(differentialFunctionBuilder.getFullDFBitSet());
        //System.out.println("ie use time : "+ (System.currentTimeMillis() - t1));
        //new ValidateDD().validate(evidenceSet, ies);
        return dds;
    }
}
