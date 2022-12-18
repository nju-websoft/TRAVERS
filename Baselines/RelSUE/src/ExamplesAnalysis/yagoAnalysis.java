package ExamplesAnalysis;

import java.util.*;
import java.util.Map.Entry;
import java.sql.*;

import GraphData.LabelGetter;
import JDBCUtils.JdbcUtil;

public class yagoAnalysis {
	static Connection conn = JdbcUtil.getConnection();
	static Map<Integer, Integer> map = new HashMap<Integer, Integer>();
	private static <K, V> Map<K, V> sortByValue(Map<K, V> map) {
	    List<Entry<K, V>> list = new LinkedList<>(map.entrySet());
	    Collections.sort(list, new Comparator<Object>() {
	        @SuppressWarnings("unchecked")
	        public int compare(Object o1, Object o2) {
	            return ((Comparable<V>) ((Map.Entry<K, V>) (o2)).getValue()).compareTo(((Map.Entry<K, V>) (o1)).getValue());
	        }
	    });

	    Map<K, V> result = new LinkedHashMap<>();
	    for (Iterator<Entry<K, V>> it = list.iterator(); it.hasNext();) {
	        Map.Entry<K, V> entry = (Map.Entry<K, V>) it.next();
	        result.put(entry.getKey(), entry.getValue());
	    }

	    return result;
	}
	private static void objAnalysis(int relation){
		try {
			Statement stmt = conn.createStatement();
			String sql = "select * from objtriples where predicate=" + relation;
			ResultSet rs = stmt.executeQuery(sql);
			while(rs.next()){
				int obj = rs.getInt("object");
				if(map.containsKey(obj)){
					int count = map.get(obj);
					map.put(obj, count + 1);
				}
				else{
					map.put(obj, 1);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		map = sortByValue(map);
		int i = 0;
		for(Map.Entry<Integer, Integer> entry : map.entrySet()){
			if(i < 50){
				System.out.println(LabelGetter.get(entry.getKey()) + " " + entry.getValue());
			}
			else
				break;
			i ++;
		}
	}
	public static void main(String[] args) {
		try {
			Statement stmt = conn.createStatement();
			String sql = "select type from onttypesinfos where entity in (select subject from objtriples where object=2193500 and predicate=4295854)";
			ResultSet rs = stmt.executeQuery(sql);
			while(rs.next()){
				int type = rs.getInt("type");
				if(map.containsKey(type)){
					int count = map.get(type);
					map.put(type, count + 1);
				}
				else{
					map.put(type, 1);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		map = sortByValue(map);
		int i = 0;
		for(Map.Entry<Integer, Integer> entry : map.entrySet()){
			if(i < 200){
				System.out.println(LabelGetter.get(entry.getKey()) + " " + entry.getValue());
			}
			else
				break;
			i ++;
		}
	}
}
