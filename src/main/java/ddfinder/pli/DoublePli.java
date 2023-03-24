package ddfinder.pli;

import java.util.*;

public class DoublePli implements IPli<Double>{

    public PliShard pliShard;   // the PliShard that this PLI belongs to

    public Double[] keys;
    List<Cluster> clusters;
    Map<Double, Integer> keyToClusterIdMap;

    Map<Integer, Map<Integer, Integer>> twoIdToThresholds;
    int[] rowToId;

    public DoublePli(List<Cluster> rawClusters, Double[] keys, Map<Double, Integer> translator, int[]rowToId) {
        this.clusters = rawClusters;
        this.keys = keys;
        this.keyToClusterIdMap = translator;
        this.twoIdToThresholds = new HashMap<>();
        this.rowToId = rowToId;
    }


    public int size() {
        return keys.length;
    }

    public Double[] getKeys() {
        return keys;
    }

    public List<Cluster> getClusters() {
        return clusters;
    }

    public Cluster getClusterByKey(Double key) {
        Integer clusterId = keyToClusterIdMap.get(key);
        return clusterId != null ? clusters.get(clusterId) : null;
    }

    public Integer getClusterIdByKey(Double key) {
        return keyToClusterIdMap.get(key);
    }

    public Cluster get(int i) {
        return clusters.get(i);
    }

    private final double ERR = 0.000000001;

    /**
    * @param inequal: 0: return LTE, 1: retrun LT
    * */
    public int getFirstIndexWhereKeyIsLT(Double target, int l, int inequal) {
        Integer i = keyToClusterIdMap.get(target);
        if (i != null) {
            return i + inequal;
        }

        int r = keys.length;
        while (l < r) {
            int m = l + ((r - l) >>> 1);
            if (keys[m] < target + ERR) {
                r = m;
            } else {
                l = m + 1;
            }
        }
        if(inequal == 1 && l < keys.length &&Math.abs(keys[l] - target) < ERR){return l + 1;}
        return l;
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
            sb.append(keys[i] + ": " + clusters.get(i) + "\n");
        }

        sb.append(Arrays.toString(keys) + "\n");
        sb.append(keyToClusterIdMap + "\n");

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
