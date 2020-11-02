package GetResult;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.Map.Entry;

import GraphData.GraphClassInstancesM;
import GraphData.GraphModelM;
import Main.Main;
import Path.RelSimFinder;
import Path.RelationPath;
import PathBasedSimilarity.*;


public class han {
	public Map<Integer, Double> getTopK(int query, List<Double> weights, List<Measure> measures, int candidateType, int k){
		List<Integer> candidates = GraphClassInstancesM.getInstances(candidateType);
		//System.out.println("candidate size: " + candidates.size());
		Map<Integer, Double> map = new HashMap<Integer, Double>();
		for(int i = 0; i < weights.size(); i ++){
			SimilarityMeasurements sm = measures.get(i).sm;
			RelationPath rp = measures.get(i).rp;
			double weight = weights.get(i);
			for(int entity : candidates){
				double sim = sm.getSim(query, entity, rp);
				//System.out.println("sim: " + sm.ID + " " + sim);

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
	
	static class Measure{
		RelationPath rp;
		SimilarityMeasurements sm;
		public Measure(RelationPath rp, SimilarityMeasurements sm){
			this.rp = rp;
			this.sm = sm;
		}
	}
//
//	public void output(String fileName){
//		File file = new File(fileName);
//		FileReader fr = null;
//		BufferedReader reader = null;
//		Set<Integer> answers = new HashSet<>();
//		try {
//			fr = new FileReader(file);
//			reader = new BufferedReader(fr);
//			String str = reader.readLine();
//			int query = Integer.parseInt(str);
//			str = reader.readLine();
//			String ss[] = str.split("\t");
//
//			for(String sans : ss){
//				answers.add(Integer.parseInt(sans));
//			}
//			//System.out.println(answers);
//			File wfile = new File(fileName +  ".ugss.prec");
//			FileWriter fw = new FileWriter(wfile);
//			BufferedWriter writer = new BufferedWriter(fw);
//			String ws = "";
//
//			for(int num = 2; num <= 4 ; num += 2){
//				RelSimFinder.SetDiameter(num);
//				long start = System.currentTimeMillis();
//				List<Integer> result = Main.topK_byRelsim(query, examples, 20);
//				long end = System.currentTimeMillis();
//				int count = 0;
//				for(int i = 0; i < result.size(); i ++){
//					if(answers.contains(result.get(i)))
//						count ++;
//					if(9 == i)
//						ws += (count + ",");
//				}
//				ws += count + " " + (end - start) + ";";
//			}
//
//			writer.write(ws);
//
//			writer.close();
//			fw.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	public static void main(String[] args){
		GraphClassInstancesM.initializeMap();
		GraphModelM.initializeMap();
		List<Measure> measures = new ArrayList<>();
		SimilarityMeasurements sm0 = new HeteSim(), sm1 = new PCRW(), sm2 = new PathCount(), sm3 = new PathSim();
		Measure m0, m1, m2, m3, m4, m5;

		List<Integer> list0 = new ArrayList<Integer>();
		list0.add(3465221);
		list0.add(-3465221);
		RelationPath rp0 = new RelationPath(list0);

		List<Integer> list1 = new ArrayList<Integer>();
		list1.add(3465221);
		list1.add(3465222);
		list1.add(-3465222);
		list1.add(-3465221);
		RelationPath rp1 = new RelationPath(list1);

		List<Integer> list2 = new ArrayList<Integer>();
		list2.add(3465221);
		list2.add(-3465221);
		list2.add(3465221);
		list2.add(-3465221);
		RelationPath rp2 = new RelationPath(list2);


		//List<Double> weights = new ArrayList<>(Arrays.asList(-2.69900586e+01,3.32132773e+02,2.63372918e+02,-2.67054735e-02,1.53457461e+02,-9.64059482e-01));
		//List<Double> weights = new ArrayList<>(Arrays.asList(1.));
		/*m0 = new Measure(rp0, sm0);
		m1 = new Measure(rp0, sm1);
		m2 = new Measure(rp2, sm0);
		m3 = new Measure(rp0, sm2);
		m4 = new Measure(rp0, sm3);
		m5 = new Measure(rp2, sm0);*/

		//List<Double> weights = new ArrayList<>(Arrays.asList(104.02244629,1.80405211,30.53913602,-0.32613627,92.86540755,-7.99160153));
		/*m0 = new Measure(rp0, sm0);
		m1 = new Measure(rp2, sm0);
		m2 = new Measure(rp0, sm1);
		m3 = new Measure(rp0, sm2);
		m4 = new Measure(rp0, sm3);
		m5 = new Measure(rp2, sm0);*/

		//List<Double> weights = new ArrayList<>(Arrays.asList(8.71099103e-03,8.31696636e-03,4.89240762e-01,-2.86409500e-04,1.52174148e-02,1.12844479e-01));
		/*m0 = new Measure(rp0, sm1);
		m1 = new Measure(rp2, sm1);
		m2 = new Measure(rp0, sm2);
		m3 = new Measure(rp2, sm2);
		m4 = new Measure(rp0, sm3);
		m5 = new Measure(rp2, sm3);*/

		//List<Double> weights = new ArrayList<>(Arrays.asList(-3.96065404e-01,1.68263374e+01,8.77706598e-01,-1.34981653e-04,-1.46995363e+00,-1.12836166e+00));
		/*m0 = new Measure(rp0, sm0);
		m1 = new Measure(rp2, sm0);
		m2 = new Measure(rp2, sm1);
		m3 = new Measure(rp2, sm2);
		m4 = new Measure(rp0, sm3);
		m5 = new Measure(rp2, sm3);*/

		List<Double> weights = new ArrayList<>(Arrays.asList(7.15597470e+00,-2.73867622e00));
		m0 = new Measure(rp1, sm0);
		m3 = new Measure(rp2, sm0);




		measures.add(m0);
		//measures.add(m1);
		//measures.add(m2);
		measures.add(m3);
		//measures.add(m4);
		//measures.add(m5);

		han h = new han();
		System.out.println("begin!");
		long start = System.currentTimeMillis();
		System.out.println(h.getTopK(1155765, weights, measures, 3465225, 21).keySet());
		System.out.println("time :" + (System.currentTimeMillis() - start));
		System.out.println(h.getTopK(388003, weights, measures, 3465225, 21).keySet());
		System.out.println(h.getTopK(585688, weights, measures, 3465225, 21).keySet());
		System.out.println(h.getTopK(219426, weights, measures, 3465225, 21).keySet());


	}
}
