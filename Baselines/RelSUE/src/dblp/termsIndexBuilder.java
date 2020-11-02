package dblp;

import java.io.*;
import java.sql.*;
import java.util.*;

import JDBCUtils.JdbcUtil;

public class termsIndexBuilder {
	static Connection conn = JdbcUtil.getConnection();
	static Map<String, Integer> termsCount = new HashMap<String, Integer>();
	static Set<String> terms = new HashSet<String>();
	public static void main(String[] args) {
		List<String> stopwords = new ArrayList<String>();
		FileReader reader;
		try {
			reader = new FileReader("src/stopwords.txt");
			BufferedReader br = new BufferedReader(reader);
			String str = null;
			while((str = br.readLine()) != null){
				stopwords.add(str);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String sql = "select * from paper";
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while(rs.next()){	
				String title = rs.getString("title");
				title = title.replaceAll("[!@#$,.?:;\"()<>]", "");
				title = title.replace("/", " ");
				title = title.toLowerCase();
				//System.out.println(title);
				String[] ss = title.split(" ");
				for(String s : ss){
					if(!stopwords.contains(s)){
						s = "<" + s + ">";
						//System.out.println(s);
						if(termsCount.containsKey(s)){
							int count = termsCount.get(s);
							count ++;
							termsCount.put(s, count);
						}
						else{
							termsCount.put(s, 1);
						}
					}
				}
			}
			for(Map.Entry<String, Integer> entry : termsCount.entrySet()){
				if(entry.getValue() >= 10)
					terms.add(entry.getKey());
			}
			//System.out.println(terms.size()); //40305
			System.out.println(terms);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
