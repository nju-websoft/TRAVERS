package GraphData;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import JDBCUtils.JdbcUtil;

/**
 * get entities of an assigned class
 * @author anonymous
 *
 */
public class GraphClassInstancesM {
	static Map<Integer, List<Integer>> map = null;
	static Connection conn = JdbcUtil.getConnection();
	public static void initializeMap(){
		if(null == map){
			map = new HashMap<Integer, List<Integer>>();

			//String sql = "select * from typesinfos";
			String sql = "select * from onttypesinfos";
			Statement stmt;
			try {
				stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				while(rs.next()){
					int entity = rs.getInt("entity");
					int type = rs.getInt("type");
					
					if(!map.containsKey(type)){
						List<Integer> list = new ArrayList<Integer>();
						list.add(entity);
						map.put(type, list);
					}
					else{
						List<Integer> list = map.get(type);
						list.add(entity);
						map.put(type, list);
					}
				}

				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			System.out.println("class instances loaded!");
		}
	}
	
	public static List<Integer> getInstances(int type){
		List<Integer> list = map.get(type);
		if(list != null)
			return list;
		else
			return new ArrayList<Integer>();
	}
	
	public static void main(String[] args) {
		GraphClassInstancesM.initializeMap();
		System.out.println(GraphClassInstancesM.getInstances(3481461));
	}
}
