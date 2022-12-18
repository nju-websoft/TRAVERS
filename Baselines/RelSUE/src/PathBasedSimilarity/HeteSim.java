package PathBasedSimilarity;

import GraphData.GraphModelM;
import GraphData.GraphOntGetterM;
import Path.MetaPath;
import Path.RelationPath;

import java.util.*;

public class HeteSim implements SimilarityMeasurements {
	public static String ID = "HeteSim";
	@Override
	public double getSim(int a, int b, MetaPath mp){
		if(!GraphOntGetterM.classOfEntityByID(a).contains(mp.getConcepts().get(0)) || !GraphOntGetterM.classOfEntityByID(b).contains(mp.getConcepts().get(mp.length()))){
		//if(!GraphOntGetter.classOfEntityByID(a).contains(mp.getConcepts().get(0)) || !GraphOntGetter.classOfEntityByID(b).contains(mp.getConcepts().get(mp.length()))){
			/*try {
				throw new Exception("meta-path is illegal!");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			//System.out.println("class of t: " + mp.getConcepts().get(mp.length()));
			//System.out.println("b; " + b);
			return 0;
		}
		Map<Integer, Double> map0 = getPM(a, mp.getHalf());
		Map<Integer, Double> map1 = getPM(b, (mp.getReverse()).getHalf());
		double magnitude0 = 0;
		double magnitude1 = 0;
		double innerProduct = 0;
		int size = mp.length();
		if(size % 2 == 0){			
			for(Map.Entry<Integer, Double> entry : map0.entrySet()){
				double prob = entry.getValue();
				magnitude0 += (prob*prob);
			}
			magnitude0 = Math.sqrt(magnitude0);
			for(Map.Entry<Integer, Double> entry : map1.entrySet()){
				double prob = entry.getValue();
				magnitude1 += (prob*prob);
			}
			magnitude1 = Math.sqrt(magnitude1);
			for(int key : map0.keySet()){
				if(map1.containsKey(key)){
					innerProduct += (map0.get(key)*map1.get(key));
				}
			}
			
		}
		else{// the key part of this algorithm, took me almost half a day
			MetaPath mp0 = mp.get(size/2);
			MetaPath mp1 = (mp.getReverse()).get(size/2);
			
			Map<Integer, Map<Integer, Double>> mmap0 = new HashMap<Integer, Map<Integer,Double>>();
			for(Map.Entry<Integer, Double> entry : map0.entrySet()){
				double prob = entry.getValue();
				int key = entry.getKey();
				
				Map<Integer, Double> m0 = getPM(key, mp0);
				for(Map.Entry<Integer, Double> e : m0.entrySet())
					e.setValue(e.getValue()*prob);
				
				mmap0.put(key, m0);
			}
			Map<Integer, Map<Integer, Double>> mmap1 = new HashMap<Integer, Map<Integer,Double>>();
			for(Map.Entry<Integer, Double> entry : map1.entrySet()){
				double prob = entry.getValue();
				int key = entry.getKey();
				
				Map<Integer, Double> m1 = getPM(key, mp1);
				for(Map.Entry<Integer, Double> e : m1.entrySet())
					e.setValue(e.getValue()*prob);
				
				mmap1.put(key, m1);
			}
			
			for(Map.Entry<Integer, Map<Integer, Double>> entry : mmap0.entrySet()){
				for(Map.Entry<Integer, Double> e : entry.getValue().entrySet()){
					double prob = e.getValue();
					magnitude0 += (prob*prob);
				}
			}
			magnitude0 = Math.sqrt(magnitude0);
			for(Map.Entry<Integer, Map<Integer, Double>> entry : mmap1.entrySet()){
				for(Map.Entry<Integer, Double> e : entry.getValue().entrySet()){
					double prob = e.getValue();
					magnitude1 += (prob*prob);
				}
			}
			magnitude1 = Math.sqrt(magnitude1);
			
			for(int key : mmap0.keySet()){
				for(int k : mmap0.get(key).keySet()){
					if(mmap1.containsKey(k) && mmap1.get(k).containsKey(key)){
						innerProduct += (mmap0.get(key).get(k) * mmap1.get(k).get(key));
					}
				}
			}
		}

		if(magnitude0 == 0 || magnitude1 == 0)
			return 0;
		else
			return innerProduct/(magnitude0*magnitude1);
	}
	@Override
	public double getSim(int a, int b, RelationPath rp) {
		Map<Integer, Double> map0 = getPM(a, rp.getHalf());
		Map<Integer, Double> map1 = getPM(b, (rp.getReverse()).getHalf());
		double magnitude0 = 0;
		double magnitude1 = 0;
		double innerProduct = 0;
		int size = rp.length();
		if(size % 2 == 0){			
			for(Map.Entry<Integer, Double> entry : map0.entrySet()){
				double prob = entry.getValue();
				magnitude0 += (prob*prob);
			}
			magnitude0 = Math.sqrt(magnitude0);
			for(Map.Entry<Integer, Double> entry : map1.entrySet()){
				double prob = entry.getValue();
				magnitude1 += (prob*prob);
			}
			magnitude1 = Math.sqrt(magnitude1);
			for(int key : map0.keySet()){
				if(map1.containsKey(key)){
					innerProduct += (map0.get(key)*map1.get(key));
				}
			}
			
		}
		else{// the key part of this algorithm, took me almost half a day
			RelationPath rp0 = rp.get(size/2);
			RelationPath rp1 = (rp.getReverse()).get(size/2);
			
			Map<Integer, Map<Integer, Double>> mmap0 = new HashMap<Integer, Map<Integer,Double>>();
			for(Map.Entry<Integer, Double> entry : map0.entrySet()){
				double prob = entry.getValue();
				int key = entry.getKey();
				
				Map<Integer, Double> m0 = getPM(key, rp0);
				for(Map.Entry<Integer, Double> e : m0.entrySet())
					e.setValue(e.getValue()*prob);
				
				mmap0.put(key, m0);
			}
			Map<Integer, Map<Integer, Double>> mmap1 = new HashMap<Integer, Map<Integer,Double>>();
			for(Map.Entry<Integer, Double> entry : map1.entrySet()){
				double prob = entry.getValue();
				int key = entry.getKey();
				
				Map<Integer, Double> m1 = getPM(key, rp1);
				for(Map.Entry<Integer, Double> e : m1.entrySet())
					e.setValue(e.getValue()*prob);
				
				mmap1.put(key, m1);
			}
			
			for(Map.Entry<Integer, Map<Integer, Double>> entry : mmap0.entrySet()){
				for(Map.Entry<Integer, Double> e : entry.getValue().entrySet()){
					double prob = e.getValue();
					magnitude0 += (prob*prob);
				}
			}
			magnitude0 = Math.sqrt(magnitude0);
			for(Map.Entry<Integer, Map<Integer, Double>> entry : mmap1.entrySet()){
				for(Map.Entry<Integer, Double> e : entry.getValue().entrySet()){
					double prob = e.getValue();
					magnitude1 += (prob*prob);
				}
			}
			magnitude1 = Math.sqrt(magnitude1);
			
			for(int key : mmap0.keySet()){
				for(int k : mmap0.get(key).keySet()){
					if(mmap1.containsKey(k) && mmap1.get(k).containsKey(key)){
						innerProduct += (mmap0.get(key).get(k) * mmap1.get(k).get(key));
					}
				}
			}
		}
		//System.out.println(innerProduct);
		if(magnitude0 == 0 || magnitude1 == 0)
			return 0;
		else
			return innerProduct/(magnitude0*magnitude1);
			//return innerProduct;
	}
	
	private Map<Integer, Double> getPM(int a, MetaPath mp){
		List<Integer> relations = mp.getRelations();
		List<Integer> concepts = mp.getConcepts();
		Map<Integer, Double> map = new HashMap<Integer, Double>();
		if(relations.size() < 0 || relations.size() != concepts.size() - 1){
			try {
				throw new Exception("error occurred during calculate the normalized hetesim!");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(relations.size() == 0){
			int concept = concepts.get(0);
			if(GraphOntGetterM.classOfEntityByID(a).contains(concept))
			//if(GraphOntGetter.classOfEntityByID(a).contains(concept))
				map.put(a, 1.0);
			return map;
		}
		if(relations.size() == 1){
			int rel = relations.get(0);
			int con = concepts.get(1);
			List<Integer> objects = GraphModelM.getObjects(a, rel);
			if(objects != null){
				List<Integer> typedObjects = new ArrayList<Integer>();
				for(int object : objects){
					if(GraphOntGetterM.classOfEntityByID(object).contains(con))
					//if(GraphOntGetter.classOfEntityByID(object).contains(con))
						typedObjects.add(object);
				}
				int size = typedObjects.size();
				for(int object : typedObjects){
					map.put(object, (double)1/(double)size);
				}
			}
			return map;
		}
		
		List<Integer> nrelations = new ArrayList<Integer>();
		for(int i = 1; i < relations.size(); i ++)
			nrelations.add(relations.get(i));
		List<Integer> nconcepts = new ArrayList<Integer>();
		for(int i = 1; i < concepts.size(); i ++)
			nconcepts.add(concepts.get(i));
		MetaPath nmp = new MetaPath(nconcepts, nrelations);
		int rel = relations.get(0);
		int con = concepts.get(1);
		List<Integer> objects = GraphModelM.getObjects(a, rel);
		
		if(objects != null){
			List<Integer> typedObjects = new ArrayList<Integer>();
			for(int object : objects){
				if(GraphOntGetterM.classOfEntityByID(object).contains(con))
				//if(GraphOntGetter.classOfEntityByID(object).contains(con))
					typedObjects.add(object);
			}
			int size = typedObjects.size();
			//System.out.println("M: " + size);
			for(int object : typedObjects){
				Map<Integer, Double> m = getPM(object, nmp);
				for(int node : m.keySet()){
					double prob = m.get(node)/size;
					if(map.containsKey(node)){	
						prob += map.get(node);
						map.put(node, prob);
					}
					else{
						map.put(node, prob);
					}
				}
			}
		}
		//System.out.println(map);
		return map;
	}
	
	/**
	 * 
	 * @param a
	 * @param rp
	 * @return the probability of node a to arrive a node through rp. For nodes not in the map, the probability is zero.
	 */
	private Map<Integer, Double> getPM(int a, RelationPath rp){
		List<Integer> relations = rp.getRelations();
		Map<Integer, Double> map = new HashMap<Integer, Double>();
		if(relations.size() < 0){
			try {
				throw new Exception("error occurred during calculate the normalized hetesim!");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(relations.size() == 0){
			//System.out.println("u can't c me");
			map.put(a, 1.0);
			return map;
		}
		if(relations.size() == 1){
			int rel = relations.get(0);
			List<Integer> objects = GraphModelM.getObjects(a, rel);
			if(objects != null){
				int size = objects.size();
				for(int object : objects){
					map.put(object, (double)1/(double)size);
				}
			}
			return map;
		}
		List<Integer> nrelations = new ArrayList<Integer>();
		for(int i = 1; i < relations.size(); i ++)
			nrelations.add(relations.get(i));
		RelationPath nrp = new RelationPath(nrelations);
		int rel = relations.get(0);
		List<Integer> objects = GraphModelM.getObjects(a, rel);
		
		if(objects != null){
			int size = objects.size();
            //System.out.println("R: " + size);
			for(int object : objects){
				Map<Integer, Double> m = getPM(object, nrp);
				for(int node : m.keySet()){
					double prob = m.get(node)/size;
					if(map.containsKey(node)){	
						prob += map.get(node);
						map.put(node, prob);
					}
					else{
						map.put(node, prob);
					}
				}
				
			}
		}
		
		return map;
	}
	
	@Deprecated
	public double getHeteSim(int a, int b, RelationPath rp){// equivalent to pairwise random walk, unnormalized version
		List<Integer> relations = rp.getRelations();
		if(relations.size() == 1){
			int rel = relations.get(0);
			List<Integer> list0 = GraphModelM.getObjects(a, rel);
			List<Integer> list1 = GraphModelM.getObjects(b, rel*-1);
			if(list0 == null || list1 == null)
				return 0;
			int n = list0.size();
			int m = list1.size();
			
			if(list0.contains(b))
				return (1/((double)(m*n)));
			else
				return 0;
		}
		if(relations.size() == 2){
			int rel0 = relations.get(0);
			int rel1 = relations.get(1)*-1;
			List<Integer> list0 = GraphModelM.getObjects(a, rel0);
			List<Integer> list1 = GraphModelM.getObjects(b, rel1);
			if(list0 == null || list1 == null)
				return 0;
			int n = list0.size();
			int m = list1.size();
			list0.retainAll(list1);
			int k = list0.size();
			return (double)k/((double)(m*n));
		}
		
		List<Integer> subRelations = new ArrayList<Integer>();
		for(int i = 1; i < relations.size() - 1; i ++)
			subRelations.add(relations.get(i));
		List<Integer> list0 = GraphModelM.getObjects(a, relations.get(0));
		List<Integer> list1 = GraphModelM.getObjects(b, relations.get(relations.size() - 1)*-1);
		double result = 0;
		if(list0 == null || list1 == null)
			return 0;
		int n = list0.size();
		int m = list1.size();
		for(int i : list0)
			for(int j : list1){
				result += getHeteSim(i, j, new RelationPath(subRelations))/((double)(m*n));
			}
		
		return result;
	}
	
	public static void main(String[] args) {
		GraphModelM.initializeMap();
		System.out.println("start to calculate");
		HeteSim hs = new HeteSim();
		List<Integer> relations = new ArrayList<Integer>();
		
		//for dbpedia
		//relations.add(3480865);  //team
		//relations.add(-3480865);
		
		//for dblp
		//relations.add(3465221);
		//relations.add(3465222);
		//relations.add(-3465222);
		//relations.add(-3465221);
		
		//for yago
		relations.add(-4295854);
		relations.add(4295851);
		relations.add(-4295851);
		relations.add(4295854);
		
		//relations.add(8);
		//relations.add(-8);

		RelationPath rp = new RelationPath(relations);
		
		System.out.println(hs.getSim(2193500, 1349798, rp));

		List<Integer> types = new ArrayList<Integer>();
		types.add(4787540);
		//types.add(4386635);
		types.add(4581106);
        types.add(4868861);
		//types.add(4386635);
        types.add(4581106);
		types.add(4787540);

		MetaPath mp = new MetaPath(types, relations);
		System.out.println(hs.getSim(2193500, 4290467, mp));
		System.out.println(hs.getSim(2193500, 2106560, mp));
		
		//System.out.println(hs.getSim(643612, 219426, rp));
		//System.out.println(hs.getSim(219426, 643612, rp));

		//System.out.println(hs.getSim(1, 4, rp));
		//System.out.println(hs.getSim(1562340, 2580708, rp));
		//System.out.println(hs.getSim(2580708, 1562340, rp));
		//System.out.println(hs.getSim(1562340, 1562340, rp));
	}
}
