package GetResult;

import Path.MetaPath;
import PathBasedSimilarity.PCRW;
import Sig.PSpair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class getResultsBySig {
    static PCRW pcrw = new PCRW();

    public static List<Integer> getTopK(int query, List<Integer> candidates, List<PSpair> pairs, int K){
        List<ESpair> ess = new ArrayList<>();

        for(int can : candidates){
            double score = 0;
            for(PSpair p : pairs){
                MetaPath mp = p.mp;
                double weight = p.sig;

                score += pcrw.getSim(query, can, mp);
            }

            ESpair es = new ESpair(can, score);
            ess.add(es);
        }

        Collections.sort(ess, new Comparator<ESpair>() {
            @Override
            public int compare(ESpair o1, ESpair o2) {
                if((o1.score - o2.score) > 0)
                    return -1;
                else if ((o1.score - o2.score) < 0)
                    return 1;
                else return 0;
            }
        });

        List<Integer> result = new ArrayList<>();
        int i = 0;
        while(ess.size() > i && i < K){
            result.add(ess.get(i).entity);
        }

        return result;
    }

    /**
     * entity score pair, which is used to rank all entities to further get the final results
     */
    static class ESpair{
        int entity;
        double score;

        public ESpair(int entity, double score){
            this.entity = entity;
            this.score = score;
        }
    }
}
