package bruteforce;

import ch.javasoft.bitset.LongBitSet;
import fastdd.differentialdependency.DifferentialDependency;
import fastdd.differentialfunction.DifferentialFunctionBuilder;
import fastdd.utils.DistanceCalculation;
import de.metanome.algorithms.dcfinder.input.Input;
import de.metanome.algorithms.dcfinder.input.ParsedColumn;

import java.util.*;

/**
 * @author tristonK 2023/2/7
 */
public class EvidenceCount {
    private final double ERR = 0.000000001;

    public Set<LongBitSet> calculate(Input input) {
        Set<LongBitSet> clueSet = new HashSet<>();
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
        for (int i = 0; i < rows - 1; i++) {
            for (int j = i + 1; j < rows; j++) {
                LongBitSet clue = new LongBitSet(DifferentialFunctionBuilder.getIntervalCnt());
                int cnt = 0;
                for (int k = 0; k < iInput.length; k++) {
                    double diff = Math.abs(iInput[k][i] - iInput[k][j]);
                    List<Double> th = columns.get(k).getThresholds();
                    clue.set(findMaskPos(diff, th) + cnt);
                    cnt += th.size() + 1;//定位到下一个属性的阈值列表的起始位置
                }
                for (int k = 0; k < dInput.length; k++) {
                    double diff = Math.abs(dInput[k][i] - dInput[k][j]);
                    List<Double> th = columns.get(k + iInput.length).getThresholds();

                    clue.set(findMaskPos(diff, th) + cnt);
                    cnt += th.size() + 1;
                }
                for (int k = 0; k < sInput.length; k++) {
                    double diff = DistanceCalculation.StringDistance(sInput[k][i], sInput[k][j]);
                    List<Double> th = columns.get(k + iInput.length + dInput.length).getThresholds();
                    clue.set(findMaskPos(diff, th) + cnt);
                    cnt += th.size() + 1;
                }
                clueSet.add(clue);
            }
        }
        return clueSet;
    }

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

    boolean TestDup = true;

    //    LongBitSet DDLeft = null;
    public Set<LongBitSet> calculateEvidence(Input input, DifferentialFunctionBuilder differentialFunctionBuilder, List<LongBitSet> DDLeft) {
        List<List<LongBitSet>> countToPredicateSets = new ArrayList<>();
        for (int i = 0; i < differentialFunctionBuilder.getColSize(); i++) {
            countToPredicateSets.add(differentialFunctionBuilder.getOffset2SatisfiedPredicates(i));
        }
        Set<LongBitSet> evidenceSet = new HashSet<>();
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

        Map<Integer,List<String>> duplist = new HashMap<>();

        for (int i = 0; i < rows - 1; i++) {
            for (int j = i + 1; j < rows; j++) {
                LongBitSet evidence = new LongBitSet();
                int cnt = 0;
                for (int k = 0; k < iInput.length; k++) {
                    double diff = Math.abs(iInput[k][i] - iInput[k][j]);
                    List<Double> th = columns.get(k).getThresholds();
                    LongBitSet mask = countToPredicateSets.get(k).get(findMaskPos(diff, th));
                    evidence.or(mask);
                }
                ;
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
                evidenceSet.add(evidence);
                if (TestDup) {
                    for (int k = 0; k < DDLeft.size(); k++) {
                        LongBitSet left = DDLeft.get(k);
                        if (left.isSubSetOf(evidence)) {
                            String temp = Integer.toString(i) + ":" + Integer.toString(j);
                            addValue(duplist,k,temp);
                        }
                    }
                }
            }
        }
        for (Map.Entry<Integer, List<String>> entry : duplist.entrySet()) {
            int key = entry.getKey();
            List<String> values = entry.getValue();
            System.out.println("Dup for DD" + (key+1) + ":");
            printList(values);
        }
        return evidenceSet;
    }

    private static void addValue(Map<Integer, List<String>> map, int key, String value) {
        List<String> values = map.getOrDefault(key, new ArrayList<>());
        values.add(value);
        map.put(key, values);
    }
    private static void printList(List<String> list) {
        for (String value : list) {
            System.out.println(value);
        }
    }
}

