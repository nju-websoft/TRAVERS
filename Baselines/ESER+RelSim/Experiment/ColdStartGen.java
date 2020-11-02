package Experiment;

import Framework.ColdStart;
import Framework.FeatureGenerator;
import Factory.ReadInput;
import JDBCUtils.JdbcUtil;
import Structures.GlobalVariances;
import Structures.fileModel;
import javafx.util.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/*

Format :
Result File :
Group_id [node_1, node_2, ..., node_k]

Model File :
Group_id
(History_NegSamples)[node_1, node_2, ..., node_k]
(ThisRound_NegSamples)[node_1, node_2, ..., node_k]
(ThisRound_PosSamples)[node_1, node_2, ..., node_k]
(if ColdStart_SC then Path-Weights(one line for one path))[Rel_1, Rel_2, ..., Rel_k] Weight

*/

public class ColdStartGen
{

    public final String ou_prefix = "./datas/ColdStart/";

    public Integer PathLen = 0;
    public Integer topK = 0;

    {
        try {
            InputStream in = JdbcUtil.class.getClassLoader()
                    .getResourceAsStream("Interactive.properties");
            Properties properties = new Properties();
            properties.load(in);

            PathLen = Integer.parseInt(properties.getProperty("PathLen"));
            topK = Integer.parseInt(properties.getProperty("TOPK"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List< Pair<Integer, Map<Integer, Integer>> > rdata;

    public List<Integer> generator_Random(Pair<Integer, Map<Integer, Integer>> data, Integer postive_num)
    {
        FeatureGenerator FG = new FeatureGenerator();
        Pair< List< List<Integer> >, Map<Integer, Map<Integer, Integer>> > features = FG.getFeatures(data.getKey(), PathLen);
        List<Integer> pos_ans = new ArrayList<>(); pos_ans.clear();
        List<Integer> neg_ans = new ArrayList<>(); neg_ans.clear();
        pos_ans.addAll(data.getValue().keySet());
        for(Integer p : features.getValue().keySet()) if(!data.getValue().containsKey(p)) neg_ans.add(p);
        List<Integer> res = new ArrayList<>(); res.clear();
        Random rd = new Random();
        // Add Positive
        while(res.size() <= postive_num)
        {
            Integer id = rd.nextInt(pos_ans.size());
            Integer node = pos_ans.get(id);
            if(!res.contains(node)) res.add(node);
        }
        // Add Negative
        while(res.size() <= topK)
        {
            Integer id = rd.nextInt(neg_ans.size());
            Integer node = neg_ans.get(id);
            if(!res.contains(node)) res.add(node);
        }
        Collections.shuffle(res);
        return res;
    }

    public void All_Random(String foldn, String group, Integer num)
    {
        for(Integer id = 1; id <= 10; ++ id)
        {
            String thisfold = foldn + id.toString() + "/";
            fileModel.CreateFolder(thisfold);
            try
            {
                FileWriter datafw = new FileWriter(new File(thisfold + "data.txt"));
                FileWriter modelfw = new FileWriter(new File(thisfold + "model.txt"));

                modelfw.write("[]\r\n[]\r\n[]\r\n");
                Integer cen = rdata.get(id - 1).getKey();
                List<Integer> res = generator_Random(rdata.get(id - 1), num);
                datafw.write(cen.toString() + " " + res.toString() + "\r\n");

                datafw.close(); modelfw.close();

            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public Pair<Integer, Integer> GetParas(String mtd)
    {
        Integer para1 = 0;
        Integer para2 = 0;

        if(mtd.equals("SC")) {para1 = 1; para2 = 2;}
        if(mtd.equals("RD")) para1 = 2;
        if(mtd.equals("PN")) para1 = 3;
        if(mtd.equals("PI")) para1 = 4;
        if(mtd.equals("RI")) para1 = 5;
        if(mtd.equals("RWR")) para1 = 6;

        return new Pair<>(para1, para2);
    }

    public void CSGEN(String foldn, String group, Pair<Integer, Integer> paras)
    {
        GlobalVariances.Exp_Current_Group = group;
        for(Integer id = 1; id <= 10; ++ id)
        {
            String thisfold = foldn + id.toString() + "/";
            fileModel.CreateFolder(thisfold);
            Pair<Integer, Map<Integer, Integer>> data = rdata.get(id - 1);
            Integer cen = data.getKey();
            GlobalVariances.Exp_Current_Center = cen;
            Pair<List<Integer>, Pair< Pair< Set<Integer>, Set<Integer> >, Pair<Set<Integer>, Map< List<Integer>, Double > > > > ret = ColdStart.Gen(
                    data, paras.getKey(), paras.getValue(), topK
            );
            try
            {
                FileWriter datafw = new FileWriter(new File(thisfold + "data.txt"));
                FileWriter modelfw = new FileWriter(new File(thisfold + "model.txt"));

                datafw.write(cen.toString() + " " + ret.getKey().toString() + "\r\n");
                modelfw.write(ret.getValue().getValue().getKey().toString() + "\r\n");
                modelfw.write(ret.getValue().getKey().getValue().toString() + "\r\n");
                modelfw.write(ret.getValue().getKey().getKey().toString() + "\r\n");
                for(Map.Entry< List<Integer>, Double > kv : ret.getValue().getValue().getValue().entrySet())
                    modelfw.write(kv.getKey().toString() + " " + kv.getValue().toString() + "\r\n");

                datafw.close(); modelfw.close();

            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void Entry(String database)
    {
        String[] suf = {"11b", "21b", "21o", "22b", "rs1b", "rs2b", "rs3b", "rs4b", "rs5b"};
        String[] method = {"AR1", "AR2", "AR3", "AR4", "AR5", "SC", "RD", "RI", "PI", "PN", "RWR"};
        // AR = All Random, SC = Main_ColdStart, RD = Random, RI = RelInfo, PI = PathInfo, PN = PathNum, RWR = RWR
        for(Integer k = 0; k < suf.length; ++ k)
        {
            String group = database + "_" + suf[k];
            String FoldName = ou_prefix + group + "/";
            fileModel.CreateFolder(FoldName);
            GlobalVariances.Exp_Current_Group = group;
            rdata = ReadInput.ParseData(group, 10);
            System.out.println(group);
            for(Integer mid = 0; mid < method.length; ++ mid)
            {
                String mtd = method[mid];

                long bg_time = System.currentTimeMillis();

                String foldn = FoldName + mtd + "/";
                fileModel.CreateFolder(foldn);
                if(mtd.startsWith("AR"))
                {
                    Integer num = Integer.parseInt(mtd.substring(2, 3));
                    All_Random(foldn, group, num);
                }else
                    CSGEN(foldn, group, GetParas(mtd));

                long ed_time = System.currentTimeMillis();

                System.out.println("    " + mtd + " : " + (ed_time - bg_time) / 1000.0);
            }
        }
    }
}
