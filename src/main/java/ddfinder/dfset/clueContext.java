package ddfinder.dfset;

import ch.javasoft.bitset.LongBitSet;
import org.roaringbitmap.RoaringBitmap;

/**
 * @author tristonK 2023/3/13
 */
public class clueContext {
    private int tid;
    private RoaringBitmap rightSide;
    private LongBitSet clue;

    public clueContext(int tid, int rightMaxSize){
        this.tid = tid;
        this.rightSide = new RoaringBitmap();
        rightSide.add(0L, (long) rightMaxSize);
        this.clue = new LongBitSet();
    }

    public clueContext(int tid, int maxTid, LongBitSet initialClue){
        this.tid = tid;
        this.rightSide = new RoaringBitmap();
        rightSide.add((long) tid + 1, (long) maxTid);
        this.clue = new LongBitSet(initialClue);
    }

    public clueContext(int tid, RoaringBitmap initialRight, LongBitSet initialClue){
        this.tid = tid;
        this.rightSide = initialRight;
        this.clue = initialClue;
    }

    public clueContext split(RoaringBitmap toSplit, int oldPos, int newPos){
        RoaringBitmap newRight = RoaringBitmap.and(rightSide, toSplit);
        if(newRight.getCardinality() > 0){
            if(rightSide.equals(newRight)){
                clue.clear(oldPos);
                clue.set(newPos);
            }else{
                rightSide.andNot(newRight);
                LongBitSet newClue = new LongBitSet(clue);
                newClue.clear(oldPos);
                newClue.set(newPos);
                return new clueContext(tid, newRight, newClue);
            }
        }
        return null;
    }

    public int getTid() {
        return tid;
    }

    public LongBitSet getClue() {
        return clue;
    }

    public RoaringBitmap getRightSide() {
        return rightSide;
    }
}
