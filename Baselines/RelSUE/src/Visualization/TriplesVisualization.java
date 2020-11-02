//package Visualization;
//
//import java.util.*;
//
//import GraphData.GraphModelM;
//import GraphData.GraphOntGetter;
//import GraphData.LabelGetter;
//
//public class TriplesVisualization {
//	public static void visualizeAllTriples(int entity){
//		String label = LabelGetter.get_DBPedia(entity);
//		for(Map.Entry<Integer, List<Integer>> entry : GraphModelM.map.get(entity).entrySet()){
//			int predicate = entry.getKey();
//			List<Integer> list = entry.getValue();
//			if(predicate > 0){
//				String p = LabelGetter.get(predicate);
//				for(int o : list){
//					String ol = LabelGetter.get_DBPedia(o);
//					System.out.println(label + " " + p + " " + ol);
//				}
//			}
//			else{
//				String p = LabelGetter.get(-1*predicate);
//				for(int o : list){
//					String ol = LabelGetter.get_DBPedia(o);
//					System.out.println(ol + " " + p + " " + label);
//				}
//			}
//		}
//
//	}
//	public static void visualizeAllTypes(int entity){
//		for(int i : GraphOntGetter.classOfEntityByID(entity)){
//			System.out.println(LabelGetter.get(i));
//		}
//	}
//
//	public static void main(String[] args) {
//		//GraphModelM.initializeMap();
//		//visualizeAllTriples(2205300); // taylor swift dbpedia
//		visualizeAllTypes(2205300);
//	}
//}
