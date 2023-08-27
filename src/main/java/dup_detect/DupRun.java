package dup_detect;

import ch.javasoft.bitset.LongBitSet;
import de.metanome.algorithms.dcfinder.input.Input;
import fastdd.differentialdependency.DifferentialDependency;
import fastdd.differentialfunction.DifferentialFunction;
import fastdd.differentialfunction.DifferentialFunctionBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tristonK 2023/8/27
 */
public class DupRun {
    public void run(Input input, DifferentialFunctionBuilder differentialFunctionBuilder, List<DifferentialDependency>dds){
        // get true
        ReadCorrectInput trueDup = new ReadCorrectInput();
        trueDup.readFile();

        List<LongBitSet> ddsLeft = new ArrayList<>();
        for(var dd: dds){
            ddsLeft.add((LongBitSet) dd.getLeftPredicateSet());
        }

        TestDD testDD = new TestDD(ddsLeft);
        testDD.calculateDup(input, differentialFunctionBuilder);

        for(int i = 0; i < dds.size(); i++){
            System.out.println("======");
            System.out.println("[Dup] dd: " + dds.get(i).toString());
            int cnt = 0;
            boolean[][] sameFind = testDD.SameMap[i];
            for(int j = 0; j < sameFind.length; j++){
                for(int k = 0; k < sameFind.length; k++){
                    if(sameFind[j][k] & trueDup.isSame[j][k]){
                        cnt++;
                    }
                }
            }
            System.out.println("[Dup] find "+testDD.findSameSize[i]+ "same and recall"+ cnt);
            System.out.println("[Dup] all true pairs is "+ trueDup.sameTupleSize + "recall rate： " + ((double) (cnt))/ trueDup.sameTupleSize);
            System.out.println("[Dup] precision: "+ (double) cnt /testDD.findSameSize[i]);
        }
    }
}
