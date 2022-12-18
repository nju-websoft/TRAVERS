package RelSim;

import Structures.graphModel;
import Structures.metapathModel;
import Structures.propertyModel;
import Structures.typeModel;

import java.util.*;

public class FindAnswer {

    public Map<Integer, Double> score = new HashMap<Integer, Double>();
    public Set<Integer> haswalked = new HashSet<Integer>();

    public void Walk(Integer now, Integer dep, metapathModel mp, double sco)
    {
        if(dep > 0 && !typeModel.getTypes(now).contains(mp.concepts.get(dep))) return;
        if(dep >= mp.relations.size())
        {
            if( !score.containsKey(now) ) score.put(now, 0.0);
            Double tmpsco = score.get(now);
            score.put(now, tmpsco + sco);
            return;
        }
        if(!graphModel.G.containsKey(now)) return;
        HashMap<Integer, List<Integer> > neigh = graphModel.getNeighbours(now);
        Integer needrel = mp.relations.get(dep);
        if(!neigh.containsKey(needrel)) return;
        haswalked.add(now);
        List<Integer> outnodes = neigh.get(needrel);
        for(Integer nd : outnodes)
        {
            if(haswalked.contains(nd)) continue;
            Walk(nd, dep + 1, mp, sco);
        }
        haswalked.remove(now);
    }

    public List<Integer> getTopK(List<metapathModel> all_metapath, List<Double> weight, Integer query, Integer K)
    {
        score.clear();
        if(all_metapath.size() != weight.size())
        {
            System.out.println("Excuse me???");
            List<Integer> fuck = new ArrayList<Integer>(); fuck.clear();
            for(Integer i = 0; i < K; ++ i) fuck.add(0);
            return fuck;
        }

        for(Integer label = 0; label < all_metapath.size(); ++ label)
        {
            Double nowsco = weight.get(label);
            //if(nowsco < 0.0001) continue;
            haswalked.clear();
            Walk(query, 0, all_metapath.get(label), nowsco);
        }

        List<Map.Entry<Integer, Double>> score_list = new ArrayList<Map.Entry<Integer, Double>>(score.entrySet());

        Collections.sort(score_list, new Comparator<Map.Entry<Integer, Double>>() {
            @Override
            public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        List<Integer> result = new ArrayList<Integer>(); result.clear();
        for(Map.Entry<Integer, Double> kv : score_list)
        {
            result.add(kv.getKey());
            if(result.size() >= K) break;
        }
        while(result.size() < K) result.add(0);
        return result;
    }

}
