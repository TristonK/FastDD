package ddfinder.pli;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;

/**
 * @author tristonK 2023/2/23
 */
public interface IPli<T> {
    public int size();

    public T[] getKeys();

    public List<Cluster> getClusters();

    public Cluster getClusterByKey(T key);

    public Integer getClusterIdByKey(T key);

    public Cluster get(int i);

    /**
     * @param inequal: 0: return LTE, 1: retrun LT
     * */
    public int getFirstIndexWhereKeyIsLT(T target, int l, int inequal);

    public PliShard getPliShard();

    public void setPlishard(PliShard pli);

    public int getThresholdsBetween(int leftIndex, int rightIndex);

    public void setThresholdsBetween(int leftIndex, int rightIndex, int thresholdIndex);

    public int getClusterIdByRow(int row);
}
