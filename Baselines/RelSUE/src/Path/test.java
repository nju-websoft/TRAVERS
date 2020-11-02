package Path;

import GraphData.GraphModelM;

import java.util.*;

public class test {
	static int MAX_LEN = 4;
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
		for(int i = 1; i <= len; i ++){
			Set<Integer> ids = new HashSet<>();
			set.addAll(rpFind(a, b, i, ids));
		}
		return set;
	}
	
	public static void main(String[] args) {
		GraphModelM.initializeMap();
		System.out.println("start to find");
		//1562340 LBJ, 1688237 KB, 2161684 DW, 2580708 JC
		//1853308 NBA
		Set<RelationPath> rps = test.rpFindAll(1562340, 2580708, test.MAX_LEN);
		System.out.println(rps.size());
		for(RelationPath rp : rps)
			System.out.println(rp);
		rps = test.rpFindAll(2580708, 1562340, test.MAX_LEN);
		System.out.println(rps.size());
		for(RelationPath rp : rps)
			System.out.println(rp);
		rps = test.rpFindAll(2580708, 2161684, test.MAX_LEN);
		System.out.println(rps.size());
		for(RelationPath rp : rps)
			System.out.println(rp);
		rps = test.rpFindAll(2580708, 2580708, test.MAX_LEN);
		System.out.println(rps.size());
		for(RelationPath rp : rps)
			System.out.println(rp);
	}
}
