package JDBCUtils;

import java.io.IOException;
import java.io.InputStream;
/**
 * 
 * @author guyu
 * 数据库连接工具，为了降低对接成本，共同使用同一个数据库
 */
import java.sql.*;
import java.util.Properties;

public class JdbcUtil{ 
    public static String URL;
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
                    .getResourceAsStream("dbinfo.properties");
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
    			connection = DriverManager.getConnection(URL, USER_NAME, PASSWORD);
    			//System.out.println("data: " + URL);
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