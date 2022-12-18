package JDBCUtils;

import java.sql.Connection;


public class Example {
	public static void main(String[] args) {
		Connection conn = JdbcUtil.getConnection();
		if(conn == null){
			try{
				throw new Exception("数据库连接失败！");
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		else{
			System.out.println("数据库连接成功！");
		}
	}
}
