package fastdd.utils;

import fastdd.differentialdependency.DifferentialDependency;
import fastdd.differentialdependency.DifferentialDependencySet;

import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;

/**
 * @author tristonK 2023/7/23
 */
public class PrintResult {
    public static void PrintDD(DifferentialDependencySet dds){
        String filePath = "dataset/rfd/dds.txt";

        try {
            FileWriter fileWriter = new FileWriter(filePath);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            for(DifferentialDependency dd: dds){
                bufferedWriter.write(dd.toString());
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
            fileWriter.close();
            System.out.println("Content has been written to the file successfully.");
        } catch (IOException e) {
            System.err.println("An error occurred while writing to the file: " + e.getMessage());
        }
    }
}
