package Path;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import GraphData.GraphModelM;
//import GraphData.GraphOntGetter;
import GraphData.GraphOntGetterM;
//import GraphData.GraphOntologyM;
import GraphData.Ontology;

public class BasicEnumerateFinder implements PathFinder {
	int diameter = 4;
	public static int op_count = 0;


	@Override
	public Set<RelationPath> findRelationPath(int query, List<Integer> examples) { // findRelationPath->rpFindAll->rpFind
		if(examples != null && examples.size() > 0){
			Set<RelationPath> set = rpFindAll(query, examples.get(0), this.diameter);
			for(int i = 1; i < examples.size(); i ++){
				set.retainAll(rpFindAll(query, examples.get(i), this.diameter));
			}
			return set;
		}			
		else
			return null;
	}

	@Override
	public Set<MetaPath> findMetaPath(int query, List<Integer> examples) {
		if(examples != null && examples.size() > 0){
			Set<MetaPath> set = mpFindAll(query, examples.get(0), this.diameter);
			System.out.println(set);
			for(int i = 1; i < examples.size(); i ++){
				Set<MetaPath> s = mpFindAll(query, examples.get(i), this.diameter);
				System.out.println(s);
				set.retainAll(s);
			}
			return set;
		}			
		else
			return null;
	}
	
	/**
	 * 
	 * @param a
	 * @param b
	 * @param len
	 * @return all distinguished relation paths between a and b with length equals to len
	 */
	public static Set<RelationPath> rpFind(int a, int b, int len, Set<Integer> ids){
		Set<RelationPath> set = new HashSet<RelationPath>();
		Map<Integer, List<Integer>> paos = GraphModelM.getAllPaos(a, ids);//考虑去环路 ###########################
		//Map<Integer, List<Integer>> paos = GraphModelM.getAllPaos(a, null);//不考虑去环路
		//Map<Integer, List<Integer>> paos = GraphModelM.map.get(a);
		//List<PredAndObj> paos = DbpediaModelM.map.get(a);
		if(len <= 0)
			return null;
		if(1 == len){
			for(int r : paos.keySet()){
				List<Integer> nodes = paos.get(r);
				for(int node : nodes){
					op_count ++;
					if(b == node){
						List<Integer> rs = new ArrayList<Integer>();
						rs.add(r);
						set.add(new RelationPath(rs));
					}
				}
			}

			return set;
		}
		
		for(int r : paos.keySet()){
			List<Integer> nodes = paos.get(r);
			if(3 == len)
				System.out.println("relation: " + r + "  size: " + nodes.size());
			for(int node : nodes){
				op_count ++;
				Set<Integer> ids0 = new HashSet<>();
				ids0.addAll(ids);
				//op_count += ids.size();
				ids0.add(a);
				//if(4 == len)
				//	System.out.println("relation: " + r + ", node: " + node);
				Set<RelationPath> paths = rpFind(node, b, len - 1, ids0);
				for(RelationPath path : paths){
					//op_count +=3;
					List<Integer> rs = new ArrayList<Integer>();
					rs.add(r);
					rs.addAll(path.getRelations());
					//op_count += path.getRelations().size();
					set.add(new RelationPath(rs));
				}
			}
		}
		
		return set;
	}
	/**
	 * 
	 * @param a
	 * @param b
	 * @param len
	 * @return all distinguished relation paths between a and b with length equals no larger than len
	 */
	public static Set<RelationPath> rpFindAll(int a, int b, int len){
		Set<RelationPath> set = new HashSet<RelationPath>();
		for(int i = 1; i <= len; i ++){
			Set<Integer> ids = new HashSet<>();
			set.addAll(rpFind(a, b, i, ids));
		}
		return set;
	}
	/**
	 * 
	 * @param a
	 * @param b
	 * @param len
	 * @param ids
	 * @return all distinguished meta paths between a and b with length equals to len
	 */
	public static Set<MetaPath> mpFind(int a, int b, int len, Set<Integer> ids){
		Set<MetaPath> set = new HashSet<MetaPath>();
		Map<Integer, List<Integer>> paos = GraphModelM.getAllPaos(a, ids);//考虑去环路
		//Map<Integer, List<Integer>> paos = GraphModelM.getAllPaos(a, null);//不考虑去环路
		
		
		//List<PredAndObj> paos = DbpediaModelM.map.get(a);
		if(len <= 0)
			return null;
		if(1 == len){
			for(int r : paos.keySet()){
				List<Integer> nodes = paos.get(r);
				for(int node : nodes){
					if(b == node){
						List<Integer> rs = new ArrayList<Integer>();
						rs.add(r);
						//Set<Integer> atypes = GraphOntGetterM.classOfEntityByID(a);
						//Set<Integer> btypes = GraphOntGetterM.classOfEntityByID(b);
						List<Integer> atypes = GraphOntGetterM.basicClassOfEntityByID(a);
						List<Integer> btypes = GraphOntGetterM.basicClassOfEntityByID(b);
						for(int atype : atypes){
							for(int btype : btypes){
								List<Integer> concepts = new ArrayList<>();
								concepts.add(atype);
								concepts.add(btype);
								
								set.add(new MetaPath(concepts, rs));
							}
						}
					}
				}
			}

			return set;
		}
		
		for(int r : paos.keySet()){
			List<Integer> nodes = paos.get(r);
			for(int node : nodes){
				Set<Integer> ids0 = new HashSet<>();
				ids0.addAll(ids);
				ids0.add(a);
				Set<MetaPath> paths = mpFind(node, b, len - 1, ids0);
				for(MetaPath path : paths){
					List<Integer> rs = new ArrayList<Integer>();
					rs.add(r);
					rs.addAll(path.getRelations());
					
					//Set<Integer> nodeTypes = GraphOntGetterM.classOfEntityByID(a);
					List<Integer> nodeTypes = GraphOntGetterM.basicClassOfEntityByID(a);
					for(int nodeType : nodeTypes){
						List<Integer> concepts = new ArrayList<Integer>();
						concepts.add(nodeType);
						concepts.addAll(path.getConcepts());
						
						set.add(new MetaPath(concepts, rs));
					}
				}
			}
		}
		
		return set;
	}
	/**
	 * 
	 * @param a
	 * @param b
	 * @param len
	 * @return all distinguished meta paths between a and b with length equals no larger than len
	 */
	public static Set<MetaPath> mpFindAll(int a, int b, int len){
		Set<MetaPath> set = new HashSet<MetaPath>();
		for(int i = 1; i <= len; i ++){
			Set<Integer> ids = new HashSet<>();
			set.addAll(mpFind(a, b, i, ids));
		}
		return set;
	}

