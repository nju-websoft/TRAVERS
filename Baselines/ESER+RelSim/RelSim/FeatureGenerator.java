package RelSim;

import Structures.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class FeatureGenerator {

    public Integer SimCount;
    public Set<Integer> haswalked = new HashSet<Integer>();

    public void Walk(Integer now, Integer dep, Integer ed, metapathModel mp)
    {
        if(dep > 0 && !typeModel.getTypes(now).contains(mp.concepts.get(dep))) return;
        if(dep >= mp.relations.size()) { if(now.equals(ed)) SimCount ++; return; }
        if(!graphModel.G.containsKey(now)) return;
        HashMap<Integer, List<Integer> > neigh = graphModel.getNeighbours(now);
        Integer needrel = mp.relations.get(dep);
        if(!neigh.containsKey(needrel)) return;
        List<Integer> outnodes = neigh.get(needrel);
        for(Integer nd : outnodes)
        {
            if(haswalked.contains(nd)) continue;
            haswalked.add(nd);
            Walk(nd, dep + 1, ed, mp);
            haswalked.remove(nd);
        }
    }

    public void generator(List<metapathModel> all_metapath, Map<Integer, List<Integer> > Sample_Cluster,
                                 List<pairModel> NegSam, Integer negtoone,
                                 List< List<Integer> > pos_sample, List< List<Integer> > neg_sample)
    {
        Integer mp_count = all_metapath.size();

        for(Integer bg : Sample_Cluster.keySet())
        {
            //System.out.println("Feature For " + bg.toString());
            List<Integer> psam = Sample_Cluster.get(bg);
            if(NegSam.size() == 0) continue;
            List<Integer> nsam = new ArrayList<Integer>(); nsam.clear();
            for(pairModel pm : NegSam) if(pm.valX.equals(bg)) nsam.add(pm.valY);
            Integer bbb = 0;
            for(Integer i = nsam.size(); i < negtoone; ++ i) nsam.add(nsam.get(bbb ++));

            //Get SimCount
            for(Integer sam_num = 0; sam_num < psam.size(); ++ sam_num)
            {
                //Positive Sample Calculate
                Integer ps = psam.get(sam_num);

                List<Integer> po_sample = new ArrayList<Integer>(); po_sample.clear();

                for(Integer mp_num = 0; mp_num < mp_count; ++ mp_num)
                {
                    SimCount = 0; haswalked.clear(); haswalked.add(bg);
                    Walk(bg, 0, ps, all_metapath.get(mp_num));
                    po_sample.add(SimCount);
                }

                //Negative Sample Calculate + Print
                for(Integer ns : nsam)
                {

                    List<Integer> ne_sample = new ArrayList<Integer>(); ne_sample.clear();

                    for(Integer mp_num = 0; mp_num < mp_count; ++ mp_num)
                    {
                        SimCount = 0; haswalked.clear(); haswalked.add(bg);
                        Walk(bg, 0, ns, all_metapath.get(mp_num));
                        ne_sample.add(SimCount);
                    }

                    // pos_sample.add((List<Integer>)(((ArrayList<Integer>)po_sample).clone()));
                    // neg_sample.add(ne_sample);

                    List<Integer> pos_fea = new ArrayList<Integer>(); pos_fea.clear();
                    List<Integer> neg_fea = new ArrayList<Integer>(); neg_fea.clear();
                    Integer checksum = 0;
                    for(Integer mp_num = 0; mp_num < mp_count; ++ mp_num)
                    {
                        pos_fea.add(po_sample.get(mp_num) - ne_sample.get(mp_num));
                        neg_fea.add(ne_sample.get(mp_num) - po_sample.get(mp_num));
                        checksum += Math.abs(po_sample.get(mp_num) - ne_sample.get(mp_num));
                    }
                    pos_sample.add(pos_fea); neg_sample.add(neg_fea);

                }
            }

        }
    }

}
