package ddfinder.pli;

import org.roaringbitmap.RoaringBitmap;

import java.util.ArrayList;
import java.util.List;

public class Cluster {

    List<Integer> tuples;
    RoaringBitmap rbm;

    public Cluster() {
        tuples = new ArrayList<>(); rbm = new RoaringBitmap();
    }

    public int get(int i) {
        return tuples.get(i);
    }

    public int size() {
        return tuples.size();
    }

    public List<Integer> getRawCluster() {
        return tuples;
    }

    public boolean isEmpty() {
        return tuples.isEmpty();
    }

    public void add(int row) {
        tuples.add(row);
        rbm.add(row);
    }

    public void add(Cluster c) {
        tuples.addAll(c.tuples);
        for(int t: c.tuples){rbm.add(t);}
    }

    public RoaringBitmap getRbm(){
        return rbm;
    }

    @Override
    public String toString() {
        return tuples.toString();
    }
}