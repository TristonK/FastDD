package bruteforce;

import de.metanome.algorithms.dcfinder.input.Input;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * @author tristonK 2023/7/9
 */
public class TranslateRFD {
    public static String[] FullRFD;
    public static void translate(String []args){

        Map<String, String> attributeMappings = readAttributeMappings(args[0]);


        String text = readTextFromFile(args[1]);


        var constraintValues = replaceAttributeNames(text, attributeMappings);


        System.out.println("**********[Thresholds]**********");
        for(String key: constraintValues.keySet()){
            System.out.println(key + " " + constraintValues.get(key).toString());
        }
        System.out.println("**********[RFD]**********");
        for(String line: FullRFD){
            System.out.println(line);
        }
    }

    private static Map<String, String> readAttributeMappings(String filename) {
        Map<String, String> attributeMappings = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    attributeMappings.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return attributeMappings;
    }
    private static String readTextFromFile(String filename) {
        StringBuilder text = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.split("cc")[0];
                text.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text.toString();
    }

    private static HashMap<String, Set<Double>> replaceAttributeNames(String text, Map<String, String> attributeMappings) {
        HashMap<String, Set<Double>> thresholds = new HashMap<>();
        String[] lines = text.split("\n");
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            String[] parts = line.split("->");
            String[] attributes = parts[0].split(", ");
            for (int j = 0; j < attributes.length; j++) {
                String[] attributeParts = attributes[j].split("@");
                String attributeName = attributeParts[0];
                String attributeConstraint = attributeParts[1];
                double threshold = Double.parseDouble(attributeConstraint);
                String mappedAttributeName = attributeMappings.getOrDefault(attributeName, attributeName);
                attributes[j] = "[" + mappedAttributeName + "(<=" + attributeConstraint + ")]";
                if(!thresholds.containsKey(mappedAttributeName)){thresholds.put(mappedAttributeName, new HashSet<Double>());}
                thresholds.get(mappedAttributeName).add(threshold);
            }
            {
                String[] attributeParts = parts[1].split("@");
                String attributeName = attributeParts[0];
                String attributeConstraint = attributeParts[1].trim();
                double threshold = Double.parseDouble(attributeConstraint);
                String mappedAttributeName = attributeMappings.getOrDefault(attributeName, attributeName);
                parts[1] = "[" + mappedAttributeName + "(<=" + attributeConstraint + ")]";
                if(!thresholds.containsKey(mappedAttributeName)){thresholds.put(mappedAttributeName, new HashSet<Double>());}
                thresholds.get(mappedAttributeName).add(threshold);
            }
            lines[i] = String.join(", ", attributes) + " -> " + parts[1];
            lines[i] = lines[i].trim();
        }
        FullRFD = Arrays.copyOfRange(lines, 1, lines.length);
        return thresholds;
    }

    public void TranslateREADME(){
        System.out.print(
                "RFDFilesSample:\n" +
                "****** DISCOVERED RFDs *******\n" +
                "COL1@0.1, COL2@0.4, COL3@0.0->COL4@0.0     cc:0.5564102564102564\n" +
                "COL1@0.0, COL2@0.0, COL3@0.0->COL0@0.3     cc:0.5532808398950131\n" +
                "COL0@0.0, COL1@0.0, COL3@0.0->COL2@0.4     cc:0.551847365233192\n" +
                "COL0@0.0, COL1@0.1, COL3@0.2->COL4@0.0     cc:0.5495659196446598\n" +
                "COL0@0.1, COL1@0.1, COL3@0.1->COL4@0.0     cc:0.5478699777912376\n" +
                "Mapping FileSample:\n" + "COL1:col1\n"
        );
    }

    public static boolean matchDD(String[] rfds, String[] dds){
       /* if(rfds.length != dds.length) {
            System.out.println("Size diff: #rfd: " + rfds.length + "; #dd: " + dds.length);
            return false;
        }*/
        HashSet<String> rfdSet = new HashSet<>();
        for(String rfd: rfds){
            String parsedRFD = parseAndConvert(rfd);
            rfdSet.add(parsedRFD);
        }
        boolean flag = true;
        for(String dd: dds){
            String parsedDD = parseAndConvert(dd);
            if(!rfdSet.contains(parsedDD)){
                System.out.println("DD not Exist in RFDs " + parsedDD);
                flag = false;
            }else{
                rfdSet.remove(parsedDD);
            }
        }
        if (!flag){
            System.out.println("RFD left: #" + rfdSet.size() + " " + rfdSet.toString());
        }
        return flag;
    }

    public static String parseAndConvert(String expression) {
        String parsedExpression = expression.replaceAll("[{}\\[\\]]", "");
        parsedExpression = parsedExpression.replaceAll("->", ",");
        parsedExpression = parsedExpression.replaceAll("\\),", "\\), ");
        parsedExpression = parsedExpression.replaceAll(" ∧ ", " , ");
        parsedExpression = parsedExpression.replaceAll("\\s*,\\s*", ",");
        parsedExpression = parsedExpression.trim();
        return parsedExpression;
    }

    public void validatByInput(Input input){
        double[] col2Input = input.getDoubleInput()[0];
        double[] col5Input = input.getDoubleInput()[3];
        int rows = col2Input.length;
        for (int i = 0; i < rows - 1; i++) {
            for (int j = i + 1; j < rows; j++) {
                if (Math.abs(col2Input[i] - col2Input[j]) <= 0.4){
                    if (Math.abs(col5Input[i]-col5Input[j])!=0){
                        System.out.println(i+" "+j+": "+col5Input[i]+" "+col5Input[j]);
                    }
                }
            }
        }
    }

}
