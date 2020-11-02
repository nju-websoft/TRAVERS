package Evalutaion;

import JDBCUtils.JdbcUtil;
import Structures.GlobalVariances;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class EvalTools
{
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
            //clickid = properties.getProperty("click_id");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void Initialize(List<Map<String, Double>> list)
    {
        list.clear();
        for (Integer i = 0; i <= Iterations; ++ i)
        {
            Map<String, Double> rec = new HashMap<>(); rec.clear();
            for(String name : GlobalVariances.Evaluation) rec.put(name, 0.0);
            list.add(rec);
        }
    }

    public static Map<String, Double> CombineMap(Map<String, Double> A, Map<String, Double> B, Double coef)
    {
        Map<String, Double> ret = new HashMap<>(); ret.clear();
        for(String name : A.keySet())
            ret.put(name, A.get(name) + B.get(name) * coef);
        return ret;
    }

    public static void Combine(List<Map<String, Double>> sta, List<Map<String, Double>> dat, Double coef) // combine dat information to sta
    {
        for(Integer i = 0; i <= Iterations; ++ i)
            sta.set(i, CombineMap(sta.get(i), dat.get(i), coef));
    }
}
