package ddfinder.utils;

import ddfinder.differentialdependency.DifferentialDependency;
import ddfinder.differentialdependency.DifferentialDependencySet;

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
            // Create a FileWriter object with the file path
            FileWriter fileWriter = new FileWriter(filePath);

            // Wrap the FileWriter with a BufferedWriter for better performance
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            // Write the content to the file
            for(DifferentialDependency dd: dds){
                bufferedWriter.write(dd.toString());
                bufferedWriter.newLine();
            }


            // Don't forget to close the writers to flush and release resources
            bufferedWriter.close();
            fileWriter.close();

            System.out.println("Content has been written to the file successfully.");
        } catch (IOException e) {
            System.err.println("An error occurred while writing to the file: " + e.getMessage());
        }
    }
}
