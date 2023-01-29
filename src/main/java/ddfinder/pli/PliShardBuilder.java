package ddfinder.pli;


import com.koloboke.collect.set.hash.HashDoubleSet;
import com.koloboke.collect.set.hash.HashDoubleSets;
import com.koloboke.collect.set.hash.HashIntSet;
import com.koloboke.collect.set.hash.HashIntSets;
import de.metanome.algorithms.dcfinder.input.ParsedColumn;
import net.mintern.primitive.Primitive;

import java.util.*;


public class PliShardBuilder {

    private final int shardLength;
    private final boolean[] isNum;

    public PliShardBuilder(int _shardLength, List<ParsedColumn<?>> pColumns) {
        shardLength = _shardLength;
        int colCount = pColumns.size();

        isNum = new boolean[colCount];
        for (int col = 0; col < colCount; col++) {
            isNum[col] = pColumns.get(col).getType() != String.class;
        }
    }

    // build the Pli of a given column within tuple id range [beg, end)
    private Pli buildPli(boolean isNum, double[] colValues, int beg, int end) {
        HashDoubleSet keySet = HashDoubleSets.newMutableSet();
        for (int row = beg; row < end; ++row) {
            keySet.add(colValues[row]);
        }

        double[] keys = keySet.toDoubleArray();
        if (isNum) {
            Primitive.sort(keys, (a, b) -> Double.compare(b, a), false);
        }

        Map<Double, Integer> keyToClusterID = new HashMap<>(); // int (key) -> cluster id
        for (int clusterID = 0; clusterID < keys.length; clusterID++) {
            keyToClusterID.put(keys[clusterID], clusterID);
        }

        List<Cluster> clusters = new ArrayList<>();             // put tuple indexes in clusters
        for (int i = 0; i < keys.length; i++) {
            clusters.add(new Cluster());
        }
        for (int row = beg; row < end; ++row) {
            clusters.get(keyToClusterID.get(colValues[row])).add(row);
        }

        return new Pli(clusters, keys, keyToClusterID);
    }

    public PliShard[] buildPliShards(double[][] doubleInput) {
        if (doubleInput == null || doubleInput.length == 0 || doubleInput[0].length == 0) {
            return new PliShard[0];
        }

        int rowBeg = 0, rowEnd = doubleInput[0].length;
        int nShards = (rowEnd - rowBeg - 1) / shardLength + 1;
        PliShard[] pliShards = new PliShard[nShards];

        for(int i=0; i< nShards; i++) {
            int shardBeg = rowBeg + i * shardLength, shardEnd = Math.min(rowEnd, shardBeg + shardLength);
            List<Pli> plis = new ArrayList<>();
            for (int col = 0; col < doubleInput.length; col++) {
                plis.add(buildPli(isNum[col], doubleInput[col], shardBeg, shardEnd));
            }
            pliShards[i] = new PliShard(plis, shardBeg, shardEnd);
        }

//        IntStream.range(0, nShards).forEach(i -> {
//            int shardBeg = rowBeg + i * shardLength, shardEnd = Math.min(rowEnd, shardBeg + shardLength);
//            List<Pli> plis = new ArrayList<>();
//            for (int col = 0; col < intInput.length; col++)
//                plis.add(buildPli(isNum[col], intInput[col], shardBeg, shardEnd));
//            pliShards[i] = new PliShard(plis, shardBeg, shardEnd);
//        });
        return pliShards;
    }
}