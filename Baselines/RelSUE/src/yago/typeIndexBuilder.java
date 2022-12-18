package yago;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import JDBCUtils.JdbcUtil;

public class typeIndexBuilder {
	static Connection conn = JdbcUtil.getConnection();
	static Set<String> types = new HashSet<String>();
	
	public static void main(String[] args) {
		FileReader reader;
		try {
			reader = new FileReader("H:/myself/yago/coreType.ttl");
			BufferedReader br = new BufferedReader(reader);
			String str = null;
			int i = 0;
			while((str = br.readLine()) != null){
				String s = str.split("\t")[1];
				//if(s.contains("\\"))
				//	System.out.println(s);
				s = s.split(" ")[0];
				types.add(s);
				i ++;
				if(i % 1000000 == 0)
					System.out.println(i);
			}
			System.out.println(i);
			System.out.println(types.size());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		java.sql.Statement stmt = null;
		try {
			stmt = conn.createStatement();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String sql = "insert into mapping (uri) ";
		int i = 0;
		int num = 0;
		Iterator<String> it0 = types.iterator();
		while(it0.hasNext()){
			String uri = it0.next();
			if(num < 573000){
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
		}
	}
}
