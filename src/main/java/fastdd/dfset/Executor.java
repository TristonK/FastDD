package fastdd.dfset;

import com.koloboke.collect.map.hash.HashLongLongMap;
import com.koloboke.collect.map.hash.HashLongLongMaps;
import fastdd.Config;
import fastdd.dfset.longclueimpl.LongClueSetBuilder;
import fastdd.dfset.longclueimpl.LongCrossClueSetBuilder;
import fastdd.dfset.longclueimpl.LongSingleClueSetBuilder;
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
    public static ConcurrentHashMap<Long, Long> res = new ConcurrentHashMap<Long, Long>();
    public Executor(PliShard[] pliShards){
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < pliShards.length; i++) {
            IClueOffset util = new BinaryCalOffset();
            tasks.add(new LongSingleClueSetBuilder(pliShards[i], util));
            for (int j = i + 1; j < pliShards.length; j++) {
                IClueOffset util2 = new BinaryCalOffset();
                tasks.add(new LongCrossClueSetBuilder(pliShards[i], pliShards[j], util2));
            }
        }

        // 运行所有任务并获取结果
        for(var task: tasks){
            executor.execute(task);
        }

        // 关闭Executor
        executor.shutdown();
        try{
            executor.awaitTermination(1000, TimeUnit.MINUTES);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }



    }
}
