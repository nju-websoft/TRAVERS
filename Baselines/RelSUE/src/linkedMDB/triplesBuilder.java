package linkedMDB;

import java.util.*;
import java.sql.*;

import JDBCUtils.JdbcUtil;
import JDBCUtils.JdbcUtil_ldx;

public class triplesBuilder {
	static Connection conn = JdbcUtil.getConnection();
	static Connection conn_ldx = JdbcUtil_ldx.getConnection();
	static Map<String, Integer> map = getMap.get();
	static List<triple> list = new ArrayList<triple>();
	
	public static void main(String[] args){
		try {
			Statement stmt_ldx = conn_ldx.createStatement();
			String sql0 = "select * from full_form_association";
			System.out.println("execute sql0...");
			long start0 = System.currentTimeMillis();
			ResultSet rs0 = stmt_ldx.executeQuery(sql0);
			while(rs0.next()){
				String subject = rs0.getString("subject");
				String predicate = rs0.getString("predicate");
				String object = rs0.getString("object");
				
				int s = map.get(subject);
				int p = map.get(predicate);
				int o = map.get(object.replace("\\", ""));;
				
				triple tp = new triple(s, p, o);
				list.add(tp);
			}
			System.out.println("query0 time: " + (System.currentTimeMillis() - start0));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(list.size()); // 2132796
		
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String sql = "insert into objtriples (subject,predicate,object) ";
		int i = 0;
		int num = 0;
		Iterator<triple> it = list.iterator();
		while(it.hasNext()){
			triple tp = it.next();
			int sub = tp.subject;
			int pre = tp.predicate;
			int obj = tp.object;
			//if(uri.contains("\\\\"))
			//	System.out.println(uri);
			if(0 == i){
				sql = sql + "values(" + sub + "," + pre + "," + obj + ")";
				i ++;
			}
			else if(i < 177){
				sql = sql + ",(" + sub + "," + pre + "," + obj + ")";
				i ++;
			}
			else{
				System.out.println(num);
				sql = sql + ",(" + sub + "," + pre + "," + obj + ")";
				//System.out.println(sql);
				try {
					stmt.execute(sql);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				i = 0;
				sql = "insert into objtriples (subject,predicate,object) ";
			}
			num ++;
		}
	}
	
	
	static class triple{
		int subject;
		int predicate;
		int object;
		public triple(int s, int p, int o){
			this.subject = s;
			this.predicate = p;
			this.object = o;
		}
	}
}
