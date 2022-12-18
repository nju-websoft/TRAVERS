package yago;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import JDBCUtils.JdbcUtil;

public class triplesBuilder {
	static Connection conn = JdbcUtil.getConnection();
	static Map<String, Integer> map = getMap.get();
	static List<triple> list = new ArrayList<triple>();
	public static void main(String[] args) {
		FileReader reader;
		try {
			reader = new FileReader("H:/myself/yago/yagoFacts.ttl");
			BufferedReader br = new BufferedReader(reader);
			String str = null;
			while((str = br.readLine()) != null){
				if(!str.contains("#")){
					String[] ss = str.split("\t");
					String  subject = ss[0];
					String predicate = ss[1];
					String object = ss[2].split(" ")[0];
					if(subject.contains("\\u"))
						subject = subject.replace("\\u", "u");
					if(subject.contains("\\\\"))
						subject = subject.replace("\\\\", "\\");
					if(object.contains("\\u"))
						object = object.replace("\\u", "u");
					if(object.contains("\\\\"))
						object = object.replace("\\\\", "\\");
					if(map.get(subject) != null && map.get(object) != null){
						triple tp = new triple(map.get(subject), map.get(predicate), map.get(object));
						list.add(tp);
					}
					if(map.get(subject) == null){
						System.out.println(subject);
					}
					if(map.get(object) == null)
						System.out.println(object);
						
				}
			}
			System.out.println(list.size());
			
			java.sql.Statement stmt = null;
			try {
				stmt = conn.createStatement();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String sql = "insert into objtriples (subject,predicate,object) ";
			int i = 0;
			int num = 0;
			Iterator<triple> it = list.iterator();
			while(it.hasNext()){
				triple tp = it.next();
				int sub = tp.subject;
				int pre = tp.predicate;
				int obj = tp.object;
				//if(uri.contains("\\\\"))
				//	System.out.println(uri);
				if(0 == i){
					sql = sql + "values(" + sub + "," + pre + "," + obj + ")";
					i ++;
				}
				else if(i < 99){
					sql = sql + ",(" + sub + "," + pre + "," + obj + ")";
					i ++;
				}
				else{
					System.out.println(num);
					sql = sql + ",(" + sub + "," + pre + "," + obj + ")";
					//System.out.println(sql);
					try {
						stmt.execute(sql);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					i = 0;
					sql = "insert into objtriples (subject,predicate,object) ";
				}
				num ++;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	static class triple{
		int subject;
		int predicate;
		int object;
		public triple(int s, int p, int o){
			this.subject = s;
			this.predicate = p;
			this.object = o;
		}
	}
}
