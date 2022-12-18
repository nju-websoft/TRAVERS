package oracle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class test {

    public static void main(String[] args){
        Map<Integer, Integer> map = new HashMap<>();
        List<Integer> list = new ArrayList<>();
        for(int i = 0; i < 20000000; i ++){
            map.put(i, i);
            list.add(i);
        }

        long startl = System.currentTimeMillis();
        for(int i = 0; i < list.size(); i ++) {
            int a = list.get(i);
        }
        long endl = System.currentTimeMillis();
        System.out.println("time for list: " + (endl - startl));

        long startm = System.currentTimeMillis();
        for(int i : map.keySet()){
            int a = map.get(i);
        }
        long endm = System.currentTimeMillis();
        System.out.println("time for map: " + (endm - startm));


    }
}
