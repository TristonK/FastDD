package bruteforce;

import org.junit.jupiter.api.Test;

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
    }
}