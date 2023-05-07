package ddfinder.search;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.helpers.BitSetTranslator;
import ch.javasoft.bitset.search.NTreeSearch;
import ch.javasoft.bitset.search.TranslatingTreeSearch;

import java.util.Collection;
import java.util.List;

/**
 * @author tristonK 2023/4/21
 */
public class TranslatingTreeSearchWithAC extends TranslatingTreeSearch {
    private NTreeSearch search;
    private BitSetTranslator translator;
    private Collection<IBitSet> bitsetListTransformed;

    /**
     * bitsetList's element : one column's all predicates -> one bitset
     *
     * @param priorities
     * @param bitsetList
     */
    public TranslatingTreeSearchWithAC(int[] priorities, List<IBitSet> bitsetList) {
        super(priorities, bitsetList);
        search = super.getSearch();
        translator = super.getTranslator();
        bitsetListTransformed = super.getBitsetListTransformed();
    }

    @Override
    public void handleInvalid(IBitSet invalidDCU) {
        IBitSet invalidDC = translator.bitsetTransform(invalidDCU);
        Collection<IBitSet> remove = search.getAndRemoveGeneralizations(invalidDC);
        for (IBitSet removed : remove) {
            for (IBitSet bitset : bitsetListTransformed) {
                IBitSet temp = removed.clone();
                temp.and(bitset);
                if (temp.isEmpty()) {
                    IBitSet valid = bitset.clone();
                    valid.andNot(invalidDC);
                    for (int i = valid.nextSetBit(0); i >= 0; i = valid.nextSetBit(i + 1)) {
                        IBitSet add = removed.clone();
                        add.set(i);
                        if (!search.containsSubset(add)) {
                            search.add(add);
                        }
                    }
                }
            }
        }
    }
}
