import bruteforce.FindDup;
import bruteforce.FindVio;
import fastdd.Config;
import fastdd.FastDD;
import fastdd.differentialdependency.DifferentialDependency;
import fastdd.differentialdependency.DifferentialDependencySet;
import de.metanome.algorithms.dcfinder.input.Input;
import de.metanome.algorithms.dcfinder.input.RelationalInput;

import java.io.IOException;
import java.util.concurrent.ForkJoinPool;

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
        if (args.length > 3){
            Config.ThreadSize = Integer.parseInt(args[3]);
        }
        if (Config.TestMultiThread && Config.ThreadSize > 0){
            System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", Integer.toString(Config.ThreadSize));
            System.out.println("Paralism: "+ ForkJoinPool.commonPool().getParallelism());
        }


        FastDD dDFast = new FastDD(new Input(new RelationalInput(fp), rowLimit), dfp);
        // DifferentialDependencySet dds = dDFast.buildDDs();

        // 找重复值
        /*FindDup.benchPath = "xx";
        FindDup.trueAssert = new String[]{
                "{ [name(<=0.0)] ∧ [addr(<=13.0)] -> [phone(<=8.0)]}"
        };
        dDFast.findDup();*/

        // 找违反对
        /*FindVio.DDFilePath = "xx";
        FindVio.BenchFilePath = "xx";
        dDFast.findVio();*/
    }

}
