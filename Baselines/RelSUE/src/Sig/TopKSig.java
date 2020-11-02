package Sig;

import Path.MetaPath;

import java.util.*;

/**
 * After getting all meta-paths from FilterFinder, we calculate the significance for each meta-path, and return topK.
 */

public class TopKSig {

    public static List<PSpair> getTopK(Set<MetaPath> paths, int query, List<Integer> examples){
        List<PSpair> pairs = new ArrayList<>();
        for(MetaPath mp : paths){
            double sig = SigCalculator.getSig(mp, query, examples);
            PSpair pair = new PSpair(mp, sig);
            pairs.add(pair);
        }

        Collections.sort(pairs, new Comparator<PSpair>() {
            public int compare(PSpair o1, PSpair o2) {
                if((o2.sig - o1.sig) > 0)
                    return 1;
                else if((o2.sig - o1.sig) < 0)
                    return -1;
                else return 0;
            }
        });

        return pairs;
    }

}
