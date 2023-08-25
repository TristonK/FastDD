package fastdd;

import bruteforce.EvidenceCount;
import bruteforce.ValidateDD;
import ch.javasoft.bitset.LongBitSet;
import de.metanome.algorithms.dcfinder.helpers.IndexProvider;
import fastdd.differentialdependency.DifferentialDependency;
import fastdd.differentialdependency.DifferentialDependencySet;
import fastdd.differentialfunction.DifferentialFunction;
import fastdd.enumeration.Enumeration;
import fastdd.enumeration.HybridEvidenceInversion;
import fastdd.dfset.*;
import fastdd.dfset.longclueimpl.LongCrossClueSetBuilder;
import fastdd.dfset.longclueimpl.LongSingleClueSetBuilder;
import fastdd.dfset.offsetimpl.BinaryCalOffset;
import fastdd.pli.PliShard;
import fastdd.pli.PliShardBuilder;
import fastdd.differentialfunction.DifferentialFunctionBuilder;
import fastdd.utils.PrintResult;
import de.metanome.algorithms.dcfinder.input.Input;
import ie.hybrid.Analyzer;
import thresholds.Determination;
import thresholds.ExtremaStrategy;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
            int sampleNum = Math.min(input.getRowCount(), 200);
            Determination determination = new Determination(sampleNum, 5, 3, this.input.getColCount(), strategy);
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
        /*LongCrossClueSetBuilder.setMaskTimeCnt = 0;
        LongSingleClueSetBuilder.setMaskTimecnt = 0;
        LongSingleClueSetBuilder.cntStrTime =0;
        LongCrossClueSetBuilder.cntStrTime = 0;*/
        DFSetBuilder DFSetBuilder = new DFSetBuilder(differentialFunctionBuilder);
//        //测试暴力生成evidenceset
//        Set<LongBitSet> evidenceSetBrutal = new EvidenceCount().calculateEvidence(input, differentialFunctionBuilder);

        DFSetBuilder.buildEvidenceSetFromLongClue(pliShards);
        DFSet dfSet = DFSetBuilder.getEvidenceSet();

//        ValidateDD.printAllDF(differentialFunctionBuilder);

        System.out.println("[EvidenceSet] build long clueSet and evidence set cost: " + (System.currentTimeMillis() - t0) + " ms");
        //System.out.println("[countOffset]: " + (BinaryCalOffset.cntTime/1000000+LongSingleClueSetBuilder.cntStrTime+LongCrossClueSetBuilder.cntStrTime/1000000) +
        //        "; [SetMask]: " + (LongCrossClueSetBuilder.setMaskTimeCnt + LongSingleClueSetBuilder.setMaskTimecnt)/1000000);
        long enmurationTime = System.currentTimeMillis();
        Enumeration ddfinder = new HybridEvidenceInversion(dfSet, differentialFunctionBuilder);
        DifferentialDependencySet dds = ddfinder.buildDifferentialDenpendency();
        System.out.println("[Enumeration] cost: " + (System.currentTimeMillis() - enmurationTime) + " ms");
        System.out.println("[Enumeration] # dds: " + dds.size());
        if (Config.OutputDFSet) {
            for (Evidence evi : dfSet) {
                // System.out.println(evi.toDFString());
                IndexProvider in = differentialFunctionBuilder.getPredicateIdProvider();
                for (DifferentialFunction df : differentialFunctionBuilder.getPredicates()) {
                    if (!evi.getBitset().get(in.getIndex(df))) {
                        System.out.print(df.toString());
                    }
                }
                System.out.println("");
                //System.out.println(evi + evi.toDFString());
            }
        }
        if (Config.OutputDDFlag) {
            for (DifferentialDependency dd : dds) {
                System.out.println(dd.toString());
            }
        }
        if (Config.OutputDD2File) {
            PrintResult.PrintDD(dds);
        }

        //ValidateDD.printAllDF(differentialFunctionBuilder);
        //ValidateDD.translateRFDToDD(differentialFunctionBuilder, evidenceSet);
        if (Config.DebugFlag) {
            List<Map.Entry<Integer, List<LongBitSet>>> DDLefts = new ValidateDD().validate(dfSet, dds);
            int count = 0;
            List<LongBitSet> DDLeft = new ArrayList<>();
            for (Map.Entry<Integer, List<LongBitSet>> entry : DDLefts) {
                int key = entry.getKey();
                List<LongBitSet> values = entry.getValue();
                for (LongBitSet value : values) {
                    DDLeft.add(value);
                    count++;
                    if (count >= 10) {
                        break;
                    }
                }
                if (count >= 10) {
                    break;
                }
            }
            //测试暴力生成evidenceset
            Set<LongBitSet> evidenceSetBrutal = new EvidenceCount().calculateEvidence(input, differentialFunctionBuilder, DDLeft);

        }
        // new TranslateRFD().validatByInput(input);
        if (Config.TestIE) {
            long t1 = System.currentTimeMillis();
            DifferentialDependencySet ies = new Analyzer(dfSet, differentialFunctionBuilder).run(differentialFunctionBuilder.getFullDFBitSet());
            System.out.println("ie use time : " + (System.currentTimeMillis() - t1));
            System.out.println("ie #dd : " + ies.size());
            System.out.println("ies == dds: " + dds.haveSameDDs(ies));
            //new ValidateDD().validate(dfSet, ies);
        }
        return dds;
    }
}