	public int getDiameter() {
		return diameter;
	}

	public void setDiameter(int diameter) {
		this.diameter = diameter;
	}
	
	public static void main(String[] args) {
		BasicEnumerateFinder be = new BasicEnumerateFinder();
		be.setDiameter(4);
		GraphModelM.initializeMap();
		//Ontology.Initialize();
		//GraphOntGetterM.initializeMap(3480806);
		//GraphOntGetterM.initializeMap();

		// dbpedia query
		//int query = 630840; //united states

		int query = 4103860; // yago query
		//int query = 2291382; // united states

		List<Integer> examples = new ArrayList<Integer>();

		examples.add(2125240); // yago example

		
		System.out.println("start to find");
		long start = System.currentTimeMillis();
		Set<RelationPath> set = be.findRelationPath(query, examples);
		//Set<MetaPath> set = be.findMetaPath(query, examples);
		long end = System.currentTimeMillis();
		//for(RelationPath rp : set)
		//	System.out.println(rp);

		System.out.println(set.size());
		//System.out.println(op_count);
		System.out.println("time: " + (end - start));
		
		/*Set<MetaPath> mpset = be.findMetaPath(query, examples);
		
		System.out.println(mpset.size());
		for(MetaPath mp : mpset)
			System.out.println(mp);*/
	}

}
