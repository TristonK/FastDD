package ie.hybrid;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author tristonK 2023/7/20
 */
class MinimalTest {
    public static Set<String> readFileToSet(String filePath) throws IOException {
        Set<String> linesSet = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                linesSet.add(line.trim());
            }
        }
        return linesSet;
    }

    public static boolean compareFiles(String aFilePath, String bFilePath) throws IOException {
        Set<String> ies = readFileToSet(aFilePath);
        Set<String> ddfinders = readFileToSet(bFilePath);
        boolean flag = true;
        int cnt = 0;
        for(String str: ddfinders){
            if(!ies.contains(str)){
                flag = false;
                cnt ++;
                System.out.println(str);
            }
        }
        System.out.println(cnt);
        return flag;
    }

    @Test
    public void compareOutput() throws IOException {
        String a = "dataset/rfd/ie_output.txt";
        String b = "dataset/rfd/iris_dd.txt";
        compareFiles(a, b);
    }
}