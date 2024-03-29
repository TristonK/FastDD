package fastdd.utils;

import org.apache.commons.text.similarity.LevenshteinDistance;

/**
 * @author tristonK 2023/2/26
 */
public class DistanceCalculation {
    public static boolean flag = true;

    public static int StringDistance(String s1, String s2){
        if(flag){
            //return LEVENSHTEIN.apply(s1, s2);
            return getLevenshteinDistance(s1,s2);
        }else{
            return getQGramDistance(s1, s2);
        }
    }
    private static int getLevenshteinDistance(String s1, String s2){
        if (s1 == null) {
            throw new NullPointerException("s1 must not be null");
        }

        if (s2 == null) {
            throw new NullPointerException("s2 must not be null");
        }

        if (s1.equals(s2)) {
            return 0;
        }

        if (s1.length() == 0) {
            return s2.length();
        }

        if (s2.length() == 0) {
            return s1.length();
        }
        // create two work vectors of integer distances
        int[] v0 = new int[s2.length() + 1];
        int[] v1 = new int[s2.length() + 1];
        int[] vtemp;

        // initialize v0 (the previous row of distances)
        // this row is A[0][i]: edit distance for an empty s
        // the distance is just the number of characters to delete from t
        for (int i = 0; i < v0.length; i++) {
            v0[i] = i;
        }

        for (int i = 0; i < s1.length(); i++) {
            // calculate v1 (current row distances) from the previous row v0
            // first element of v1 is A[i+1][0]
            //   edit distance is delete (i+1) chars from s to match empty t
            v1[0] = i + 1;

            int minv1 = v1[0];

            // use formula to fill in the rest of the row
            for (int j = 0; j < s2.length(); j++) {
                int cost = 1;
                if (s1.charAt(i) == s2.charAt(j)) {
                    cost = 0;
                }
                v1[j + 1] = Math.min(
                        v1[j] + 1,              // Cost of insertion
                        Math.min(
                                v0[j + 1] + 1,  // Cost of remove
                                v0[j] + cost)); // Cost of substitution

                minv1 = Math.min(minv1, v1[j + 1]);
            }

            if (minv1 >= Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }

            // copy v1 (current row) to v0 (previous row) for next iteration
            //System.arraycopy(v1, 0, v0, 0, v0.length);

            // Flip references to current and previous row
            vtemp = v0;
            v0 = v1;
            v1 = vtemp;

        }

        return v0[s2.length()];
    }

    private static int getQGramDistance(String a, String b){
        return 0;
    }

    private static double MinMDDiff = 0.0;
    private static double MaxMDDiff = 1.0;

    public static double MDLongDistance(long x, long y){
        if (Math.max(x,y) == 0){return MinMDDiff;}
        return Math.abs(x-y)*1.0 / Math.max(x,y);
    }
    //private static final LevenshteinDistance LEVENSHTEIN = LevenshteinDistance.getDefaultInstance();
    public static double MDDoubleDistance(double x, double y){
        if (Math.max(x,y) == 0.0){return MinMDDiff;}
        return Math.abs(x - y) / Math.max(x,y);
    }

    public static double MDLevenstheinDistance(String x, String y){
        if(x.length()== 0 && y.length() == 0){return MinMDDiff;}

        return Math.max(0.0, (double) getLevenshteinDistance(x, y) /Math.max(x.length(), y.length()));
    }

    public static long MDDiffLong(long x, double diff, boolean XIsBigger){return 0;}

    public static long MDDiffDouble(double x, double y, boolean XISBigger){return 0;}
}
