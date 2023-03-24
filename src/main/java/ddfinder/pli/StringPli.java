package ddfinder.pli;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tristonK 2023/2/23
 */
public class StringPli implements IPli<String>{

    public PliShard pliShard;   // the PliShard that this PLI belongs to

    public String[] keys;
    List<Cluster> clusters;
    Map<String, Integer> keyToClusterIdMap;

    Map<Integer, Map<Integer, Integer>> twoIdToThresholds;

    int[] rowToId;

    public StringPli(List<Cluster> rawClusters, String[] keys, Map<String, Integer> translator, int[] rowToId) {
        this.clusters = rawClusters;
        this.keys = keys;
        this.keyToClusterIdMap = translator;
        this.twoIdToThresholds = new HashMap<>();
        this.rowToId = rowToId;
    }


    public int size() {
        return keys.length;
    }

    public String[] getKeys() {
        return keys;
    }

    public List<Cluster> getClusters() {
        return clusters;
    }

    public Cluster getClusterByKey(String key) {
        Integer clusterId = keyToClusterIdMap.get(key);
        return clusterId != null ? clusters.get(clusterId) : null;
    }

    public Integer getClusterIdByKey(String key) {
        return keyToClusterIdMap.get(key);
    }

    public Cluster get(int i) {
        return clusters.get(i);
    }

    /**
     * @param inequal: 0: return LTE, 1: retrun LT
     * */
    public int getFirstIndexWhereKeyIsLT(String target, int l, int inequal) {
        throw new IllegalCallerException("should not call binary search in string pli");
    }

    /**
     * @return
     */
    @Override
    public PliShard getPliShard() {
        return this.pliShard;
    }

    /**
     * @param pliShard
     */
    @Override
    public void setPlishard(PliShard pliShard) {
        this.pliShard = pliShard;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < clusters.size(); i++) {
            sb.append(keys[i]).append(": ").append(clusters.get(i)).append("\n");
        }

        sb.append(Arrays.toString(keys)).append("\n");
        sb.append(keyToClusterIdMap).append("\n");

        return sb.toString();
    }

    /**
     * @param leftIndex
     * @param rightIndex
     * @return
     */
    @Override
    public int getThresholdsBetween(int leftIndex, int rightIndex) {
        if(leftIndex == rightIndex){return 0;}
        if(rightIndex < leftIndex){
            int tmp = rightIndex;
            rightIndex = leftIndex;
            leftIndex = tmp;
        }
        Map<Integer, Integer> rightKeyMap = twoIdToThresholds.getOrDefault(leftIndex, new HashMap<>());
        return rightKeyMap.getOrDefault(rightIndex, -1);
    }

    /**
     * @param leftIndex
     * @param rightIndex
     * @param thresholdIndex
     */
    @Override
    public void setThresholdsBetween(int leftIndex, int rightIndex, int thresholdIndex) {
        if(rightIndex == leftIndex){return;}
        if(rightIndex < leftIndex){
            int tmp = rightIndex;
            rightIndex = leftIndex;
            leftIndex = tmp;
        }
        Map<Integer, Integer> rightKeyMap = twoIdToThresholds.getOrDefault(leftIndex, new HashMap<>());
        rightKeyMap.putIfAbsent(rightIndex, thresholdIndex);
    }

    /**
     * @param row
     * @return
     */
    @Override
    public int getClusterIdByRow(int row) {
        return rowToId[row];
    }
}
