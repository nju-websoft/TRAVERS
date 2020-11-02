package Experiment;

import Factory.ReadInput;
import Framework.FeatureGenerator;
import JDBCUtils.JdbcUtil;
import Structures.GlobalVariances;
import Structures.fileModel;
import Test.GlobalCounter;
import javafx.util.Pair;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ColdStart_Count
{

    public Integer PathLen = 0;
    public Integer topK = 0;
    public Integer Iterations = 0;

    {
        try {
            InputStream in = JdbcUtil.class.getClassLoader()
                    .getResourceAsStream("Interactive.properties");
            Properties properties = new Properties();
            properties.load(in);

            PathLen = Integer.parseInt(properties.getProperty("PathLen"));
            topK = Integer.parseInt(properties.getProperty("TOPK"));
            Iterations = Integer.parseInt(properties.getProperty("Iterations"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Entry(String name)
    {
        String[] suf = {"11b", "21b", "21o", "22b", "rs1b", "rs2b", "rs3b", "rs4b", "rs5b"};
        for(Integer i = 0; i < 9; ++ i)
        {
            if(name.equals("yago") && suf[i].equals("21b")) continue;
            List<Pair<Integer, Map<Integer, Integer>>> rd = ReadInput.ParseData(name + "_" + suf[i], 10);
            //System.out.println(name + "_" + suf[i]);
            for(Integer id = 1; id <= 10; ++ id)
            {
                //System.out.println(name + "_" + suf[i] + "(" + id.toString() + ") : ");
                /*Pair< List<List<Integer>>, Map<Integer, Map<Integer, Integer>> > features =
                        FeatureGenerator.getFeatures(rd.get(id - 1).getKey(), PathLen);
                System.out.println(
                        rd.get(id - 1).getKey().toString()
                                + " -- Entities : " + ((Integer)features.getValue().size()).toString()
                                + " -- RPs : " + ((Integer)features.getKey().size()).toString()
                );*/
                //if(true) continue;
                Integer[] cnt = new Integer[10];
                for(Integer k = 0; k < 10; ++ k) cnt[k] = 0;
                for(Integer rpt = 0; rpt < GlobalVariances.repeat_times; ++ rpt)
                {
                    GlobalCounter GC = new GlobalCounter();
                    GC.ADD("SC[one]", (double)Framework.ColdStart.Times_Count(rd.get(id - 1), 1, 1, topK));
                    GC.ADD("SC[log]", (double)Framework.ColdStart.Times_Count(rd.get(id - 1), 1, 3, topK));
                    /*
                    cnt[2] += Framework.ColdStart.Times_Count(rd.get(id - 1), 2, 0, topK);
                    cnt[6] += -1;
                    cnt[0] += Framework.ColdStart.Times_Count(rd.get(id - 1), 1, 1, topK);
                    //cnt[1] += Framework.ColdStart.Times_Count(rd.get(id - 1), 1, 2, topK);
                    cnt[3] += Framework.ColdStart.Times_Count(rd.get(id - 1), 3, 0, topK);
                    cnt[4] += Framework.ColdStart.Times_Count(rd.get(id - 1), 4, 0, topK);
                    cnt[5] += Framework.ColdStart.Times_Count(rd.get(id - 1), 5, 0, topK);
                    cnt[7] += Framework.ColdStart.Times_Count(rd.get(id - 1), 1, 3, topK);
                     */

                }
                /*
                System.out.println("         SC[one] : " + (double)cnt[0] / (double)repeat_times);
                //System.out.println("         SC[2] : " + (double)cnt[1] / (double)repeat_times);
                System.out.println("         SC[log] : " + (double)cnt[7] / (double)repeat_times);
                System.out.println("        Random : " + (double)cnt[2] / (double)repeat_times);
                System.out.println("       Pathnum : " + (double)cnt[3] / (double)repeat_times);
                System.out.println("      Pathinfo : " + (double)cnt[4] / (double)repeat_times);
                System.out.println("       Relinfo : " + (double)cnt[5] / (double)repeat_times);
                System.out.println("    BruteForce : " + (double)cnt[6] / (double)repeat_times);

                 */

            }
        }
    }

}
