package JDBCUtils;

import Structures.GlobalVariances;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class JdbcUtil{ 
    private static String URL;
    private static String JDBC_DRIVER;
    private static String USER_NAME;
    private static String PASSWORD;
    private static Connection connection = null;
    /*
     * 静态代码块，类初始化时加载数据库驱动
     */
    static {
        try {
            // 加载dbinfo.properties配置文件
            InputStream in = JdbcUtil.class.getClassLoader()
                    .getResourceAsStream("JavaUtil.properties");
            Properties properties = new Properties();
            properties.load(in);

            // 获取驱动名称、url、用户名以及密码
            JDBC_DRIVER = properties.getProperty("JDBC_DRIVER");
            URL = properties.getProperty("URL");
            USER_NAME = properties.getProperty("USER_NAME");
            PASSWORD = properties.getProperty("PASSWORD");

            // 加载驱动
            Class.forName(JDBC_DRIVER);
            
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /*
     * 获取连接
     */
    public static Connection getConnection(){
        if(connection == null){
        	try {
        	    URL = URL.replace("needreplace", GlobalVariances.Database_name);
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

    public static void close(Connection connection, Statement statement, ResultSet resultSet) {
        if (connection != null)
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        if (statement != null)
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        if (resultSet != null)
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
    }
}