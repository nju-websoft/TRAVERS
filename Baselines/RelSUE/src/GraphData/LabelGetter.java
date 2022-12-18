package GraphData;

import java.io.*;
import java.sql.*;

import JDBCUtils.JdbcUtil;
public class LabelGetter {
	static Connection conn = JdbcUtil.getConnection();
	
	public static String get(int id){
		String uri = null;
		try {
			Statement stmt = conn.createStatement();
			String sql = "select * from mapping where id=" + id;
			ResultSet rs = stmt.executeQuery(sql);
			while(rs.next()){
				uri = rs.getString("uri");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return uri;
	}
	
	public static String get_DBPedia(int id){
		String uri = null;
		try {
			Statement stmt = conn.createStatement();
			String sql = "select * from mapping where id=" + id;
			ResultSet rs = stmt.executeQuery(sql);
			while(rs.next()){
				uri = rs.getString("uri");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String label = null;
		try {
			Statement stmt = conn.createStatement();
			String sql = "select * from labelinfos where entity=\"" + uri + "\"";
			//System.out.println(sql);
			ResultSet rs = stmt.executeQuery(sql);
			while(rs.next()){
				label = rs.getString("label");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return label;
	}

	public static void convertQueries(String directory){
		File file = new File(directory);
		File[] files = file.listFiles();
		int i = 0;
		for(File f : files){
			String content = genSingleFile(f);
			try {
				FileWriter fw = new FileWriter(new File("H:\\myself\\hin\\WSDM\\开源\\Queries\\yago/"
                + i + ".txt"));
				fw.write(content);
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			i ++;
		}
	}

	public static String genSingleFile(File f){
		String content = "";
		try {
			FileReader fr = new FileReader(f);
			BufferedReader reader = new BufferedReader(fr);
			String str;

			while((str = reader.readLine()) != null){
				String[] items = str.split("\t");
				if(items.length == 1){
					content += LabelGetter.get(Integer.parseInt(items[0]));
					content += "\n";
				}
				else{
					for(String item : items){
						String[] ss = item.split(":");
						content += LabelGetter.get(Integer.parseInt(ss[0]));
						content += ":";
						content += ss[1];
						content += "\t";
					}
					content += "\n";
				}
			}
			fr.close();
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(content);
		return content;
	}
	
	public static void main(String[] args) {
		//System.out.println(get_DBPedia(2205300));
		//System.out.println(get(2205300));
		//genSingleFile(new File("H:\\myself\\hin\\WSDM\\Queries\\dbpedia/单步单条布尔.txt"));
		convertQueries("H:\\\\myself\\\\hin\\\\WSDM\\\\Queries\\\\yago");
	}
}
