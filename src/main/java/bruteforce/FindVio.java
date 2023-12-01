package bruteforce;

import ch.javasoft.bitset.LongBitSet;
import de.metanome.algorithms.dcfinder.input.Input;
import fastdd.differentialdependency.Parser;
import fastdd.differentialfunction.DifferentialFunctionBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author tristonK 2023/12/1
 */
public class FindVio {
    public static String DDFilePath = "";
    public static String BenchFilePath = "";

    Set<Parser> parserSet;
    public DifferentialFunctionBuilder dfBuilder;

    public FindVio(DifferentialFunctionBuilder dfBuilder){
        parserSet = new HashSet<>();
        this.dfBuilder = dfBuilder;
        Parser.configure(dfBuilder);
    }

    public void parseDDs(){
        Path path = Paths.get(DDFilePath);
        try {
            Files.lines(path).forEach(line -> parserSet.add(new Parser(line.trim())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void run(Input input) {
        parseDDs();
        CalculateDF calWorker = new CalculateDF(input, dfBuilder);
        Set<String> vioString = new HashSet<>();
        int rows = calWorker.rows;
        for (int i = 0; i < rows - 1; i++) {
            for (int j = i + 1; j < rows; j++) {
                LongBitSet dfset = calWorker.getDF(i, j);
                for(Parser dd: parserSet){
                    if(dd.left.isSubSetOf(dfset) && !dd.right.isSubSetOf(dfset)){
                        vioString.add(String.valueOf(i));
                        vioString.add(String.valueOf(j));
                    }
                }
            }
        }
        check(vioString);
    }

    private void check(Set<String> found){
        Path path = Paths.get(BenchFilePath);
        Set<String> lines = new HashSet<>();
        try {
            Files.lines(path).forEach(line -> lines.add(line.trim()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Find candidate #vio " + found.size() + " Truth #vio "+ lines.size());

        // Accurency
        Set<String> wrongCall = new HashSet<>(found);
        wrongCall.removeAll(lines);
        System.out.println("Precision: "+(100.00 * (found.size()-wrongCall.size())/found.size()) + "%");
        System.out.println("Recall: "+(100.00 * (found.size()-wrongCall.size())/lines.size()) + "%");
    }
}
