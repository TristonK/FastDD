package fastdd.dfset;

import fastdd.pli.IPli;

import java.util.List;

/**
 * @author tristonK 2023/5/29
 */
public interface IOffset {
    final double ERR = 0.000000001;
    int[] countDouble(IPli probePli, int isSingle, Double[] keys, int startPos, double key, List<Double> thresholds);
    int[] countInt(IPli probePli, int isSingle, Long[] keys, int startPos, long key, List<Double> thresholds);

}
