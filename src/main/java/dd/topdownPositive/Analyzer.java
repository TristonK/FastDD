package dd.topdownPositive;

import ch.javasoft.bitset.LongBitSet;
import de.metanome.algorithms.dcfinder.helpers.IndexProvider;
import fastdd.Config;
import fastdd.differentialdependency.DifferentialDependency;
import fastdd.differentialdependency.DifferentialDependencySet;
import fastdd.differentialfunction.DifferentialFunction;
import fastdd.differentialfunction.DifferentialFunctionBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author tristonK 2023/7/15
 */
public class Analyzer {
    private final DifferentialFunctionBuilder differentialFunctionBuilder;

    private final Validator validator;

    public Analyzer(List<LongBitSet> differentialSet, DifferentialFunctionBuilder dfBuilder) {
        List<LongBitSet> evis = new ArrayList<>(differentialSet);
        this.differentialFunctionBuilder = dfBuilder;
        SearchSpace.configure(dfBuilder);
        validator = new Validator(evis);
    }

    public DifferentialDependencySet run(LongBitSet pSpace) {
        DifferentialDependencySet dds = new DifferentialDependencySet();
        //System.out.println("pspacesize = " + pSpace.cardinality());
        long t1 = System.currentTimeMillis();
        for (int i = pSpace.nextSetBit(0); i >= 0; i = pSpace.nextSetBit(i + 1)) {
            LongBitSet right = new LongBitSet();
            right.set(i);
            if(Objects.equals(Config.method, "TD-Po")){
                dds.addAll(reduce(right, new SearchSpace(i)));
            } else{
                dds.addAll(reduceBF(right, new SearchSpace(i)));
            }
        }
        System.out.println(Config.method + " reduce time: " + (System.currentTimeMillis() - t1));
        t1 = System.currentTimeMillis();
        dds = new Minimal().minimize(dds);
        System.out.println(Config.method + " minimize Time: " + (System.currentTimeMillis() - t1));
        return dds;
    }

    public DifferentialDependencySet reduce(LongBitSet right, SearchSpace dfSpace) {
        DifferentialDependencySet ret = new DifferentialDependencySet();
        // the first element removed from Phi(X)
        if (dfSpace.phis.size() == 0) {
            return ret;
        }
        LongBitSet W = dfSpace.phis.get(0);
        List<SearchSpace> splitSpace = dfSpace.extractPositive(W);
        SearchSpace phi1 = splitSpace.get(0);
        SearchSpace phi2 = splitSpace.get(1);
        if(validator.satisfy(W, right)){
            addDDToRet(right, ret, W);
            ret.addAll(reduce(right, phi2));
        } else{
            ret.addAll(reduce(right, phi1));
            ret.addAll(reduce(right, phi2));
        }
        return ret;
    }

    private void addDDToRet(LongBitSet right, DifferentialDependencySet ret, LongBitSet w) {
        IndexProvider<DifferentialFunction> p = differentialFunctionBuilder.getPredicateIdProvider();
        List<DifferentialFunction> leftDf = new ArrayList<>();
        DifferentialFunction rightDf = p.getObject(right.nextSetBit(0));
        for (int i = w.nextSetBit(0); i >= 0; i = w.nextSetBit(i + 1)) {
            leftDf.add(p.getObject(i));
        }
        ret.add(new DifferentialDependency(leftDf, rightDf, w.clone().getOr(right), w.clone()));
    }

    public DifferentialDependencySet reduceBF(LongBitSet right, SearchSpace dfSpace){
        DifferentialDependencySet ret = new DifferentialDependencySet();
        // the first element removed from Phi(X)
        if (dfSpace.phis.size() == 0) {
            return ret;
        }
        for(LongBitSet W : dfSpace.phis){
            if(validator.satisfy(W, right)){
                addDDToRet(right, ret, W);
            }
        }
        return ret;
    }
}
