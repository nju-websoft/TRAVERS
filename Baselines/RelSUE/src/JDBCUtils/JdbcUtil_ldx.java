package JDBCUtils;

import java.io.IOException;
import java.io.InputStream;
/**
 *
 * @author anonymous
 * 数据库连接工具，为了降低对接成本，共同使用同一个数据库
 */
import java.sql.*;
import java.util.Properties;

public class JdbcUtil_ldx{
    private static String URL = "jdbc:mysql://114.212.86.158:3306/linkedMDB?autoReconnect=true";
    private static String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static String USER_NAME = "root";
    private static String PASSWORD = "325569";
    private static Connection connection = null;

    /*
     * 获取连接
     */
    public static Connection getConnection(){
        if(connection == null){
            try {
                connection = DriverManager.getConnection(URL, USER_NAME, PASSWORD);
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return connection;
    }

    public static void closeConnection(){
        if(connection != null){
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}