package Structures;

import JDBCUtils.JdbcUtil;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class typeModel {

    public static String typeTableName = "";

    static {
        try {
            // 加载dbinfo.properties配置文件
            InputStream in = JdbcUtil.class.getClassLoader()
                    .getResourceAsStream("Graph.properties");
            Properties properties = new Properties();
            properties.load(in);

            //给出的example的数目
            typeTableName =properties.getProperty("TypeTable");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Set<Integer>[] typ = new HashSet[10000005]; //dbpedia
    public static Integer EntityCnt = 0;

    public static void clean(){ for(Integer id = 0; id <= 10000000; ++ id) typ[id] = new HashSet<Integer>(); EntityCnt = 0;}

    public static void loadTypes(Connection conn)
    {
        for(Integer id = 0; id <= 10000000; ++id) typ[id].add(14); //dbpedia owl:Thing
        try {
            Statement stmt = conn.createStatement();
            String sql = "select * from " + typeTableName + ";";
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next())
            {
                Integer entity = rs.getInt("entity");
                Integer type = rs.getInt("type");
                typ[entity].add(type);
                EntityCnt = Math.max(EntityCnt, entity);
            }
            rs.close(); stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Set<Integer> getTypes(Integer entity)
    {
        return typ[entity];
    }

}
