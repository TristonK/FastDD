package fastdd.dfset.cluebitset;

import java.util.BitSet;

/**
 * @author tristonK 2023/3/6
 */
public interface IClueBitSet {
    /**
     * Set the specified {@code bit} to true
     *
     * @param bit
     *            The index of the bit to set
     * @throws IndexOutOfBoundsException
     *             if the specified index is negative.
     */
    void set(int bit);

    /**
     * Set the specified {@code bit} to the given {@code value}
     *
     * @param bit
     *            the index of the bit to set
     * @param value
     *            the value to be set
     * @throws IndexOutOfBoundsException
     *             if the specified index is negative.
     */
    void set(int bit, boolean value);

    /**
     * Set the specified bit to false
     *
     * @param bit
     *            The index of the bit to clear
     * @throws IndexOutOfBoundsException
     *             if the specified index is negative.
     */
    void clear(int bit);

    /**
     * Set all bits to false
     */
    void clear();

    /**
     * Set the specified bit to the opposite of the current value (new=not old)
     *
     * @param bit
     *            The index of the bit to flip
     * @throws IndexOutOfBoundsException
     *             if the specified index is negative.
     */
    void flip(int bit);

    /**
     * Returns the specified
     *
     * @param bit
     *            The index of the asked bit
     * @return true if the bit is set, false otherwise
     * @throws IndexOutOfBoundsException
     *             if the specified index is negative.
     */
    boolean get(int bit);

    /**
     * Returns true if this bit set is a subset of the given bit set. A bit set
     * is a subset of another bit set if the all bits which are true in this bit
     * set are also true in the other bit set. If two bit sets are equal, each
     * is a subset of the other.
     *
     * @param of
     *            The bit set, which is a superset of this bit set if the latter
     *            is a subset of it
     * @return true if this bit set is a subset of <code>of</code>
     */
    boolean isSubSetOf(IClueBitSet of);

    /**
     * Returns true if this bit set is a superset of the intersection of the
     * given bit sets. This method returns true if for every bit {@code i}, the
     * following condition holds:
     * {@code this.bit[i] >= (interA.bit[i] AND with.bit[i])}. The condition is
     * equivalent to {@code this.bit[i] OR NOT interA.bit[i] OR NOT with.bit[i]}
     * .
     *
     * @param interA
     *            The first part of the intersection set
     * @param interB
     *            The second part of the intersection set
     * @return true if this bit set is a superset of {@code (interA AND interB)}
     */
    boolean isSuperSetOfIntersection(IClueBitSet interA, IClueBitSet interB);

    /**
     * The current bit set is logically and-ed with the given bit set:<br>
     * {@code this.bit[i] = this.bit[i] AND with.bit[i]}
     *
     * @param with
     *            The bit set with which this bit set is logically and-ed
     */
    void and(IClueBitSet with);

    /**
     * The current bit set is logically and-ed with the given bit set, and the
     * cardinality of the result is returned:<br>
     * {@code result = |this.bit[i] AND with.bit[i]|}
     *
     * @param with
     *            The bit set with which this bit set is logically and-ed
     * @return the cardinality of the result
     */
    int getAndCardinality(IClueBitSet with);

    /**
     * The current bit set is logically and-ed with the given bit set, and the
     * result is returned as a new bit set instance:<br>
     * {@code result.bit[i] = this.bit[i] AND with.bit[i]}
     *
     * @param with
     *            The bit set with which this bit set is logically and-ed
     * @return the resulting bit set
     */
    IClueBitSet getAnd(IClueBitSet with);

    /**
     * The current bit set is logically and-ed with the complement of the given
     * bit set:<br>
     * {@code this.bit[i] = this.bit[i] AND (NOT with.bit[i])}
     *
     * @param with
     *            The bit set with which's complement this bit set is logically
     *            and-ed
     */
    void andNot(IClueBitSet with);

