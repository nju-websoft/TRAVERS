package Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobalCounter
{
    public Map<String, List<Double>> REC = new HashMap<>();

    public void clear() {REC.clear();}

    public void ADD(String name, Double val)
    {
        if(!REC.containsKey(name))
        {
            List<Double> tmp = new ArrayList<>(); tmp.clear();
            REC.put(name, tmp);
        }
        List<Double> tmp = REC.get(name);
        tmp.add(val); REC.put(name, tmp);
    }

    public void Statistic()
    {
        for(Map.Entry<String, List<Double>> kv : REC.entrySet())
        {
            System.out.println(kv.getKey());
            Double ma = 0.0, mi = 100000.0, mean = 0.0, std = 0.0;
            for(Double v : kv.getValue())
            {
                ma = Math.max(ma, v);
                mi = Math.min(mi, v);
                mean += v;
            }
            mean /= (double)kv.getValue().size();
            for(Double v : kv.getValue()) std += Math.pow(v - mean, 2.0);
            std = Math.sqrt(std / (double)kv.getValue().size());

            System.out.println("    Max  : " + ma);
            System.out.println("    Min  : " + mi);
            System.out.println("    Mean : " + mean);
            System.out.println("    Std  : " + std);
        }
    }
}
