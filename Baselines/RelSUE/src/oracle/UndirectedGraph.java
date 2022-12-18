package oracle;

import JDBCUtils.JdbcUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class UndirectedGraph {

    static Connection conn = JdbcUtil.getConnection();
    public static Map<Integer, Set<Integer>> map = null;

    public static void initializeMap(){
        if(null == map){
            map = new HashMap<>();
            String sql = "select * from objtriples";
            try {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                //int i = 0;
                while(rs.next()){
                    //i ++;
                    int subject = rs.getInt("subject");
                    int object = rs.getInt("object");

                    if(!map.containsKey(subject)){
                        Set<Integer> set = new HashSet<>();
                        set.add(object);

                        map.put(subject, set);
                    }
                    else{
                        Set<Integer> set = map.get(subject);
                        if(!set.contains(object)){
                            set.add(object);
                            map.put(subject, set);
                        }
                    }

                    if(!map.containsKey(object)){
                        Set<Integer> set = new HashSet<>();
                        set.add(subject);

                        map.put(object, set);
                    }
                    else{
                        Set<Integer> set = map.get(object);
                        if(!set.contains(subject)){
                            set.add(subject);
                            map.put(object, set);
                        }
                    }


                }

                System.out.println("undirected graph loaded!");
                stmt.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        else
            System.out.println("Graph model has already be initialized");
    }
}
