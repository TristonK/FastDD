package fastdd.utils;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author tristonK 2023/5/10
 */
public class parseRFDThresholds {
    // rfd文件的路径， 输出文件路径
    public static void getThresholds2File(String rfdPath, String ThresholdsPath) {
        Map<String, List<Double>> thresholds = getThresholds(rfdPath);
        //TODO
    }

    //please ensure you have 0
    public static Map<String, List<Double>> getThresholds(String rfdPath) {
        //TODO
        HashMap<String, Set<Double>> rfdThresholds = new HashMap<>();
        String filePath = rfdPath;

        // 获取最后一个反斜杠的位置
        int lastBackslashIndex = filePath.lastIndexOf("\\");

        // 获取最后一个下划线的位置
        int lastUnderscoreIndex = filePath.lastIndexOf("_");

        // 获取子字符串
        String result = filePath.substring(lastUnderscoreIndex + 1, filePath.length() - 4);

        String outputfilename = result + "_5.txt";


        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String lines;
            // 逐行读取文件内容，直到读到最后一行
            while ((lines = br.readLine()) != null) {

                // 在这里处理每一行的内容
                // 去除逗号之后的空格，便于正则匹配
                String line = lines.replaceAll(",\\s+", ",");

                // 匹配列名和值的正则表达式
                Pattern pattern = Pattern.compile("([\\w\\d\\s!@#$%^&*()-]+)@(\\d+\\.\\d+)");
                Matcher matcher = pattern.matcher(line);
                // 依次提取匹配到的列名和值
                while (matcher.find()) {
                    String columnName = matcher.group(1);
                    Double value = Double.parseDouble(matcher.group(2));
                    Set<Double> values = rfdThresholds.getOrDefault(columnName, new HashSet<>());
                    values.add(value);
                    rfdThresholds.put(columnName, values);
                }
//                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


//         输出结果

        HashMap<String, List<Double>> Thresholds = new HashMap<>();
        for(Map.Entry<String,Set<Double>> entry : rfdThresholds.entrySet()){
            String col =entry.getKey();
            List<Double> thresholds = new ArrayList<>(entry.getValue());
            Collections.sort(thresholds);
            Thresholds.put(col,thresholds);
        }

        return Thresholds;
    }
}

