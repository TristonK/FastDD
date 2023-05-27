package ddfinder.evidence;

import ch.javasoft.bitset.LongBitSet;
import ddfinder.evidence.ClueSetBuilder;
import ddfinder.pli.*;
import ddfinder.predicate.PredicateBuilder;
import ddfinder.utils.StringCalculation;

import java.util.*;

/**
 * @author tristonK 2023/3/3
 */
public class FullClueSetBuilder extends ClueSetBuilder {
    private final List<IPli> plis;
    private final int beg, end;

    private int[][] clusterInput;

    private final double ERR = 0.000000001;

    public FullClueSetBuilder(PliShard fullPliShard){
        plis = fullPliShard.plis;
        beg = fullPliShard.beg;
        end = fullPliShard.end;
        clusterInput = PliShardBuilder.clusterInput;
    }

    /**
     * @return
     */
    @Override
    public HashMap<LongBitSet, Long> buildClueSet() {
        System.out.println("shit");
        List<Map<Integer, Map<Integer, Integer>>> diffMatrics = new ArrayList<>();
        int[] pos = new int[intPacks.size() + doublePacks.size() + strPacks.size()];
        int cnt = 0;
        long t1 = System.currentTimeMillis();
        for(PredicatePack intPack: intPacks){
            diffMatrics.add(buildDiff(plis.get(intPack.colIndex), intPack.thresholds));
            pos[cnt] = intPack.pos; cnt++;
        }
        for(PredicatePack doublePack: doublePacks){
            diffMatrics.add(buildDiff(plis.get(doublePack.colIndex), doublePack.thresholds));
            pos[cnt] = doublePack.pos; cnt++;
        }
        for(PredicatePack strPack : strPacks){
            diffMatrics.add(buildDiff(plis.get(strPack.colIndex), strPack.thresholds));
            pos[cnt] = strPack.pos; cnt++;
        }
        System.out.println("shit"+ (System.currentTimeMillis() - t1));
        HashMap<LongBitSet, Long> clueSet = new HashMap<>();
        for(int i = beg; i < end-1; i++){
            for(int j = i + 1; j < end; j++){
                LongBitSet clue = new LongBitSet(PredicateBuilder.getIntervalCnt());
                for(int k = 0; k < pos.length; k++){
                    int c1 = clusterInput[k][i], c2 = clusterInput[k][j];
                    if(c1 > c2){int temp = c1; c1 = c2; c2 = temp;}
                    clue.set(diffMatrics.get(k).get(c1).get(c2) + pos[k]);
                }
                clueSet.merge(clue, 1L, Long::sum);
            }
        }
        System.out.println("shit");
        return clueSet;
    }

    public HashMap<LongBitSet, Long> linearBuildClueSet(){
        //TODO:实现线性
        HashMap<LongBitSet, Long> clueSet = new HashMap<>();
        return clueSet;
    }

    public HashMap<LongBitSet, Long> binaryBuildClueSet(){
        //TODO:实现二分
        HashMap<LongBitSet, Long> clueSet = new HashMap<>();
        return clueSet;
    }




    /**
     * @return (cluster1, cluster2) -> threshold(from 0 to thresholds.size())
     * */
    private Map<Integer, Map<Integer, Integer>> buildDiff(IPli pli, List<Double> thresholds){
        Map<Integer, Map<Integer, Integer>> pairsToThresholds = new HashMap<>();
        for(int cluster1 = 0; cluster1 < pli.size(); cluster1++){
            Map<Integer, Integer> cluster2ToThresholds = new HashMap<>();
            cluster2ToThresholds.put(cluster1, 0);
            int start = cluster1 + 1;
            if(pli.getClass() == StringPli.class){
                for(int j = cluster1 + 1; j < pli.size(); j++){
                    int diff = StringCalculation.getDistance((String) pli.getKeys()[cluster1], (String) pli.getKeys()[j]);
                    int c = 0;
                    if(diff < ERR + thresholds.get(0)){
                        c = 0;
                    } else if (diff > ERR + thresholds.get(thresholds.size()-1)) {
                        c = thresholds.size();
                    }else{
                        while(c < thresholds.size()-1){
                            if(diff > thresholds.get(c) + ERR && diff < ERR + thresholds.get(c+1)){
                                c++;
                                break;
                            }
                            c++;
                        }
                    }
                    cluster2ToThresholds.put(j, c);
                }
            }else if(pli.getClass() == IntPli.class){
                Integer key = (Integer) pli.getKeys()[cluster1];
                for(int index = 1; index < thresholds.size() && start < pli.size(); index++){
                    int end = pli.getFirstIndexWhereKeyIsLT(key-thresholds.get(index).intValue(), start, 1);
                    for(int correct = start ; correct < end && correct < pli.size(); correct++){
                       cluster2ToThresholds.put(correct, index);
                    }
                    start = end;
                }
                for(int correct = start; correct < pli.size(); correct++){
                    cluster2ToThresholds.put(correct, thresholds.size());
                }

            } else if(pli.getClass() == DoublePli.class){
                Double key = (Double)pli.getKeys()[cluster1];
                for(int index = 1; index < thresholds.size() && start < pli.size(); index++){
                    int end = pli.getFirstIndexWhereKeyIsLT(key-thresholds.get(index), start, 1);
                    for(int correct = start ; correct < end && correct < pli.size(); correct++){
                        cluster2ToThresholds.put(correct, index);
                    }
                    start = end;
                }
                for(int correct = start; correct < pli.size(); correct++){
                    cluster2ToThresholds.put(correct, thresholds.size());
                }
            }
            pairsToThresholds.put(cluster1, cluster2ToThresholds);
        }
        return pairsToThresholds;
    }
}
