package ddfinder.pli;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tristonK 2023/2/23
 */
public class LongPli implements IPli<Long>{
    public PliShard pliShard;   // the PliShard that this PLI belongs to

    public Long[] keys;
    List<Cluster> clusters;
    Map<Long, Integer> keyToClusterIdMap;

    Map<Integer, Map<Integer, Integer>> twoIdToThresholds;

    int[] rowToId;

    public LongPli(List<Cluster> rawClusters, Long[] keys, Map<Long, Integer> translator, int[] rowToId) {
        this.clusters = rawClusters;
        this.keys = keys;
        this.keyToClusterIdMap = translator;
        this.twoIdToThresholds = new HashMap<>();
        this.rowToId = rowToId;
    }


    public int size() {
        return keys.length;
    }

    public Long[] getKeys() {
        return keys;
    }

    public List<Cluster> getClusters() {
        return clusters;
    }

    public Cluster getClusterByKey(Long key) {
        Integer clusterId = keyToClusterIdMap.get(key);
        return clusterId != null ? clusters.get(clusterId) : null;
    }

    public Integer getClusterIdByKey(Long key) {
        return keyToClusterIdMap.get(key);
    }

    public Cluster get(int i) {
        return clusters.get(i);
    }

    private final double ERR = 0.000000001;
    /**
     * @param inequal: 0: return LTE, 1: retrun LT
     * */
    public int getFirstIndexWhereKeyIsLT(Long target, int l, int inequal) {
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
        if(inequal == 1 && l < keys.length && Math.abs(keys[l] - target) < ERR){return l + 1;}
        return l;
    }

    @Override
    public PliShard getPliShard() {
        return this.pliShard;
    }

    @Override
    public void setPlishard(PliShard pliShard) {
        this.pliShard = pliShard;
    }

    @Override
    public int getClusterIdByRow(int row) {
        return rowToId[row];
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
}
