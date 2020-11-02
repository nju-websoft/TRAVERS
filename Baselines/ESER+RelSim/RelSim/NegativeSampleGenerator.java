package RelSim;

import Structures.graphModel;
import Structures.metapathModel;
import Structures.propertyModel;
import Structures.typeModel;

import java.util.*;

public class NegativeSampleGenerator {

    public Set<Integer> haswalked = new HashSet<Integer>();

    public void Walk(Integer now, Integer dep, Set<Integer> answer, metapathModel mp)
    {
        if(dep > 0 && !typeModel.getTypes(now).contains(mp.concepts.get(dep))) return;
        if(dep >= mp.relations.size()) { answer.add(now); return; }
        if(!graphModel.G.containsKey(now)) return;
        HashMap<Integer, List<Integer> > neigh = graphModel.getNeighbours(now);
        Integer needrel = mp.relations.get(dep);
        if(!neigh.containsKey(needrel)) return;
        List<Integer> outnodes = neigh.get(needrel);
        for(Integer nd : outnodes)
        {
            if(haswalked.contains(nd)) continue;
            haswalked.add(nd);
            Walk(nd, dep + 1, answer, mp);
            haswalked.remove(nd);
        }
    }

    public Set<Integer> getNegativeSample_oneMP(Integer query, metapathModel mp)
    {
        Set<Integer> ret = new HashSet<Integer>(); ret.clear();
        haswalked.clear(); haswalked.add(query); Walk(query, 0, ret, mp); haswalked.remove(query);

        return ret;
    }

    public List<Integer> getNegativeSample_allMP(Integer query, List<metapathModel> mpm, List<Integer> postivesample, Integer K)
    {
        Set<Integer> allsample = new HashSet<Integer>(); allsample.clear();

        for(metapathModel mp : mpm) allsample.addAll(getNegativeSample_oneMP(query, mp));

        //System.out.println("Query : " + query.toString() + " --- allSample : " + allsample.toString());

        allsample.remove(query); for (Integer e : postivesample) allsample.remove(e);

        List<Integer> result = new ArrayList<Integer>(); result.clear(); result.addAll(allsample);
        List<Integer> ret = new ArrayList<Integer>(); ret.clear();

        Collections.shuffle(result);

        if(result.size() <= K) ret = result; else ret = result.subList(0, K);

        //System.out.println(ret.size());

        return ret;
    }

    public void main(String[] args)
    {

    }

}
