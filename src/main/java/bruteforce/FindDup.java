package bruteforce;

import ch.javasoft.bitset.LongBitSet;
import de.metanome.algorithms.dcfinder.input.Input;
import de.metanome.algorithms.dcfinder.input.ParsedColumn;
import fastdd.differentialdependency.Parser;
import fastdd.differentialfunction.DifferentialFunctionBuilder;
import fastdd.utils.DistanceCalculation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author tristonK 2023/12/1
 */
public class FindDup {
    public static String benchPath = "D:\\Research\\2022-2023\\DDFinder\\dataset\\restaurant_true.txt";
    public static String[] trueAssert = new String[]{
            "{ [name(<=0.0)] ∧ [addr(<=13.0)] -> [phone(<=8.0)]}"
    };

    String[] wrongAssert = new String[]{

    };

    Set<Parser> parserSet;

    public DifferentialFunctionBuilder dfBuilder;

    public FindDup(DifferentialFunctionBuilder dfBuilder){
        this.dfBuilder = dfBuilder;
        Parser.configure(dfBuilder);
        parserSet = new HashSet<>();
        for(String assertion: trueAssert){
            Parser parser = new Parser(assertion.trim());
            parserSet.add(parser);
        }
    }

    public void run(Input input) {
        CalculateDF calWorker = new CalculateDF(input, dfBuilder);
        Set<String> sameString = new HashSet<>();
        int rows = calWorker.rows;
        for (int i = 0; i < rows - 1; i++) {
            for (int j = i + 1; j < rows; j++) {
               LongBitSet dfset = calWorker.getDF(i, j);
                for(Parser dd: parserSet){
                    if(dd.left.isSubSetOf(dfset)){
                        String s = i+" "+j;
                        sameString.add(s);
                    }
                }
            }
        }
        check(sameString);
    }


    private void check(Set<String> finds){
        Path path = Paths.get(benchPath);
        Set<String> lines = new HashSet<>();
        try {
            Files.lines(path).forEach(line -> lines.add(line.trim()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Find #dup " + finds.size() + " Truth #dup "+ lines.size());
        Set<String> wrongCall = new HashSet<>(finds);
        wrongCall.removeAll(lines);
        System.out.println("Precision: "+(100.00 * (finds.size()-wrongCall.size())/finds.size()) + "%");
        System.out.println("Recall: "+(100.00 * (finds.size()-wrongCall.size())/lines.size()) + "%");
    }
}
