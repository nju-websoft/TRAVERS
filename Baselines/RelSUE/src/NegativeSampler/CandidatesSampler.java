package NegativeSampler;

import java.util.*;

/**
 * 从candidates中随机选一部分作为负例进行训练
 */

public class CandidatesSampler {
    static Random random = new Random();
    public static List<Integer> getNsampes(Set<Integer> candidates, int num){
        List<Integer> list = new ArrayList<>();
        list.addAll(candidates);
        if(list.size() <= num)
            return  list;
        List<Integer> result = new ArrayList<>();
        for(int i = 0; i < num; i ++){
           int id = random.nextInt(list.size());
           result.add(list.get(id));
           list.remove(id);
        }

        return result;
    }
}
