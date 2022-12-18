package GraphData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class wtf {

    public static void main(String[] args){
        Map<Integer, List<Integer>> map = new HashMap<>();
        List<Integer> list = new ArrayList<>();
        list.add(1);
        map.put(1, list);

        map.get(1).add(5);

        System.out.println(map);
    }
}
