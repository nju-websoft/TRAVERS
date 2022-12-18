package dblp;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import JDBCUtils.JdbcUtil;

/**
 * 不区分会议的年份，只考虑会议的名称
 * @author anonymous
 *
 */
public class confIndexBuilder {
	static Connection conn = JdbcUtil.getConnection();
	static Set<String> confs = new HashSet<String>();
	public static void main(String[] args) {
		String sql = "select * from conference";
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			
			while(rs.next()){
				String conf = rs.getString("name");
				confs.add(conf);	
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sql = "select * from paper";
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			
			while(rs.next()){
				String conf = rs.getString("conference");
				confs.add(conf);	
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(confs.size());
		Iterator<String> it = confs.iterator();
		
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int i = 0, num = 0;
		sql = "insert into mapping (uri) ";
		while(it.hasNext()){
			String con = it.next();
			if(con.contains("\""))
				con = con.replace("\"", "'");
			if(0 == i){
				sql += "values(\"" + con + "\")";
				i ++;
			}
			else if(i < 1034){
				sql = sql + ",(\"" + con + "\")";
				i ++;
			}
			else{
				System.out.println(num);
				sql = sql + ",(\"" + con + "\")";
				//System.out.println(sql);
				try {
					stmt.execute(sql);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println(sql);
				}
				i = 0;
				sql = "insert into mapping (uri) ";
			}
			num ++;
		}
	}
	
}
