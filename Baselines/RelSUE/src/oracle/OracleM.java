package oracle;

import JDBCUtils.JdbcUtil;
import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

@Deprecated // 读数据库总是会超时，实在是没办法，只能改成读文件
public class OracleM {
    public static Connection conn = JdbcUtil.getConnection();
    public static Map<Integer, Byte>[] map = null;

    private OracleM(){

    }

    public static void initialize(){
        if(null == map){

            if(JdbcUtil.URL.contains("yago")){
                map = (Map<Integer, Byte>[])new HashMap[4295826];

                String sql = "select * from oracle";
                try {
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sql);

                    while(rs.next()){
                        int v = rs.getInt("v");
                        int u = rs.getInt("u");
                        byte distance = rs.getByte("distance");

                        if(null == map[v])
                            map[v] = new HashMap<>();

                        map[v].put(u, distance);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }
            else if(JdbcUtil.URL.contains("dbpedia")){
                map = (Map<Integer, Byte>[])new HashMap[3480807];
                for(int i = 0; i < 35; i ++){
                    String sql = "select * from oracle where v>=" + i*100000 + " and v<" + (i+1)*100000;
                    try {
                        Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery(sql);

                        while(rs.next()){
                            int v = rs.getInt("v");
                            int u = rs.getInt("u");
                            byte distance = rs.getByte("distance");

                            if(null == map[v])
                                map[v] = new HashMap<>();

                            map[v].put(u, distance);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    System.out.println("dbpedia oracle loaded: " + (i + 1) + "/" + 35);
                }


                System.out.println("dbpedia oracle loaded completely!");


            }



        }
    }

    public static int query(int s, int t) {
        if (null == map) initialize();
        if(s == t)
            return 0;
        if(s >= map.length || t >= map.length){
            try {
                throw new Exception("Oracle out of index!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(null == map[s] || null == map[t]){
            System.out.println("s: " + s + ", t: " + t);
            System.exit(-1);
            return 100;
        }
        else{
            if(map[s].containsKey(t))
                return map[s].get(t);

            if(map[t].containsKey(s))
                return map[t].get(s);


            byte dis = 111;
            if(map[s].size() < map[t].size()){
                for(int u : map[s].keySet()){
                    if(map[t].containsKey(u)){
                        byte distance = (byte) (map[s].get(u) + map[t].get(u));
                        if(distance < dis)
                            dis = distance;
                    }
                }
            }
            else{
                for(int u : map[t].keySet()){
                    if(map[s].containsKey(u)){
                        byte distance = (byte) (map[s].get(u) + map[t].get(u));
                        if(distance < dis)
                            dis = distance;
                    }
                }
            }

            return dis;
        }
    }

    public static void main(String[] args){
        long start = System.currentTimeMillis();
        OracleM.initialize();
        long end = System.currentTimeMillis();

        System.out.println("time: " + (end - start));
    }
}
