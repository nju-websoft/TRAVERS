package GraphData;

import JDBCUtils.JdbcUtil;

import java.sql.*;
import java.util.*;

/**
 * 将DBpedia数据加载到内存
 * @author anonymous
 *
 */
@Deprecated //替换为相对节省空间的RelationIndex
public class GraphModelM {
	static Connection conn = JdbcUtil.getConnection();
	public static Map<Integer, Map<Integer, List<Integer>>> map = null;
	
	public static void initializeMap(){
		if(null == map){
			map = new HashMap<Integer, Map<Integer, List<Integer>>>();
			String sql = "select * from objtriples";
			try {
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				while(rs.next()){
					int subject = rs.getInt("subject");
					int predicate = rs.getInt("predicate");
					int object = rs.getInt("object");
					
					if(!map.containsKey(subject)){
						Map<Integer, List<Integer>> rmap = new HashMap<Integer, List<Integer>>();
						List<Integer> nodes = new ArrayList<Integer>();
						nodes.add(object);
						rmap.put(predicate, nodes);
						map.put(subject, rmap);
					}
					else{
						Map<Integer, List<Integer>> rmap = map.get(subject);
						if(rmap.containsKey(predicate)){
							List<Integer> nodes = rmap.get(predicate);
							nodes.add(object);
							rmap.put(predicate, nodes);
						}
						else{
							List<Integer> nodes = new ArrayList<Integer>();
							nodes.add(object);
							rmap.put(predicate, nodes);
						}
						map.put(subject, rmap);
					}
					
					if(!map.containsKey(object)){
						Map<Integer, List<Integer>> rmap = new HashMap<Integer, List<Integer>>();
						List<Integer> nodes = new ArrayList<Integer>();
						nodes.add(subject);
						rmap.put(predicate*-1, nodes);
						map.put(object, rmap);
					}
					else{
						Map<Integer, List<Integer>> rmap = map.get(object);
						if(rmap.containsKey(predicate*-1)){
							List<Integer> nodes = rmap.get(predicate*-1);
							nodes.add(subject);
							rmap.put(predicate*-1, nodes);
						}
						else{
							List<Integer> nodes = new ArrayList<Integer>();
							nodes.add(subject);
							rmap.put(predicate*-1, nodes);
						}
						map.put(object, rmap);
					}
				}
				
				System.out.println("triples loaded! " + map.size());
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		else
			System.out.println("Graph model has already be initialized");
	}
	public boolean isConnected(int v0, int v1){
		if(!map.containsKey(v0) || !map.containsKey(v1))
			return false;
		
		Map<Integer, List<Integer>> rmap = map.get(v0);
		for(int key : rmap.keySet()){
			List<Integer> nodes = rmap.get(key);
			for(int node : nodes){
				if(node == v1)
					return true;
			}
		}
		return false;
	}

	public static Set<Integer> getAllRelations(int id){
		Map<Integer, List<Integer>> m = map.get(id);
		Set<Integer> relations = new HashSet<>();
		if(m != null)
			relations.addAll(m.keySet());

		return  relations;
	}
	
	public static Set<Integer> getAllRelations(Set<Integer> ids){ //返回一个并集，这个方法没什么功能性的作用
		Set<Integer> result = new HashSet<>();
		if(null == ids || ids.size() == 0)
			return result;
		
		for(int id : ids){
			result.addAll(getAllRelations(id));
		}
		return result;
	}

	public static Set<Integer> getAllRelations(int id, Set<Integer> ids){ //去除会出现环路的relation
		Set<Integer> result = new HashSet<>();
		Map<Integer, List<Integer>> rmap = map.get(id);
		if(rmap != null){
			if(ids != null){
				for(int key : rmap.keySet()){
					List<Integer> nodes = rmap.get(key);
					for(int node : nodes){
						if(!ids.contains(node)){
							result.add(key);
							break;
						}

					}
				}
			}
			else
				return rmap.keySet();
		}

		return result;
	}

	public static Map<Integer, List<Integer>> getAllPaos(int id, Set<Integer> ids){//ids用来记录路径上已经出现过的点，避免路径上形成环路
		Map<Integer, List<Integer>> result = new HashMap<Integer, List<Integer>>();
		Map<Integer, List<Integer>> rmap = map.get(id);

		if(rmap != null){
			if(ids != null){
				for(int key : rmap.keySet()){
					List<Integer> nodes = rmap.get(key);
					List<Integer> legalnodes = new ArrayList<Integer>();
					for(int node : nodes){ //这里可能需要改成set，不然会很浪费时间，不过这个方法似乎只有在dfs时候会调用到
						if(!ids.contains(node))
							legalnodes.add(node);
					}
					
					result.put(key, legalnodes);
				}
			}
			else
				return rmap;
		}

		return result;
	}

	@Deprecated
	public static Map<Integer, List<Integer>> get(int id){//获取所有给定实体的三元组，一般不用
		return map.get(id);
	}


	//路径去环路， list即为已经访问过的点
	public static List<Integer> getObjects(int subject, int predicate, Set<Integer> list){
		List<Integer> result = new ArrayList<>();
		Map<Integer, List<Integer>> m = map.get(subject);
		if(m != null){
		    List<Integer> objs = m.get(predicate);
		    if(objs != null){
                result.addAll(objs);
            }
        }
        else
			return null;
		
		for(Integer i : list){
			result.remove(i);
		}
		
		return result;
	}
	/**
	 * 
	 * @param subject
	 * @param predicate
	 * @return all objects satisfy the subject and predicate
	 */
	public static List<Integer> getObjects(int subject, int predicate){
		if(map.get(subject) != null)
			return (map.get(subject)).get(predicate);
		else
			return null;
	}
	/**
	 * 
	 * @param subject
	 * @param predicate
	 * @param type
	 * @return all objects have type type satisfy the subject and predicate
	 */
	public static List<Integer> getTypedObjects(int subject, int predicate, int type){ // 这种get方式实在太慢（快的方式又存不下来，空间太大）
		List<Integer> objects = (map.get(subject)).get(predicate);
		List<Integer> result = new ArrayList<Integer>();
		if(objects != null){
            for(int object : objects){
                Set<Integer> types = GraphOntGetterM.classOfEntityByID(object);
                if(types.contains(type))
                    result.add(object);
            }
        }
		
		return result;
	}

	//采样计算sig时使用，如果直接用getTypedObjects当objects多的时候会非常慢，所以这里限制objects数量不超过一百
	public static List<Integer> getMPObjects(int subject, int predicate, int type, int window){
		List<Integer> objects = (map.get(subject)).get(predicate);
		List<Integer> result = new ArrayList<Integer>();

		if(null == objects) return result;
		else{
			if(objects.size() <= window){
				for(int object : objects){
					Set<Integer> types = GraphOntGetterM.classOfEntityByID(object);
					if(types.contains(type))
						result.add(object);
				}
			}
			else{
				Random rand = new Random();
				int start = rand.nextInt(objects.size());
				List<Integer> nlist = new ArrayList<>();
				if((start + window - 1) < objects.size()){
					nlist.addAll(objects.subList(start, start + window));
				}
				else{
					int remain = start + window - 1 - objects.size();
					nlist.addAll(objects.subList(0, remain));
					nlist.addAll(objects.subList(start, objects.size()));
				}

				for(int object : nlist){
					Set<Integer> types = GraphOntGetterM.classOfEntityByID(object);
					if(types.contains(type))
						result.add(object);
				}
			}
		}



		return result;
	}
	
	public static void main(String[] args) {
		GraphModelM.initializeMap();
		//System.out.println(GraphModelM.map.get(1349999));
		//System.out.println(GraphModelM.getObjects(1562340, 3480880));
		System.out.println(GraphModelM.getAllRelations(985891, new HashSet<Integer>()));
	}
}