    /**
     * The current bit set is logically and-ed with the complement of the given
     * bit set, and the result is returned as a new bit set instance:<br>
     * {@code result.bit[i] = this.bit[i] AND (NOT with.bit[i])}
     *
     * @param with
     *            The bit set with which's complement this bit set is logically
     *            and-ed
     * @return the resulting bit set
     */
    IClueBitSet getAndNot(IClueBitSet with);

    /**
     * The current bit set is logically or-ed with the given bit set:<br>
     * {@code this.bit[i] = this.bit[i] OR with.bit[i]}
     *
     * @param with
     *            The bit set with which this bit set is logically or-ed
     */
    void or(IClueBitSet with);

    /**
     * The current bit set is logically or-ed with the given bit set, and the
     * result is returned as a new bit set instance:<br>
     * {@code result.bit[i] = this.bit[i] OR with.bit[i]}
     *
     * @param with
     *            The bit set with which this bit set is logically or-ed
     * @return the resulting bit set
     */
    IClueBitSet getOr(IClueBitSet with);

    /**
     * The current bit set is logically xor-ed with the given bit set:<br>
     * {@code this.bit[i] = this.bit[i] XOR with.bit[i]}
     *
     * @param with
     *            The bit set with which this bit set is logically xor-ed
     */
    void xor(IClueBitSet with);

    /**
     * The current bit set is logically xor-ed with the given bit set, and the
     * result is returned as a new bit set instance:<br>
     * {@code result.bit[i] = this.bit[i] XOR with.bit[i]}
     *
     * @param with
     *            The bit set with which this bit set is logically xor-ed
     * @return the resulting bit set
     */
    IClueBitSet getXor(IClueBitSet with);

    /**
     * The current bit set is logically xor-ed with the given bit set, and the
     * cardinality of the result is returned:<br>
     * {@code result = |this.bit[i] XOR with.bit[i]|}
     *
     * @param with
     *            The bit set with which this bit set is logically xor-ed
     * @return the cardinality of the result
     */
    int getXorCardinality(IClueBitSet with);

    /**
     * Returns <code>index + 1</code>, with the index of the highest bit set to
     * true.
     *
     * @return <code>index + 1</code> of the highest true bit
     */
    int length();

    /**
     * Returns the number of true bits in this bit set
     *
     * @return the number of set bits
     */
    int cardinality();

    /**
     * Returns the number of true bits in this bit set, starting from
     * {@code fromBit} (inclusive), ending at {@code toBit} (exclusive).
     *
     * @param fromBit
     *            the start bit, inclusive
     * @param toBit
     *            the end bit, exclusive
     *
     * @return the number of set bits
     */
    int cardinality(int fromBit, int toBit);

    /**
     * Returns the index of the first bit that is set to <code>true</code> that
     * occurs on or after the specified starting index. If no such bit exists
     * then -1 is returned.
     *
     * To iterate over the <code>true</code> bits in a <code>BitSet</code>, use
     * the following loop:
     *
     * <pre>
     * for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
     * 	// operate on index i here
     * }
     * </pre>
     *
     * @param from
     *            the index to start checking from (inclusive).
     * @return the index of the next set bit.
     * @throws IndexOutOfBoundsException
     *             if the specified index is negative.
     */
    int nextSetBit(int from);

    /**
     * Returns the index of the first bit that is set to <code>false</code> that
     * occurs on or after the specified starting index.
     *
     * @param from
     *            the index to start checking from (inclusive).
     * @return the index of the next clear bit.
     * @throws IndexOutOfBoundsException
     *             if the specified index is negative.
     */
    int nextClearBit(int from);

    /**
     * Returns a clone of this bit set
     *
     * @return A cloned (new) instance of this bit set
     */
    IClueBitSet clone();

    /**
     * Converts this bit set to a Java bit set and returns it
     *
     * @return a java bit set representing the same bits as this bit set
     */
    BitSet toBitSet();

    boolean isEmpty();

    int getUnitLength();

}
