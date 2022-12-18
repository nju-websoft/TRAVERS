package linkedMDB;

import java.sql.*;
import java.util.*;

import JDBCUtils.JdbcUtil;
import JDBCUtils.JdbcUtil_ldx;

public class indexBuilder {
	static Connection conn = JdbcUtil.getConnection();
	static Connection conn_ldx = JdbcUtil_ldx.getConnection();
	static Set<String> entities = new HashSet<>();
	static Set<String> relations = new HashSet<>();
	static Set<String> types = new HashSet<>();
	
	public static void main(String[] args) {
		try {
			Statement stmt_ldx = conn_ldx.createStatement();
			String sql0 = "select * from instance_type";
			System.out.println("execute sql0...");
			long start0 = System.currentTimeMillis();
			ResultSet rs0 = stmt_ldx.executeQuery(sql0);
			while(rs0.next()){
				String instance = rs0.getString("instance");
				entities.add(instance);
				String type = rs0.getString("class");
				types.add(type);
			}
			System.out.println("query0 time: " + (System.currentTimeMillis() - start0));

			String sql1 = "select * from full_form_association";
			System.out.println("execute sql1...");
			long start1 = System.currentTimeMillis();
			ResultSet rs1 = stmt_ldx.executeQuery(sql1);
			while(rs1.next()){
				String subject = rs1.getString("subject");
				String object = rs1.getString("object");
				String predicate = rs1.getString("predicate");
				
				entities.add(subject);
				entities.add(object);
				
				relations.add(predicate);
			}
			System.out.println("query1 time "+ (System.currentTimeMillis() - start1));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(entities.size()); // 1326934
		System.out.println(relations.size()); // 69
		System.out.println(types.size()); // 53
		
		java.sql.Statement stmt = null;
		try {
			stmt = conn.createStatement();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String sql = "insert into mapping (uri) ";
		/*int i = 0;
		int num = 0;
		Iterator<String> it0 = entities.iterator();
		while(it0.hasNext()){
			String uri = it0.next();
			if(uri.contains("\"")){
				uri = uri.replace("\"", "\\\"");
			}
			if(num < 1326000){
				if(0 == i){
					sql = sql + "values(\"" + uri + "\")";
					i ++;
				}
				else if(i < 999){
					sql = sql + ",(\"" + uri + "\")";
					i ++;
				}
				else{
					System.out.println(num);
					sql = sql + ",(\"" + uri + "\")";
					//System.out.println(sql);
					try {
						stmt.execute(sql);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.out.println(sql);
						return;
					}
					i = 0;
					sql = "insert into mapping (uri) ";
				}
			}
			else{
				sql = "insert into mapping (uri) " + "values(\"" + uri + "\")";
				try {
					stmt.execute(sql);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			num ++;
		}*/
		System.out.println("entities is done");
		
		Iterator<String> it1 = relations.iterator();
		while(it1.hasNext()){
			String uri = it1.next();
			sql = "insert into mapping (uri) values(\"" + uri + "\")";
			try {
				stmt.execute(sql);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("relations is done");
		Iterator<String> it2 = types.iterator();
		while(it2.hasNext()){
			String uri = it2.next();
			sql = "insert into mapping (uri) values(\"" + uri + "\")";
			try {
				stmt.execute(sql);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("types is done");
		//TODO conferences index
		try {
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
