package bruteforce;

import ddfinder.differentialdependency.DifferentialDependency;
import ddfinder.differentialdependency.DifferentialDependencySet;
import ddfinder.evidence.Evidence;
import ddfinder.evidence.EvidenceSet;

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
                        System.out.printf("Bad DD: %s Evidence is %s", dd.toString(), evi.toDFString());
                    }
                }
            }
            if (flag)  {System.out.println("dd SUPPORT = "+ left +" " + dd.toString());count++;}
        }
        System.out.println("count :: " + count);
    }
}
