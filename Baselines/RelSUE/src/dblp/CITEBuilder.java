package dblp;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import JDBCUtils.JdbcUtil;

public class CITEBuilder {
	static Connection conn = JdbcUtil.getConnection();
	static Map<String, Integer> map = getMap.get();
	public static void main(String[] args) {
		String sql = "select * from citation";
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			int i = 0, num = 0;
			String sql0 = "insert into objtriples (subject,predicate,object) ";
			Statement stmt0 = null;
			stmt0 = conn.createStatement();
			while(rs.next()){// 共43596条有效数据
				String subject = rs.getString("paper_cited_key");
				String object = rs.getString("paper_cite_key");
				if(map.get(subject) != null && map.get(object) != null){
					int s = map.get(subject);
					int p = 3465223;
					int o = map.get(object);
					if(0 == i){
						sql0 += "values(" + s + "," + p + "," + o + ")";
						i ++;
					}
					else if(i < 345){
						sql0 += ",(" + s + "," + p + "," + o + ")";
						i ++;
					}
					else{
						System.out.println(num);
						sql0 += ",(" + s + "," + p + "," + o + ")";
						stmt0.execute(sql0);
						sql0 = "insert into objtriples (subject,predicate,object) ";
						i = 0;
					}
					
					num ++;
				}
							
			}
			System.out.println(num);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
