package dd.topdownPositive;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.LongBitSet;
import fastdd.differentialdependency.DifferentialDependency;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tristonK 2023/11/24
 */
public class Validator {
    List<LongBitSet> differentialSet;

    public Validator(List<LongBitSet> dfset){
        differentialSet = dfset;
    }

    public boolean satisfy(LongBitSet left, LongBitSet right){
        List<LongBitSet> D1 = new ArrayList<>();
        for (LongBitSet bs : differentialSet) {
            if (left.isSubSetOf(bs) && !right.isSubSetOf(bs)) {
                return false;
            }
        }
        return true;
    }
}
