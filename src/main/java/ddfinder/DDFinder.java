package ddfinder;

import bruteforce.EvidenceCount;
import ch.javasoft.bitset.LongBitSet;
import ddfinder.differentialdependency.DifferentialDependencySet;
import ddfinder.enumeration.Enumeration;
import ddfinder.enumeration.HybridEvidenceInversion;
import ddfinder.evidence.EvidenceSet;
import ddfinder.evidence.EvidenceSetBuilder;
import ddfinder.pli.PliShard;
import ddfinder.pli.PliShardBuilder;
import ddfinder.predicate.PredicateBuilder;
import de.metanome.algorithms.dcfinder.input.Input;
import thresholds.Determination;
import thresholds.ExtremaStrategy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
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

        //Determination

        ExtremaStrategy s = new ExtremaStrategy();
        Determination d = new Determination(Math.min(input.getRowCount()/5,200), 6, 2, this.input.getColCount(), s);
        d.sampleAndCalculate(this.input);
        List<List<List<Double>>> theresholds = d.determine(); //接收阈值

//        String fileName = input.getName();
//        try {
//            File file = new File("thresholdsOut/" + fileName + ".txt");
//            if (!file.getParentFile().exists()) {
//                file.getParentFile().mkdirs();
//            }
//            if (!file.exists()) {
//                file.createNewFile();
//            }
//            System.out.println("[thresholdsNum]: " + maxThresholdsSize);
//            System.out.println("[thresholds Output] Save thresholds to " + fileName);
//            BufferedWriter out = new BufferedWriter(new FileWriter(file));
//            out.write(input.getParsedColumns()+"\n"); //打印属性名
//            for (List<List<Double>> thresholds : theresholds) {
//                out.write(thresholds.toString() + "\n");
//            }
//            out.close();
//            System.out.println("文件创建成功！");
//            System.out.println("[Thresholds] determine thresholds cost: " + (System.currentTimeMillis() - t0) + " ms");
//        } catch (IOException e) {
//            System.out.println(e.toString());



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
        //long bfTime = System.currentTimeMillis();
        //Set<LongBitSet> bf = new EvidenceCount().calculate(input);
        //System.out.println("[Brute Force] cost: " + (System.currentTimeMillis()-bfTime) + " ms");
        //System.out.println("[Brute Force] # clueSet Size "+ bf.size());
        long t0 = System.currentTimeMillis();
        //pliShardBuilder = new PliShardBuilder(350, input.getParsedColumns());
        pliShardBuilder = new PliShardBuilder(350, input.getParsedColumns());
        PliShard[] pliShards = pliShardBuilder.buildPliShards(input.getDoubleInput(), input.getIntInput(), input.getStringInput());
        long buildPliTime = System.currentTimeMillis() - t0;
        System.out.println("[PLIs] build PLIs cost: " + buildPliTime + "ms");
        t0 = System.currentTimeMillis();
        EvidenceSetBuilder evidenceSetBuilder = new EvidenceSetBuilder(predicateBuilder);
        //Set<LongBitSet> clues = evidenceSetBuilder.buildEvidenceSet(pliShards);
        //evidenceSetBuilder.buildFullClueSet(pliShards);
        evidenceSetBuilder.buildEvidenceSet(pliShards);
        EvidenceSet evidenceSet = evidenceSetBuilder.getEvidenceSet();
        System.out.println("[EvidenceSet] build clueSet and evidence set cost: " + (System.currentTimeMillis()-t0) + " ms");
        //Enumeration ddfiner = new SingleThresholdDD(clues, predicateBuilder);
        long enmurationTime = System.currentTimeMillis();
        Enumeration ddfinder = new HybridEvidenceInversion(evidenceSet, predicateBuilder);
        DifferentialDependencySet dds = ddfinder.buildDifferentialDenpendency();
        System.out.println("[Enumeration] cost: " + (System.currentTimeMillis() - enmurationTime));
        System.out.println("[Enumeration] # dds: " + dds.size());
        return dds;
    }
}
