package JDBCUtils;

import oracle.DistanceOracleM;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class TestPS {
    static Connection conn = JdbcUtil.getConnection();

    public static void main(String[] args){

        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement("INSERT into oracle values (?, ?, ?)");

            int j = 0;
            for(int i = 0; i < 2333; i ++){
                ps.setInt(1, i);
                ps.setInt(2, 0);
                ps.setInt(3, 0);

                if(j < 1000){
                    ps.addBatch();
                    j ++;
                }
                else if(1000 == j){
                    ps.executeBatch();
                    j = 0;

                    ps = conn.prepareStatement("INSERT into oracle values (?, ?, ?)");
                }

            }

            ps.executeBatch();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
