package ddfinder.evidence.cluebitset;

import ch.javasoft.bitset.LongBitSet;

import java.util.Arrays;
import java.util.BitSet;

/**
 * @author tristonK 2023/3/6
 */
public class ByteClueBitSet implements IClueBitSet {
    //only 7 thresholds/ 8 intervals
    private static final int BITS_PER_UNIT = Byte.SIZE;
    private byte[] mUnits;

    public ByteClueBitSet(){
        this(BITS_PER_UNIT);
    }

    public ByteClueBitSet(int bitCapacity){
        mUnits = new byte[1 + (bitCapacity - 1) / BITS_PER_UNIT];
    }

    public ByteClueBitSet(int columnCnt, int thresholdsSize){
        if(thresholdsSize >= BITS_PER_UNIT){
            throw new IllegalArgumentException("Only supports attributes lower than " + BITS_PER_UNIT + "thresholds");
        }
        mUnits = new byte[columnCnt];
    }

    public ByteClueBitSet(IClueBitSet bitSet){
        this(bitSet.length());
        for (int bit = bitSet.nextSetBit(0); bit >= 0; bit = bitSet.nextSetBit(bit + 1)) {
            set(bit);
        }
    }

    public ByteClueBitSet(byte[] units, boolean cloneArray) {
        mUnits = cloneArray ? Arrays.copyOf(units, units.length) : units;
    }


    private void ensureCapacity(int unitLen) {
        if (mUnits.length < unitLen) {
            final byte[] newUnits = new byte[unitLen];
            System.arraycopy(mUnits, 0, newUnits, 0, mUnits.length);
            mUnits = newUnits;
        }
    }

    /**
     * @param bit The index of the bit to set
     */
    @Override
    public void set(int bit) {
        int unit = bit / BITS_PER_UNIT;
        ensureCapacity(unit + 1);
        mUnits[unit] |= 1L << (bit % BITS_PER_UNIT);
    }

    /**
     * @param bit   the index of the bit to set
     * @param value the value to be set
     */
    @Override
    public void set(int bit, boolean value) {
        if (value){
            set(bit);
        } else{
            clear(bit);
        }
    }

    /**
     * @param bit The index of the bit to clear
     */
    @Override
    public void clear(int bit) {
        int unit = bit / BITS_PER_UNIT;
        int index = bit % BITS_PER_UNIT;
        byte mask = (byte) (1 << index);
        mUnits[unit] &= ~mask;
    }

    /**
     *
     */
    @Override
    public void clear() {
        Arrays.fill(mUnits, (byte) 0);
    }

    /**
     * @param bit The index of the bit to flip
     */
    @Override
    public void flip(int bit) {
        int unit = bit / BITS_PER_UNIT;
        int index = bit % BITS_PER_UNIT;
        byte mask = (byte) (1 << index);
        ensureCapacity(unit + 1);
        mUnits[unit] ^= (mUnits[unit] & mask);
    }

    /**
     * @param bit The index of the asked bit
     * @return
     */
    @Override
    public boolean get(int bit) {
        int unit = bit / BITS_PER_UNIT;
        return unit < mUnits.length && 0L != (mUnits[unit] & (byte) (1 << (bit % BITS_PER_UNIT)));
    }

    /**
     * @param of The bit set, which is a superset of this bit set if the latter
     *           is a subset of it
     * @return
     */
    @Override
    public boolean isSubSetOf(IClueBitSet of) {
        return isSubSetOf(of instanceof ByteClueBitSet? (ByteClueBitSet) of : new ByteClueBitSet(of));
    }

    public boolean isSubSetOf(ByteClueBitSet of){
        if (this == of)
            return true;

        int min = Math.min(mUnits.length, of.mUnits.length);
        for (int ii = 0; ii < min; ii++) {
            byte and = (byte) (mUnits[ii] & of.mUnits[ii]);
            if (and != mUnits[ii])
                return false;
        }
        for (int i = min; i < mUnits.length; i++) {
            if (mUnits[i] != (byte) 0)
                return false;
        }
        return true;
    }

    /**
     * @param interA The first part of the intersection set
     * @param interB The second part of the intersection set
     * @return
     */
    @Override
    public boolean isSuperSetOfIntersection(IClueBitSet interA, IClueBitSet interB) {
        final ByteClueBitSet bA = interA instanceof ByteClueBitSet? (ByteClueBitSet) interA : new ByteClueBitSet(interA);
        final ByteClueBitSet bB = interB instanceof ByteClueBitSet? (ByteClueBitSet) interB : new ByteClueBitSet(interB);
        return isSuperSetOfIntersection(bA, bB);
    }

