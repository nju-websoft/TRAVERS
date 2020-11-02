package TRAVERS;

import UserExp.Formats.PathFormat;
import Structures.graphModel;
import javafx.util.Pair;

import java.util.*;

public class FeatureGenerator
{
    public Integer PathLen;

    public Map<List<Integer>, Integer > RelationMap = new HashMap<>();
    public List< List<Integer> > Relations = new ArrayList<>();
    public Integer RelationCnt = 0;
    public Map<Integer, Map<Integer, Integer>> Features = new HashMap<>();
    public Set<Integer> walked = new HashSet<>();
    public Map<Integer, List<Integer>> rela2node = new HashMap<>();
    public Map<Integer, Map<Integer, List<PathFormat>>> samplePath = new HashMap<>();
    public Map<Integer, Map<Integer, Integer>> mapStatistics = new HashMap<>();
    public Set[] steps_set = new HashSet[5];
    public Integer[] steps_cnt = new Integer[5];

    public void clear()
    {
        rela2node.clear();
        RelationMap.clear(); Relations.clear(); RelationCnt = 0;
        Features.clear();
        walked.clear();
        for(Integer len = 1; len < 5; ++ len)
        {
            steps_set[len] = new HashSet(); steps_set[len].clear();
            steps_cnt[len] = 0;
        }
    }

    public void addFeature(Integer node, List<Integer> path)
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

    public void DFS(Integer now, Integer len, List<Integer> path)
    {
        if(len > 0)
        {
            addFeature(now, (List<Integer>) ((ArrayList<Integer>)path).clone());
            if(!steps_set[len].contains(now))
            {
                steps_set[len].add(now);
                steps_cnt[len] += graphModel.degreeCount(now);
            }
        }
        if(len >= PathLen) return;
        HashMap<Integer, List<Integer>> outnodes = graphModel.getNeighbours(now);
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

    public void findFeatures(Integer center)
    {
        //clear();
        List<Integer> relation = new ArrayList<>(); relation.clear();
        DFS(center, 0, relation);
    }

    public Pair< List< List<Integer> >, Map<Integer, Map<Integer, Integer>> >
    getFeatures(Integer center, Integer pathlen)
    {
        clear(); PathLen = pathlen;
        findFeatures(center);
        mapStatistics.clear();
        for(Map.Entry<Integer, Map<Integer, Integer>> kv : Features.entrySet())
        {
            Map<Integer, Integer> score = new HashMap<>(); score.clear();
            for(Map.Entry<Integer, Integer> p : kv.getValue().entrySet())
            {
                score.put(p.getKey(), p.getValue());
            }
            mapStatistics.put(kv.getKey(), score);
        }
        for(Map.Entry<Integer, Map<Integer, Integer>> kv : mapStatistics.entrySet())
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
        return new Pair(Relations, mapStatistics);
    }

    /*

    public Pair< List< List<Integer> >, Map<Integer, Map<Integer, Integer>> >
    getFeatures_withClass(Integer center, Integer pathlen)
    {
        clear(); PathLen = pathlen;
        findFeatures(center);
        mapStatistics.clear();
        for(Map.Entry<Integer, Map<Integer, Integer>> kv : Features.entrySet())
        {
            Map<Integer, Integer> score = new HashMap<>(); score.clear();
            for(Map.Entry<Integer, Integer> p : kv.getValue().entrySet())
            {
                score.put(p.getKey(), p.getValue());
            }
            mapStatistics.put(kv.getKey(), score);
        }

        Map<Integer, Integer> class2num = new HashMap<>(); class2num.clear();
        for(Integer node : mapStatistics.keySet())
            for(Integer cla : typeModel.getTypes(node))
                if(!class2num.containsKey(cla))
                {
                    List<Integer> tmp = new ArrayList<>(); tmp.clear();
                    tmp.add(cla + 1000000000); Relations.add(tmp);
                    class2num.put(cla, RelationCnt);
                    RelationCnt ++;
                }
        for(Integer node : mapStatistics.keySet())
        {
            Map<Integer, Integer> nodemap = mapStatistics.get(node);
            for(Integer cla : typeModel.getTypes(node))
                nodemap.put(class2num.get(cla + 1000000000), 1);
            mapStatistics.put(node, nodemap);
        }
        return new Pair(Relations, mapStatistics);
    }

    */

    public void path_Sample_DFS(Integer now, List<Integer> mp, Integer mpid, List<Integer> entities)
    {
        List<Integer> newent = (List<Integer>) ((ArrayList<Integer>)entities).clone();
        newent.add(now);
        if(newent.size() > mp.size())
        {
            if(!samplePath.containsKey(now))
            {
                Map<Integer, List<PathFormat>> tmp = new HashMap<>(); tmp.clear();
                samplePath.put(now, tmp);
            }
            Map<Integer, List<PathFormat>> spx = samplePath.get(now);
            if(!spx.containsKey(mpid))
            {
                List<PathFormat> tmp = new ArrayList<>();
                spx.put(mpid, tmp);
            }
            List<PathFormat> pfs = spx.get(mpid); pfs.add(new PathFormat(newent, mp));
            spx.put(mpid, pfs); samplePath.put(now, spx);
            return;
        }
        HashMap<Integer, List<Integer>> outnodes = graphModel.getNeighbours(now);
        if(null == outnodes) return;
        Integer rela = mp.get(entities.size());
        if(!outnodes.containsKey(rela)) return;
        walked.add(now);
        for(Integer nextnode : outnodes.get(rela))
        {
            if(walked.contains(nextnode)) continue;
            path_Sample_DFS(nextnode, mp, mpid, newent);
        }
        walked.remove(now);
    }

    public Map<Integer, Map<Integer, List<PathFormat>>> samplePaths(Integer center, List<List<Integer>> metapaths)
    {
        samplePath.clear();
        for(Integer id = 0; id < metapaths.size(); ++ id)
        {
            walked.clear();
            List<Integer> ent = new ArrayList<>(); ent.clear();
            path_Sample_DFS(center, metapaths.get(id), id, ent);
        }
        return samplePath;
    }

    public Double AvgDegOfLen(Integer len)
    {
        return (double)steps_cnt[len] / (double)steps_set[len].size();
    }
}
