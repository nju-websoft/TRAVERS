package dblp;

import java.sql.*;

import JDBCUtils.JdbcUtil;

public class typesinfosBuilder {
	static Connection conn = JdbcUtil.getConnection();
	
	public static void main(String[] args) {
		try {
			Statement stmt = conn.createStatement();
			String sql = "insert into onttypesinfos (entity,type) ";
			
			int i = 0;
			int num = 0;
			while(num < 3465220){
				int entity = num + 1;
				int type = -1;
				if(0 == i){
					if(num < 1354999){
						type = 3465225;
					}
					else if(num < 3454870){
						type = 3465226;
					}
					else{
						type = 3465227;
					}
					sql += "values(" + entity + "," + type + ")";
					i ++;
				}
				else if(i < 828){
					if(num < 1354999){
						type = 3465225;
					}
					else if(num < 3454870){
						type = 3465226;
					}
					else{
						type = 3465227;
					}
					sql += ",(" + entity + "," + type + ")";
					i ++;
				}
				else{
					System.out.println(num);
					if(num < 1354999){
						type = 3465225;
					}
					else if(num < 3454870){
						type = 3465226;
					}
					else{
						type = 3465227;
					}
					sql += ",(" + entity + "," + type + ")";
					stmt.execute(sql);
					
					i = 0;
					sql = "insert into onttypesinfos (entity,type) ";
				}
				num ++;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
