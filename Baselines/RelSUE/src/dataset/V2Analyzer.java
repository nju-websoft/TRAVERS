package dataset;

import JDBCUtils.JdbcUtil;

import java.io.*;
import java.sql.*;

public class V2Analyzer {
    static Connection conn = JdbcUtil.getConnection();

    public static void analyze(String fileName){
        File file = new File(fileName);
        try {
            FileReader fr = new FileReader(file);
            BufferedReader reader = new BufferedReader(fr);

            String str;
            int i = 1;
            while((str = reader.readLine()) != null){
                if(i % 2 == 1){
                    int query = Integer.parseInt(str);
                    String sql = "select count(*) as cou from objtriples where object=" + query +  " or subject=" + query;
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sql);
                    while(rs.next()){
                        System.out.println((i/2) + ": " + rs.getInt("cou"));
                    }
                }

                i ++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        analyze("V2.txt");
    }
}