    public boolean isSuperSetOfIntersection(ByteClueBitSet interA, ByteClueBitSet interB){
        if (this == interA || this == interB)
            return true;
        int minInter = Math.min(interA.mUnits.length, interB.mUnits.length);
        int minAll = Math.min(mUnits.length, minInter);
        for (int ii = 0; ii < minAll; ii++) {
            byte inter = (byte) (interA.mUnits[ii] & interB.mUnits[ii]);
            if (inter != (byte) (inter & mUnits[ii]))
                return false;
        }
        for (int i = minAll; i < minInter; i++) {
            if ((byte)0 != (byte) (interA.mUnits[i] & interB.mUnits[i]))
                return false;
        }
        return true;
    }

    /**
     * @param with The bit set with which this bit set is logically and-ed
     */
    @Override
    public void and(IClueBitSet with) {
        and(with instanceof ByteClueBitSet ? (ByteClueBitSet) with : new ByteClueBitSet(with));
    }

    public void and(ByteClueBitSet with) {
        if (this == with)
            return;

        int len = Math.min(mUnits.length, with.mUnits.length);
        for (int ii = 0; ii < len; ii++) {
            mUnits[ii] &= with.mUnits[ii];
        }
        for (int ii = len; ii < mUnits.length; ii++) {
            mUnits[ii] = (byte) 0;
        }
    }

    /**
     * @param with The bit set with which this bit set is logically and-ed
     * @return
     */
    @Override
    public int getAndCardinality(IClueBitSet with) {
        return 0;
    }

    /**
     * @param with The bit set with which this bit set is logically and-ed
     * @return
     */
    @Override
    public IClueBitSet getAnd(IClueBitSet with) {
        return null;
    }

    /**
     * @param with The bit set with which's complement this bit set is logically
     *             and-ed
     */
    @Override
    public void andNot(IClueBitSet with) {

    }

    /**
     * @param with The bit set with which's complement this bit set is logically
     *             and-ed
     * @return
     */
    @Override
    public IClueBitSet getAndNot(IClueBitSet with) {
        return null;
    }

    /**
     * @param with The bit set with which this bit set is logically or-ed
     */
    @Override
    public void or(IClueBitSet with) {

    }

    /**
     * @param with The bit set with which this bit set is logically or-ed
     * @return
     */
    @Override
    public IClueBitSet getOr(IClueBitSet with) {
        return null;
    }

    /**
     * @param with The bit set with which this bit set is logically xor-ed
     */
    @Override
    public void xor(IClueBitSet with) {

    }

    /**
     * @param with The bit set with which this bit set is logically xor-ed
     * @return
     */
    @Override
    public IClueBitSet getXor(IClueBitSet with) {
        return null;
    }

    /**
     * @param with The bit set with which this bit set is logically xor-ed
     * @return
     */
    @Override
    public int getXorCardinality(IClueBitSet with) {
        return 0;
    }

    /**
     * @return
     */
    @Override
    public int length() {
        int index = mUnits.length;
        do
            index--;
        while (index >= 0 && mUnits[index] == (byte) 0);
        return index < 0 ? 0 : index * BITS_PER_UNIT + BITS_PER_UNIT - (Integer.numberOfLeadingZeros(mUnits[index])-24);
    }

    /**
     * @return
     */
    @Override
    public int cardinality() {
        int card = 0;
        for (byte mUnit : mUnits) {
            card += Integer.bitCount(mUnit);
        }
        return card;
    }

    /**
     * @param fromBit the start bit, inclusive
     * @param toBit   the end bit, exclusive
     * @return
     */
    @Override
    public int cardinality(int fromBit, int toBit) {
        int fromUnit = fromBit / BITS_PER_UNIT;
        int toUnit = (toBit + BITS_PER_UNIT - 1) / BITS_PER_UNIT;
        int unitStart = Math.max(0, fromUnit);
        int unitLen = Math.min(mUnits.length, toUnit);
        int card = 0;
        for (int ii = unitStart; ii < unitLen; ii++) {
            if (mUnits[ii] != (byte) 0) {
                byte unit = mUnits[ii];
                if (ii == fromUnit) {
                    int bit = fromBit % BITS_PER_UNIT;
                    unit &= (0xff >>> bit);
                }
                if (ii == toUnit - 1) {
                    int bit = fromBit % BITS_PER_UNIT;
                    unit &= (0xffL << (BITS_PER_UNIT - bit));
                }
                card += Integer.bitCount(unit);
            }
        }
        return card;
    }

    /**
     * @param from the index to start checking from (inclusive).
     * @return
     */
    @Override
    public int nextSetBit(int from) {
        if (from < 0) throw new IndexOutOfBoundsException("fromIndex < 0: " + from);
        int fromUnit = from / BITS_PER_UNIT;
        if (fromUnit >= mUnits.length) return -1;

        int unit = mUnits[fromUnit] & (0xff << (from % BITS_PER_UNIT));
        if (unit != 0)
            return fromUnit * BITS_PER_UNIT + Integer.numberOfTrailingZeros(unit);

        for (int ii = fromUnit + 1; ii < mUnits.length; ii++) {
            if (mUnits[ii] != (byte) 0)
                return ii * BITS_PER_UNIT + Integer.numberOfTrailingZeros(mUnits[ii]);
        }

        return -1;
    }

