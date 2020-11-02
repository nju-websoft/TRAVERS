package Evalutaion;

import java.io.*;
import java.util.*;

public class CalNDCG {

    static Comparator<Map.Entry<Integer, Integer>> valueComparator = new Comparator<Map.Entry<Integer, Integer>>() {
        @Override
        public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
            if(o1.getValue() < o2.getValue()) return 1;
            if(o1.getValue() > o2.getValue()) return -1;
            return 0;
        }
    };

    public static double calculateNDCG(List<Integer> examples, List<Integer> results, Map<Integer, Integer> answerMap, int K){
        double dcg = 0, idcg = 0;
        int j = 0;
        /*for(int e : examples){
            dcg += (Math.pow(2, answerMap.get(e)) - 1.0)/(Math.log10(j+2)/Math.log10(2));
            j ++;
        }*/
        for(int i = 0; i < K; i ++){
            if(results.size() > i){
                if(answerMap.containsKey(results.get(i)))
                    dcg += (answerMap.get(results.get(i)))/(Math.log10(i+2)/Math.log10(2));
            }
            else break;
        }

        int i = 0;
        for(Map.Entry<Integer, Integer> entry : answerMap.entrySet()){
            if(K == i )
                break;
            if(!examples.contains(entry.getKey())){
                idcg += (entry.getValue())/(Math.log10(i+2)/Math.log10(2));
                i ++;
            }
        }

//        if(dcg > idcg){
//            System.out.println("what the fuck??");
//            System.out.println(results.subList(0, K));
//        }
        return dcg/idcg;
    }

    public static double calculateNDCG(List<Integer> results, Map<Integer, Integer> answerMap, int K){
        double dcg = 0, idcg = 0;
        int j = 0;
        /*for(int e : examples){
            dcg += (Math.pow(2, answerMap.get(e)) - 1.0)/(Math.log10(j+2)/Math.log10(2));
            j ++;
        }*/
        for(int i = 0; i < K; i ++){
            if(results.size() > i){
                if(answerMap.containsKey(results.get(i)))
                    dcg += (answerMap.get(results.get(i)))/(Math.log10(i+2)/Math.log10(2));
            }
            else break;
        }

        List<Map.Entry<Integer, Integer>> score_list = new ArrayList<Map.Entry<Integer, Integer>>(answerMap.entrySet());

        Collections.sort(score_list, valueComparator);
        int i = 0;
        for(Map.Entry<Integer, Integer> kv : score_list)
        {
            if(K == i) break;
            idcg += (double) (kv.getValue())/(Math.log10(i+2)/Math.log10(2));
            i ++;
        }

//        if(dcg > idcg){
//            System.out.println("what the fuck??");
//            System.out.println(results.subList(0, K));
//        }
        return dcg/idcg;
    }

    public static <K, V> Map<K, V> sortByValue(Map<K, V> map) {
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


    public static void process(String resultFile, String answerFile){
        double[] ndcg10 = new double[5];
        double[] ndcg20 = new double[5];
        File resultfile = new File(resultFile);
        File answerfile = new File(answerFile);
        try {
            FileReader fr = new FileReader(resultfile);
            BufferedReader reader = new BufferedReader(fr);
            List<String> contents = new ArrayList<>();
            String content;
            while((content = reader.readLine()) != null){
                contents.add(content);
            }

            FileReader fra = new FileReader(answerfile);
            BufferedReader readera = new BufferedReader(fra);
            String str;
            int i = 0;
            while((str = readera.readLine()) != null){
                if((i+2) % 2 == 1){
                    Map<Integer, Integer> anserMap = new HashMap<>();
                    List<Integer> answerList = new ArrayList<>();
                    String[] ss = str.split("\t");
                    for(String s : ss){
                       // System.out.println(s);
                        String[] pair = s.split(":");
                        anserMap.put(Integer.parseInt(pair[0]), Integer.parseInt(pair[1]));
                        answerList.add(Integer.parseInt(pair[0]));
                    }
                    anserMap = sortByValue(anserMap);
                    List<Integer> examples = new ArrayList<>();
                    examples.addAll(answerList.subList(0, 5));

                    int lineNum = (i/2)*5;

                    for(int j = 0; j < 5; j ++){
                        String content_j = contents.get(j + lineNum);
                        List<Integer> results = new ArrayList<>();
                        String[] answers = content_j.split("\t");
                        for(String a : answers){
                            results.add(Integer.parseInt(a));
                        }
                        // 10 queries for each group, that's why we have to divide it by 10
                        ndcg10[j] += calculateNDCG(examples.subList(0, j + 1), results, anserMap, 10)/10;
                        ndcg20[j] += calculateNDCG(examples.subList(0, j + 1), results, anserMap, 20)/10;
                    }


                }
                i ++;
            }




        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < 5; i ++){
            System.out.println(ndcg10[i] + ";" + ndcg20[i]);
        }
    }
}
