import fastdd.Config;
import fastdd.RunDD;
import fastdd.differentialdependency.DifferentialDependencySet;
import de.metanome.algorithms.dcfinder.input.Input;
import de.metanome.algorithms.dcfinder.input.RelationalInput;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class Main {
    public static void main(String[] args) throws IOException {
        if(args.length <= 2){
            System.out.println("Error: You must call 3 or 4 params: <file-path> <rows-limit> <thresholds-file-path>, the forth param: 1: use td-po, else: use bf");
            return;
        }

        String fp = args[0];
        // limit the number of tuples in dataset, -1 means no limit
        int rowLimit = Integer.parseInt(args[1]);
        String dfp = args[2];

        if (args.length > 3 && Integer.parseInt(args[3]) == 1){
                Config.method = "TD-Po";
        }
        System.out.println("Method: " + Config.method);

        long memoryConsumption = 0L;
        List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
        RunDD worker = new RunDD(new Input(new RelationalInput(fp), rowLimit), dfp);
        worker.buildDDs();
        for (MemoryPoolMXBean pool : pools) {
            MemoryUsage peak = pool.getPeakUsage();
            memoryConsumption += peak.getUsed();
        }
        memoryConsumption /= 1048576L;
        System.out.println("Memory Consumption: " + memoryConsumption + "MB");
    }

}
