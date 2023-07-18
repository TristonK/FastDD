package bruteforce;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author tristonK 2023/7/9
 */
class TranslateRFDTest {

    @Test
    void translate() {
        String[] args = new String[]{
                "dataset/rfd/iris_map.txt",
                "dataset/rfd/output_false_4_iris.txt"
        };
        TranslateRFD.translate(args);
        String ddFile = "dataset/rfd/iris_dd.txt";
        System.out.println("Equal Test Result: " + TranslateRFD.matchDD(TranslateRFD.FullRFD, readDDFromFile(ddFile)));
    }
    private static String[] readDDFromFile(String filename) {
        List<String> ret = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                ret.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret.toArray(new String[0]);
    }
}