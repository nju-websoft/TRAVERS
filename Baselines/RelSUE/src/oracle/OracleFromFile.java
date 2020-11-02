package oracle;

import JDBCUtils.JdbcUtil;

public class OracleFromFile {
    static DistanceOracleM oracle = null;

    public static void initialize(){
        if(null == oracle){
            System.out.println("start to load oracle...");
            long start = System.currentTimeMillis();
            if(JdbcUtil.URL.contains("yago"))
                oracle = DistanceOracleM.DeserializeOracle("./datas/oracle/yago.txt");
            else if(JdbcUtil.URL.contains("dbpedia"))
                oracle = DistanceOracleM.DeserializeOracle("./datas/oracle/dbpedia.txt");
            long end = System.currentTimeMillis();

            System.out.println("time for loading oracle: " + (end - start));
        }


    }

    public static int query(int s, int t){
        initialize();
        return oracle.query(s, t);
    }
}
