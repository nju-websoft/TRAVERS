package yago;

import java.util.*;
import java.sql.*;
import java.io.*;

import JDBCUtils.JdbcUtil;

public class ontologyBuilder {
	static Connection conn = JdbcUtil.getConnection();
	static Map<String, Integer> map = getMap.get();
	
	public static void main(String[] args) {
		try {
			FileReader reader = new FileReader("H:/myself/yago/yagoTaxonomy.ttl");
			BufferedReader br = new BufferedReader(reader);
			String str;
			Statement stmt = null;
			try {
				stmt = conn.createStatement();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String sql = "insert into ontinfos (child,parent) ";
			int i = 0;
			int num = 0;
			while((str = br.readLine()) != null){// 共1356853条有效数据
				if(!str.contains("#@")){
					String[] ss = str.split("\t");
					String type0 = ss[0];
					String type1 = ss[2].split(" ")[0];
					if(type0.contains("\\u"))
						type0 = type0.replace("\\u", "u");
					if(type0.contains("\\\\"))
						type0 = type0.replace("\\\\", "\\");
					if(type0.contains("\\n"))
						type0 = type0.replace("\\n", "");
					if(type0.equals("<wikicat_Malayalam_Cinematographers>"))
						type0 = "<wikicat_Malayalam_cinematographers>";
					if(type1.contains("\\u"))
						type1 = type1.replace("\\u", "u");
					if(type1.contains("\\\\"))
						type1 = type1.replace("\\\\", "\\");
					if(type1.contains("\\n"))
						type1 = type1.replace("\\n", "");
					if(type1.equals("<wikicat_Malayalam_Cinematographers>"))
						type1 = "<wikicat_Malayalam_cinematographers>";
					
					if(map.get(type0) != null && map.get(type1) != null){
						int t0 = map.get(type0);
						int t1 = map.get(type1);

						if(num < 1356000){
							if(0 == i){
								sql = sql + "values(" + t0 + "," + t1 + ")";
								i ++;
							}
							else if(i < 999){
								sql = sql + ",(" + t0 + "," + t1 + ")";
								i ++;
							}
							else{
								System.out.println(num);
								sql = sql + ",(" + t0 + "," + t1 + ")";
								//System.out.println(sql);
								try {
									stmt.execute(sql);
								} catch (SQLException ee) {
									// TODO Auto-generated catch block
									ee.printStackTrace();
								}
								i = 0;
								sql = "insert into ontinfos (child,parent) ";
							}
						}
						else{
							sql = "insert into ontinfos (child,parent) values(" + t0 + "," + t1 + ")";
							try {
								stmt.execute(sql);
							} catch (SQLException ee) {
								// TODO Auto-generated catch block
								ee.printStackTrace();
							}
						}
						num ++;
					}
					
					
				}
			}
			System.out.println(num);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
