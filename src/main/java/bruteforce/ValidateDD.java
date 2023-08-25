package bruteforce;

import ch.javasoft.bitset.LongBitSet;
import fastdd.differentialdependency.DifferentialDependency;
import fastdd.differentialdependency.DifferentialDependencySet;
import fastdd.differentialfunction.DifferentialFunction;
import fastdd.dfset.Evidence;
import fastdd.dfset.DFSet;
import fastdd.differentialfunction.DifferentialFunctionBuilder;
import de.metanome.algorithms.dcfinder.helpers.IndexProvider;

import java.util.*;

/**
 * @author tristonK 2023/6/30
 */
public class ValidateDD {
    public List<Map.Entry<Integer, List<LongBitSet>>> validate(DFSet DFSet, DifferentialDependencySet dds) {
        int count = 0;
        HashMap<Integer, List<String>> hashMap = new HashMap<>();
        HashMap<Integer, List<LongBitSet>> leftPredicateMap = new HashMap<>();
        for (DifferentialDependency dd : dds) {
            boolean flag = false;
            int left = 0;
            for (Evidence evi : DFSet) {
                if (dd.getLeftPredicateSet().isSubSetOf(evi.getBitset())) {
                    flag = true;
                    left++;
                    if (!dd.getPredicateSet().isSubSetOf(evi.getBitset())) {
                        System.out.printf("Bad DD: %s DFSet is %s", dd.toString(), evi.toDFString());
                    }
                }
            }
            if (flag) {
//               System.out.println("dd SUPPORT = "+ left +" " + dd.toString());
                addToHashMap(hashMap, left, dd.toString());
                count += left;
                addToHashMap(leftPredicateMap, left, (LongBitSet) dd.getLeftPredicateSet());
            }
        }
        List<Map.Entry<Integer, List<String>>> list = new ArrayList<>(hashMap.entrySet());
        List<Map.Entry<Integer, List<LongBitSet>>> DDLeft = new ArrayList<>(leftPredicateMap.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<Integer, List<String>>>() {
            public int compare(Map.Entry<Integer, List<String>> o1, Map.Entry<Integer, List<String>> o2) {
                return o2.getKey().compareTo(o1.getKey());
            }
        });

        // 对同一int类型变量对应的List<String>根据∧字符的数量进行升序排序
        for (Map.Entry<Integer, List<String>> entry : list) {
            List<String> values = entry.getValue();
            Collections.sort(values, new Comparator<String>() {
                public int compare(String s1, String s2) {
                    int count1 = countOccurrences(s1, '∧');
                    int count2 = countOccurrences(s2, '∧');
                    return Integer.compare(count1, count2);
                }
            });
        }

        Collections.sort(DDLeft, new Comparator<Map.Entry<Integer, List<LongBitSet>>>() {
            public int compare(Map.Entry<Integer, List<LongBitSet>> o1, Map.Entry<Integer, List<LongBitSet>> o2) {
                return o2.getKey().compareTo(o1.getKey());
            }
        });

        for (Map.Entry<Integer, List<String>> entry : list) {
            int key = entry.getKey();
            List<String> values = entry.getValue();
            for (String value : values) {
//                System.out.println(key + " : " + value);
            }
        }

        System.out.println("count :: " + count);
        return DDLeft;
    }


    // 将数据添加到HashMap中
    private static void addToHashMap(HashMap<Integer, List<String>> hashMap, int key, String value) {
        if (!hashMap.containsKey(key)) {
            hashMap.put(key, new ArrayList<>());
        }
        hashMap.get(key).add(value);
    }

    private static void addToHashMap(HashMap<Integer, List<LongBitSet>> hashMap, int key, LongBitSet value) {
        if (!hashMap.containsKey(key)) {
            hashMap.put(key, new ArrayList<>());
        }
        hashMap.get(key).add(value);
    }


    // 计算字符在字符串中的出现次数
    private static int countOccurrences(String str, char ch) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ch) {
                count++;
            }
        }
        return count;
    }

    // 最后一个string为right,string格式，
    public static void translateRFDToDD(DifferentialFunctionBuilder builder, DFSet DFSet) {
        LongBitSet leftBs = new LongBitSet();
        LongBitSet allBs = new LongBitSet();
        {
            leftBs.set(2);
            allBs.set(2);
            leftBs.set(9);
            allBs.set(9);
            leftBs.set(16);
            allBs.set(16);
        }
        {
            allBs.set(4);
        }
        // validate
        boolean flag = true;
        int left = 0;
        for (Evidence evi : DFSet) {
            if (leftBs.isSubSetOf(evi.getBitset())) {
                left++;
                if (!allBs.isSubSetOf(evi.getBitset())) {
                    flag = false;
                    System.out.printf("Bad RFD as %s", evi.toDFString());
                }
            }
        }
        if (flag) {
            System.out.println("rfd is true and SUPPORT = " + left);
        }
    }

    public static void printAllDF(DifferentialFunctionBuilder builder) {
        IndexProvider<DifferentialFunction> indexProvider = builder.getPredicateIdProvider();
        for (DifferentialFunction df : indexProvider.getObjects()) {
            System.out.println(df.toString() + ":" + indexProvider.getIndex(df));
        }
    }
}
