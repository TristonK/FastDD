package ddfinder.pli;

import java.util.*;

public class Pli implements IPli<Double>{

    public PliShard pliShard;   // the PliShard that this PLI belongs to

    public Double[] keys;
    List<Cluster> clusters;
    Map<Double, Integer> keyToClusterIdMap;

    public Pli(List<Cluster> rawClusters, Double[] keys, Map<Double, Integer> translator) {
        this.clusters = rawClusters;
        this.keys = keys;
        this.keyToClusterIdMap = translator;
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
            if (keys[m] <= target) {
                r = m;
            } else {
                l = m + 1;
            }
        }

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
}
