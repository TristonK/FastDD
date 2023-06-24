package ddfinder.pli;


import com.koloboke.collect.set.hash.*;
import de.metanome.algorithms.dcfinder.input.ParsedColumn;
import net.mintern.primitive.Primitive;

import java.util.*;


public class PliShardBuilder {

    private final int shardLength;
    private final int colSize;
    private final boolean[] isNum;

    public static int[][] clusterInput;

    public PliShardBuilder(int _shardLength, List<ParsedColumn<?>> pColumns) {
        shardLength = _shardLength;
        colSize = pColumns.size();

        isNum = new boolean[colSize];
        for (int col = 0; col < colSize; col++) {
            isNum[col] = pColumns.get(col).getType() != String.class;
        }
    }

    // build the Pli of a given column within tuple id range [beg, end)
    private DoublePli buildDoublePli(double[] colValues, int beg, int end, int index) {
        HashDoubleSet keySet = HashDoubleSets.newMutableSet();
        for (int row = beg; row < end; ++row) {
            keySet.add(colValues[row]);
        }

        double[] keys = keySet.toDoubleArray();
        Double[] dkeys = new Double[keys.length];

        Primitive.sort(keys, (a, b) -> Double.compare(b, a), false);


        Map<Double, Integer> keyToClusterID = new HashMap<>(); // int (key) -> cluster id
        for (int clusterID = 0; clusterID < keys.length; clusterID++) {
            dkeys[clusterID] = keys[clusterID];
            keyToClusterID.put(dkeys[clusterID], clusterID);
        }

        List<Cluster> clusters = new ArrayList<>();             // put tuple indexes in clusters
        for (int i = 0; i < keys.length; i++) {
            clusters.add(new Cluster());
        }
        for (int row = beg; row < end; ++row) {
            clusters.get(keyToClusterID.get(colValues[row])).add(row);
            if(shardLength == -1){clusterInput[index][row] = keyToClusterID.get(colValues[row]);}
        }
        if(shardLength == -1){
            return new DoublePli(clusters, dkeys, keyToClusterID, clusterInput[index]);
        }
        return  new DoublePli(clusters, dkeys, keyToClusterID, null);
    }

    private LongPli buildIntPli(long[] colValues, int beg, int end, int index) {
        HashLongSet keySet = HashLongSets.newMutableSet();
        for (int row = beg; row < end; ++row) {
            keySet.add(colValues[row]);
        }

        long[] keys = keySet.toLongArray();
        Long[] ikeys = new Long[keys.length];
        Primitive.sort(keys, (a, b) -> Long.compare(b, a), false);

        Map<Long, Integer> keyToClusterID = new HashMap<>(); // long (key) -> cluster id
        for (int clusterID = 0; clusterID < keys.length; clusterID++) {
            ikeys[clusterID] = keys[clusterID];
            keyToClusterID.put(ikeys[clusterID], clusterID);
        }

        List<Cluster> clusters = new ArrayList<>();             // put tuple indexes in clusters
        for (int i = 0; i < keys.length; i++) {
            clusters.add(new Cluster());
        }
        for (int row = beg; row < end; ++row) {
            clusters.get(keyToClusterID.get(colValues[row])).add(row);
            if(shardLength == -1){clusterInput[index][row] = keyToClusterID.get(colValues[row]);}
        }
        if(shardLength == -1){
            return new LongPli(clusters, ikeys, keyToClusterID, clusterInput[index]);
        }
        return  new LongPli(clusters, ikeys, keyToClusterID, null);
    }

    private StringPli buildStringPli(String[] colValues, int beg, int end, int index) {
        Set<String> keySet = new HashSet<>();
        for (int row = beg; row < end; ++row) {
            keySet.add(colValues[row]);
        }

        String[] keys = keySet.toArray(new String[0]);

        Map<String, Integer> keyToClusterID = new HashMap<>(); // int (key) -> cluster id
        for (int clusterID = 0; clusterID < keys.length; clusterID++) {
            keyToClusterID.put(keys[clusterID], clusterID);
        }

        List<Cluster> clusters = new ArrayList<>();             // put tuple indexes in clusters
        for (int i = 0; i < keys.length; i++) {
            clusters.add(new Cluster());
        }
        for (int row = beg; row < end; ++row) {
            clusters.get(keyToClusterID.get(colValues[row])).add(row);
            if(shardLength == -1){clusterInput[index][row] = keyToClusterID.get(colValues[row]);}
        }
        if(shardLength == -1){
            return new StringPli(clusters, keys, keyToClusterID, clusterInput[index]);
        }
        return  new StringPli(clusters, keys, keyToClusterID, null);
    }

    public PliShard[] buildPliShards(double[][] doubleInput, long[][] longInput, String[][] stringInput) {
        int rowEnd;
        if (doubleInput == null || doubleInput.length == 0 || doubleInput[0].length == 0) {
            if(longInput == null || longInput.length == 0 || longInput[0].length == 0){
                if(stringInput== null || stringInput.length == 0 || stringInput[0].length == 0){
                    return new PliShard[0];
                }else{
                    rowEnd = stringInput[0].length;
                }
            }else{
                rowEnd = longInput[0].length;
            }
        }else {
            rowEnd = doubleInput[0].length;
        }

        int rowBeg = 0;
        int nShards = 1;
        if(shardLength != -1) {
            nShards = (rowEnd - rowBeg - 1) / shardLength + 1;
        }
        PliShard[] pliShards = new PliShard[nShards];
        if(shardLength == -1){
            clusterInput = new int[colSize][rowEnd];
        }

        for(int i=0; i< nShards; i++) {
            int shardBeg = rowBeg + i * shardLength, shardEnd = Math.min(rowEnd, shardBeg + shardLength);
            if(shardLength == -1){
                shardBeg = rowBeg; shardEnd = rowEnd;
            }
            List<IPli> plis = new ArrayList<>();
            for (int j =0 ;j < longInput.length; j++) {
                plis.add(buildIntPli(longInput[j], shardBeg, shardEnd, j));
            }
            for (int j =0 ;j < doubleInput.length; j++) {
                plis.add(buildDoublePli(doubleInput[j], shardBeg, shardEnd,  j + longInput.length));
            }
            for (int j =0 ;j < stringInput.length; j++) {
                plis.add(buildStringPli(stringInput[j], shardBeg, shardEnd, j + longInput.length + doubleInput.length));
            }
            pliShards[i] = new PliShard(plis, shardBeg, shardEnd);
        }
        return pliShards;
    }
}