package Experiment;

import ESER.GetAnswer_SingleCenterNode;
import Evalutaion.EvalTools;
import Evalutaion.alpha_nDCG;
import Framework.Clicks;
import Framework.ColdStart;
import Framework.FeatureGenerator;
import Framework.Method_Main;
import Factory.ReadInput;
import JDBCUtils.JdbcUtil;
import RelSim.RelSim;
import Structures.GlobalVariances;
import Structures.TimeCounter;
import Structures.fileModel;
import javafx.util.Pair;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class Experiment_WarmStart
{
    public final DecimalFormat decifm = new DecimalFormat("#0.00");

    public static Integer PathLen = 0;
    public static Integer topK = 0;
    public static Integer Iterations = 0;

    public static FeatureGenerator FG = new FeatureGenerator();

    static {
        try {
            InputStream in = JdbcUtil.class.getClassLoader()
                    .getResourceAsStream("Interactive.properties");
            Properties properties = new Properties();
            properties.load(in);

            PathLen = Integer.parseInt(properties.getProperty("PathLen"));
            topK = Integer.parseInt(properties.getProperty("TOPK"));
            Iterations = Integer.parseInt(properties.getProperty("Iterations"));
            //clickid = properties.getProperty("click_id");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static final Integer RepeatTimes = 10;


    public static List<Map<String, Double>> Solve(String method, Pair<Integer, Map<Integer, Integer>> data, List<Integer> coldstart, TimeCounter TC)
    {
        List<Map<String, Double>> ret = new ArrayList<>(); ret.clear();

        TC.addTime("0", 0.0);

        Method_Main MM = new Method_Main(); MM.clear();

        if(method.equals("Method_Stage"))
        {
            List<Integer> cs = new ArrayList<>(); cs.clear(); cs.addAll(coldstart);
            MM.clear();
            MM.features = MM.FG.getFeatures(data.getKey(), PathLen);
            MM.fea_map =  MM.MapFromNodeToBitSet();
            MM.Classifier_Features = new BitSet(MM.bs_size);
            ColdStart.init(MM.features);
            ColdStart.SC_init(MM.features.getKey(), MM.features.getValue(), MM.fea_map, MM.FG);
            Pair<Set<Integer>, Set<Integer>> click_res = null;
            click_res = Clicks.ClickModel(data.getValue(), cs);
            while(click_res.getKey().size() <= 0 ) click_res = Clicks.ClickModel(data.getValue(), cs);
            ColdStart.UpdateResult(click_res, MM.cumu_Pos, MM.cumu_Neg);
            for(Integer node : click_res.getKey()) MM.Classifier_Features.or(MM.fea_map.get(node));
            Map<String, Double> tmp;
            tmp = new HashMap<>(); tmp.clear();
            if(GlobalVariances.Evaluation.contains("nDCG")) tmp.put("nDCG", Evalutaion.CalNDCG.calculateNDCG(cs, data.getValue(), topK));
            if(GlobalVariances.Evaluation.contains("alpha-nDCG")) tmp.put("alpha-nDCG", Evalutaion.alpha_nDCG.Calculate(cs, topK));
            ret.add(tmp);
            ColdStart.Adjust(click_res, MM.fea_map);

            for(Integer ite = 1; ite <= Iterations; ++ ite)
            {
                long bg_time = System.currentTimeMillis();

                if(MM.cumu_Pos.size() <= 0)
                    cs = ColdStart.Coldstart_SC(MM.features.getKey(), MM.features.getValue(), MM.fea_map, MM.FG, topK);
                else
                    cs = MM.Rerank_Pairwise(MM.features.getValue(), MM.fea_map, topK, MM.cumu_Pos, MM.cumu_Neg);
                click_res = Clicks.ClickModel(data.getValue(), cs);
                ColdStart.UpdateResult(click_res, MM.cumu_Pos, MM.cumu_Neg);
                for(Integer node : click_res.getKey()) MM.Classifier_Features.or(MM.fea_map.get(node));
                tmp = new HashMap<>(); tmp.clear();
                if(GlobalVariances.Evaluation.contains("nDCG")) tmp.put("nDCG", Evalutaion.CalNDCG.calculateNDCG(cs, data.getValue(), topK));
                if(GlobalVariances.Evaluation.contains("alpha-nDCG")) tmp.put("alpha-nDCG", Evalutaion.alpha_nDCG.Calculate(cs, topK));
                ret.add(tmp);
                ColdStart.Adjust(click_res, MM.fea_map);

                long ed_time = System.currentTimeMillis();
                double timecost = (ed_time - bg_time) / 1000.0;
                TC.addTime(ite.toString(), timecost);
            }
        }

        if(method.equals("Method_Interleave1"))
        {
            List<Integer> cs = new ArrayList<>(); cs.clear(); cs.addAll(coldstart);
            MM.clear();
            MM.features = MM.FG.getFeatures(data.getKey(), PathLen);
            MM.fea_map =  MM.MapFromNodeToBitSet();
            MM.Classifier_Features = new BitSet(MM.bs_size);
            ColdStart.init(MM.features);
            ColdStart.SC_init(MM.features.getKey(), MM.features.getValue(), MM.fea_map, MM.FG);
            Pair<Set<Integer>, Set<Integer>> click_res = null;
            click_res = Clicks.ClickModel(data.getValue(), cs);
            while(click_res.getKey().size() <= 0 ) click_res = Clicks.ClickModel(data.getValue(), cs);
            ColdStart.UpdateResult(click_res, MM.cumu_Pos, MM.cumu_Neg);
            for(Integer node : click_res.getKey()) MM.Classifier_Features.or(MM.fea_map.get(node));
            Map<String, Double> tmp;
            tmp = new HashMap<>(); tmp.clear();
            if(GlobalVariances.Evaluation.contains("nDCG")) tmp.put("nDCG", Evalutaion.CalNDCG.calculateNDCG(cs, data.getValue(), topK));
            if(GlobalVariances.Evaluation.contains("alpha-nDCG")) tmp.put("alpha-nDCG", Evalutaion.alpha_nDCG.Calculate(cs, topK));
            ret.add(tmp);
            ColdStart.Adjust(click_res, MM.fea_map);

            for(Integer ite = 1; ite <= Iterations; ++ ite)
            {
                long bg_time = System.currentTimeMillis();

                // Gen
                List<Integer> res_cs = ColdStart.Coldstart_SC(MM.features.getKey(), MM.features.getValue(), MM.fea_map, MM.FG, topK);
                List<Integer> res_rr = null;
                if(MM.cumu_Pos.size() > 0) res_rr = MM.Rerank_Pairwise(MM.features.getValue(), MM.fea_map, topK, MM.cumu_Pos, MM.cumu_Neg);

                Pair< List<Integer>, List<Integer> > inter_res = MM.Interleave1(res_cs, res_rr, MM.Interleave_P);
                cs = inter_res.getKey();

                // Click
                click_res = Clicks.ClickModel(data.getValue(), cs);

                // Adjust
                if(MM.cumu_Pos.size() > 0) MM.Adjust_P(click_res, inter_res);
                ColdStart.Adjust(click_res, MM.fea_map);
                ColdStart.UpdateResult(click_res, MM.cumu_Pos, MM.cumu_Neg);
                for(Integer node : click_res.getKey()) MM.Classifier_Features.or(MM.fea_map.get(node));

                // Statistics
                tmp = new HashMap<>(); tmp.clear();
                if(GlobalVariances.Evaluation.contains("nDCG")) tmp.put("nDCG", Evalutaion.CalNDCG.calculateNDCG(cs, data.getValue(), topK));
                if(GlobalVariances.Evaluation.contains("alpha-nDCG")) tmp.put("alpha-nDCG", Evalutaion.alpha_nDCG.Calculate(cs, topK));
                ret.add(tmp);

                long ed_time = System.currentTimeMillis();
                double timecost = (ed_time - bg_time) / 1000.0;
                TC.addTime(ite.toString(), timecost);
            }
        }

        if(method.equals("RelSim"))
        {
            RelSim RS = new RelSim();

            List<Integer> cs = new ArrayList<>(); cs.clear(); cs.addAll(coldstart);
            Set<Integer> cumu_Pos = new HashSet<>(); cumu_Pos.clear();
            Set<Integer> cumu_Neg = new HashSet<>(); cumu_Neg.clear();
            Pair<Set<Integer>, Set<Integer>> click_res = null;

            click_res = Clicks.ClickModel(data.getValue(), cs);
            while(click_res.getKey().size() <= 0 ) click_res = Clicks.ClickModel(data.getValue(), cs);
            ColdStart.UpdateResult(click_res, cumu_Pos, cumu_Neg);
            Map<String, Double> tmp;
            tmp = new HashMap<>(); tmp.clear();
            if(GlobalVariances.Evaluation.contains("nDCG")) tmp.put("nDCG", Evalutaion.CalNDCG.calculateNDCG(cs, data.getValue(), topK));
            if(GlobalVariances.Evaluation.contains("alpha-nDCG")) tmp.put("alpha-nDCG", Evalutaion.alpha_nDCG.Calculate(cs, topK));
            ret.add(tmp);

            for(Integer ite = 1; ite <= Iterations; ++ ite)
            {

                long bg_time = System.currentTimeMillis();

                List<Integer> ltmp = new ArrayList<>(); ltmp.clear(); ltmp.addAll(cumu_Pos);

                cs = RS.RelSim_List(data.getKey(), ltmp, PathLen, topK, 0);

                click_res = Clicks.ClickModel(data.getValue(), cs);
                ColdStart.UpdateResult(click_res, cumu_Pos, cumu_Neg);
                tmp = new HashMap<>(); tmp.clear();
                if(GlobalVariances.Evaluation.contains("nDCG")) tmp.put("nDCG", Evalutaion.CalNDCG.calculateNDCG(cs, data.getValue(), topK));
                if(GlobalVariances.Evaluation.contains("alpha-nDCG")) tmp.put("alpha-nDCG", Evalutaion.alpha_nDCG.Calculate(cs, topK));
                ret.add(tmp);

                long ed_time = System.currentTimeMillis();
                double timecost = (ed_time - bg_time) / 1000.0;
                TC.addTime(ite.toString(), timecost);
            }

        }

        if(method.equals("PRA"))
        {
            RelSim RS = new RelSim();

            List<Integer> cs = new ArrayList<>(); cs.clear(); cs.addAll(coldstart);
            Set<Integer> cumu_Pos = new HashSet<>(); cumu_Pos.clear();
            Set<Integer> cumu_Neg = new HashSet<>(); cumu_Neg.clear();
            Pair<Set<Integer>, Set<Integer>> click_res = null;

            click_res = Clicks.ClickModel(data.getValue(), cs);
            while(click_res.getKey().size() <= 0 ) click_res = Clicks.ClickModel(data.getValue(), cs);
            ColdStart.UpdateResult(click_res, cumu_Pos, cumu_Neg);
            Map<String, Double> tmp;
            tmp = new HashMap<>(); tmp.clear();
            if(GlobalVariances.Evaluation.contains("nDCG")) tmp.put("nDCG", Evalutaion.CalNDCG.calculateNDCG(cs, data.getValue(), topK));
            if(GlobalVariances.Evaluation.contains("alpha-nDCG")) tmp.put("alpha-nDCG", Evalutaion.alpha_nDCG.Calculate(cs, topK));
            ret.add(tmp);

            for(Integer ite = 1; ite <= Iterations; ++ ite)
            {

                long bg_time = System.currentTimeMillis();

                List<Integer> ltmp = new ArrayList<>(); ltmp.clear(); ltmp.addAll(cumu_Pos);

                cs = RS.RelSim_List(data.getKey(), ltmp, PathLen, topK, 1);

                click_res = Clicks.ClickModel(data.getValue(), cs);
                ColdStart.UpdateResult(click_res, cumu_Pos, cumu_Neg);
                tmp = new HashMap<>(); tmp.clear();
                if(GlobalVariances.Evaluation.contains("nDCG")) tmp.put("nDCG", Evalutaion.CalNDCG.calculateNDCG(cs, data.getValue(), topK));
                if(GlobalVariances.Evaluation.contains("alpha-nDCG")) tmp.put("alpha-nDCG", Evalutaion.alpha_nDCG.Calculate(cs, topK));
                ret.add(tmp);

                long ed_time = System.currentTimeMillis();
                double timecost = (ed_time - bg_time) / 1000.0;
                TC.addTime(ite.toString(), timecost);
            }

        }

        if(method.equals("ESER"))
        {
            GetAnswer_SingleCenterNode GASC = new GetAnswer_SingleCenterNode(); GASC.clean();

            List<Integer> cs = new ArrayList<>(); cs.clear(); cs.addAll(coldstart);
            Set<Integer> cumu_Pos = new HashSet<>(); cumu_Pos.clear();
            Set<Integer> cumu_Neg = new HashSet<>(); cumu_Neg.clear();
            Pair<Set<Integer>, Set<Integer>> click_res = null;

            click_res = Clicks.ClickModel(data.getValue(), cs);
            while(click_res.getKey().size() <= 0 ) click_res = Clicks.ClickModel(data.getValue(), cs);
            ColdStart.UpdateResult(click_res, cumu_Pos, cumu_Neg);
            Map<String, Double> tmp;
            tmp = new HashMap<>(); tmp.clear();
            if(GlobalVariances.Evaluation.contains("nDCG")) tmp.put("nDCG", Evalutaion.CalNDCG.calculateNDCG(cs, data.getValue(), topK));
            if(GlobalVariances.Evaluation.contains("alpha-nDCG")) tmp.put("alpha-nDCG", Evalutaion.alpha_nDCG.Calculate(cs, topK));
            ret.add(tmp);

            for(Integer ite = 1; ite <= Iterations; ++ ite)
            {
                long bg_time = System.currentTimeMillis();

                List<Integer> ltmp = new ArrayList<>(); ltmp.clear(); ltmp.addAll(cumu_Pos);

                cs = GASC.findanswer_withsample(data.getKey(), ltmp, PathLen, topK);

                click_res = Clicks.ClickModel(data.getValue(), cs);
                ColdStart.UpdateResult(click_res, cumu_Pos, cumu_Neg);
                tmp = new HashMap<>(); tmp.clear();
                if(GlobalVariances.Evaluation.contains("nDCG")) tmp.put("nDCG", Evalutaion.CalNDCG.calculateNDCG(cs, data.getValue(), topK));
                if(GlobalVariances.Evaluation.contains("alpha-nDCG")) tmp.put("alpha-nDCG", Evalutaion.alpha_nDCG.Calculate(cs, topK));
                ret.add(tmp);

                long ed_time = System.currentTimeMillis();
                double timecost = (ed_time - bg_time) / 1000.0;
                TC.addTime(ite.toString(), timecost);
            }

        }

        return ret;
    }

    public void Expe(String TestName, String method, Integer groupperfile, String clicks, Boolean repeat)
    {
        String name = GlobalVariances.Database_name;
        String Expefold = "./datas/Experiments/" + TestName + "/" + clicks + "/" + method + "/";
        String[] cs_type = {"SC"};
        String[] suf = {"11b", "22b", "rs1b", "rs2b", "rs3b", "rs4b", "rs5b", "21ob", "21b"};
        //String[] suf = {"21b"};
        Integer suf_len = suf.length;
        List<String> EvalNames = GlobalVariances.Evaluation_List;
        //if(GlobalVariances.Database_name.equals("yago")) suf_len --;
        try
        {
            for(Integer cs_id = 0; cs_id < cs_type.length; ++ cs_id)
            {
                String resfold = Expefold + "/";
                System.out.println();  System.out.println(resfold);
                for (Integer exid = 0; exid < suf_len; ++exid)
                {
                    System.out.print(suf[exid] + " ");
                    String gp = suf[exid];
                    GlobalVariances.Exp_Ordered_To_Bool = false;
                    if(gp.equals("21ob")) {GlobalVariances.Exp_Ordered_To_Bool = true; gp = "21o";}
                    fileModel.CreateFolder(resfold);
                    GlobalVariances.Exp_Current_Group = GlobalVariances.Database_name + "_" + gp;
                    FileWriter fw = new FileWriter(new File(resfold + name + "_" + suf[exid] + ".txt"));
                    List<Pair<Integer, Map<Integer, Integer>>> rd = ReadInput.ParseData(
                            name + "_" + gp, groupperfile);
                    List<Map<String, Double>> avg = new ArrayList<>(); EvalTools.Initialize(avg);
                    List<Double> tim = new ArrayList<>(); tim.clear(); for (Integer i = 0; i <= Iterations; ++i) tim.add(0.0);
                    List<Pair<Integer, List<Integer>>> samples = ReadInput.ParseColdStart_Sample(
                            name + "_" + gp, groupperfile);
                    String tim_out = "";
                    for (Integer num = 1; num <= groupperfile; ++num)
                    {
                        GlobalVariances.Exp_Current_Center = rd.get(num - 1).getKey();
                        //System.out.println(method + " : " + name + "_" + suf[exid] + " : " + num.toString());
                        List<Integer> coldstart = samples.get(num - 1).getValue();
                        List<Map<String, Double>> sta = new ArrayList<>(); EvalTools.Initialize(sta);
                        Integer rpt = 1;
                        if (repeat) rpt = RepeatTimes;
                        TimeCounter TC = new TimeCounter(); TC.clear();
                        alpha_nDCG.Init(rd.get(num - 1).getKey());
                        for (Integer exnum = 1; exnum <= rpt; ++ exnum)
                        {
                            TC.addInstance();
                            List<Map<String, Double>> res = Solve(method, rd.get(num - 1), coldstart, TC);
                            EvalTools.Combine(sta, res, 1.0);
                        }

                        // Print
                        tim_out += num.toString();
                        if (num < 10) tim_out += " ";
                        tim_out += " : ";
                        fw.write(num.toString());
                        if (num < 10) fw.write(" ");
                        fw.write(" : ");
                        List<Pair<String, Double>> tim_sta = TC.getStatistics_num();
                        EvalTools.Combine(avg, sta, 1.0 / (double)rpt);
                        for (Integer i = 0; i <= Iterations; ++i)
                        {
                            double val = tim_sta.get(i).getValue();
                            tim_out += "(" + i.toString() + ")" + decifm.format(val) + " ";
                            tim.set(i, tim.get(i) + val);
                            fw.write("(" + i.toString() + ")");
                            fw.write(decifm.format(sta.get(i).get(EvalNames.get(0)) / (double) rpt));
                            for(Integer ii = 1; ii < EvalNames.size(); ++ ii) fw.write("[" + decifm.format(sta.get(i).get(EvalNames.get(ii)) / (double) rpt) + "]");
                            fw.write(" ");
                        }

                        fw.write("\r\n");
                        tim_out += "\r\n";
                    }

                    fw.write(((Integer) (groupperfile + 1)).toString() + " : ");
                    tim_out += ((Integer) (groupperfile + 1)).toString() + " : ";
                    for (Integer i = 0; i <= Iterations; ++i)
                    {
                        tim_out += "(" + i.toString() + ")" + decifm.format(tim.get(i) / (double) groupperfile) + " ";
                        fw.write("(" + i.toString() + ")");
                        fw.write(decifm.format(avg.get(i).get(EvalNames.get(0)) / (double) groupperfile));
                        for(Integer ii = 1; ii < EvalNames.size(); ++ ii) fw.write("[" + decifm.format(avg.get(i).get(EvalNames.get(ii)) / (double) groupperfile) + "]");
                        fw.write(" ");
                    }
                    fw.write("\r\n");
                    tim_out += "\r\n";
                    fw.write("\r\n");
                    fw.write(tim_out);
                    fw.close();
                }
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
