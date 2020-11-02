package Experiment;

import Evalutaion.EvalTools;
import Evalutaion.alpha_nDCG;
import Factory.ReadInput;
import Framework.Method_Main;
import JDBCUtils.JdbcUtil;
import Structures.GlobalVariances;
import Structures.TimeCounter;
import Structures.fileModel;
import javafx.util.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.*;

public class Experiment_ColdStart
{
    public static final DecimalFormat decifm = new DecimalFormat("#0.00");
    public static final Integer RepeatTimes = 10;

    public static Integer PathLen = 0;
    public static Integer topK = 0;
    public static Integer Iterations = 0;

    static {
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

    public static List<Map<String, Double>> Solve(String method, String csmethod, String rankmethod, Pair<Integer, Map<Integer, Integer>> data, TimeCounter TC)
    {
        List<Map<String, Double>> ret = new ArrayList<>(); ret.clear();

        Method_Main MM = new Method_Main();

        if(method.equals("Method_Stage")) ret = MM.Stage(data, csmethod, rankmethod, TC);

        if(method.equals("Method_Interleave1")) ret = MM.Solve_Interleave1(data, csmethod, rankmethod, TC);

        if(method.equals("Method_Interleave2")) ret = MM.Solve_Interleave2(data, csmethod, rankmethod, TC);

        return ret;
    }

    public static void Entry(
            String TestName,
            String method,
            String rankmethod,
            Integer groupperfile,
            String clicks,
            Boolean repeat
    )
    {
        List<String> EvalNames = GlobalVariances.Evaluation_List;
        String Expefold = "./datas/Experiments/" + TestName + "/" + clicks + "/" + method + "/";
        String name = GlobalVariances.Database_name;
        String[] suf = {"11b", "21o", "22b", "rs1b", "rs2b", "rs3b", "rs4b", "rs5b", "21ob", "21b"};
        //String[] suf = {"21b"};
        Integer suf_len = suf.length;
        //if(GlobalVariances.Database_name.equals("yago")) suf_len --;
        //String[] cs_method = {"SC", "RI", "PI", "RWR"};
        String[] cs_method = {"SC"};
        Integer cs_len = cs_method.length;
        //cs_len = 1;
        //if(GlobalVariances.Database_name.equals("yago")) cs_len = 1;
        //String[] suf = {"11b", "21b", "21o", "22b"};
        try
        {
            for(Integer csid = 0; csid < cs_len; ++ csid)
            {
                String resfold = Expefold;
                System.out.println(); System.out.println(resfold);
                for (Integer exid = 0; exid < suf_len; ++ exid)
                {
                    System.out.print(suf[exid] + " "); if(TestName.equals("Test")) System.out.println();
                    String gp = suf[exid];
                    GlobalVariances.Exp_Ordered_To_Bool = false;
                    if (gp.equals("21ob")) { GlobalVariances.Exp_Ordered_To_Bool = true; gp = "21o"; }
                    GlobalVariances.Exp_Current_Group = GlobalVariances.Database_name + "_" + gp;
                    fileModel.CreateFolder(resfold);
                    FileWriter fw = new FileWriter(new File(resfold + name + "_" + suf[exid] + ".txt"));
                    List<Pair<Integer, Map<Integer, Integer>>> rd = ReadInput.ParseData(
                            name + "_" + gp, groupperfile);
                    List<Map<String, Double>> avg = new ArrayList<>(); EvalTools.Initialize(avg);
                    List<Double> tim = new ArrayList<>(); tim.clear();
                    for (Integer i = 0; i <= Iterations; ++i) tim.add(0.0);
                    String tim_out = "";
                    for (Integer num = 1; num <= groupperfile; ++num)
                    {
                        //if(num < 5) continue;
                        GlobalVariances.Exp_Current_Center = rd.get(num - 1).getKey();
                        alpha_nDCG.Init(rd.get(num - 1).getKey());

                        //System.out.println(method + " : " + name + "_" + suf[exid] + " : " + num.toString());
                        List<Map<String, Double>> sta = new ArrayList<>(); EvalTools.Initialize(sta);
                        Integer rpt = 1; if (repeat) rpt = RepeatTimes;
                        TimeCounter TC = new TimeCounter(); TC.clear();
                        for (Integer exnum = 1; exnum <= rpt; ++exnum)
                        {
                            TC.addInstance();
                            List<Map<String, Double>> res = Solve(method, cs_method[csid], rankmethod, rd.get(num - 1), TC);
                            EvalTools.Combine(sta, res, 1.0);
                        }

                        if(TestName.equals("Test")) System.out.println("--------------------------------------------------------------");

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
