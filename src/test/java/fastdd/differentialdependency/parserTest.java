package fastdd.differentialdependency;

import org.junit.jupiter.api.Test;

/**
 * @author tristonK 2023/12/1
 */
class parserTest {

    @Test
    void parse() {
        String testString = "{ [name(<=10.0)] ∧ [addr(>19.0)] ∧ [city(<=0.0)] ∧ [type(<=1.0)] -> [phone(<=8.0)]}";
        new Parser(testString).parse();
    }
}