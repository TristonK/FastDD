package bruteforce;

import ch.javasoft.bitset.LongBitSet;
import ddfinder.differentialdependency.DifferentialDependency;
import ddfinder.differentialdependency.DifferentialDependencySet;
import ddfinder.predicate.DifferentialFunction;
import ddfinder.evidence.Evidence;
import ddfinder.evidence.EvidenceSet;
import ddfinder.predicate.DifferentialFunctionBuilder;
import de.metanome.algorithms.dcfinder.helpers.IndexProvider;
import de.metanome.algorithms.dcfinder.predicates.Operator;
import de.metanome.algorithms.dcfinder.predicates.Predicate;

import java.util.BitSet;

/**
 * @author tristonK 2023/6/30
 */
public class ValidateDD {
    public void validate(EvidenceSet evidenceSet, DifferentialDependencySet dds){
        int count = 0;
        for(DifferentialDependency dd: dds){
            boolean flag = false;
            int left = 0;
            for(Evidence evi: evidenceSet){
                if(dd.getLeftPredicateSet().isSubSetOf(evi.getBitset())){
                   flag = true;left++;
                    if(!dd.getPredicateSet().isSubSetOf(evi.getBitset())){
                        System.out.printf("Bad DD: %s DFSet is %s", dd.toString(), evi.toDFString());
                    }
                }
            }
           if (flag)  {System.out.println("dd SUPPORT = "+ left +" " + dd.toString());count++;}
        }
        System.out.println("count :: " + count);
    }

    // 最后一个string为right,string格式，
    public static void translateRFDToDD(DifferentialFunctionBuilder builder, EvidenceSet evidenceSet){
        LongBitSet leftBs = new LongBitSet();
        LongBitSet allBs = new LongBitSet();
        {
            leftBs.set(4);
            allBs.set(4);
        }
        {
            allBs.set(13);
        }
        // validate
        boolean flag = false;
        int left = 0;
        for(Evidence evi: evidenceSet){
            if(leftBs.isSubSetOf(evi.getBitset())){
                flag = true;left++;
                if(!allBs.isSubSetOf(evi.getBitset())){
                    System.out.printf("Bad RFD as %s", evi.toDFString());
                }
            }
        }
        if (flag)  {System.out.println("rfd is true and SUPPORT = "+ left);}
    }

    public static void printAllDF(DifferentialFunctionBuilder builder){
        IndexProvider<DifferentialFunction> indexProvider = builder.getPredicateIdProvider();
        for (DifferentialFunction df: indexProvider.getObjects()){
            System.out.println(df.toString() + ":" + indexProvider.getIndex(df));
        }
    }
}
