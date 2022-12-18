package Path;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import GraphData.GraphModelM;

public class BasicSymmetricEnumerateFinder implements PathFinder {
	int diameter = 4;
	@Override
	public Set<RelationPath> findRelationPath(int query, List<Integer> examples) {
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
		// TODO Auto-generated method stub
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
		Map<Integer, List<Integer>> paos = GraphModelM.getAllPaos(a, ids);
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
						set.add(new RelationPath(rs));
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
				Set<RelationPath> paths = rpFind(node, b, len - 1, ids0);
				for(RelationPath path : paths){
					List<Integer> rs = new ArrayList<Integer>();
					rs.add(r);
					rs.addAll(path.getRelations());
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
		for(int i = 1; i <= len/2; i ++){
			Set<Integer> ids = new HashSet<>();
			set.addAll(rpFind(a, b, i*2, ids));
		}
		Set<RelationPath> set0 = new HashSet<RelationPath>();
		for(RelationPath rp: set){
			if(rp.isSymmetric())
				set0.add(rp);
		}
		return set0;
	}

	public int getDiameter() {
		return diameter;
	}

	public void setDiameter(int diameter) {
		this.diameter = diameter;
	}
	
	public static void main(String[] args) {
		BasicSymmetricEnumerateFinder bsef = new BasicSymmetricEnumerateFinder();
		bsef.setDiameter(4);
		
		GraphModelM.initializeMap();
		
		int query = 1562340;
		List<Integer> examples = new ArrayList<Integer>();
		examples.add(2580708);
		//examples.add(1688237);
		//examples.add(2161684);
		
		System.out.println("start to find");
		Set<RelationPath> set = bsef.findRelationPath(query, examples);
		
		System.out.println(set.size());
		for(RelationPath rp : set)
			System.out.println(rp);
	}
}
