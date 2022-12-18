package Main;

import javafx.util.Pair;

import java.util.*;

public class TimeCounter
{

    public static Map<String, Double>[] counter = new HashMap[505];
    public static Integer cnt;

    public static void clear()
    {
        cnt = 0;
        for(Integer i = 0; i < 505; ++ i) counter[i] = new HashMap<>();
    }

    public static void addTime(String name, double val)
    {
        if(!counter[cnt].containsKey(name)) counter[cnt].put(name, 0.0);
        double tmpval = counter[cnt].get(name);
        counter[cnt].put(name, tmpval + val);
    }

    public static void addInstance() {cnt ++;}

    public static void printMap()
    {
        Set<String> names = counter[1].keySet();
        for(String nm : names)
        {
            double sum = 0;
            for(Integer i = 1; i <= cnt; ++ i) sum += counter[i].get(nm);
            sum /= (double) cnt;
            System.out.println(nm + " : " + sum / 1000.0 + " s");
        }
    }

    public static List<Pair<String, Double>> getStatistics_num()
    {
        List<Pair<String, Double>> ret = new ArrayList<>();
        ret.clear();
        Set<String> names = counter[1].keySet();
        Integer max_lable = 0;
        for(String name : names) max_lable = Math.max(max_lable, Integer.parseInt(name));
        for (Integer id = 0; id <= max_lable; ++ id)
        {
            String nm = id.toString();
            double sum = 0;
            for (Integer i = 1; i <= cnt; ++i)
            {
                if(!counter[i].containsKey(nm))
                {
                    System.out.println("Error : " + i.toString());
                    System.out.println("KeySet : " + counter[i].keySet().toString());
                }
                sum += counter[i].get(nm);
            }
            sum /= (double) cnt;
            ret.add(new Pair<>(nm, sum));
        }
        return ret;
    }


}
