package Main;

import GraphData.RelationIndex;
import javafx.util.Pair;

import java.util.*;

public class FeatureGenerator
{
    public static Integer PathLen;

    public static Map<List<Integer>, Integer > RelationMap = new HashMap<>();
    public static List< List<Integer> > Relations = new ArrayList<>();
    public static Integer RelationCnt = 0;
    public static Map<Integer, Map<Integer, Integer>> Features = new HashMap<>();
    public static Set<Integer> walked = new HashSet<>();
    public static Map<Integer, List<Integer>> rela2node = new HashMap<>();

    public static void clear()
    {
        rela2node.clear();
        RelationMap.clear(); Relations.clear(); RelationCnt = 0;
        Features.clear();
        walked.clear();
    }

    public static void addFeature(Integer node, List<Integer> path)
    {
        if(!RelationMap.containsKey(path))
        {
            RelationMap.put(path, RelationCnt);
            RelationCnt ++;
            Relations.add(path);
        }
        Integer id = RelationMap.get(path);
        if(!Features.containsKey(node))
        {
            Map<Integer, Integer> tmp = new HashMap<>();
            Features.put(node, tmp);
        }
        Map<Integer, Integer> f = Features.get(node);
        if(!f.containsKey(id)) f.put(id, 0);
        Integer v = f.get(id); f.put(id, v + 1);
        Features.put(node, f);
    }

    public static void DFS(Integer now, Integer len, List<Integer> path)
    {
        addFeature(now, (List<Integer>) ((ArrayList<Integer>)path).clone());
        if(len >= PathLen) return;
        Map<Integer, List<Integer>> outnodes = RelationIndex.map[now];
        if(null == outnodes) return;
        walked.add(now);
        for(Map.Entry<Integer, List<Integer>> kv : outnodes.entrySet())
        {
            List<Integer> newpath = (List<Integer>) ((ArrayList<Integer>)path).clone();
            newpath.add(kv.getKey());
            for(Integer nextnode : kv.getValue())
            {
                if(walked.contains(nextnode)) continue;
                DFS(nextnode, len + 1, newpath);
            }
        }
        walked.remove(now);
    }

    public static void findFeatures(Integer center)
    {
        //clear();
        List<Integer> relation = new ArrayList<>(); relation.clear();
        DFS(center, 0, relation);
    }

    public static Pair< List< List<Integer> >, Map<Integer, Map<Integer, Integer>> >
    getFeatures(Integer center, Integer pathlen)
    {
        clear(); PathLen = pathlen;
        findFeatures(center);
        Map<Integer, Map<Integer, Integer>> ret = new HashMap<>(); ret.clear();
        for(Map.Entry<Integer, Map<Integer, Integer>> kv : Features.entrySet())
        {
            Map<Integer, Integer> score = new HashMap<>(); score.clear();
            for(Map.Entry<Integer, Integer> p : kv.getValue().entrySet())
            {
                score.put(p.getKey(), p.getValue());
            }
            ret.put(kv.getKey(), score);
        }
        for(Map.Entry<Integer, Map<Integer, Integer>> kv : ret.entrySet())
        {
            Integer node = kv.getKey();
            for(Integer rela : kv.getValue().keySet())
            {
                if(!rela2node.containsKey(rela))
                {
                    List<Integer> tmp = new ArrayList<>(); tmp.clear();
                    rela2node.put(rela, tmp);
                }
                List<Integer> now = rela2node.get(rela);
                now.add(node); rela2node.put(rela, now);
            }
        }
        return new Pair(Relations, ret);
    }
}