    /**
     * @param from the index to start checking from (inclusive).
     * @return
     */
    @Override
    public int nextClearBit(int from) {
        int fromBit = from % BITS_PER_UNIT;
        int fromUnit = from / BITS_PER_UNIT;
        for (int ii = fromUnit; ii < mUnits.length; ii++) {
            if (mUnits[ii] != (byte) -1) {
                byte unit = (byte) ~mUnits[ii];
                if (ii == fromUnit)
                    unit &= (0xff << fromBit);
                if (unit != 0)
                    return ii * BITS_PER_UNIT + Integer.numberOfTrailingZeros(unit);
            }
        }
        return Math.max(from, length());
    }

    /**
     * @return
     */
    @Override
    public IClueBitSet clone() {
        return new ByteClueBitSet(mUnits.clone(), false);
    }

    /**
     * @return
     */
    @Override
    public BitSet toBitSet() {
        BitSet bitSet = new BitSet(length());
        for (int bit = nextSetBit(0); bit >= 0; bit = nextSetBit(bit + 1)) {
            bitSet.set(bit);
        }
        return bitSet;
    }

    /**
     * @return
     */
    @Override
    public boolean isEmpty() {
        int index = mUnits.length;
        do
            index--;
        while (index >= 0 && mUnits[index] == (byte) 0);
        return index < 0;
    }

    /**
     * @return
     */
    @Override
    public int getUnitLength() {
        return BITS_PER_UNIT;
    }

    public int compareTo(IClueBitSet o) {
        return compareTo(o instanceof ByteClueBitSet ? (ByteClueBitSet) o : new ByteClueBitSet(o));
    }

    private byte flipByte(byte c)
    {
        c = (byte) (((c>>1)&0x55)|((c<<1)&0xAA));
        c = (byte) (((c>>2)&0x33)|((c<<2)&0xCC));
        c = (byte) ((c>>4) | (c<<4));

        return c;
    }

    public int compareTo(ByteClueBitSet o) {
        final int min = Math.min(mUnits.length, o.mUnits.length);
        for (int i = 0; i < min; i++) {
            // make unsigned comparison
            final boolean bitA = 0 != (0x1 & mUnits[i]);
            final boolean bitB = 0 != (0x1 & o.mUnits[i]);
            if (bitA != bitB) {
                return bitA ? 1 : -1;
            }
            final byte revA = flipByte((byte) ((byte)0xff & mUnits[i]));
            final byte revB = flipByte((byte) ((byte)0xff & o.mUnits[i]));
            final byte cmp = (byte) (revA - revB);

            if (cmp < 0)
                return -1;
            if (cmp > 0)
                return 1;
        }
        for (int i = min; i < mUnits.length; i++) {
            if (mUnits[i] != (byte) 0)
                return 1;
        }
        for (int i = min; i < o.mUnits.length; i++) {
            if (o.mUnits[i] != (byte) 0)
                return -1;
        }
        return 0;
    }

    @Override
    public int hashCode() {
        int code = 0;
        for (int i = 0; i < mUnits.length; i++){
            code ^= (mUnits[i] << ((i%4)*8));
        }
        return code;
    }

    public int hashCodeObj() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof LongBitSet))
            return false;

        final ByteClueBitSet bitSet = (ByteClueBitSet) obj;
        final int len = Math.min(mUnits.length, bitSet.mUnits.length);
        for (int ii = 0; ii < len; ii++) {
            if (mUnits[ii] != bitSet.mUnits[ii])
                return false;
        }
        for (int ii = len; ii < mUnits.length; ii++) {
            if (mUnits[ii] != (byte) 0)
                return false;
        }
        for (int ii = len; ii < bitSet.mUnits.length; ii++) {
            if (bitSet.mUnits[ii] != (byte) 0)
                return false;
        }
        return true;
    }



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        int unitLen = unitLength();
        for (int ii = 0; ii < unitLen; ii++) {
            int len = ii == unitLen - 1 ? 1 + (length() - 1) % BITS_PER_UNIT : BITS_PER_UNIT;
            if (mUnits[ii] != 0) {
                byte bit = 1;
                for (int jj = 0; jj < len; jj++, bit <<= 1) {
                    if ((mUnits[ii] & bit) != 0)
                        sb.append('1');
                    else
                        sb.append('0');
                }
            } else {
                sb.append("0000000000000000000000000000000000000000000000000000000000000000", 0, len);
            }
        }
        sb.append('}');
        return sb.toString();
    }

    private int unitLength() {
        int index = mUnits.length;
        do
            index--;
        while (index >= 0 && mUnits[index] == (byte) 0);
        return index + 1;
    }

}
