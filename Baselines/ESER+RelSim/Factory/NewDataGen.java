package Factory;

import Structures.graphModel;
import Structures.typeModel;
import javafx.util.Pair;

import java.io.*;
import java.util.*;

public class NewDataGen
{
    public static final String in_prefix = "./datas/Semantic/";
    public static final Integer find_lower_bound = 15;
    public static final Integer find_upper_bound = 70;
    public static final Integer find_limit = 200;

    public static Set<Integer> haswalked = new HashSet<>();

    public static void DFS(Integer now, Integer dep, List<Integer> path, Map<Integer, Integer> count)
    {
        if(dep >= path.size())
        {
            if(!count.containsKey(now)) count.put(now, 0);
            Integer cnt = count.get(now);
            count.put(now, cnt + 1);
            return;
        }
        Integer rela = path.get(dep);
        HashMap< Integer, List<Integer> > neigh = graphModel.getNeighbours(now);
        if(null == neigh || !neigh.containsKey(rela)) return;
        haswalked.add(now);
        for(Integer node : neigh.get(rela))
        {
            if(haswalked.contains(node)) continue;
            DFS(node, dep + 1, path, count);
        }
        haswalked.remove(now);
    }

    public static Integer stoi(String s)
    {
        Integer val = 1;
        if(s.startsWith("-")) {val = -1; s = s.substring(1);}
        return val * Integer.parseInt(s);
    }

    public static void SolveCase(String name, List<Integer> centers, List< List<Integer> > paths)
    {
        try
        {
            FileWriter fw = new FileWriter(new File(in_prefix + name + ".txt"));
            for(Integer center : centers)
            {
                fw.write(center.toString() + "\r\n");
                Map<Integer, Integer> count = new HashMap<>(); count.clear();
                Set<Integer> ansnodes = new HashSet<>(); ansnodes.clear();
                haswalked.clear();
                DFS(center, 0, paths.get(0), count);
                ansnodes.addAll(count.keySet());
                for(Integer pathid = 1; pathid < paths.size(); ++ pathid)
                {
                    Map<Integer, Integer> tmpcnt = new HashMap<>(); tmpcnt.clear();
                    haswalked.clear();
                    DFS(center, 0, paths.get(pathid), tmpcnt);
                    for(Integer node : tmpcnt.keySet())
                    {
                        if(!ansnodes.contains(node)) continue;
                        Integer tmp = count.get(node);
                        count.put(node, tmp + tmpcnt.get(node));
                    }
                    List<Integer> dels = new ArrayList<>(); dels.clear();
                    for(Integer node : ansnodes) if(!tmpcnt.containsKey(node)) dels.add(node);
                    for(Integer node : dels) ansnodes.remove(node);
                }
                for(Integer node : ansnodes)
                {
                    fw.write(node.toString() + ":");
                    if(name.endsWith("b")) fw.write("1"); else fw.write(count.get(node).toString());
                    fw.write("\t");
                }
                fw.write("\r\n");
            }
            fw.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static List<Integer> FindCenters(
            List< List<Integer> > paths,
            Integer need
    ){
        List<Integer> ret = new ArrayList<>(); ret.clear();

        Map<Integer, Integer> cnt = new HashMap<>(); cnt.clear();

        for(Integer center = 1; center <= typeModel.EntityCnt; ++ center)
        {
            Map<Integer, Integer> count = new HashMap<>(); count.clear();
            Set<Integer> ansnodes = new HashSet<>(); ansnodes.clear();
            haswalked.clear();
            DFS(center, 0, paths.get(0), count);
            ansnodes.addAll(count.keySet());
            for(Integer pathid = 1; pathid < paths.size(); ++ pathid)
            {
                Map<Integer, Integer> tmpcnt = new HashMap<>(); tmpcnt.clear();
                haswalked.clear();
                DFS(center, 0, paths.get(pathid), tmpcnt);
                for(Integer node : tmpcnt.keySet())
                {
                    if(!ansnodes.contains(node)) continue;
                    Integer tmp = count.get(node);
                    count.put(node, tmp + tmpcnt.get(node));
                }
                List<Integer> dels = new ArrayList<>(); dels.clear();
                for(Integer node : ansnodes) if(!tmpcnt.containsKey(node)) dels.add(node);
                for(Integer node : dels) ansnodes.remove(node);
            }
            if(ansnodes.size() >= find_lower_bound && ansnodes.size() <= find_upper_bound) ret.add(center); else cnt.put(center, ansnodes.size());
            if(ret.size() >= need) break;
        }
        if(ret.size() < need)
        {
            System.out.println(paths.toString());
            System.out.println(ret.size());
            List<Map.Entry<Integer, Integer>> list = new ArrayList<Map.Entry<Integer, Integer>>(cnt.entrySet());
            Collections.sort(list,new Comparator<Map.Entry<Integer, Integer>>() {
                public int compare(Map.Entry<Integer, Integer> o1,
                                   Map.Entry<Integer, Integer> o2) {
                    return o2.getValue().compareTo(o1.getValue());
                }
            });
            for(Map.Entry<Integer, Integer> p : list)
            {
                if(p.getValue() >= find_limit) continue;
                ret.add(p.getKey());
                if(ret.size() >= need) break;
            }
        }
        return ret;
    }

    public static void main(String database, Integer num_per_group)
    {
        try
        {
            Scanner sc = new Scanner(new FileInputStream( new File(in_prefix + database + ".txt")));
            while(sc.hasNextLine())
            {
                String suffix = sc.nextLine();
                String[] info1 = sc.nextLine().split("=");
                List< List<Integer> > paths = new ArrayList<>();
                for(Integer id = 0; id < info1.length; ++ id)
                {
                    List<Integer> rp = new ArrayList<>(); rp.clear();
                    String[] relas = info1[id].split(",");
                    for(Integer i = 0; i < relas.length; ++ i) rp.add(stoi(relas[i]));
                    paths.add(rp);
                }
                List<Integer> centers = new ArrayList<>(); centers.clear();
                if(suffix.startsWith("rs")) centers = FindCenters(paths, num_per_group);
                else
                {
                    String[] cens = sc.nextLine().split(",");
                    for(Integer i = 0; i < cens.length; ++ i) centers.add(stoi(cens[i]));
                }
                if(suffix.startsWith("rs"))
                {
                    SolveCase(database + "_" + suffix + "b", centers, paths);
                    SolveCase(database + "_" + suffix + "o", centers, paths);
                }else SolveCase(database + "_" + suffix, centers, paths);
            }
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }
}
