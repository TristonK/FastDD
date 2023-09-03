package dup_detect;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author tristonK 2023/8/27
 */
public class ReadCorrectInput {

    public boolean[][] isSame = new boolean[DupConfig.tupleSize][DupConfig.tupleSize];
    public long sameTupleSize = 0;
    public void readFile(){
        try (BufferedReader reader = new BufferedReader(new FileReader(DupConfig.CorrectFileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                String[] nums = line.split(" ");
                int num1 = Integer.parseInt(nums[0].trim());
                int num2 = Integer.parseInt(nums[1].trim());
                if (!isSame[num1][num2]){
                    sameTupleSize++;
                    isSame[num1][num2] = isSame[num2][num1] = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
