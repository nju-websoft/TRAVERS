package PathBasedSimilarity;

import GraphData.GraphModelM;
import GraphData.GraphOntGetterM;
import Path.MetaPath;
import Path.RelationPath;

import java.util.*;

public class PathCount implements SimilarityMeasurements {
	public static String ID = "PathCount";
	@Override
	public double getSim(int a, int b, MetaPath mp) {
		//if(GraphOntGetterM.classOfEntityByID(a).contains(mp.getConcepts().get(0)) && GraphOntGetterM.classOfEntityByID(b).contains(mp.getConcepts().get(mp.length())))
		if(GraphOntGetterM.classOfEntityByID(a).contains(mp.getConcepts().get(0)))
			return getCount(a, b, mp, mp.length());
		else{
			try {
				throw new Exception("meta-path is illegal!");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 0;
		}
	}
	
	@Override
	public double getSim(int a, int b, RelationPath rp) {
		return getCount(a, b, rp, rp.length());
	}

	private double getCount(int a, int b, MetaPath mp, int left) {
		double count = 0;
		Map<Integer, List<Integer>> rmap = GraphModelM.map.get(a);
		Set<Integer> btypes = GraphOntGetterM.classOfEntityByID(b);
		List<Integer> rs = mp.getRelations();
		if(rs.size() == 0){
			if(a == b)
				return 1;
			else
				return 0;
		}
		int relation = rs.get(rs.size() - left);
		List<Integer> cs = mp.getConcepts();
		int concept = cs.get(rs.size() - left + 1);
		if(left <= 0){
			try {
				throw new Exception("pathcount error!");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 0;
		}
		if(left == 1) {
			if(rmap.containsKey(relation) && rmap.get(relation).contains(b) && btypes.contains(concept))
				return 1;
			else
				return 0;
		}
		
		List<Integer> nodes = rmap.get(relation);
		if(nodes == null)
			return 0;
		List<Integer> typednodes = new ArrayList<Integer>();
		for(int node : nodes){
			if(GraphOntGetterM.classOfEntityByID(node).contains(concept))
				typednodes.add(node);
		}
		for(int tnode : typednodes){
			count += getCount(tnode, b, mp, left - 1);
		}
		
		return count;
	}
	
	private double getCount(int a, int b, RelationPath rp, int left) {
		double count = 0;
		Map<Integer, List<Integer>> rmap = GraphModelM.map.get(a);
		if(rmap != null){
			List<Integer> rs = rp.getRelations();
			int relation = rs.get(rs.size() - left);
			if (left <= 0) {
				try {
					throw new Exception("pathcount error!");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return 0;
			}
			if (left == 1) {
				if (rmap.containsKey(relation) && rmap.get(relation).contains(b))
					return 1;
				else
					return 0;
			}
			
			List<Integer> nodes = rmap.get(relation);
			if(nodes == null)
				return 0;
			for (int node : nodes) {
				count += getCount(node, b, rp, left - 1);
			}
		}

		return count;
	}

	public static void main(String[] args) {
		GraphModelM.initializeMap();
		System.out.println("start to calculate");
		PathCount pc = new PathCount();
		List<Integer> relations = new ArrayList<Integer>();
		relations.add(3465221);
		relations.add(-3465221);
		relations.add(3465221);
		relations.add(-3465221);

		RelationPath rp = new RelationPath(relations);

		System.out.println(pc.getSim(219426, 1155765, rp));
	}

}
