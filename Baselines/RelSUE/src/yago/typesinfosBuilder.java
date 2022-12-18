package yago;

import java.util.*;
import java.sql.*;
import java.io.*;

import JDBCUtils.JdbcUtil;

public class typesinfosBuilder {
	static Connection conn = JdbcUtil.getConnection();
	static Map<String, Integer> map = getMap.get();
	
	public static void main(String[] args) {
		try {
			FileReader reader = new FileReader("H:/myself/yago/coreType.ttl");
			BufferedReader br = new BufferedReader(reader);
			String str;
			Statement stmt = null;
			try {
				stmt = conn.createStatement();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String sql = "insert into onttypesinfos (entity,type) ";
			int i = 0;
			int num = 0;
			while((str = br.readLine()) != null){
				String[] ss = str.split("\t");
				String entity = ss[0];
				String type = ss[1].split(" ")[0];
				if(entity.contains("\\u"))
					entity = entity.replace("\\u", "u");
				if(entity.contains("\\\\"))
					entity = entity.replace("\\\\", "\\");
				if(type.contains("\\u"))
					type = type.replace("\\u", "u");
				if(type.contains("\\\\"))
					type = type.replace("\\\\", "\\");
				if(type.contains("\\n"))
					type = type.replace("\\n", "");
				if(type.equals("<wikicat_Malayalam_Cinematographers>"))
					type = "<wikicat_Malayalam_cinematographers>";
				int e = map.get(entity);
				int t = map.get(type);
				
				//System.out.println(e + "\t" + t);
				if(0 == i){
					sql = sql + "values(" + e + "," + t + ")";
					i ++;
				}
				else if(i < 378){
					sql = sql + ",(" + e + "," + t + ")";
					i ++;
				}
				else{
					System.out.println(num);
					sql = sql + ",(" + e + "," + t + ")";
					//System.out.println(sql);
					try {
						stmt.execute(sql);
					} catch (SQLException ee) {
						// TODO Auto-generated catch block
						ee.printStackTrace();
					}
					i = 0;
					sql = "insert into onttypesinfos (entity,type) ";
				}
				num ++;
			}
			System.out.println(i);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
