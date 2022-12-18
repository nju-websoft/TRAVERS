package Visualization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import GraphData.GraphModelM;
import GraphData.LabelGetter;
import Path.RelationPath;
import PathBasedSimilarity.HeteSim;

public class VisualizationOfHSim {
	
	public static void Visualize(Map<Integer, Double> map){
		map = sortByValue(map);
		double sum = 0;
		for(Map.Entry<Integer, Double> entry : map.entrySet()){
			System.out.print(LabelGetter.get(entry.getKey()) + ":" + entry.getValue() + " ");
			sum += entry.getValue();
		}
		System.out.println();
		System.out.println(sum);
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
	
	public static void main(String[] args) {
		GraphModelM.initializeMap();
		
		List<Integer> relations = new ArrayList<Integer>();	
		relations.add(3465221);
		relations.add(3465222);
		RelationPath rp = new RelationPath(relations);
		HeteSim hs = new HeteSim();
		//Visualize(hs.getPM(219426, rp));
		//Visualize(hs.getPM(643612, rp));
	
		//Visualize(hs.getPM(580416, rp));
	}
	
}
