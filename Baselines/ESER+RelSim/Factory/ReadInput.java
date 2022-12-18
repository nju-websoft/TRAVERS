package Factory;

import Structures.GlobalVariances;
import javafx.util.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

public class ReadInput
{

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
                ret.add(new Pair<>(cent, sortByValue(answer)));
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

}
