package yago;

import java.sql.*;
import java.util.*;

import JDBCUtils.JdbcUtil;

public class typeModifier {
	static Connection conn = JdbcUtil.getConnection();
	public static void main(String[] args) {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int count = 0;
		for(int i = 4295863; i <= 4869452; i ++){
			String sql = "select * from mapping where id="  + i;
			String type = null;
			try {
				ResultSet rs = stmt.executeQuery(sql);
				while(rs.next()){
					type = rs.getString("uri");
					int len = type.length();
					if(type.charAt(len - 2) == ' ' && type.charAt(len - 1) == '.'){
						type = type.substring(0, len - 2);
					}
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(type);
			sql = "update mapping set uri=\"" + type + "\" where id=" + i;
			try {
				stmt.execute(sql);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			count ++;
		}
		System.out.println(count);
	}
}
