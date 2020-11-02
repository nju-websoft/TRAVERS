package dataset;

import GraphData.GraphOntGetterM;

import java.io.*;
import java.util.*;

/**
 * yago query 类型分析
 */

public class YagoQuery {

    /**
     *
     * @param fileName file name of previously generated yago query
     * @return map: key->the id of a type, value->the frequency of a type in the answer list
     */
    public static Map<Integer, Integer> Freq4Types(String fileName){
        Map<Integer, Integer> result = new HashMap<>();
        File file = new File(fileName);
        try {
            FileReader reader = new FileReader(file);
            BufferedReader br = new BufferedReader(reader);

            br.readLine();

            String[] answers = br.readLine().split("\t");
            System.out.println("total answers: " + answers.length);
            for(String answer : answers){
                Set<Integer> types = GraphOntGetterM.classOfEntityByID(Integer.parseInt(answer));
                for(Integer type : types){
                    if(result.containsKey(type)){
                        result.put(type, result.get(type) + 1);
                    }
                    else
                        result.put(type, 1);
                }
            }



        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sortByValue(result);
    }

    private static <K, V> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Object>() {
            @SuppressWarnings("unchecked")
            public int compare(Object o1, Object o2) {
                return ((Comparable<V>) ((Map.Entry<K, V>) (o2)).getValue()).compareTo(((Map.Entry<K, V>) (o1)).getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<>();
        for (Iterator<Map.Entry<K, V>> it = list.iterator(); it.hasNext();) {
            Map.Entry<K, V> entry = (Map.Entry<K, V>) it.next();
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public static void main(String[] args){
        GraphOntGetterM.initializeMap();

        System.out.println(Freq4Types("F:\\JavaProject\\workspace\\YagoPOP\\4295834\\1.txt"));
    }
}
