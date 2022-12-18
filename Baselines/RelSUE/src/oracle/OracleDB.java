package oracle;

import JDBCUtils.JdbcUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * 外存版oracle
 */
public class OracleDB {
    static Map<Integer, Map<Integer, Byte>> cacheIndex = new HashMap<>();
    static Connection conn = JdbcUtil.getConnection();
    public static int query(int s, int t){
        Map<Integer, Byte> maps = getMap(s);
        Map<Integer, Byte> mapt = getMap(t);

        int distance = 100;
        for(int keys : maps.keySet()){
            if(mapt.containsKey(keys)){
                int ndis = maps.get(keys) + mapt.get(keys);
                if(distance > ndis) {
                    distance = ndis;
                  //  System.out.println(keys);
                }
            }
        }

        return  distance;
    }

    public static Map<Integer, Byte> getMap(int s){
        Map<Integer, Byte> map = cacheIndex.get(s);
        if(null == map){
            map = getMapFromDB(s);
            cacheIndex.put(s, map);
        }

        return map;
    }

    public static Map<Integer, Byte> getMapFromDB(int s){
        Map<Integer, Byte> result = new HashMap<>();
        String sql = "select * from oracle where v=" + s;
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while(rs.next()){
                int u = rs.getInt("u");
                byte dis = rs.getByte("distance");
                result.put(u, dis);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static void main(String[] args){
        int s = 21289;
        int t = 3237970;

        long start = System.currentTimeMillis();
        int dis = query(s, t);
        long end = System.currentTimeMillis();
        System.out.println(dis);
        System.out.println("time: " + (end - start));

    }
}
