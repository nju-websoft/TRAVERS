package Experiment;

import Factory.FindPaths;
import Factory.ReadInput;
import Structures.metapathModel;
import javafx.util.Pair;

import java.util.*;

public class GetPathLabel
{
    public final Integer groupperfile = 10;
    public final String[] suf = {"11b", "21b", "21o", "22b"};

    public Set< List<Integer> > CombineSets(Set< List<Integer> > s1, Set< List<Integer> > s2)
    {
        Set< List<Integer> > ret = new HashSet<>(); ret.clear();
        if(s2.size() <= 0) ret.addAll(s1);
        if(s1.size() <= 0) ret.addAll(s2);
        for(List<Integer> rp : s2) if(s1.contains(rp)) ret.add(rp);
        return ret;
    }

    public Set< List<Integer> > Paths(Integer a, Integer b)
    {
        Set<metapathModel> mps = FindPaths.returnPath(a, b, 2);
        Set< List<Integer> > ret = new HashSet<>(); ret.clear();
        for(metapathModel mp : mps) ret.add(mp.relations);
        if(ret.size() <= 0 && a != b)
        {
            List<Integer> tmp = new ArrayList<>();
            tmp.clear(); tmp.add(1);
            ret.add(tmp);
        }
        return ret;
    }

    public Set< List<Integer> > GetFromOneGroup(Pair<Integer, Map<Integer, Integer>> SP)
    {
        Integer center = SP.getKey();
        List<Integer> nodes = new ArrayList<>();
        nodes.addAll(SP.getValue().keySet());
        Set< List<Integer> > ret = new HashSet<>();
        ret = Paths(center, nodes.get(0));
        for(Integer nodeid = 1; nodeid < nodes.size(); ++ nodeid) ret = CombineSets(ret, Paths(center, nodes.get(nodeid)));
        return ret;
    }

    public void main(String database)
    {
        for(Integer sufid = 0; sufid < 4; ++ sufid)
        {
            System.out.println(database + "_" + suf[sufid]);
            Set< List<Integer> > thisrp = new HashSet<>();
            List<Pair<Integer, Map<Integer, Integer>>>  rd = ReadInput.ParseData(database + "_" + suf[sufid], groupperfile);
            thisrp = GetFromOneGroup(rd.get(0));
            for(Integer id = 1; id < 10; ++ id) thisrp = CombineSets(thisrp, GetFromOneGroup(rd.get(id)));
            for(List<Integer> rp : thisrp) System.out.println(rp.toString());
        }
    }
}
