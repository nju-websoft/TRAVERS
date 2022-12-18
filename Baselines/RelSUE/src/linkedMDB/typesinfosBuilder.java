package linkedMDB;

import java.util.*;
import java.sql.*;

import JDBCUtils.JdbcUtil;
import JDBCUtils.JdbcUtil_ldx;

public class typesinfosBuilder {
	static Connection conn = JdbcUtil.getConnection();
	static Connection conn_ldx = JdbcUtil_ldx.getConnection();
	static Map<String, Integer> map = getMap.get();
	static List<tuple> list = new ArrayList<>();
	
	public static void main(String[] args){
		try {
			Statement stmt_ldx = conn_ldx.createStatement();
			String sql0 = "select * from instance_type";
			System.out.println("execute sql0...");
			long start0 = System.currentTimeMillis();
			ResultSet rs0 = stmt_ldx.executeQuery(sql0);
			while(rs0.next()){
				String entity = rs0.getString("instance");
				String type = rs0.getString("class");
				
				int e = map.get(entity.replace("\\", ""));
				int t = map.get(type);
		
				tuple tp = new tuple(e, t);
				list.add(tp);
			}
			System.out.println("query0 time: " + (System.currentTimeMillis() - start0));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(list.size()); // 740469
		
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String sql = "insert into onttypesinfos (entity,type) ";
		int i = 0;
		int num = 0;
		Iterator<tuple> it = list.iterator();
		while(it.hasNext()){
			tuple tp = it.next();
			int ent = tp.entity;
			int typ = tp.type;
			//if(uri.contains("\\\\"))
			//	System.out.println(uri);
			if(0 == i){
				sql = sql + "values(" + ent + "," + typ + ")";
				i ++;
			}
			else if(i < 50){
				sql = sql + ",(" + ent + "," + typ + ")";
				i ++;
			}
			else{
				System.out.println(num);
				sql = sql + ",(" + ent + "," + typ + ")";
				//System.out.println(sql);
				try {
					stmt.execute(sql);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				i = 0;
				sql = "insert into onttypesinfos (entity,type) ";
			}
			num ++;
		}
	}
	
	
	static class tuple{
		int entity;
		int type;
		public tuple(int e, int t){
			this.entity = e;
			this.type = t;
		}
	}
}
