package GraphData;

import java.util.*;
import java.sql.*;


import JDBCUtils.JdbcUtil;
/**
 * get type infos of entities in dbpedia
 * @author anonymous
 *
 */
public class GraphOntGetterM {
    static final int SEGMENT = 500000;
	static Map<Integer, Set<Integer>> map = null;
	static Connection conn = JdbcUtil.getConnection();
	static int rootid = -1;

	public static void initializeMap( ){
		if(null == map){
			if(JdbcUtil.URL.contains("yago")) {
				rootid = 4832388;
			}
			else if(JdbcUtil.URL.contains("dbpedia")) {
				rootid = 3481453;
			}

			map = new HashMap<Integer, Set<Integer>>();
			//String sql = "select * from typesinfos";
			Statement stmt;
			try {
				//System.out.println("graphont 23");
				stmt = conn.createStatement();
				//System.out.println("graphont 25");
				String sql = "select * from onttypesinfos" ;
				//System.out.println(sql);
				ResultSet rs = stmt.executeQuery(sql);
				//System.out.println("graphont 34");
				//System.out.println("Begin building GraphOntGetterM");
				while(rs.next()){
					int entity = rs.getInt("entity");
					int type = rs.getInt("type");

					if(!map.containsKey(entity)){
						Set<Integer> set = new HashSet<>();
						set.add(type);
						map.put(entity, set);
					}
					else{
						Set<Integer> set = map.get(entity);
						set.add(type);
						map.put(entity, set);
					}
				}
				//  System.out.println(map.size());

				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


			System.out.println("typesinfos loaded! size:" + map.size());
		}
	}

	public static void initializeMap(int numOfEntities){
	    if(null == map){
	        int k = numOfEntities/SEGMENT;
			map = new HashMap<Integer, Set<Integer>>();
			//String sql = "select * from typesinfos";

			Statement stmt;
			try {
				//System.out.println("graphont 23");
				stmt = conn.createStatement();
				//System.out.println("graphont 25");
				for(int i = 0; i <= k; i ++){
                    int a = i*SEGMENT;
                    int b = (i+1)*SEGMENT;
                    String sql = "select * from onttypesinfos where entity >" + a + " and entity <=" + b;
                    //System.out.println(sql);
				    ResultSet rs = stmt.executeQuery(sql);
                    //System.out.println("graphont 34");
                    while(rs.next()){
                        int entity = rs.getInt("entity");
                        int type = rs.getInt("type");

                        if(!map.containsKey(entity)){
                            Set<Integer> set = new HashSet<>();
                            set.add(type);
                            map.put(entity, set);
                        }
                        else{
                            Set<Integer> set = map.get(entity);
                            set.add(type);
                            map.put(entity, set);
                        }
                    }
                  //  System.out.println(map.size());
                }
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			System.out.println("typesinfos loaded! size:" + map.size());
		}
	}
	
	public static Set<Integer> classOfEntityByID(int id) {
		Set<Integer> set = map.get(id);
		if(set != null)
			return set;
		else{
			Set<Integer> root = new HashSet<>();
			root.add(rootid);
			return root;
		}
	}

	public static Set<Integer> classOfEntityByID_yagoSimple(int id){
		Set<Integer> set = map.get(id);
		Set<Integer> result = new HashSet<>();
		int[] types = {4516273, 4538925, 4544745, 4590295, 4645318, 4683384, 4808597};
		if(set != null){
			for(int t : types){
				if(set.contains(t)){
					result.add(t);
					break;
				}
			}
		}
		if(result.size() == 0){
			result.add(rootid);
		}
		return result;
	}

	public static boolean HasType(int entity, int type){
		Set<Integer> set = map.get(entity);
		if(set == null)
			return false;
		else{
			if(set.contains(type))
				return true;
			else return false;
		}
	}
	private static boolean isBasic(List<Integer> list){
		//List<Integer> list = new ArrayList<Integer>();
		//list.addAll(types);
		for(int i = 0; i < list.size() - 1; i ++){
			for(int j = i + 1; j < list.size(); j ++){
				if(!Ontology.isDescendantOf(list.get(i), list.get(j)) && !Ontology.isDescendantOf(list.get(j), list.get(i)))
					return true;
			}
		}

		return false;
	}

	// 只保留实体最具体的类，去掉hyperconcept
	public static List<Integer> basicClassOfEntityByID(int id){
		if(Ontology.allConcepts.size() == 0){
			try {
				throw new Exception("Please load ontology first!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else{
			List<Integer> list = new ArrayList<>();
			Set<Integer> ss = map.get(id);
			if(ss != null)
				list.addAll(ss);
			Set<Integer> set = new HashSet<Integer>();
			//System.out.println("before: " + list);
			if(list.size() > 0){
				for(int i = 0; i < list.size(); i ++){
					set.add(list.get(i));
					for(int j = 0; j < list.size(); j ++){
						if(i != j){
							if(Ontology.isDescendantOf(list.get(i), list.get(j))){
								set.remove(list.get(j));
							}
							else if(Ontology.isDescendantOf(list.get(j), list.get(i))){
								set.remove(list.get(i));
							}
						}
					}
				}
			}
			list = new ArrayList<>();
			list.addAll(set);
			//System.out.println("after: " + list);
			
			if(list.size() == 0){
				if(JdbcUtil.URL.contains("dbpedia"))
					list.add(3481453);
				else if(JdbcUtil.URL.contains("yago")){
					list.add(4832388);
				}
			}

			return list;
		}
		return null;
	}

	/**
	 * get all the LCA of a list of examples, for most of the case, there is only a unique LCA
	 * @param examples
	 * @return
	 */
	public static List<Integer> LCA(List<Integer> examples){
		Set<Integer> ss = new HashSet<>();
		ss.addAll(classOfEntityByID(examples.get(0)));
		for(int i = 1; i < examples.size(); i ++){
			ss.retainAll(classOfEntityByID(examples.get(i)));
		}

		return basicClassOfEntityBySet(ss);
	}

	public static List<Integer> basicClassOfEntityBySet(Set<Integer> ss){
		if(Ontology.allConcepts.size() == 0){
			try {
				throw new Exception("Please load ontology first!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else{
			List<Integer> list = new ArrayList<>();
			if(ss != null)
				list.addAll(ss);
			Set<Integer> set = new HashSet<Integer>();
			//System.out.println("before: " + list);
			if(list.size() > 0){
				for(int i = 0; i < list.size(); i ++){
					set.add(list.get(i));
					for(int j = 0; j < list.size(); j ++){
						if(i != j){
							if(Ontology.isDescendantOf(list.get(i), list.get(j))){
								set.remove(list.get(j));
							}
							else if(Ontology.isDescendantOf(list.get(j), list.get(i))){
								set.remove(list.get(i));
							}
						}
					}
				}
			}
			list = new ArrayList<>();
			list.addAll(set);
			//System.out.println("after: " + list);

			if(list.size() == 0){
				list.add(Ontology.root.id);
			}

			return list;
		}
		return null;
	}
	
	public static void main(String[] args) {
		GraphOntGetterM.initializeMap(4295825);
		System.out.println(GraphOntGetterM.classOfEntityByID(2878823));
	}

}
