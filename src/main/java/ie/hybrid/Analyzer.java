package ie.hybrid;

import ch.javasoft.bitset.LongBitSet;
import fastdd.Config;
import fastdd.differentialdependency.DifferentialDependency;
import fastdd.differentialdependency.DifferentialDependencySet;
import fastdd.dfset.Evidence;
import fastdd.dfset.DFSet;
import fastdd.differentialfunction.DifferentialFunction;
import fastdd.differentialfunction.DifferentialFunctionBuilder;
import de.metanome.algorithms.dcfinder.helpers.IndexProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tristonK 2023/7/15
 */
public class Analyzer {
    private final List<LongBitSet> fullEvidenceSet;
    private final DifferentialFunctionBuilder differentialFunctionBuilder;

    public Analyzer(DFSet DFSet, DifferentialFunctionBuilder dfBuilder) {
        List<LongBitSet> evis = new ArrayList<>();
        for (Evidence evi : DFSet) {
            evis.add(evi.getBitset().clone());
        }
        fullEvidenceSet = evis;
        this.differentialFunctionBuilder = dfBuilder;
        SearchSpace.configure(dfBuilder);
    }

    public DifferentialDependencySet run(LongBitSet pSpace) {
        DifferentialDependencySet dds = new DifferentialDependencySet();
        //System.out.println("pspacesize = " + pSpace.cardinality());
        long t1 = System.currentTimeMillis();
        for (int i = pSpace.nextSetBit(0); i >= 0; i = pSpace.nextSetBit(i + 1)) {
            //LongBitSet dfSpace = pSpace.clone();
            //dfSpace.clear(i);
            LongBitSet right = new LongBitSet();
            right.set(i);
            dds.addAll(reduce(fullEvidenceSet, right, new SearchSpace(i)));
        }
        System.out.println("[IE] reduce time: " + (System.currentTimeMillis() - t1));
        t1 = System.currentTimeMillis();
        dds = new Minimal().minimize(dds);
        System.out.println("[IE] minimize Time: " + (System.currentTimeMillis() - t1));
        if(Config.OutputIEFlag){
            System.out.println("==============[IE dds]=======================");
            for (DifferentialDependency dd : dds) {
                System.out.println(dd);
            }
        }
        return dds;
    }

    public DifferentialDependencySet reduce(List<LongBitSet> D, LongBitSet right, SearchSpace dfSpace) {
        DifferentialDependencySet ret = new DifferentialDependencySet();
        // the first element removed from Phi(X)
        if (dfSpace.phis.size() == 0) {
            return ret;
        }
        LongBitSet W = dfSpace.phis.get(0);
        List<LongBitSet> D1 = exclude(D, W, right);
        // transfer to negative pruning
        if(D1.size() == D.size()){
            List<SearchSpace> splitSpace = dfSpace.extractNegative(W);
            ret.addAll(reduce(D1, right, splitSpace.get(1)));
        } else{
            List<SearchSpace> splitSpace = dfSpace.extractPositive(W);
            SearchSpace phi1 = splitSpace.get(0);
            SearchSpace phi2 = splitSpace.get(1);
            if (D1.size() > 0) {
                ret.addAll(reduce(D1, right, phi1));
            } else {
                IndexProvider<DifferentialFunction> p = differentialFunctionBuilder.getPredicateIdProvider();
                List<DifferentialFunction> leftDf = new ArrayList<>();
                DifferentialFunction rightDf = p.getObject(right.nextSetBit(0));
                for (int i = W.nextSetBit(0); i >= 0; i = W.nextSetBit(i + 1)) {
                    leftDf.add(p.getObject(i));
                }
                ret.add(new DifferentialDependency(leftDf, rightDf, W.clone().getOr(right), W.clone()));
            }
            ret.addAll(reduce(D, right, phi2));
        }
        return ret;
    }

    public List<LongBitSet> exclude(List<LongBitSet> evidenceSet, LongBitSet left, LongBitSet right) {
        List<LongBitSet> D1 = new ArrayList<>();
        for (LongBitSet bs : evidenceSet) {
            if (left.isSubSetOf(bs) && !right.isSubSetOf(bs)) {
                D1.add(bs.clone());
            }
        }
        return D1;
    }

    public List<LongBitSet> excludeV2(List<List<Integer>> evidenceSet, LongBitSet left, LongBitSet right){
        List<LongBitSet> D1 = new ArrayList<>();
        /*for (List<Integer> list: evidenceSet) {
            differentialFunctionBuilder.getBitsetIndex2ThresholdsIndex()
            if (left.isSubSetOf(bs) && !right.isSubSetOf(bs)) {
                D1.add(bs.clone());
            }
        }*/
        return D1;
    }

}
