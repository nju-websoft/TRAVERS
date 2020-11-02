package GetResult;

import GraphData.*;
import JDBCUtils.JdbcUtil;
import Path.MetaPath;
import Path.RelationPath;
import PathBasedSimilarity.HeteSim;
import PathBasedSimilarity.PCRW;
import PathBasedSimilarity.PathCount;
import PathBasedSimilarity.PathSim;
import PathBasedSimilarity.SimilarityMeasurements;

import java.util.*;
import java.util.Map.Entry;
import java.io.*;

/**
 * 主要定语义时和生成candidates时使用
 */

public class BasicRanker {

	@Deprecated
	public Map<Integer, Double> getTopK(int query, List<Double> weights, List<RelationPath> paths, int candidateType, int k, SimilarityMeasurements sm){
		if(weights.size() != paths.size()){
			try {
				throw new Exception("the number of weights and the number of paths don't match!");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		if(SumOfList(weights) != 1){
			try {
				throw new Exception("the sum of weights doesn't equal 1!");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		List<Integer> candidates = GraphClassInstancesM.getInstances(candidateType);
		Map<Integer, Double> map = new HashMap<Integer, Double>();
		for(int i = 0; i < weights.size(); i ++){
			RelationPath rp = paths.get(i);
			double weight = weights.get(i);
			for(int entity : candidates){
				double sim = sm.getSim(query, entity, rp);
				double wsim = sim*weight;
				if(!map.containsKey(entity))
					map.put(entity, wsim);
				else{
					double sim0 = map.get(entity);
					map.put(entity, sim0 + wsim);
				}
			}
		}
		
		
		
		map = sortByValue(map);
		Map<Integer, Double> result = new LinkedHashMap<Integer, Double>();
		int i = 0;
		for(Map.Entry<Integer, Double> entry : map.entrySet()){
			if(i < k){
				result.put(entry.getKey(), entry.getValue());
				i ++;
				//System.out.println(entry.getValue());
			}
			else
				break;
		}
		
		return result;
	}
	
	public Map<Integer, Double> getTopK(int query, RelationPath rp, int candidateType, int k, SimilarityMeasurements sm){
		List<Integer> candidates = GraphClassInstancesM.getInstances(candidateType);
		//List<Integer> candidates = GraphClassInstances.getInstances(candidateType);
		System.out.println("candidates size: " + candidates.size());
		Map<Integer, Double> map = new HashMap<Integer, Double>();
		for(int entity : candidates){
			double sim = sm.getSim(query, entity, rp);
			//System.out.println(sim);
			map.put(entity, sim);
		}
		map = sortByValue(map);
		Map<Integer, Double> result = new LinkedHashMap<Integer, Double>();
		int i = 0;
		for(Map.Entry<Integer, Double> entry : map.entrySet()){
			if(i < k){
				if(!entry.getKey().equals(query)){
					result.put(entry.getKey(), entry.getValue());
					i ++;
				}
		        //System.out.println(entry.getValue());
			}
			else
				break;
		}
		return result;
	}
	public Map<Integer, Double> getTopK(int query, MetaPath mp, int candidateType, int k, SimilarityMeasurements sm){
		List<Integer> candidates = GraphClassInstancesM.getInstances(candidateType);
		//List<Integer> candidates = GraphClassInstances.getInstances(candidateType);
		Map<Integer, Double> map = new HashMap<Integer, Double>();
		for(int entity : candidates){
			double sim = sm.getSim(query, entity, mp);
			map.put(entity, sim);
		}
		map = sortByValue(map);
		Map<Integer, Double> result = new LinkedHashMap<Integer, Double>();
		int i = 0;
		for(Map.Entry<Integer, Double> entry : map.entrySet()){
			if(i < k){
				result.put(entry.getKey(), entry.getValue());
				i ++;
			}
			else
				break;
		}
		
		return result;
	}
	public Map<Integer, Double> getTopK(int query, RelationPath rp, List<Integer> candidates, int k, SimilarityMeasurements sm){
		Map<Integer, Double> map = new HashMap<Integer, Double>();
		for(int entity : candidates){
			double sim = sm.getSim(query, entity, rp);
			map.put(entity, sim);
		}
		map = sortByValue(map);
		Map<Integer, Double> result = new LinkedHashMap<Integer, Double>();
		int i = 0;
		for(Map.Entry<Integer, Double> entry : map.entrySet()){
			if(i < k){
				if(!entry.getKey().equals(query)){
					result.put(entry.getKey(), entry.getValue());
					i ++;
					System.out.println(entry.getValue());
				}
			}
			else
				break;
		}
		
		return result;
	}

	public Map<Integer, Double> getTopK(int query, MetaPath mp, Set<Integer> candidates, int k, SimilarityMeasurements sm){
		Map<Integer, Double> map = new HashMap<Integer, Double>();
		for(int entity : candidates){
			double sim = sm.getSim(query, entity, mp);
			//System.out.println(sim);
			map.put(entity, sim);
		}
		map = sortByValue(map);

		Map<Integer, Double> result = new LinkedHashMap<Integer, Double>();
		int i = 0;
		for(Map.Entry<Integer, Double> entry : map.entrySet()){
			if(i < k){
				result.put(entry.getKey(), entry.getValue());
				i ++;
			}
			else
				break;
		}

		return result;
	}

	public Set<Integer> getTopKNodes(int query, MetaPath mp, Set<Integer> candidates, int k, SimilarityMeasurements sm){
		if(candidates.size() <= k)
			return candidates;
		else
			return getTopK(query, mp, candidates, k, sm).keySet();
	}

	private static <K, V> Map<K, V> sortByValue(Map<K, V> map) {
	    List<Entry<K, V>> list = new LinkedList<>(map.entrySet());
	    Collections.sort(list, new Comparator<Object>() {
	        @SuppressWarnings("unchecked")
	        public int compare(Object o1, Object o2) {
	            return ((Comparable<V>) ((Map.Entry<K, V>) (o2)).getValue()).compareTo(((Map.Entry<K, V>) (o1)).getValue());
	        }
	    });

	    Map<K, V> result = new LinkedHashMap<>();
	    for (Iterator<Entry<K, V>> it = list.iterator(); it.hasNext();) {
	        Map.Entry<K, V> entry = (Map.Entry<K, V>) it.next();
	        result.put(entry.getKey(), entry.getValue());
	    }

	    return result;
	}
	private static double SumOfList(List<Double> list){
		double result = 0;
		for(double d : list)
			result += d;
		
		return result;
	}

	private static void output(Map<Integer, Double> map, String fileName){
		File file = new File(fileName);
		FileWriter fw = null;
		BufferedWriter writer = null;
		try {
			fw = new FileWriter(file);
			writer = new BufferedWriter(fw);

			for(Map.Entry<Integer, Double> entry : map.entrySet()){
				writer.write(entry.getKey() + " : " + entry.getValue());
				writer.newLine();
			}

			writer.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		GraphModelM.initializeMap();
		GraphClassInstancesM.initializeMap();
		System.out.println("begin to load ontgetter!");
		GraphOntGetterM.initializeMap();
		BasicRanker br = new BasicRanker();
		
		/*List<Integer> relations = new ArrayList<Integer>();
		relations.add(3481028);
		relations.add(-3481028);
		
		RelationPath rp = new RelationPath(relations);
		
		//int query = 2205300; // taylor swift dbpedia
		//int query = 1134320; // jay z
		int query = 2503024;
		//for(int i : br.getTopK(query, rp, 3481454, 50, new HeteSim()).keySet()){
		for(int i : br.getTopK(query, rp, 3481739, 30, new HeteSim()).keySet()){
			//System.out.println(LabelGetter.get_DBPedia(i));
			System.out.print(i + "\t");
		}*/
			
		List<Integer> relations = new ArrayList<Integer>();

		relations.add(3465221);
		relations.add(3465222);
		relations.add(-3465222);
		relations.add(-3465221);
		RelationPath rp = new RelationPath(relations);

		List<Integer> relations1 = new ArrayList<Integer>();
		relations1.add(3465221);
		relations1.add(-3465221);
		RelationPath rp1 = new RelationPath(relations1);
		
		List<RelationPath> paths = new ArrayList<RelationPath>();
		paths.add(rp);
		paths.add(rp1);
		
		List<Double> weights = new ArrayList<Double>();
		weights.add(0.2);
		weights.add(0.8);
		
		List<Integer> relations2 = new ArrayList<Integer>();

		relations2.add(3465221);
		relations2.add(-3465221);
		relations2.add(3465221);
		relations2.add(-3465221);
		RelationPath rp2 = new RelationPath(relations2);
		
		int query = 219426; //jiawei han
		//int query = 643612; //christos faloutsos
		//int query = 580416; //yuzhong qu
		//int query = 388003;
		

		
		for(Map.Entry<Integer, Double> entry : br.getTopK(query, rp, 3465225, 1000, new HeteSim()).entrySet()){
			System.out.println(entry.getValue());
			//System.out.print(entry.getKey() + "\t");
		}
		System.out.println();
		
	
		
		
		
		
		/*File file = new File("candidates/candidates2");
		FileWriter fw = null;
		BufferedWriter writer = null;
		try {
			fw = new FileWriter(file);
			writer = new BufferedWriter(fw);
			for(int i : br.getTopK(query, rp2, 3465225, 250, new HeteSim())){
				System.out.println(LabelGetter.get(i));
				writer.write(String.valueOf(i));
				writer.newLine();
			}
			
			writer.close();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		JdbcUtil.closeConnection();
	}
}
