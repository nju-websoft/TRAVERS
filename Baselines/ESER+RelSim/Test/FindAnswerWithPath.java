package Test;

import Structures.graphModel;

import java.util.*;

public class FindAnswerWithPath
{

    public Set<Integer> wk = new HashSet<>();
    public Map<Integer, Integer> count = new HashMap<>();

    public void DFS(Integer now, Integer dep, List<Integer> path, Set<Integer> r)
    {
        if(dep >= path.size())
        {
            r.add(now);
            return;
        }
        HashMap<Integer, List<Integer> > nei = graphModel.getNeighbours(now);
        if(null == nei || !nei.containsKey(path.get(dep))) return;
        wk.add(now);
        for(Integer node : nei.get(path.get(dep)))
        {
            if(wk.contains(node)) continue;
            DFS(node, dep + 1, path, r);
        }
        wk.remove(now);
    }

    public List<Integer> FindAll(Integer cen, List<List<Integer>> path)
    {
        List<Integer> ret = new ArrayList<>(); ret.clear();
        for(List<Integer> pt : path)
        {
            Set<Integer> r = new HashSet<>(); r.clear();
            DFS(cen, 0, pt, r);
            for(Integer node : r)
            {
                if(!count.containsKey(node)) count.put(node, 0);
                Integer val = count.get(node);
                count.put(node, val + 1);
            }
        }
        for(Map.Entry<Integer, Integer> kv : count.entrySet())
            if(kv.getValue() >= path.size()) ret.add(kv.getKey());
        return ret;
    }

    public void Do()
    {
        while(true)
        {
            wk.clear();
            Scanner sc = new Scanner(System.in);
            Integer center = sc.nextInt();
            List< List<Integer> > path = new ArrayList<>(); path.clear();
            Integer num = sc.nextInt();
            for(Integer id = 0; id < num; ++ id)
            {
                Integer len = sc.nextInt();
                List<Integer> pt = new ArrayList<>(); pt.clear();
                for(Integer i = 0; i < len; ++ i)
                {
                    Integer x = sc.nextInt();
                    pt.add(x);
                }
                path.add(pt);
            }
            List<Integer> res = FindAll(center, path);
            System.out.println(res.size());
            if(res.size() > 0)
            {
                System.out.print(res.get(0) + ":1");
                for(Integer i = 1; i < res.size(); ++ i) System.out.print("\t" + res.get(i).toString() + ":1");
                System.out.println();
            }
        }
    }

}
