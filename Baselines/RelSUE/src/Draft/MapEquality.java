package Draft;

import java.util.*;

public class MapEquality {

    public static void main(String[] args){
        Map<Integer, Set<List<Integer>>> map1 = new HashMap<>();
        Map<Integer, Set<List<Integer>>> map2 = new HashMap<>();

        Set<List<Integer>> paths1 = new HashSet<>();
        Set<List<Integer>> paths2 = new HashSet<>();

        List<Integer> path1 = new ArrayList<>();
        List<Integer> path11 = new ArrayList<>();
        List<Integer> path2 = new ArrayList<>();
        List<Integer> path22 = new ArrayList<>();

        path1.add(1);
        path2.add(1);
        path11.add(2);
        path22.add(2);

        paths1.add(path11);
        paths1.add(path1);

        paths2.add(path2);
        paths2.add(path22);

        map1.put(1, paths1);
        map2.put(1, paths2);

        System.out.println(map1.equals(map2));
        System.out.println(map1.keySet().equals(map2.keySet()));

    }
}
