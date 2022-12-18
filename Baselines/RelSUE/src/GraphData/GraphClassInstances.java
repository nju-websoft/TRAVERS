package GraphData;

import java.util.*;
import java.sql.*;

import JDBCUtils.JdbcUtil;
/**
 * get istances of a given type in dbpedia directly from database rather than memory
 * @author anonymous
 *
 */
public class GraphClassInstances {
	public static List<Integer> getInstances(int type) {
		List<Integer> list = new ArrayList<Integer>();
		try {
			Statement stmt = JdbcUtil.getConnection().createStatement();
			String sql = "select * from onttypesinfos where type=" + type;
			ResultSet rs = stmt.executeQuery(sql);
			while(rs.next()){
				int entity = rs.getInt("entity");
				list.add(entity);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}

	public static void main(String[] args) {
		System.out.println(GraphClassInstances.getInstances(4663898).size());
	}
}
