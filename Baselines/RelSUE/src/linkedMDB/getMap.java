package linkedMDB;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import JDBCUtils.JdbcUtil;

public class getMap {
	static Connection conn = JdbcUtil.getConnection();
	
	//返回(uri,id)的map
	public static Map<String,Integer> get(){
		String sql = "select * from mapping";
		Map<String,Integer> map = new HashMap<String,Integer>();
		int i = 0;
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			
			while(rs.next()){
				String uri = rs.getString("uri");
				int id = rs.getInt("id");
				
				map.put(uri, id);
				i ++;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
	}
	
	//返回(id,uri)的map
	public static Map<Integer, String> getReverse(){
		String sql = "select * from mapping";
		Map<Integer,String> map = new HashMap<Integer,String>();
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			
			while(rs.next()){
				String uri = rs.getString("uri");
				int id = rs.getInt("id");
				
				map.put(id, uri);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;
	}
	
	public static void main(String[] args) {
		Map<String,Integer> map = get();
		System.out.println(map.size());
		System.out.println(map.get("<http://xmlns.com/foaf/0.1/Agent>"));
		System.out.println(map.get("hehe"));
	}
}
