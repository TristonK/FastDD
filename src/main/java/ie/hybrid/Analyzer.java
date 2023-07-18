package ie.hybrid;

import ch.javasoft.bitset.LongBitSet;
import ddfinder.differentialdependency.DifferentialDependency;
import ddfinder.differentialdependency.DifferentialDependencySet;
import ddfinder.evidence.Evidence;
import ddfinder.evidence.EvidenceSet;
import ddfinder.predicate.DifferentialFunctionBuilder;
import ddfinder.predicate.PredicateSet;
import de.metanome.algorithms.dcfinder.predicates.Predicate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tristonK 2023/7/15
 */
public class Analyzer {
    private final List<LongBitSet> fullEvidenceSet;

    public Analyzer(EvidenceSet evidenceSet, DifferentialFunctionBuilder dfBuilder){
        List<LongBitSet> evis = new ArrayList<>();
        for(Evidence evi : evidenceSet){
            evis.add(evi.getBitset().clone());
        }
        fullEvidenceSet = evis;
        SearchSpace.configure(dfBuilder);
    }

    public void run(PredicateSet predicateSet){
        LongBitSet pSpace = predicateSet.getLongBitSet();
        DifferentialDependencySet dds = new DifferentialDependencySet();
        for(int i = pSpace.nextSetBit(0); i >= 0; i = pSpace.nextSetBit(i + 1)){
            LongBitSet dfSpace = pSpace.clone();
            dfSpace.clear(i);
            LongBitSet right = new LongBitSet();
            right.set(i);
            dds.addAll(reduce(fullEvidenceSet, right, new SearchSpace(i)));
        }
        dds = new Minimal().minimize(dds);
        for(DifferentialDependency dd:dds){
            System.out.println(dd);
        }
    }

    public DifferentialDependencySet reduce(List<LongBitSet> D, LongBitSet right, SearchSpace dfSpace){
        DifferentialDependencySet ret = new DifferentialDependencySet();
        // the first element removed from Phi(X)
        if(dfSpace.phis.size() == 0){
            return ret;
        }
        LongBitSet W = dfSpace.phis.get(0);
        List<SearchSpace> splitSpace = dfSpace.extractPositive(W);
        SearchSpace phi1 = splitSpace.get(0);
        SearchSpace phi2 = splitSpace.get(1);

        List<LongBitSet> D1 = exclude(D, W, right);
        if (D1.size() > 0){
            ret.addAll(reduce(D1, right, phi1));
        }else{
            ret.add(new DifferentialDependency(W, right));
        }
        ret.addAll(reduce(D, right, phi2));
        return null;
    }

    public List<LongBitSet> exclude(List<LongBitSet> evidenceSet, LongBitSet left, LongBitSet right){
        List<LongBitSet> D1 = new ArrayList<>();
        for(LongBitSet bs: evidenceSet){
            if(left.isSubSetOf(bs) && !right.isSubSetOf(bs)){
                D1.add(bs.clone());
            }
        }
        return D1;
    }
}
