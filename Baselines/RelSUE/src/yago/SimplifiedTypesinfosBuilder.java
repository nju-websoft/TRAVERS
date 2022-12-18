package yago;

import JDBCUtils.JdbcUtil;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

public class SimplifiedTypesinfosBuilder {
    static Connection conn = JdbcUtil.getConnection();
    static Map<String, Integer> map = getMap.get();
    public static void main(String[] args) {
        try {
            FileReader reader = new FileReader("H:/myself/yago/yagoSimpleTypes.ttl");
            BufferedReader br = new BufferedReader(reader);
            String str;
            Statement stmt = null;
            try {
                stmt = conn.createStatement();
            } catch (SQLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            String sql = "insert into onttypesinfos_simple values ";
            int i = 0;
            while((str = br.readLine()) != null){
                if(str.contains("rdf:type")){
                    String[] ss = str.split("\t");
                    String entity = ss[0];
                    String type = ss[2].split(" ")[0];
                    if(entity.contains("\\u"))
                        entity = entity.replace("\\u", "u");
                    if(entity.contains("\\\\"))
                        entity = entity.replace("\\\\", "\\");
                    if(type.contains("\\u"))
                        type = type.replace("\\u", "u");
                    if(type.contains("\\\\"))
                        type = type.replace("\\\\", "\\");
                    if(type.contains("\\n"))
                        type = type.replace("\\n", "");
                    if(type.equals("<wikicat_Malayalam_Cinematographers>"))
                        type = "<wikicat_Malayalam_cinematographers>";

                    //System.out.println(entity + " " + type);

                    if(map.get(entity) != null && map.get(type) != null){
                        int e = map.get(entity);
                        int t = map.get(type);

                        if(i < 1000){
                            sql = sql + "(" + e + "," + t + "),";
                            i ++;
                        }
                        else{
                            sql = sql + "(" + e + "," + t + ")";
                            //System.out.println(sql);
                            try {
                                stmt.execute(sql);
                            } catch (SQLException ee) {
                                // TODO Auto-generated catch block
                                ee.printStackTrace();
                            }
                            i = 0;
                            sql = "insert into onttypesinfos_simple values ";
                        }
                    }

                }
            }

            if(sql.charAt(sql.length() - 1) == ','){
                sql = sql.substring(0, sql.length() - 1);

                stmt.execute(sql);
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
