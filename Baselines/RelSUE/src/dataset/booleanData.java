package dataset;

import java.sql.*;
import java.io.*;

import JDBCUtils.JdbcUtil;

/**
 * 布尔型数据在此处生成，排序型数据可用basicranker生成
 * @author anonymous
 *
 */

public class booleanData {
	static Connection conn = JdbcUtil.getConnection();
	
	public static void output(String fileName){
		try {
			Statement stmt = conn.createStatement();
			String sql = "(select subject from objtriples where object in (select object from objtriples where subject=1497205 and predicate=3480835) and predicate=3480835) and predicate=3480838 and object in (select object from objtriples where predicate=3480838 and subject=1497025)";
			ResultSet rs = stmt.executeQuery(sql);
			int i = 0;
			while(rs.next()){
				System.out.print(rs.getInt("subject") + "\t");
				i ++;
			}
			System.out.println(i);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		output("1.txt");
	}
	

}
