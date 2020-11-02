package Evalutaion;

import Framework.FeatureGenerator;
import JDBCUtils.JdbcUtil;
import javafx.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class alpha_nDCG
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

    public static Pair< List<List<Integer>>, Map<Integer, Map<Integer, Integer>> > data;

    public static final Double alpha = 0.5;
    public static final Double eps = 0.00001;
    public static Double MaxAns = 0.0;
    public static List<Integer> MaxList = new ArrayList<>();
    public static List<Double> MP_Scores = new ArrayList<>();
    public static Double[] logs = new Double[21];

    public static void Init(Integer center)
    {
        FeatureGenerator FG = new FeatureGenerator(); FG.clear();

        data = FG.getFeatures(center, PathLen);

        // Initialize
        for(Integer i = 1; i <= 20; ++ i) logs[i] = Math.log((double)i + 1.0);
        List<Integer> ent_list = new ArrayList<>(data.getValue().keySet());
        MP_Scores.clear();
        for(Integer i = 0; i < data.getKey().size(); ++ i) MP_Scores.add(0.0);
        MaxAns = 0.0; MaxList.clear();

        // Solve Max
        for(Integer num = 1; num <= topK; ++ num)
        {
            Collections.shuffle(ent_list);
            Double ma = -1.0;
            Integer am = -1;
            for(Integer id : ent_list)
            {
                if(MaxList.contains(id)) continue;
                Double score = 0.0;
                for(Integer intent : data.getValue().get(id).keySet()) score += Math.pow(1.0 - alpha, MP_Scores.get(intent));
                if(score > ma + eps) {ma = score; am = id;}
            }
            MaxList.add(am);
            for(Integer intent : data.getValue().get(am).keySet()) MP_Scores.set(intent, MP_Scores.get(intent) + 1.0);
            MaxAns += ma / logs[num];
        }
        //System.out.println(MaxList.toString());
    }

    public static Double Cal_List(List<Integer> ents, Integer topk)
    {
        Map<Integer, Double> sco = new HashMap<>(); sco.clear();
        Double ret = 0.0;
        for(Integer id = 1; id <= topk; ++ id)
        {
            Integer ent = ents.get(id - 1);
            Double sum = 0.0;
            if(!data.getValue().containsKey(ent)) continue;
            for (Integer intent : data.getValue().get(ent).keySet())
            {
                if (!sco.containsKey(intent)) sco.put(intent, 0.0);
                Double val = sco.get(intent);
                sum += Math.pow(1.0 - alpha, val);
                sco.put(intent, val + 1.0);
            }
            ret += sum / logs[id];
        }
        return ret;
    }

    public static Double Calculate(List<Integer> ents, Integer topk)
    {
        return Cal_List(ents, topk) / Cal_List(MaxList, topk);
    }
}
