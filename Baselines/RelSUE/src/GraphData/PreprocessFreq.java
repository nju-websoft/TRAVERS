package GraphData;

import java.sql.*;
import java.util.*;
import java.io.*;

import JDBCUtils.JdbcUtil;

public class PreprocessFreq {
	static Connection conn = JdbcUtil.getConnection();
	static Map<Integer, Set<Integer>> relObj = new HashMap<Integer, Set<Integer>>();
	static Map<Integer, Set<Integer>> relSub = new HashMap<Integer, Set<Integer>>();
	static Map<Integer, Integer> count = new HashMap<Integer, Integer>();
	
	public static void main(String[] args) {
		try {
			Statement stmt = conn.createStatement();
			String sql = "select * from objtriples";
			ResultSet rs = stmt.executeQuery(sql);
			
			while(rs.next()){
				int sub = rs.getInt("subject");
				int rel = rs.getInt("predicate");
				int obj = rs.getInt("object");
				
				if(count.containsKey(rel)){
					int cou = count.get(rel);
					cou ++;
					count.put(rel, cou);
					
					Set<Integer> objs = relObj.get(rel);
					objs.add(obj);
					relObj.put(rel, objs);
					
					Set<Integer> subs = relSub.get(rel);
					subs.add(sub);
					relSub.put(rel, subs);
				}
				else{
					int cou = 1;
					count.put(rel, cou);
					
					Set<Integer> objs = new HashSet<>();
					objs.add(obj);
					relObj.put(rel, objs);
					
					Set<Integer> subs = new HashSet<>();
					subs.add(sub);
					relSub.put(rel, subs);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("graph loaded!");
		
		File file = new File("dbpedia.freq");
		FileWriter fw = null;
		BufferedWriter writer = null;
		
		try {
			fw = new FileWriter(file);
			writer = new BufferedWriter(fw);
			for(int rel : count.keySet()){
				int cou = count.get(rel);
				if(cou > 0){
					writer.write(rel + " " + ((double)cou/relObj.get(rel).size()));
					writer.newLine();
					writer.write(-1*rel + " " + ((double)cou/relSub.get(rel).size()));
					writer.newLine();
				}
			}
			
			writer.close();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
