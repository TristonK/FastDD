package bruteforce;

import ch.javasoft.bitset.LongBitSet;
import ddfinder.predicate.PredicateBuilder;
import ddfinder.utils.StringCalculation;
import de.metanome.algorithms.dcfinder.input.Input;
import de.metanome.algorithms.dcfinder.input.ParsedColumn;

import java.util.*;

/**
 * @author tristonK 2023/2/7
 */
public class EvidenceCount {
    private final double ERR = 0.000000001;
    public Set<LongBitSet> calculate(Input input){
        Set<LongBitSet> clueSet = new HashSet<>();
        //int -> double -> string
        double[][] dInput = input.getDoubleInput();
        int[][] iInput = input.getIntInput();
        String[][] sInput = input.getStringInput();
        Map<Double, Integer> countp1 = new HashMap<>();
        List<ParsedColumn<?>> columns = input.getParsedColumns();
        int rows = 0;
        if(dInput.length == 0){
            if(iInput.length!=0){rows = iInput[0].length;}
            else{
                if(sInput.length == 0){assert false:"input rows is 0";}
                else{rows = sInput[0].length;}
            }
        }else{
            rows = dInput[0].length;
        }
        for(int i = 0; i < rows-1; i++){
            for(int j = i+1; j<rows; j++){
                LongBitSet clue = new LongBitSet(PredicateBuilder.getIntervalCnt());
                int cnt = 0;
                for(int k = 0; k < iInput.length; k++){
                    double diff = Math.abs(iInput[k][i]- iInput[k][j]);
                    List<Double> th = columns.get(k).getThresholds();
                    if(k == 1){
                        countp1.put(diff, countp1.getOrDefault(diff, 0)+1);
                    }
                    clue.set(findMaskPos(diff, th) + cnt);
                    cnt += th.size() + 1;
                }
                for(int k = 0; k < dInput.length; k++){
                    double diff = Math.abs(dInput[k][i]- dInput[k][j]);
                    List<Double> th = columns.get(k + iInput.length).getThresholds();
                    clue.set(findMaskPos(diff, th) + cnt);
                    cnt += th.size() + 1;
                }
                for(int k = 0; k < sInput.length; k++){
                    double diff = StringCalculation.getDistance(sInput[k][i], sInput[k][j]);
                    List<Double> th = columns.get(k + iInput.length+ dInput.length).getThresholds();
                    clue.set(findMaskPos(diff, th) + cnt);
                    cnt += th.size() + 1;
                }
                clueSet.add(clue);
            }
        }
        Double[] keys = new Double[countp1.size()];
        countp1.keySet().toArray(keys);
        Arrays.sort(keys);
        int cnt = 0;
        for(int i =2; i < keys.length; i++){
            cnt += countp1.get(keys[i]);
            System.out.printf("%.2f  ", cnt*1.0/keys[i]);
        }
        System.out.println(" ");
        return clueSet;
    }

    private int findMaskPos(double diff, List<Double> th){
        int c = 0;
        if(diff < th.get(0) + ERR){
            c = 0;
        } else if (diff > th.get(th.size()-1) + ERR) {
            c = th.size();
        }else{
            while(c < th.size()-1){
                if(diff > th.get(c)+ERR && diff < th.get(c+1) + ERR){
                    c++;
                    break;
                }
                c++;
            }
        }
        return c;
    }
}
