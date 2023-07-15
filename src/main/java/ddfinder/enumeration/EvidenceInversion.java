package ddfinder.enumeration;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.LongBitSet;
import ch.javasoft.bitset.search.TranslatingTreeSearch;
import ch.javasoft.bitset.search.TreeSearch;
import ddfinder.search.TranslatingMinimizeTree;

import java.util.*;

/**
 * @author tristonK 2023/2/27
 */
public class EvidenceInversion {

    List<IBitSet> colPredicatesBitSet;
    List<LongBitSet> evidenceBitSets;
    TranslatingTreeSearch posCover;

    int predicateSetSize;
    public EvidenceInversion(Integer mustTruePredicate, List<BitSet> preidcatesGroupsBiteset, Set<Integer> notMatchPredicates, List<LongBitSet> evidenceBitSets, int predicatesSize){
        colPredicatesBitSet = new ArrayList<>();
        this.evidenceBitSets = evidenceBitSets;
        this.predicateSetSize = predicatesSize;
        for(int i = 0; i < preidcatesGroupsBiteset.size(); i++){
            if(i == mustTruePredicate){continue;}
            IBitSet preidcateGroup = new LongBitSet(preidcatesGroupsBiteset.get(i));
            notMatchPredicates.forEach(pid->{if(preidcateGroup.get(pid)){preidcateGroup.clear(pid);}});
            colPredicatesBitSet.add(preidcateGroup);
        }
    }

    public Set<IBitSet> getCovers(){
        //TODO: 修改排序，此处的排序只会影响
        posCover = new TranslatingTreeSearch(countPredsInEvidenceSet(), colPredicatesBitSet);
        List<IBitSet> sortedNegCover = new ArrayList<>();
        for (LongBitSet bitset : evidenceBitSets) {
            sortedNegCover.add(bitset.clone());
        }

        // for evidence minimize
        sortedNegCover = minimizeEvidenceSet(sortedNegCover);

        posCover.add(new LongBitSet());
        /*for(IBitSet bs: colPredicatesBitSet){
            for(int i = bs.nextSetBit(0); i >= 0;i = bs.nextSetBit(i+1)){
                LongBitSet pred = new LongBitSet();
                pred.set(i);
                posCover.add(pred);
            }
        }*/

        Collections.sort(sortedNegCover, posCover.getComparator());

        for (int i = 0; i < sortedNegCover.size(); ++i) {
            posCover.handleInvalid(sortedNegCover.get(i));
        }

        HashSet<IBitSet> result = new HashSet<>();
        posCover.forEach(result::add);

        return result;
    }

    private List<IBitSet> minimizeEvidenceSet(final List<IBitSet> sortedNegCover) {

        Collections.sort(sortedNegCover, new Comparator<IBitSet>() {
            @Override
            public int compare(IBitSet o1, IBitSet o2) {
                int erg = Integer.compare(o2.cardinality(), o1.cardinality());
                return erg != 0 ? erg : o2.compareTo(o1);
            }
        });

        TreeSearch neg = new TreeSearch();
        sortedNegCover.stream().forEach(invalid -> addInvalidToNeg(neg, invalid));

        final ArrayList<IBitSet> list = new ArrayList<>();
        neg.forEach(list::add);
        return list;
    }

    private void addInvalidToNeg(TreeSearch neg, IBitSet invalid) {
        if (neg.findSuperSet(invalid) != null)
            return;

        neg.getAndRemoveGeneralizations(invalid);
        neg.add(invalid);
    }

    private int[] countPredsInEvidenceSet(){
        int[] counts = new int[predicateSetSize];
        for(LongBitSet evidence: evidenceBitSets){
            for(int i = evidence.nextSetBit(0); i>=0 ; i = evidence.nextSetBit(i+1)){
                counts[i]++;
            }
        }
        return counts;
    }

}
