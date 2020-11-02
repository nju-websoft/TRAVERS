package dblp;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import JDBCUtils.JdbcUtil;

public class PCBuilder {
	static Connection conn = JdbcUtil.getConnection();
	static Map<String, Integer> map = getMap.get();
	public static void main(String[] args) {
		String sql = "select * from paper";
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			int i = 0, num = 0;
			String sql0 = "insert into objtriples (subject,predicate,object) ";
			Statement stmt0 = null;
			stmt0 = conn.createStatement();
			while(rs.next()){// 共2099804条有效数据
				String subject = rs.getString("paper_key");
				String object = rs.getString("conference");
				if(object.contains("\""))
					object = object.replace("\"", "");
				if(map.get(subject) != null && map.get(object) != null){
					int s = map.get(subject);
					int p = 3465222;
					int o = map.get(object);
					
					if(0 == i){
						sql0 += "values(" + s + "," + p + "," + o + ")";
						i ++;
					}
					else if(i < 3946){
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
