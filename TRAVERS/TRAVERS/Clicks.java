package TRAVERS;

import JDBCUtils.JdbcUtil;
import Structures.GlobalVariances;
import javafx.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Clicks
{

    public static Double click_p = 0.0;
    public static Double click_n = 0.0;
    public static Double stop_p = 0.0;
    public static Double stop_n = 0.0;

    public static Double eta = 0.0;
    public static Double c_right = 0.0;
    public static Double c_wrong = 0.0;

    public static Map<String, Double> ModelGen_1(Double cp, Double cn, Double sp, Double sn)
    {
        Map<String, Double> ret = new HashMap<>(); ret.clear();
        ret.put("cp", cp); ret.put("cn", cn); ret.put("sp", sp); ret.put("sn", sn);
        return ret;
    }

    public static Map<String, Double> ModelGen_2(Double et, Double cr, Double cw)
    {
        Map<String, Double> ret = new HashMap<>(); ret.clear();
        ret.put("eta", et); ret.put("c_right", cr); ret.put("c_wrong", cw);
        return ret;
    }

    public static void LoadPara_1()
    {
        click_p = GlobalVariances.ClickModel.get("cp");
        click_n = GlobalVariances.ClickModel.get("cn");
        stop_p = GlobalVariances.ClickModel.get("sp");
        stop_n = GlobalVariances.ClickModel.get("sn");
    }

    public static void LoadPara_2()
    {
        eta = GlobalVariances.ClickModel.get("eta");
        c_right = GlobalVariances.ClickModel.get("c_right");
        c_wrong = GlobalVariances.ClickModel.get("c_wrong");
    }

    public static void LoadPara() { LoadPara_2(); }

    //------------------------------------------------------------------------------------------------------------------

    public static Pair<List<Integer>, List<Integer>> label(
            Pair< List<Integer>, Integer > ret,
            List<Integer> ans,
            Set<Integer> has_labeled
    )
    {
        List<Integer> pos = new ArrayList<>(); pos.clear();
        List<Integer> neg = new ArrayList<>(); neg.clear();
        List<Integer> list = (List<Integer>) ((ArrayList<Integer>)ret.getKey()).clone();

        Set<Integer> A = new HashSet<>(); A.clear(); A.addAll(ans);
        for(Integer node : list) if(!A.contains(node) && !has_labeled.contains(node)) {neg.add(node); break;}
        Collections.reverse(list);
        for(Integer node : list) if(A.contains(node) && !has_labeled.contains(node)) {pos.add(node); break;}
        if(!has_labeled.contains(ret.getValue()))
        {
            if(A.contains(ret.getValue())) pos.add(ret.getValue());
            else neg.add(ret.getValue());
        }
        if(pos.size() <= 0)
        {
            Collections.shuffle(ans);
            for(Integer node : ans) if(!has_labeled.contains(ans.get(node))){pos.add(node); break;}
        }
        has_labeled.addAll(pos); has_labeled.addAll(neg);

        return new Pair(pos, neg);
    }

    //------------------------------------------------------------------------------------------------------------------

    public static Pair< Set<Integer>, Set<Integer> > List2Set(Pair< List<Integer>, List<Integer> > ret)
    {
        Set<Integer> pos_c = new HashSet<>(); pos_c.clear();
        Set<Integer> neg_c = new HashSet<>(); neg_c.clear();
        pos_c.addAll(ret.getKey()); neg_c.addAll(ret.getValue());
        return new Pair<>(pos_c, neg_c);
    }

    public static Pair<Set<Integer>, Set<Integer>> ClickModel(Map<Integer, Integer> STD, List<Integer> list)
    {
        if(GlobalVariances.Click_xxplicit.equals("Explicit")) return List2Set(list_ClickModel_Explicit(STD, list));
        else return List2Set(list_ClickModel_Implicit(STD, list));
    }

    public static Pair<List<Integer>, List<Integer>> ClickModel_List(Map<Integer, Integer> STD, List<Integer> list)
    {
        if(GlobalVariances.Click_xxplicit.equals("Explicit")) return list_ClickModel_Explicit(STD, list);
        else return list_ClickModel_Implicit(STD, list);
    }

    public static Pair<Set<Integer>, Set<Integer>> ClickModel_Perfect(Map<Integer, Integer> STD, List<Integer> list)
    {
        return List2Set(list_ClickModel_Perfect(STD, list));
    }

    public static Pair<List<Integer>, List<Integer>> list_ClickModel_Explicit(Map<Integer, Integer> STD, List<Integer> list)
    {
        LoadPara();
        List<Integer> pos_c = new ArrayList<>(); pos_c.clear();
        List<Integer> neg_c = new ArrayList<>(); neg_c.clear();
        Random rd = new Random();
        Integer cnt = 0;
        for(Integer id = 1; id <= list.size(); ++ id)
        {
            double ob = rd.nextDouble();
            if(ob <= Math.pow(1.0 / (double)id, eta))
            {
                cnt ++;
                Integer node = list.get(id - 1);
                if(STD.containsKey(node)) //Right
                {
                    double poss = rd.nextDouble();
                    if(poss <= c_right) if(node > 0) pos_c.add(node);
                    else if(node > 0) neg_c.add(node);
                }else // Wrong
                {
                    double poss = rd.nextDouble();
                    if(poss <= c_right) if(node > 0) neg_c.add(node);
                    else if(node > 0) pos_c.add(node);
                }
            }
        }
        //System.out.println("Observed : " + cnt.toString());
        pos_c.remove(GlobalVariances.Exp_Current_Center);
        neg_c.remove(GlobalVariances.Exp_Current_Center);
        return new Pair<>(pos_c, neg_c);
    }

    public static Pair<List<Integer>, List<Integer>> list_ClickModel_Perfect(Map<Integer, Integer> STD, List<Integer> list)
    {
        LoadPara();
        List<Integer> pos_c = new ArrayList<>(); pos_c.clear();
        List<Integer> neg_c = new ArrayList<>(); neg_c.clear();
        for(Integer node : list)
        {
            if(STD.containsKey(node)) //Right
            {
                if(node > 0) pos_c.add(node);
            }else // Wrong
            {
                if(node > 0) neg_c.add(node);
            }
        }
        pos_c.remove(GlobalVariances.Exp_Current_Center);
        neg_c.remove(GlobalVariances.Exp_Current_Center);
        return new Pair<>(pos_c, neg_c);
    }

    public static Pair<List<Integer>, List<Integer>> list_ClickModel_Implicit(Map<Integer, Integer> STD, List<Integer> list)
    {
        LoadPara();
        List<Integer> pos_c = new ArrayList<>(); pos_c.clear();
        List<Integer> neg_c = new ArrayList<>(); neg_c.clear();
        Random rd = new Random();
        Boolean flag = false;
        for(Integer id = list.size(); id >= 1; -- id)
        {
            Boolean click = false;
            double ob = rd.nextDouble();
            if(ob <= Math.pow(1.0 / (double)id, eta))
            {
                Integer node = list.get(id - 1);
                if(STD.containsKey(node)) //Right
                {
                    double poss = rd.nextDouble();
                    if(poss <= c_right) click = true;
                }else // Wrong
                {
                    double poss = rd.nextDouble();
                    if(poss <= c_wrong) click = true;
                }
            }
            if(click)
            {
                if(list.get(id - 1) > 0) pos_c.add(list.get(id - 1));
                if(!flag && id < list.size()) if(list.get(id) > 0) neg_c.add(list.get(id));
                flag = true;
            }else if(flag) if(list.get(id - 1) > 0) neg_c.add(list.get(id - 1));
        }
        if(pos_c.size() <= 0 && neg_c.size() <= 0)
        {
            for(Integer id = list.size(); id >= 1; -- id)
            {
                double ob = rd.nextDouble();
                if(ob <= Math.pow(1.0 / (double)id, eta)) if(list.get(id - 1) > 0) neg_c.add(list.get(id - 1));
            }
        }
        //System.out.println("Observed : " + cnt.toString());
        pos_c.remove(GlobalVariances.Exp_Current_Center);
        neg_c.remove(GlobalVariances.Exp_Current_Center);
        return new Pair<>(pos_c, neg_c);
    }
}
