package Test;

import Structures.graphModel;

import java.util.*;

public class Counts
{
    public Set<Integer> visited = new HashSet<>();
    public Set<List<Integer>> rps = new HashSet<>();
    public Map<Integer, Set< List<Integer> >> info = new HashMap<>();

    public void DFS(Integer now, Integer s, Integer steps, List<Integer> path)
    {
        visited.add(now);
        if(s >= steps)
        {
            List<Integer> np = (List<Integer>) ((ArrayList<Integer>)path).clone();
            rps.add(np); visited.remove(now);
            if(!info.containsKey(now)) info.put(now, new HashSet<>());
            Set< List<Integer> > pts = info.get(now);
            pts.add(np); info.put(now, pts);
            return;
        }
        HashMap<Integer, List<Integer> > outnodes = graphModel.getNeighbours(now);
        if(null == outnodes) { visited.remove(now); return; }
        for(Map.Entry<Integer, List<Integer>> kv : outnodes.entrySet())
        {
            List<Integer> p = (List<Integer>) ((ArrayList<Integer>)path).clone();
            p.add(kv.getKey());
            for(Integer nd : kv.getValue())
            {
                if(visited.contains(nd)) continue;
                DFS(nd, s + 1, steps ,p);
            }
        }
        visited.remove(now);
    }

    public void solve()
    {
        while(true)
        {
            Scanner sc = new Scanner(System.in);
            Integer center = sc.nextInt();
            for(Integer step = 1; step <= 3; ++ step)
            {
                visited.clear(); rps.clear(); info.clear();
                List<Integer> beginpath = new ArrayList<>(); beginpath.clear();
                DFS(center, 0, step, beginpath);
                Integer ans = rps.size();
                Integer nodenum = info.size();
                Integer maxrp = 0;
                for(Map.Entry<Integer, Set< List<Integer> >> kv : info.entrySet()) maxrp = Math.max(kv.getValue().size(), maxrp);
                Integer maxrp2 = maxrp * (maxrp - 1) / 2;
                System.out.println(center.toString()
                        + " --- " + step.toString()
                        + " : " + ans.toString()
                        + " + " + nodenum.toString()
                        + " + " + maxrp.toString()
                        + " + " + maxrp2.toString()
                );
            }
        }
    }

}
