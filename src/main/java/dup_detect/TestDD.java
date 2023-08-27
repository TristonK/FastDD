package dup_detect;

import ch.javasoft.bitset.LongBitSet;
import de.metanome.algorithms.dcfinder.input.Input;
import de.metanome.algorithms.dcfinder.input.ParsedColumn;
import fastdd.differentialfunction.DifferentialFunctionBuilder;
import fastdd.utils.DistanceCalculation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author tristonK 2023/8/27
 */
public class TestDD {
    boolean TestDup = false;
    List<LongBitSet> DDLefts = null;

    private final double ERR = 0.000000001;

    public TestDD(List<LongBitSet> ddLefts){
        this.DDLefts = ddLefts;
    }
    public boolean[][][] SameMap;
    public long[] findSameSize;

    private int findMaskPos(double diff, List<Double> th) {//找到元组差值在阈值列表中的位置
        int c = 0;
        if (diff < th.get(0) + ERR) {
            c = 0;
        } else if (diff > th.get(th.size() - 1) + ERR) {
            c = th.size();
        } else {
            while (c < th.size() - 1) {
                if (diff > th.get(c) + ERR && diff < th.get(c + 1) + ERR) {
                    c++;
                    break;
                }
                c++;
            }
        }
        return c;
    }

    public void calculateDup(Input input, DifferentialFunctionBuilder differentialFunctionBuilder) {
        List<List<LongBitSet>> countToPredicateSets = new ArrayList<>();
        for (int i = 0; i < differentialFunctionBuilder.getColSize(); i++) {
            countToPredicateSets.add(differentialFunctionBuilder.getOffset2SatisfiedPredicates(i));
        }
        //int -> double -> string
        double[][] dInput = input.getDoubleInput();
        long[][] iInput = input.getLongInput();
        String[][] sInput = input.getStringInput();
        List<ParsedColumn<?>> columns = input.getParsedColumns();
        int rows = 0;
        if (dInput.length == 0) {
            if (iInput.length != 0) {
                rows = iInput[0].length;
            } else {
                if (sInput.length == 0) {
                    assert false : "input rows is 0";
                } else {
                    rows = sInput[0].length;
                }
            }
        } else {
            rows = dInput[0].length;
        }

        SameMap = new boolean[DDLefts.size()][rows][rows];
        findSameSize = new long[DDLefts.size()];

        for (int i = 0; i < rows - 1; i++) {
            for (int j = i + 1; j < rows; j++) {
                LongBitSet evidence = new LongBitSet();
                for (int k = 0; k < iInput.length; k++) {
                    double diff = Math.abs(iInput[k][i] - iInput[k][j]);
                    List<Double> th = columns.get(k).getThresholds();
                    LongBitSet mask = countToPredicateSets.get(k).get(findMaskPos(diff, th));
                    evidence.or(mask);
                }
                for (int k = 0; k < dInput.length; k++) {
                    double diff = Math.abs(dInput[k][i] - dInput[k][j]);
                    List<Double> th = columns.get(k + iInput.length).getThresholds();
                    LongBitSet mask = countToPredicateSets.get(k + iInput.length).get(findMaskPos(diff, th));
                    evidence.or(mask);
                }
                for (int k = 0; k < sInput.length; k++) {
                    double diff = DistanceCalculation.StringDistance(sInput[k][i], sInput[k][j]);
                    List<Double> th = columns.get(k + iInput.length + dInput.length).getThresholds();
                    LongBitSet mask = countToPredicateSets.get(k + iInput.length + dInput.length).get(findMaskPos(diff, th));
                    evidence.or(mask);
                }
                for(int ll = 0; ll < DDLefts.size(); ll++){
                    if (DDLefts.get(ll).isSubSetOf(evidence)){
                        SameMap[ll][i][j] = SameMap[ll][j][i] = true;
                        findSameSize[ll] ++;
                    }
                }
            }
        }
    }
}
