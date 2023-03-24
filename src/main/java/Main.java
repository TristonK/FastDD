import ddfinder.DDFinder;
import FastADC.FastADC;
import ddfinder.differentialdependency.DifferentialDependency;
import ddfinder.differentialdependency.DifferentialDependencySet;
import de.metanome.algorithms.dcfinder.denialconstraints.DenialConstraintSet;
import de.metanome.algorithms.dcfinder.input.Input;
import de.metanome.algorithms.dcfinder.input.RelationalInput;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main {

    /**
     * input: filePath
     * input: filePath size
     * input: filePath size dfFlie
     */
    public static void main(String[] args) throws IOException {
        String fp = args[0];

        // limit the number of tuples in dataset, -1 means no limit
        int rowLimit = -1;
        if(args.length >= 2){
            rowLimit = Integer.parseInt(args[1]);
        }

        String dfp = "";
        if(args.length > 2){
            dfp = args[2];
        }

        DDFinder dDFinder = new DDFinder(rowLimit, new Input(new RelationalInput(fp), rowLimit), dfp);
        DifferentialDependencySet dds = dDFinder.buildDDs();
        /*try {
            String[] fileName = fp.split("/");
            File file = new File("ddsOut/"+ fileName[fileName.length -1].split("\\.")[0] + ".txt");
            if (!file .getParentFile().exists()) {
                file .getParentFile().mkdirs();
            }
            if(!file .exists()) {
                file .createNewFile();
            }
            System.out.println("[DD Output] Save dds to" + file.name)
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            for(DifferentialDependency dd: dds){
                out.write(dd.toString() + "\n");
            }
            out.close();
            System.out.println("文件创建成功！");
        } catch (IOException e) {
            System.out.println(e.toString());
        }*/
        //System.out.println("get dds size: " + dds.size());
    }

}
