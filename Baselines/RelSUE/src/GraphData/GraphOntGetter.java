//package GraphData;
//
//import java.util.*;
//import java.sql.*;
//
//import JDBCUtils.JdbcUtil;
///**
// * get type infos of entities in dbpedia directly from database rather than memory
// * @author anonymous
// *
// */
//public class GraphOntGetter {
//	public static List<Integer> classOfEntityByID(int id) {
//		List<Integer> list = new ArrayList<Integer>();
//		try {
//			Statement stmt = JdbcUtil.getConnection().createStatement();
//			String sql = "select * from onttypesinfos where entity=" + id;
//			ResultSet rs = stmt.executeQuery(sql);
//			while(rs.next()){
//				int type = rs.getInt("type");
//				list.add(type);
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return list;
//	}
//
//	public static void main(String[] args) {
//		System.out.println(GraphOntGetter.classOfEntityByID(622074));
//	}
//}
