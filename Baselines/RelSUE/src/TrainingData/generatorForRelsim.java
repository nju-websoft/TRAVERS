package TrainingData;

import Path.MetaPath;
import PathBasedSimilarity.PCRW;
import PathBasedSimilarity.PathCount;
import PathBasedSimilarity.SimilarityMeasurements;

import java.util.*;
import java.io.*;

public class generatorForRelsim {
    //static SimilarityMeasurements pc = new PathCount();
    static SimilarityMeasurements pc = new PCRW();
    public static void outputData(int query, List<Integer> examples, List<Integer> candidates, List<MetaPath> paths, String fileName){
        List<Integer> nexamples = new ArrayList<>();
        for(int i = 0; i < examples.size(); i ++){
            nexamples.add(candidates.get((int)Math.random()*candidates.size()%candidates.size()));
        }

        File file = new File(fileName);
        FileWriter fw = null;
        BufferedWriter writer = null;

        try {
            fw = new FileWriter(file);
            writer = new BufferedWriter(fw);
            for(int i = 0; i < examples.size(); i ++){
                String str = "";
                int count = 0;
                for(MetaPath path : paths){
                    count ++;
                    str += pc.getSim(query, examples.get(i), path);
                    if(count != paths.size())
                        str += " ";
                }
                str += ":";
                count = 0;
                for(MetaPath path : paths){
                    count ++;
                    str += pc.getSim(query, nexamples.get(i), path);
                    if(count != paths.size())
                        str += " ";
                }
                writer.write(str);
                writer.newLine();

            }

            writer.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
