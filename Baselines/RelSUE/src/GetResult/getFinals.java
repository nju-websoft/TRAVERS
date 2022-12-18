package GetResult;

import Path.MetaPath;
import PathBasedSimilarity.PCRW;
import PathBasedSimilarity.PathCount;
import PathBasedSimilarity.SimilarityMeasurements;
import Sig.PSpair;

import java.util.*;
import java.io.*;

public class getFinals {

    public static List<Integer> getTopK(List<Double> weights, String fileName, List<Integer> candidates, int K){
        List<Integer> all = new ArrayList<>();
        all.addAll(candidates);
        Map<Integer, Double> map = new HashMap<>();
        File file = new File(fileName);
        FileReader fr = null;
        BufferedReader reader = null;

        try {
            fr = new FileReader(file);
            reader = new BufferedReader(fr);
            String str = null;
            int id = 0;
            while((str = reader.readLine()) != null){
                double score = 0;
                String[] features = str.split(" ");
                if((features.length - 1) != weights.size()){
                    try {
                        throw new Exception("weights and features not match!");
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                else{
                    for(int i = 1; i < features.length; i ++){
                        String fw = features[i].split(":")[1];
                        double val = Double.parseDouble(fw);
                        score += val*weights.get(i - 1);
                    }
                }

                map.put(all.get(id), score);
                id ++;
            }

            reader.close();
            fr.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        map = sortByValue(map);
        //System.out.println(map);
        List<Integer> result = new ArrayList<>();
        if(map.keySet().size() <= K)
            result.addAll(map.keySet());
        else{
            int count = 0;
            for(int key : map.keySet()){
                if(count >= K) break;
                result.add(key);
                count ++;
            }
        }

        return  result;
    }

    public static List<Integer> getTopK(List<Double> weights, String fileName, List<Integer> examples, List<Integer> nexamples, int K){
        List<Integer> all = new ArrayList<>();
        all.addAll(examples);
        all.addAll(nexamples);
        Map<Integer, Double> map = new HashMap<>();
        File file = new File(fileName);
        FileReader fr = null;
        BufferedReader reader = null;

        try {
			fr = new FileReader(file);
			reader = new BufferedReader(fr);
			String str = null;
			int id = 0;
			while((str = reader.readLine()) != null){
			    double score = 0;
			    String[] features = str.split(" ");
			    if((features.length - 1) != weights.size()){
			        try {
						throw new Exception("weights and features not match!");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                }
                else{
			        for(int i = 1; i < features.length; i ++){
			            String fw = features[i].split(":")[1];
			            double val = Double.parseDouble(fw);
			            score += val*weights.get(i - 1);
                    }
                }

                map.put(all.get(id), score);
			    id ++;
            }

            reader.close();
			fr.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		map = sortByValue(map);
        //System.out.println(map);
        List<Integer> result = new ArrayList<>();
        if(map.keySet().size() <= K)
            result.addAll(map.keySet());
        else{
            int count = 0;
            for(int key : map.keySet()){
                if(count >= K) break;
                result.add(key);
                count ++;
            }
        }

        return  result;
    }

    public static List<Integer> getTopKForRelSim(int query, List<Double> weights, List<MetaPath> paths, List<Integer> examples, List<Integer> nexamples, int K){
        if(weights.size() != paths.size()){
            try {
                throw new Exception("weights size don't match paths size!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        List<Integer> all = new ArrayList<>();
        all.addAll(examples);
        all.addAll(nexamples);
        Map<Integer, Double> map = new HashMap<>();

       // SimilarityMeasurements pc = new PathCount();
        SimilarityMeasurements pcrw = new PCRW();
        for(int entity : all){
            double score = 0;
            for(int i = 0; i < weights.size(); i ++){
               // score += pc.getSim(query, entity, paths.get(i))*weights.get(i);
                score += pcrw.getSim(query, entity, paths.get(i))*weights.get(i);
            }
            map.put(entity, score);
        }

        map = sortByValue(map);
        //System.out.println(map);
        List<Integer> result = new ArrayList<>();
        if(map.keySet().size() <= K)
            result.addAll(map.keySet());
        else{
            int count = 0;
            for(int key : map.keySet()){
                if(count >= K) break;
                if(!examples.contains(key)){
                    result.add(key);
                    count ++;
                }
            }
        }

        return  result;
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
}
