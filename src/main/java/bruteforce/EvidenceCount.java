package bruteforce;

import ch.javasoft.bitset.LongBitSet;
import ddfinder.predicate.PredicateBuilder;
import de.metanome.algorithms.dcfinder.input.Input;
import de.metanome.algorithms.dcfinder.input.ParsedColumn;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author tristonK 2023/2/7
 */
public class EvidenceCount {
    public static int calculate(Input input){
        Set<LongBitSet> clueSet = new HashSet<>();
        double[][] dInput = input.getDoubleInput();
        List<ParsedColumn<?>> columns = input.getParsedColumns();
        for(int i = 0; i < dInput[0].length-1; i++){
            for(int j = i+1; j<dInput[0].length; j++){
                LongBitSet clue = new LongBitSet(PredicateBuilder.getIntervalCnt());
                int cnt = 0;
                for(int k = 0; k < dInput.length; k++){
                    double diff = Math.abs(dInput[k][i]- dInput[k][j]);
                    List<Double> th = columns.get(k).getThresholds();
                    int c = 0;
                    if(diff <= th.get(0)){
                        c = 0;
                    } else if (diff > th.get(th.size()-1)) {
                        c = th.size();
                    }else{
                        while(c < th.size()-1){
                            if(diff > th.get(c) && diff <= th.get(c+1)){
                                c++;
                                break;
                            }
                            c++;
                        }
                    }
                    clue.set(c + cnt);
                    cnt += th.size() + 1;
                }
                clueSet.add(clue);
            }
        }
        return clueSet.size();
    }
}
