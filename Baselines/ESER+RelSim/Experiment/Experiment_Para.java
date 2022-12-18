package Experiment;

import Evalutaion.alpha_nDCG;
import Framework.Clicks;
import Framework.Method_Main;
import Structures.GlobalVariances;
import Structures.TimeCounter;
import Structures.fileModel;
import javafx.util.Pair;
import Evalutaion.CD_nDCG;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class Experiment_Para
{
    public static final DecimalFormat decifm = new DecimalFormat("#0.00");

    public static final Map<String, Double> per = Clicks.ModelGen_2(0.0, 1.0, 0.0);
    public static final Map<String, Double> bin = Clicks.ModelGen_2(1.0, 0.9, 0.1);
    public static final Map<String, Double> rnd = Clicks.ModelGen_2(2.0, 0.6, 0.4);

    public static final Integer rpt = 5;

    public static Integer pergp = 10;
    public static String in_prefix = "./datas/Semantic/ParaStudy/";

    public static <K, V> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Object>() {
            @SuppressWarnings("unchecked")
            public int compare(Object o1, Object o2) {
                return ((Comparable<V>) ((Map.Entry<K, V>) (o2)).getValue()).compareTo(((Map.Entry<K, V>) (o1)).getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<>();
        for (Iterator<Map.Entry<K, V>> it = list.iterator(); it.hasNext();) {
            Map.Entry<K, V> entry = it.next();
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

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
                ret.add(new Pair<>(cent, sortByValue(answer)));
            }
            sc.close();
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return ret;
    }

    public static Pair<Double, Double> DoExp(String suf)
    {
        String gpname = GlobalVariances.Database_name + "_" + suf;
        List< Pair<Integer, Map<Integer, Integer>> > rd = ParseData(gpname, pergp);
        Double ndcg_sum = 0.0;
        Double cdndcg_sum = 0.0;
        TimeCounter TC = new TimeCounter(); TC.clear();
        Method_Main MM = new Method_Main(); MM.clear();
        for(Integer id = 0; id < pergp; ++ id)
        {
            List<Double> ndcgs = new ArrayList<>(); ndcgs.clear();
            alpha_nDCG.Init(rd.get(id).getKey());
            List<Map<String, Double>> sss = MM.Solve_Interleave1(rd.get(id), "SC", "Pairwise", TC);
            for(Map<String, Double> unit : sss) ndcgs.add(unit.get("nDCG"));
            cdndcg_sum += CD_nDCG.calculate(ndcgs);
            ndcg_sum += ndcgs.get(ndcgs.size() - 1);
        }
        return new Pair<>(ndcg_sum / (double)pergp, cdndcg_sum / (double)pergp);
    }

    public static void ParaStu_Alpha_Study(String paraname)
    {
        System.out.println(paraname);
        Double[] paras = {0.001, 0.005, 0.01, 0.05, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
        String fold = "./datas/Experiments/ParaStduy/" + paraname + "/";
        fileModel.CreateFolder(fold);
        List<Pair<String, Map<String, Double>>> clicks = new ArrayList<>(); clicks.clear();
        clicks.add(new Pair<>("per", per));
        clicks.add(new Pair<>("bin", bin));
        clicks.add(new Pair<>("rnd", rnd));
        String[] suf = {"11b", "21o", "22b", "rs1b", "rs2b", "rs3b", "rs4b", "rs5b", "21b"};
        for(Integer click_plicit = 0; click_plicit < 2; ++ click_plicit)
        {
            //if (mid.equals(0) && click_plicit.equals(0)) continue;
            Pair<String, Map<String, Double>> cli = null;
            if (click_plicit.equals(0)) { GlobalVariances.Click_xxplicit = "Implicit"; cli = new Pair<>("bin", bin); }
            if (click_plicit.equals(1)) { GlobalVariances.Click_xxplicit = "Explicit"; cli = new Pair<>("rnd", rnd); }
            GlobalVariances.SetPara_ClickModel(cli.getValue(), cli.getKey());
            try
            {
                System.out.println(GlobalVariances.Click_xxplicit + "_" + cli.getKey());
                FileWriter fw = new FileWriter(new File(fold + GlobalVariances.Click_xxplicit + "_" + cli.getKey() + ".txt"));
                fw.write(GlobalVariances.Click_xxplicit + "_" + cli.getKey() + "\r\n");
                for(Integer pid = 0; pid < paras.length; ++ pid)
                {
                    Double all_ndcg_sum = 0.0;
                    Double all_cdndcg_sum = 0.0;
                    for (Integer sid = 0; sid < suf.length; ++sid)
                    {
                        GlobalVariances.ColdStart_SC_Adjust_coeff_neg = paras[pid];
                        Double ndcg_sum = 0.0;
                        Double cdndcg_sum = 0.0;
                        for (Integer rp = 1; rp <= rpt; ++rp)
                        {
                            Pair<Double, Double> ret = DoExp(suf[sid]);
                            ndcg_sum += ret.getKey();
                            cdndcg_sum += ret.getValue();
                        }
                        ndcg_sum /= (double) rpt;
                        cdndcg_sum /= (double) rpt;
                        fw.write(suf[sid] + " : " + paras[pid] + "   (" + decifm.format(
                                ndcg_sum) + ", " + decifm.format(cdndcg_sum) + ")\r\n");

                        all_ndcg_sum += ndcg_sum;
                        all_cdndcg_sum += cdndcg_sum;
                    }
                    all_cdndcg_sum /= (double)suf.length;
                    all_ndcg_sum /= (double)suf.length;
                    fw.write("All" + " : " + paras[pid] + "   (" + decifm.format(
                            all_ndcg_sum) + ", " + decifm.format(all_cdndcg_sum) + ")\r\n");
                }
                fw.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void ParaStu_Alpha_Test(String paraname)
    {
        System.out.println(paraname);
        Double[] paras = {0.001, 0.005, 0.01, 0.05, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
        String fold = "./datas/Experiments/ParaStduy/" + paraname + "/";
        fileModel.CreateFolder(fold);
        List<Pair<String, Map<String, Double>>> clicks = new ArrayList<>(); clicks.clear();
        clicks.add(new Pair<>("per", per));
        clicks.add(new Pair<>("bin", bin));
        clicks.add(new Pair<>("rnd", rnd));
        String[] suf = {"11b", "21o", "22b", "rs1b", "rs2b", "rs3b", "rs4b", "rs5b", "21b"};
        for(Integer click_plicit = 0; click_plicit < 2; ++ click_plicit)
        {
            //if (mid.equals(0) && click_plicit.equals(0)) continue;
            if (click_plicit.equals(0)) GlobalVariances.Click_xxplicit = "Implicit";
            if (click_plicit.equals(1)) GlobalVariances.Click_xxplicit = "Explicit";
            for (Pair<String, Map<String, Double>> cli : clicks)
            {
                GlobalVariances.SetPara_ClickModel(cli.getValue(), cli.getKey());
                try
                {
                    System.out.println(GlobalVariances.Click_xxplicit + "_" + cli.getKey());
                    FileWriter fw = new FileWriter(
                            new File(fold + GlobalVariances.Click_xxplicit + "_" + cli.getKey() + ".txt"));
                    fw.write(GlobalVariances.Click_xxplicit + "_" + cli.getKey() + "\r\n");
                    for (Integer pid = 0; pid < paras.length; ++pid)
                    {
                        Double all_ndcg_sum = 0.0;
                        Double all_cdndcg_sum = 0.0;
                        for (Integer sid = 0; sid < suf.length; ++sid)
                        {
                            GlobalVariances.ColdStart_SC_Adjust_coeff_neg = paras[pid];
                            Double ndcg_sum = 0.0;
                            Double cdndcg_sum = 0.0;
                            for (Integer rp = 1; rp <= rpt; ++rp)
                            {
                                Pair<Double, Double> ret = DoExp(suf[sid]);
                                ndcg_sum += ret.getKey();
                                cdndcg_sum += ret.getValue();
                            }
                            ndcg_sum /= (double) rpt;
                            cdndcg_sum /= (double) rpt;
                            fw.write(suf[sid] + " : " + paras[pid] + "   (" + decifm.format(
                                    ndcg_sum) + ", " + decifm.format(cdndcg_sum) + ")\r\n");

                            all_ndcg_sum += ndcg_sum;
                            all_cdndcg_sum += cdndcg_sum;
                        }
                        all_cdndcg_sum /= (double) suf.length;
                        all_ndcg_sum /= (double) suf.length;
                        fw.write("All" + " : " + paras[pid] + "   (" + decifm.format(
                                all_ndcg_sum) + ", " + decifm.format(all_cdndcg_sum) + ")\r\n");
                    }
                    fw.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void ParaStu_Bound_Study(String paraname)
    {
        System.out.println(paraname);
        String fold = "./datas/Experiments/ParaStduy/" + paraname + "/";
        fileModel.CreateFolder(fold);
        List<Pair<String, Map<String, Double>>> clicks = new ArrayList<>(); clicks.clear();
        clicks.add(new Pair<>("per", per));
        clicks.add(new Pair<>("bin", bin));
        clicks.add(new Pair<>("rnd", rnd));
        String[] suf = {"11b", "21o", "22b", "rs1b", "rs2b", "rs3b", "rs4b", "rs5b", "21b"};
        for(Integer click_plicit = 0; click_plicit < 2; ++ click_plicit)
        {
            //if (mid.equals(0) && click_plicit.equals(0)) continue;
            Pair<String, Map<String, Double>> cli = null;
            if (click_plicit.equals(0)) { GlobalVariances.Click_xxplicit = "Implicit"; cli = new Pair<>("bin", bin); }
            if (click_plicit.equals(1)) { GlobalVariances.Click_xxplicit = "Explicit"; cli = new Pair<>("rnd", rnd); }
            GlobalVariances.SetPara_ClickModel(cli.getValue(), cli.getKey());
            try
            {
                System.out.println(GlobalVariances.Click_xxplicit + "_" + cli.getKey());
                FileWriter fw = new FileWriter(new File(fold + GlobalVariances.Click_xxplicit + "_" + cli.getKey() + ".txt"));
                fw.write(GlobalVariances.Click_xxplicit + "_" + cli.getKey() + "\r\n");
                for(Integer lb = 1; lb <= 9; ++ lb)
                    for(Integer rb = lb; rb <= 9; ++ rb)
                    {
                        Double lower_b = (double)lb / 10.0;
                        Double upper_b = (double)rb / 10.0;
                        Double all_ndcg_sum = 0.0;
                        Double all_cdndcg_sum = 0.0;
                        for (Integer sid = 0; sid < suf.length; ++sid)
                        {
                            GlobalVariances.Interleave_epsilon_lower = lower_b;
                            GlobalVariances.Interleave_epsilon_upper = upper_b;
                            Double ndcg_sum = 0.0;
                            Double cdndcg_sum = 0.0;
                            for (Integer rp = 1; rp <= rpt; ++rp)
                            {
                                Pair<Double, Double> ret = DoExp(suf[sid]);
                                ndcg_sum += ret.getKey();
                                cdndcg_sum += ret.getValue();
                            }
                            ndcg_sum /= (double) rpt;
                            cdndcg_sum /= (double) rpt;
                            fw.write(suf[sid] + " : [" + lb.toString() + ", " + rb.toString() + "]   (" + decifm.format(
                                    ndcg_sum) + ", " + decifm.format(cdndcg_sum) + ")\r\n");

                            all_ndcg_sum += ndcg_sum;
                            all_cdndcg_sum += cdndcg_sum;
                        }
                        all_cdndcg_sum /= (double)suf.length;
                        all_ndcg_sum /= (double)suf.length;
                        fw.write("All" + " : [" + lb.toString() + ", " + rb.toString() + "]   (" + decifm.format(
                                all_ndcg_sum) + ", " + decifm.format(all_cdndcg_sum) + ")\r\n");
                    }
                fw.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void ParaStu_Bound_Test(String paraname)
    {
        System.out.println(paraname);
        String fold = "./datas/Experiments/ParaStduy/" + paraname + "/";
        fileModel.CreateFolder(fold);
        List<Pair<String, Map<String, Double>>> clicks = new ArrayList<>(); clicks.clear();
        clicks.add(new Pair<>("per", per));
        clicks.add(new Pair<>("bin", bin));
        clicks.add(new Pair<>("rnd", rnd));
        String[] suf = {"11b", "21o", "22b", "rs1b", "rs2b", "rs3b", "rs4b", "rs5b", "21b"};
        for(Integer click_plicit = 0; click_plicit < 2; ++ click_plicit)
        {
            //if (mid.equals(0) && click_plicit.equals(0)) continue;
            if (click_plicit.equals(0)) GlobalVariances.Click_xxplicit = "Implicit";
            if (click_plicit.equals(1)) GlobalVariances.Click_xxplicit = "Explicit";
            for (Pair<String, Map<String, Double>> cli : clicks)
            {
                GlobalVariances.SetPara_ClickModel(cli.getValue(), cli.getKey());
                try
                {
                    System.out.println(GlobalVariances.Click_xxplicit + "_" + cli.getKey());
                    FileWriter fw = new FileWriter(new File(fold + GlobalVariances.Click_xxplicit + "_" + cli.getKey() + ".txt"));
                    fw.write(GlobalVariances.Click_xxplicit + "_" + cli.getKey() + "\r\n");
                    for(Integer lb = 1; lb <= 9; ++ lb)
                        for(Integer rb = lb; rb <= 9; ++ rb)
                        {
                            Double lower_b = (double)lb / 10.0;
                            Double upper_b = (double)rb / 10.0;
                            Double all_ndcg_sum = 0.0;
                            Double all_cdndcg_sum = 0.0;
                            for (Integer sid = 0; sid < suf.length; ++sid)
                            {
                                GlobalVariances.Interleave_epsilon_lower = lower_b;
                                GlobalVariances.Interleave_epsilon_upper = upper_b;
                                Double ndcg_sum = 0.0;
                                Double cdndcg_sum = 0.0;
                                for (Integer rp = 1; rp <= rpt; ++rp)
                                {
                                    Pair<Double, Double> ret = DoExp(suf[sid]);
                                    ndcg_sum += ret.getKey();
                                    cdndcg_sum += ret.getValue();
                                }
                                ndcg_sum /= (double) rpt;
                                cdndcg_sum /= (double) rpt;
                                fw.write(suf[sid] + " : [" + lb.toString() + ", " + rb.toString() + "]   (" + decifm.format(
                                        ndcg_sum) + ", " + decifm.format(cdndcg_sum) + ")\r\n");

                                all_ndcg_sum += ndcg_sum;
                                all_cdndcg_sum += cdndcg_sum;
                            }
                            all_cdndcg_sum /= (double)suf.length;
                            all_ndcg_sum /= (double)suf.length;
                            fw.write("All" + " : [" + lb.toString() + ", " + rb.toString() + "]   (" + decifm.format(
                                    all_ndcg_sum) + ", " + decifm.format(all_cdndcg_sum) + ")\r\n");
                        }
                    fw.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }

            }
        }
    }

    public static void ParaStu_Lambda_Study(String paraname)
    {
        System.out.println(paraname);
        Double[] paras = {0.01, 0.02, 0.05, 0.07, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
        String fold = "./datas/Experiments/ParaStduy/" + paraname + "/";
        fileModel.CreateFolder(fold);
        List<Pair<String, Map<String, Double>>> clicks = new ArrayList<>(); clicks.clear();
        clicks.add(new Pair<>("per", per));
        clicks.add(new Pair<>("bin", bin));
        clicks.add(new Pair<>("rnd", rnd));
        String[] suf = {"11b", "21o", "22b", "rs1b", "rs2b", "rs3b", "rs4b", "rs5b", "21b"};
        for(Integer click_plicit = 0; click_plicit < 2; ++ click_plicit)
        {
            //if (mid.equals(0) && click_plicit.equals(0)) continue;
            Pair<String, Map<String, Double>> cli = null;
            if (click_plicit.equals(0)) { GlobalVariances.Click_xxplicit = "Implicit"; cli = new Pair<>("bin", bin); }
            if (click_plicit.equals(1)) { GlobalVariances.Click_xxplicit = "Explicit"; cli = new Pair<>("rnd", rnd); }
            GlobalVariances.SetPara_ClickModel(cli.getValue(), cli.getKey());
            try
            {
                System.out.println(GlobalVariances.Click_xxplicit + "_" + cli.getKey());
                FileWriter fw = new FileWriter(new File(fold + GlobalVariances.Click_xxplicit + "_" + cli.getKey() + ".txt"));
                fw.write(GlobalVariances.Click_xxplicit + "_" + cli.getKey() + "\r\n");
                for(Integer pid = 0; pid < paras.length; ++ pid)
                {
                    Double all_ndcg_sum = 0.0;
                    Double all_cdndcg_sum = 0.0;
                    for (Integer sid = 0; sid < suf.length; ++sid)
                    {
                        GlobalVariances.Interleave_lambda = paras[pid];
                        Double ndcg_sum = 0.0;
                        Double cdndcg_sum = 0.0;
                        for (Integer rp = 1; rp <= rpt; ++rp)
                        {
                            Pair<Double, Double> ret = DoExp(suf[sid]);
                            ndcg_sum += ret.getKey();
                            cdndcg_sum += ret.getValue();
                        }
                        ndcg_sum /= (double) rpt;
                        cdndcg_sum /= (double) rpt;
                        fw.write(suf[sid] + " : " + paras[pid] + "   (" + decifm.format(
                                ndcg_sum) + ", " + decifm.format(cdndcg_sum) + ")\r\n");

                        all_ndcg_sum += ndcg_sum;
                        all_cdndcg_sum += cdndcg_sum;
                    }
                    all_cdndcg_sum /= (double)suf.length;
                    all_ndcg_sum /= (double)suf.length;
                    fw.write("All" + " : " + paras[pid] + "   (" + decifm.format(
                            all_ndcg_sum) + ", " + decifm.format(all_cdndcg_sum) + ")\r\n");
                }
                fw.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void ParaStu_Lambda_Test(String paraname)
    {
        System.out.println(paraname);
        Double[] paras = {0.01, 0.02, 0.05, 0.07, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
        String fold = "./datas/Experiments/ParaStduy/" + paraname + "/";
        fileModel.CreateFolder(fold);
        List<Pair<String, Map<String, Double>>> clicks = new ArrayList<>(); clicks.clear();
        clicks.add(new Pair<>("per", per));
        clicks.add(new Pair<>("bin", bin));
        clicks.add(new Pair<>("rnd", rnd));
        String[] suf = {"11b", "21o", "22b", "rs1b", "rs2b", "rs3b", "rs4b", "rs5b", "21b"};
        for(Integer click_plicit = 0; click_plicit < 2; ++ click_plicit)
        {
            //if (mid.equals(0) && click_plicit.equals(0)) continue;
            if (click_plicit.equals(0)) GlobalVariances.Click_xxplicit = "Implicit";
            if (click_plicit.equals(1)) GlobalVariances.Click_xxplicit = "Explicit";
            for (Pair<String, Map<String, Double>> cli : clicks)
            {
                GlobalVariances.SetPara_ClickModel(cli.getValue(), cli.getKey());
                try
                {
                    System.out.println(GlobalVariances.Click_xxplicit + "_" + cli.getKey());
                    FileWriter fw = new FileWriter(new File(fold + GlobalVariances.Click_xxplicit + "_" + cli.getKey() + ".txt"));
                    fw.write(GlobalVariances.Click_xxplicit + "_" + cli.getKey() + "\r\n");
                    for(Integer pid = 0; pid < paras.length; ++ pid)
                    {
                        Double all_ndcg_sum = 0.0;
                        Double all_cdndcg_sum = 0.0;
                        for (Integer sid = 0; sid < suf.length; ++sid)
                        {
                            GlobalVariances.Interleave_lambda = paras[pid];
                            Double ndcg_sum = 0.0;
                            Double cdndcg_sum = 0.0;
                            for (Integer rp = 1; rp <= rpt; ++rp)
                            {
                                Pair<Double, Double> ret = DoExp(suf[sid]);
                                ndcg_sum += ret.getKey();
                                cdndcg_sum += ret.getValue();
                            }
                            ndcg_sum /= (double) rpt;
                            cdndcg_sum /= (double) rpt;
                            fw.write(suf[sid] + " : " + paras[pid] + "   (" + decifm.format(
                                    ndcg_sum) + ", " + decifm.format(cdndcg_sum) + ")\r\n");

                            all_ndcg_sum += ndcg_sum;
                            all_cdndcg_sum += cdndcg_sum;
                        }
                        all_cdndcg_sum /= (double)suf.length;
                        all_ndcg_sum /= (double)suf.length;
                        fw.write("All" + " : " + paras[pid] + "   (" + decifm.format(
                                all_ndcg_sum) + ", " + decifm.format(all_cdndcg_sum) + ")\r\n");
                    }
                    fw.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }

            }
        }
    }

    public static void initialize()
    {
        GlobalVariances.Interleave_epsilon_upper = 0.6;
        GlobalVariances.Interleave_epsilon_lower = 0.1;
        GlobalVariances.ColdStart_SC_Adjust_coeff_neg = 0.01;
        GlobalVariances.Interleave_lambda = 0.6;
    }

    public static void Study(String database)
    {
        pergp = 4; in_prefix = "./datas/Semantic/ParaStudy/";
        //ValiDataGen.main(database, pergp);
        initialize(); ParaStu_Alpha_Study("Alpha");
        initialize(); ParaStu_Bound_Study("Bound");
        initialize(); ParaStu_Lambda_Study("Lambda");
    }

    public static void Test()
    {
        pergp = 10; in_prefix = "./datas/Semantic/";
        initialize(); ParaStu_Alpha_Test("Alpha");
        initialize(); ParaStu_Bound_Test("Bound");
        initialize(); ParaStu_Lambda_Test("Lambda");
    }

    public static void Entry(String database)
    {
        if (GlobalVariances.Database_name.length() <= 1)
        {
            GlobalVariances.SetPara_Database_name(database);
            MainEntry.Entry.initializeKG();
        }

        //Study(database);
        Test();
    }
}
