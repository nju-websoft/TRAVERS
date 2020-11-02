package Path;

import java.util.*;

import GraphData.GraphModelM;

public class BFSFinder implements PathFinder {
	int diameter = 4;
	@Override
	public Set<RelationPath> findRelationPath(int query, List<Integer> examples) {
		return null;
	}
	
	public void setDiameter(int d){
		this.diameter = d;
	}
	
	public Set<RelationPath> RP_bfs(int query, int example){//没考虑去除环路的BFS
		Map<RelationPath, Set<Integer>> map = new HashMap<>();
		Set<Integer> init = new HashSet<>();
		init.add(query);
		map.put(new RelationPath(new ArrayList<Integer>()), init);
		Set<RelationPath> result = new HashSet<>();
		for(int i = 0; i < this.diameter; i ++){
			Map<RelationPath, Set<Integer>> map_i = new HashMap<>();
			for(RelationPath rp : map.keySet()){
				Set<Integer> nodes = map.get(rp);
				Set<Integer> relations = GraphModelM.getAllRelations(nodes);
				for(int r : relations){
					List<Integer> rs = new ArrayList<>();
					rs.addAll(rp.getRelations());
					rs.add(r);
					RelationPath path = new RelationPath(rs);
					Set<Integer> objs = new HashSet<>();
					for(int node : nodes){
						List<Integer> wtf = GraphModelM.getObjects(node, r);
						if(wtf != null)
						objs.addAll(wtf);
					}
					if(objs.contains(example)){
						result.add(path);
					}
					
					map_i.put(path, objs);
				}
			}
			map = map_i;
		}
		
		return result;
	}

	public Set<RelationPath> RP_bfs_acyclic(int query, int example){// 考虑去除环路的BFS
		int op_count = 0;
		Map<RelationPath, Map<Integer, List<Set<Integer>>>> map = new HashMap<>();
		Map<Integer, List<Set<Integer>>> init = new HashMap<>();
		//List<List<Integer>> ilist = new ArrayList<>();
		List<Set<Integer>> ilist = new LinkedList<>();
		Set<Integer> iid = new HashSet<>();
		iid.add(query);
		ilist.add(iid);
		init.put(query, ilist);
		map.put(new RelationPath(new ArrayList<Integer>()), init);
		Set<RelationPath> result = new HashSet<>();
		//op_count += 5;
		for(int i = 0; i < this.diameter; i ++){
			Map<RelationPath, Map<Integer, List<Set<Integer>>>> map_i = new HashMap<>();
			//System.out.println("i: " + i);
			for(RelationPath rp : map.keySet()){
				Map<Integer, List<Set<Integer>>> nodesMap = map.get(rp);
				Set<Integer> relations = GraphModelM.getAllRelations(nodesMap.keySet());
				//op_count += 3*nodesMap.keySet().size();
				//System.out.println("relations: " + relations);
				for(int r : relations){
					List<Integer> rs = new ArrayList<>();
					rs.addAll(rp.getRelations());
					rs.add(r);
					//op_count += (rp.getRelations().size() + 4);
					RelationPath path = new RelationPath(rs);
					Map<Integer, List<Set<Integer>>> objsMap = new HashMap<>();
					for(int node : nodesMap.keySet()){
						List<Set<Integer>> idss = nodesMap.get(node);
						for(Set<Integer> ids : idss){
							List<Integer> wtf = GraphModelM.getObjects(node, r, ids);
							if(wtf != null){
								//op_count += wtf.size()*ids.size();
								for(int w : wtf){
									if(objsMap.containsKey(w)){
										List<Set<Integer>> l = objsMap.get(w);
										Set<Integer> nids = new HashSet<>();
										//System.out.println(ids.size());
										nids.addAll(ids);
										nids.add(w);
										l.add(nids);
										objsMap.put(w, l);
										//op_count += (5 + ids.size());
										op_count ++;
										//System.out.println("added");
									}
									else{
										List<Set<Integer>> l = new LinkedList<>();
										Set<Integer> nids = new HashSet<>();
										nids.addAll(ids);
										nids.add(w);
										l.add(nids);
										objsMap.put(w, l);
										//op_count += (5 + ids.size());
										op_count ++;
									}
								}
							}
						}

					}
					if(objsMap.containsKey(example)){
						result.add(path);
						//op_count ++;
					}

					map_i.put(path, objsMap);
					//op_count ++;
				}
			}
			map = map_i;
			//op_count ++;
		}
		System.out.println("op_count: " + op_count);
		return result;
	}


	@Override
	public Set<MetaPath> findMetaPath(int query, List<Integer> examples) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	public static void main(String[] args){
		GraphModelM.initializeMap();
		BFSFinder bfsf = new BFSFinder();
		bfsf.setDiameter(2);
		//Set<RelationPath> set = bfsf.RP_bfs(1562340, 2580708);
		long startTime = System.currentTimeMillis();
		Set<RelationPath> set = bfsf.RP_bfs_acyclic(2291382, 2125240);
		//for(int i = 0; i < 9 ; i ++)
		//	bfsf.RP_bfs_acyclic(1562340, 2580708);
		System.out.println("finished in " + (System.currentTimeMillis()-startTime)+" ms");
		System.out.println(set.size());
		System.out.println(set);
	}
	
	

}
