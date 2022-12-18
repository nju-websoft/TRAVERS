package Main;

import ExperimentResults.CalNDCG;
import ExperimentResults.alpha_nDCG;
import GraphData.GraphOntGetterM;
import GraphData.Ontology;
import GraphData.RelationIndex;
import GraphData.TypedGraphModelM;
import JDBCUtils.JdbcUtil;
import javafx.util.Pair;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class InteractiveExperiment {
    public static final DecimalFormat decifm = new DecimalFormat("#0.00");

    public static final Map<String, Double> per = Clicks.ModelGen_2(0.0, 1.0, 0.0);
    public static final Map<String, Double> bin = Clicks.ModelGen_2(2.0, 0.9, 0.1);
    public static final Map<String, Double> rnd = Clicks.ModelGen_2(2.0, 0.6, 0.4);

    public static final Integer RepeatTimes = 3;
    public static final Integer Iterations = 10;
    public static final Integer topK = 10;

    public static final String in_prefix = "./datas/Semantic/";

    public static List< Pair<Integer, Map<Integer, Integer>> > ParseData(String filename, Integer number)
    {
        List< Pair<Integer, Map<Integer, Integer>> > ret = new ArrayList<>(); ret.clear();
        try
        {
            Scanner sc = new Scanner(new FileInputStream( new File(in_prefix + filename + ".txt")));
            for(Integer i = 1; i <= number; ++ i)
            {
                Integer cent = Integer.parseInt(sc.nextLine());
                String[] tmp = sc.nextLine().split("\t");
                Map<Integer, Integer> answer = new HashMap<>(); answer.clear();
                for(Integer id = 0; id < tmp.length; ++ id)
                {
                    String[] now = tmp[id].split(":");
                    Integer node = Integer.parseInt(now[0]);
                    Integer rela = Integer.parseInt(now[1]);
                    if(GlobalVariances.Exp_Ordered_To_Bool) rela = Math.min(rela, 1);
                    answer.put(node, rela);
                }
                ret.add(new Pair<>(cent, WSDMExperiment.sortByValue(answer)));
            }
            sc.close();
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return ret;
    }

    public static List< Pair< Integer, List<Integer> > > ParseColdStart_Sample(String filename, Integer number)
    {
        List< Pair< Integer, List<Integer> > > res = new ArrayList<>(); res.clear();

        try
        {
            for(Integer id = 1; id <= number; ++ id)
            {
                Scanner sc = new Scanner(new FileInputStream( new File("./datas/Semantic/WarmStart/" + filename + "/SC/" + id.toString() + "/data.txt")));
                String now = sc.nextLine();
                Integer pos = now.indexOf(" [");
                Integer center = Integer.parseInt(now.substring(0, pos));
                String[] s = now.substring(pos + 2, now.length() - 1).split(", ");
                List<Integer> samples = new ArrayList<>(); samples.clear();
                for(Integer i = 0; i < s.length; ++ i) samples.add(Integer.parseInt(s[i]));
                res.add(new Pair<>(center, samples));
                sc.close();
            }
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        return res;
    }

    public static void UpdateResult(Pair<Set<Integer>, Set<Integer>> click_res, Set<Integer> cumu_pos, Set<Integer> cumu_neg)
    {
        Set<Integer> need_del = new HashSet<>(); need_del.clear();
        for(Integer node : cumu_pos) if(click_res.getKey().contains(node) || click_res.getValue().contains(node)) need_del.add(node);
        for(Integer node : need_del) cumu_pos.remove(node);
        need_del.clear();
        for(Integer node : cumu_neg) if(click_res.getKey().contains(node) || click_res.getValue().contains(node)) need_del.add(node);
        for(Integer node : need_del) cumu_neg.remove(node);
        need_del.clear();
        cumu_pos.addAll(click_res.getKey()); cumu_neg.addAll(click_res.getValue());
    }

    public static List<Map<String, Double>> Solve(String method, Pair<Integer, Map<Integer, Integer>> data, List<Integer> coldstart)
    {
        List<Map<String, Double>> ret = new ArrayList<>(); ret.clear();

        TimeCounter.addTime("0", 0.0);

        List<Integer> cs = new ArrayList<>(); cs.clear(); cs.addAll(coldstart);
        Set<Integer> cumu_Pos = new HashSet<>(); cumu_Pos.clear();
        Set<Integer> cumu_Neg = new HashSet<>(); cumu_Neg.clear();
        Pair<Set<Integer>, Set<Integer>> click_res = null;

        click_res = Clicks.ClickModel(data.getValue(), cs);
        while(click_res.getKey().size() <= 0 ) click_res = Clicks.ClickModel(data.getValue(), cs);
        UpdateResult(click_res, cumu_Pos, cumu_Neg);
        Map<String, Double> tmp;
        tmp = new HashMap<>(); tmp.clear();
        if(GlobalVariances.Evaluation.contains("nDCG")) tmp.put("nDCG", CalNDCG.calculateNDCG(cs, data.getValue(), topK));
        if(GlobalVariances.Evaluation.contains("alpha-nDCG")) tmp.put("alpha-nDCG", alpha_nDCG.Calculate(cs, topK));
        ret.add(tmp);

        for(Integer ite = 1; ite <= Iterations; ++ ite)
        {

            long bg_time = System.currentTimeMillis();

            List<Integer> pexamplelist = new ArrayList<>(); pexamplelist.addAll(cumu_Pos);
            if (pexamplelist.size() > 5) pexamplelist = pexamplelist.subList(0, 5);

            InteractiveMain itaMain = new InteractiveMain();
            ResultBean resultBean = null;
            if (method.equals("RelSUE"))
                resultBean = itaMain.topK_relevance(data.getKey(), pexamplelist, topK, 3);
            if (method.equals("RelSim"))
                resultBean = itaMain.topK_byRelsim(data.getKey(), pexamplelist, topK);
            cs = resultBean.getResults();

            click_res = Clicks.ClickModel(data.getValue(), cs);
            UpdateResult(click_res, cumu_Pos, cumu_Neg);
            tmp = new HashMap<>(); tmp.clear();
            if(GlobalVariances.Evaluation.contains("nDCG")) tmp.put("nDCG", CalNDCG.calculateNDCG(cs, data.getValue(), topK));
            if(GlobalVariances.Evaluation.contains("alpha-nDCG")) tmp.put("alpha-nDCG", alpha_nDCG.Calculate(cs, topK));
            ret.add(tmp);

            long ed_time = System.currentTimeMillis();
            double timecost = (ed_time - bg_time) / 1000.0;
            TimeCounter.addTime(ite.toString(), timecost);
        }

        return ret;
    }

    public static void Expe(String method, Integer groupperfile, String clicks, Boolean repeat)
    {
        String name = GlobalVariances.Database_name;
        String Expefold = "./datas/Experiments/WarmStart/" + clicks + "/" + method + "/";
        String[] cs_type = {"SC"};
        String[] suf = {"11b", "21b", "21o", "22b", "rs1b", "rs2b", "rs3b", "rs4b", "rs5b", "21ob"};
        Integer suf_len = suf.length;
        List<String> EvalNames = GlobalVariances.Evaluation_List;
        try
        {
            for(Integer cs_id = 0; cs_id < cs_type.length; ++ cs_id)
            {
                String resfold = Expefold + cs_type[cs_id] + "/";
                System.out.println(resfold);
                for (Integer exid = 0; exid < suf.length; ++exid)
                {
                    System.out.print(suf[exid] + " ");
                    String gp = suf[exid];
                    GlobalVariances.Exp_Ordered_To_Bool = false;
                    if(gp.equals("21ob")) {GlobalVariances.Exp_Ordered_To_Bool = true; gp = "21o";}
                    fileModel.CreateFolder(resfold);
                    GlobalVariances.Exp_Current_Group = GlobalVariances.Database_name + "_" + gp;
                    FileWriter fw = new FileWriter(new File(resfold + name + "_" + suf[exid] + ".txt"));
                    List<Pair<Integer, Map<Integer, Integer>>> rd = ParseData(
                            name + "_" + gp, groupperfile);
                    List<Map<String, Double>> avg = new ArrayList<>(); EvalTools.Initialize(avg);
                    List<Double> tim = new ArrayList<>(); tim.clear(); for (Integer i = 0; i <= Iterations; ++i) tim.add(0.0);
                    List<Pair<Integer, List<Integer>>> samples = ParseColdStart_Sample(
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
                        TimeCounter.clear();
                        alpha_nDCG.Init(rd.get(num - 1).getKey());
                        for (Integer exnum = 1; exnum <= rpt; ++ exnum)
                        {
                            TimeCounter.addInstance();
                            List<Map<String, Double>> res = Solve(method, rd.get(num - 1), coldstart);
                            EvalTools.Combine(sta, res, 1.0);
                        }

                        // Print
                        tim_out += num.toString();
                        if (num < 10) tim_out += " ";
                        tim_out += " : ";
                        fw.write(num.toString());
                        if (num < 10) fw.write(" ");
                        fw.write(" : ");
                        List<Pair<String, Double>> tim_sta = TimeCounter.getStatistics_num();
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

    public static void Experiments_WarmStart(String database)
    {
        GlobalVariances.Database_name = database;
        String[] mtds = {"RelSUE"};
        List<Pair<String, Map<String, Double>>> clicks = new ArrayList<>(); clicks.clear();
        clicks.add(new Pair<>("per", per));
        clicks.add(new Pair<>("bin", bin));
        clicks.add(new Pair<>("rnd", rnd));
        for(Integer click_plicit = 0; click_plicit < 2; ++ click_plicit)
        {
            if(click_plicit.equals(0)) GlobalVariances.Click_xxplicit = "Implicit";
            if(click_plicit.equals(1)) GlobalVariances.Click_xxplicit = "Explicit";
            for(Pair<String, Map<String, Double>> cli : clicks)
            {
                GlobalVariances.SetPara_ClickModel(cli.getValue(), cli.getKey());
                for (Integer mid = 0; mid < mtds.length; ++mid)
                    Expe(
                        mtds[mid],
                        10,
                        GlobalVariances.Click_xxplicit + "_" + cli.getKey(),
                        false
                    );
            }
        }
    }

    public static void main(String[] args) {
        Ontology.Initialize();
        GraphOntGetterM.initializeMap();
        RelationIndex.initializeMap();
        TypedGraphModelM.initializeMap();
        if(JdbcUtil.URL.contains("dbpedia")) Experiments_WarmStart("dbpedia");
        if(JdbcUtil.URL.contains("yago")) Experiments_WarmStart("yago");
    }
}
