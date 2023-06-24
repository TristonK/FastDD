import ddfinder.DDFinder;
import ddfinder.differentialdependency.DifferentialDependencySet;
import de.metanome.algorithms.dcfinder.input.Input;
import de.metanome.algorithms.dcfinder.input.RelationalInput;

import java.io.IOException;

public class Main {

    /**
     * input: filePath
     * input: filePath size
     * input: filePath size dfFlie
     */
    public static void main(String[] args) throws IOException {
        String fp = args[0];

        // limit the number of tuples in dataset, -1 means no limit
        int rowLimit = -1;
        if(args.length >= 2){
            rowLimit = Integer.parseInt(args[1]);
        }

        String dfp = "";
        if(args.length > 2){
            dfp = args[2];
        }

        DDFinder dDFinder = new DDFinder(new Input(new RelationalInput(fp), rowLimit), dfp);
        DifferentialDependencySet dds = dDFinder.buildDDs();
    }

}
