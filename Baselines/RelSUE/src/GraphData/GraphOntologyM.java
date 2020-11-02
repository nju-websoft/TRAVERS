package GraphData;

import JDBCUtils.JdbcUtil;

import java.sql.*;
import java.util.*;

@Deprecated
public class GraphOntologyM {
    static Connection conn = JdbcUtil.getConnection();
    static Map<Integer, List<Integer>> map = null;

    public static void initializeMap(){
        if(null == map){
            map = new HashMap<Integer, List<Integer>>();
            String sql = "select * from ontinfos";
            try {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                while(rs.next()){
                    int child = rs.getInt("child");
                    int parent = rs.getInt("parent");
                    if(map.containsKey(child)){
                        List<Integer> list = map.get(child);
                        list.add(parent);
                        map.put(child, list);
                    }
                    else{
                        List<Integer> list = new ArrayList<Integer>();
                        list.add(parent);
                        map.put(child, list);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            System.out.println("ontology loaded!");
        }
    }

    /**
     *
     * @param a
     * @param b
     * @return whether a is a descendant of b
     */
    public static boolean isDescendantOf(int a, int b){
        if(map.get(a) == null)
        	return false;
    	if(map.get(a).contains(b))
            return true;
        else{
            for(int ap : map.get(a)){
                if(isDescendantOf(ap, b))
                    return true;
            }
        }
        return false;
    }
    private static boolean islegal(Set<Integer> types){
        List<Integer> list = new ArrayList<Integer>();
        list.addAll(types);
        for(int i = 0; i < list.size() - 1; i ++){
            for(int j = i + 1; j < list.size(); j ++){
                if(!GraphOntologyM.isDescendantOf(list.get(i), list.get(j)) && !GraphOntologyM.isDescendantOf(list.get(j), list.get(i))){
                	System.out.println(j + " " + i);
                	return true;
                }
                    
            }
        }

        return false;
    }
    public static void main(String args[]){
        GraphOntologyM.initializeMap();
        System.out.println(isDescendantOf(3481579, 3481565));
    }
}
