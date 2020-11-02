package Structures;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Tools
{
    public static Set<Integer> SetUnion(Set<Integer> s1, Set<Integer> s2)
    {
        Set<Integer> ret = new HashSet<>(); ret.clear();
        ret.addAll(s1); ret.addAll(s2);
        return ret;
    }

    public static void SetUnion_Self(Set<Integer> s1, Set<Integer> s2)
    {
        s1.addAll(s2);
    }

    public static Set<Integer> SetIntersection(Set<Integer> s1, Set<Integer> s2)
    {
        Set<Integer> ret = new HashSet<>(); ret.clear();
        for(Integer v : s1) if(s2.contains(v)) ret.add(v);
        return ret;
    }

    public static void SetIntersection_Self(Set<Integer> s1, Set<Integer> s2)
    {
        Set<Integer> ret = new HashSet<>(); ret.clear();
        for(Integer v : s1) if(s2.contains(v)) ret.add(v);
        s1 = ret;
    }

    public static void DFS(Integer now, Integer len, List<Integer> path, Set<Integer> result, Set<Integer> walked)
    {
        if(len >= path.size())
        {
            result.add(now);
            return;
        }
        Integer rela = path.get(len);
        HashMap<Integer, List<Integer>> neigh = graphModel.getNeighbours(now);
        if(null == neigh || !neigh.containsKey(rela)) return;
        walked.add(now);
        for(Integer nxt : neigh.get(rela)) DFS(nxt, len + 1, path, result, walked);
        walked.remove(now);
    }

    public static Set<Integer> getNodesFromCenterAccordingPath(Integer center, List<Integer> path)
    {
        Set<Integer> result = new HashSet<>(); result.clear();
        Set<Integer> walked = new HashSet<>(); result.clear();
        DFS(center, 0, path, result, walked);
        return result;
    }
}
