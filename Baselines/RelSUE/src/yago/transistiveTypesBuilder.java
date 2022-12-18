package yago;

import GraphData.Concept;
import GraphData.Ontology;
import JDBCUtils.JdbcUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

/**
 * 因为simplified版本的types文件中只包含了叶子类的类型信息，没有包含传递的父类，所以需要单独处理导入数据库
 */

public class transistiveTypesBuilder {
    static Connection conn = JdbcUtil.getConnection();
    static Set<Pair> pairs = new HashSet<>();
    public static void main(String[] args){
        Ontology.Initialize();
        //Concept c = Ontology.returnById(4306545);
        //System.out.println(c.upperClass.id);

        String sql = "select * from onttypesinfos";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);


            while(rs.next()){
                int entity = rs.getInt("entity");
                int type = rs.getInt("type");

                int upperType = Ontology.returnById(type).upperClass.id;
                pairs.add(new Pair(entity, upperType));
                pairs.add(new Pair(entity, 4832388));

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("Size of added: " + pairs.size());

        String sql0 = "insert into onttypesinfos values";
        try {
            Statement stmt0 = conn.createStatement();

            int i = 0;
            for(Pair p : pairs){
                if(i < 1000){
                    sql0 += "(" + p.entity + "," + p.type + "),";
                    i ++;
                }
                else if(1000 == i){
                    sql0 += "(" + p.entity + "," + p.type + ")";
                    stmt0.execute(sql0);
                    i = 0;
                    sql0 = "insert into onttypesinfos values";
                }
            }

            if(sql0.charAt(sql0.length() - 1) == ','){
                sql0 = sql0.substring(0, sql0.length() - 1);
                stmt0.execute(sql0);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    static class Pair{
        public int entity;
        public int type;

        public Pair(int entity, int type){
            this.entity = entity;
            this.type = type;
        }

        public int hashCode(){
            return this.entity + this.type;
        }
        public boolean equals(Object hp){
            if(((Pair)hp).entity == this.entity || ((Pair)hp).type == this.type)
                return true;
            else
                return false;
        }
    }
}
