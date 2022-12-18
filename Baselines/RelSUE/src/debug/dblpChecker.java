package debug;

import java.util.*;
import java.sql.*;

import GraphData.LabelGetter;
import JDBCUtils.JdbcUtil;

public class dblpChecker {
	static Connection conn = JdbcUtil.getConnection();
	static List<Integer> list0 = new ArrayList<Integer>();
	static List<Integer> list1 = new ArrayList<Integer>();
	public static void main(String[] args) {
		try {
			Statement stmt = conn.createStatement();
			String sql = "select id from mapping where uri in (select paper_key from author where name=\"jiawei han 0001\")";
			ResultSet rs = stmt.executeQuery(sql);
			while(rs.next()){
				list0.add(rs.getInt("id"));
			}
			sql = "select subject from objtriples where subject in (select id from mapping where uri in (select paper_key from author where name=\"jiawei han 0001\"))";
			rs = stmt.executeQuery(sql);
			while(rs.next()){
				list1.add(rs.getInt("subject"));
			}
					
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(int i : list0){
			if(!list1.contains(i)){
				System.out.println(i);
				System.out.println(LabelGetter.get(i));
			}
		}
	}
}
