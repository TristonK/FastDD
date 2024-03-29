package fastdd.dfset;

import fastdd.Config;
import fastdd.dfset.isnimpl.CrossISNBuilder;
import fastdd.dfset.isnimpl.SingleISNBuilder;
import fastdd.dfset.offsetimpl.BinaryCalOffset;
import fastdd.pli.PliShard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author tristonK 2023/8/2
 */
public class Executor {
    int numThreads = Config.TestMultiThread ? Config.ThreadSize : 1;
    HashMap<Long, Long> res = new HashMap<>();
    public Executor(PliShard[] pliShards){
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Callable<HashMap<Long, Long>>> tasks = new ArrayList<>();
        List<Future<HashMap<Long, Long>>> futures = new ArrayList<>();

        for (int i = 0; i < pliShards.length; i++) {
            IOffset util = new BinaryCalOffset();
           // tasks.add(new LongSingleClueSetBuilder(pliShards[i], util));
            futures.add(executor.submit(new SingleISNBuilder(pliShards[i], util)));
            for (int j = i + 1; j < pliShards.length; j++) {
                IOffset util2 = new BinaryCalOffset();
                futures.add(executor.submit(new CrossISNBuilder(pliShards[i], pliShards[j], util2)));
            }
        }

        try {
            for (Future<HashMap<Long, Long>> future : futures) {
                var result = future.get();
                //for (var e : result.entrySet()) {
                    result.forEach((k, v) -> res.merge(k, v, Long::sum));
                    //res.put(e.getKey(), res.getOrDefault(e.getKey(), 0L) + e.getValue());
                //}
            }
        } catch (ExecutionException | InterruptedException e) {
            System.err.println("Task execution failed.");
            e.printStackTrace();
        }

        executor.shutdown();
    }
}
