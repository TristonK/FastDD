import ddfinder.DDFinder;
import FastADC.FastADC;
import de.metanome.algorithms.dcfinder.denialconstraints.DenialConstraintSet;

public class Main {

    /**
     * input: filePath
     * input: filePath size
     * input: filePath size dfFlie
     */
    public static void main(String[] args) {
        String fp = args[0];

        // limit the number of tuples in dataset, -1 means no limit
        int rowLimit = -1;
        if(args.length >= 2){
            rowLimit = Integer.parseInt(args[1]);
        }

        String dfp;
        if(args.length > 2){
            dfp = args[2];
        }

        double threshold = 0.01d;
        int shardLength = 350;
        boolean linear = false;         // linear single-thread in EvidenceSetBuilder
        boolean singleColumn = false;   // only single-attribute predicates

        DDFinder dDFinder = new DDFinder(rowLimit);
        FastADC fastADC = new FastADC(singleColumn, threshold, shardLength, linear);
        DenialConstraintSet dcs = fastADC.buildApproxDCs(fp, rowLimit);
        System.out.println();
    }

}
