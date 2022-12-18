package dblp;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;


import JDBCUtils.JdbcUtil;

public class indexBuilder {
	static Connection conn = JdbcUtil.getConnection();
	static Set<String> authors = new HashSet<String>();
	static Set<String> papers = new HashSet<String>();
	static Set<String> confs = new HashSet<String>();
	public static void main(String[] args) {
		String sql = "select * from author";
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			
			while(rs.next()){
				String name = rs.getString("name");
				if(name.contains("\\"))
					name = name.replace("\\", "");
				authors.add(name);	
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sql = "select * from conference";
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			
			while(rs.next()){
				String conf = rs.getString("conf_key");
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
				String paper = rs.getString("paper_key");
				papers.add(paper);	
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(authors.size());//1348918    1354999
		System.out.println(confs.size());//35535 for 9720  35706 for 10350
		System.out.println(papers.size());//2086136 2099871 
		
		java.sql.Statement stmt = null;
		try {
			stmt = conn.createStatement();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		sql = "insert into mapping (uri) ";
		int i = 0;
		int num = 0;
		Iterator<String> it0 = authors.iterator();
		while(it0.hasNext()){
			String uri = it0.next();
			if(uri.contains("\"")){
				uri = uri.replace("\"", "\\\"");
			}
			if(num < 1354000){
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
		}
		System.out.println("authors is done");
		Iterator<String> it1 = papers.iterator();
		sql = "insert into mapping (uri) ";
		i = 0;
		num = 0;
		while(it1.hasNext()){
			String uri = it1.next();
			if(uri.contains("\"")){
				uri = uri.replace("\"", "\\\"");
			}
			if(num < 2099000){
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
