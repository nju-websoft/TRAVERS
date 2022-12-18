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

public class propertyModel {

    public String propertyTableName = "";

    {
        try {
            // 加载dbinfo.properties配置文件
            InputStream in = JdbcUtil.class.getClassLoader()
                    .getResourceAsStream("Graph.properties");
            Properties properties = new Properties();
            properties.load(in);

            //给出的example的数目
            propertyTableName =properties.getProperty("PropertyTable");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Set<Integer>[] pro = new HashSet[10000005]; //dbpedia
    public Integer PropCnt = 0;

    public void clean(){ for(Integer id = 0; id <= 10000000; ++ id) pro[id] = new HashSet<Integer>(); PropCnt = 0;}

    public void loadTypes(Connection conn)
    {
        try {
            Statement stmt = conn.createStatement();
            String sql = "select * from " + propertyTableName + ";";
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next())
            {
                Integer entity = rs.getInt("entity");
                Integer type = rs.getInt("property");
                pro[entity].add(type);
                PropCnt = Math.max(PropCnt, entity);
            }
            rs.close(); stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Set<Integer> getTypes(Integer entity)
    {
        return pro[entity];
    }

}
