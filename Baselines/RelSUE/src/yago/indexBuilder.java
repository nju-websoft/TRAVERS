package yago;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import JDBCUtils.JdbcUtil;

public class indexBuilder {
	static Connection conn = JdbcUtil.getConnection();
	static Set<String> entities = new HashSet<String>();
	static Set<String> relations = new HashSet<String>();
	
	public static void main(String[] args) {
		FileReader reader;
		try {
			reader = new FileReader("H:/myself/yago/yagoFacts.ttl");
			BufferedReader br = new BufferedReader(reader);
			String str = null;
			while((str = br.readLine()) != null){
				if(!str.contains("#")){
					String[] ss = str.split("\t");
					entities.add(ss[0]);
					entities.add(ss[2].split(" ")[0]);
					relations.add(ss[1]);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(entities.size());
		System.out.println(relations.size());
		System.out.println(relations);
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
		Iterator<String> it0 = entities.iterator();
		while(it0.hasNext()){
			String uri = it0.next();
			//if(uri.contains("\\\\"))
			//	System.out.println(uri);
			if(0 == i){
				sql = sql + "values(\"" + uri + "\")";
				i ++;
			}
			else if(i < 1204){
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
			num ++;
		}
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
		try {
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
