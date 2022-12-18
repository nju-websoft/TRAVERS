package oracle;

import JDBCUtils.JdbcUtil;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 *
 *
 */
public class DistanceOracleM implements Serializable {
    public static final long serialVersionUID = -3008951528370405368l;
    static Connection conn = JdbcUtil.getConnection();
    Map<Integer, Integer> idMap = new HashMap<>(); // key : id, value : rank. And we store rank in oracle index instead of id
    // rank starts from 0, not 1
    List<Integer> sortedList = new ArrayList<>(); // each item is the id of an entity, sorted by the degree in descendant

    //Map<Integer, List<VDpair>> Index = new HashMap<>(); // key : rank, value : label. This map is the foundation of oracle query.
    // Moreover, since the order we follow to conduct BFS is determined by rank, all VDpairs in each label are sorted by their rank.

    List<List<VDpair>> Index = new ArrayList<>(); // 直接用线性表的下标来索引，比hashmap更高效

    public static void storeIndex2DB(){ // 因为经实践发现原有的序列化方式不现实，故尝试先将index写到数据库里，且直接
        // 按照原id来写


        System.out.println("load oracle for storing...");
        DistanceOracleM oracle = DeserializeOracle("oracle/yago.txt");
        //DistanceOracleM oracle = new DistanceOracleM();
        System.out.println("oracle loaded!");
        PreparedStatement ps = null;
        try {
            String sql = "insert into oracle values";
            Statement stmt = conn.createStatement();

            int j = 0;

            for(int i = 0; i < oracle.Index.size(); i ++){
                int v = oracle.sortedList.get(i);
                List<VDpair> label = oracle.Index.get(i);
                for(VDpair vd : label){
                    int u = oracle.sortedList.get(vd.rank);
                    int distance = vd.distance;

                    if(j < 1000){
                        sql += "(" + v + "," + u + "," + distance + "),";
                        j ++;
                    }
                    else if(j == 1000){
                        sql += "(" + v + "," + u + "," + distance + ")";
                        stmt.execute(sql);

                        sql = "insert into oracle values";
                        j = 0;
                    }

                }
            }
            if(sql.charAt(sql.length() - 1) == ','){ // 说明不能被1000整除
                sql = sql.substring(0, sql.length()-1); // 把逗号去掉
                stmt.execute(sql);
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private DistanceOracleM(){
        sortedList = ToolKit.getSortedList();
        try {
            buildMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
        buildIndex();
    }

    public void buildMap() throws Exception { // build idMap based on sortedList
        if(sortedList.size() == 0){
            throw new Exception("Please first build sortedList correctly");
        }

        for(int i = 0; i < sortedList.size(); i ++){
            idMap.put(sortedList.get(i), i);
        }
    }

    public void buildIndex() {
        byte[] P = new byte[sortedList.size()];
        //initialize P
        for(int i = 0; i < P.length; i ++)
            P[i] = 100;

        for(int i = 0; i < sortedList.size(); i ++){
            Index.add(new ArrayList<VDpair>());
        }

        for(int v = 0; v < sortedList.size(); v ++){
            System.out.println(v);
            Set<Integer> visited = PrunedBFS(v, P);
            System.out.println("visited size: " + visited.size());
            for(int node : visited)
                P[node] = 100;
        }
    }

    /**
     *
     * @param v the rank of node to conduct pruned BFS
     * @return all ranks of node visited from V. It is used to clear P for next round of pruned BFS
     */
    public Set<Integer> PrunedBFS (int v, byte[] P){
        Set<Integer> visited = new HashSet<>();
        byte[] T = new byte[sortedList.size()];
        for(int i = 0; i < T.length; i ++)
            T[i] = 100;

       /*List<VDpair> label_v = Index.get(v);
        if(label_v != null){
            for(VDpair vd : label_v){
                T[vd.rank] = vd.distance;
            }
        }*/
        List<VDpair> label_v = Index.get(v);
        for(VDpair vd : label_v){
            T[vd.rank] = vd.distance;
        }

        Queue<Integer> queue = new Queue<>();
        queue.enqueue(v);
        P[v] = 0;
        visited.add(v);

        while(!queue.isEmpty()){
            int v0 = queue.dequeue();

            if(query(T, v0) <= P[v0]){
                continue;
            }

            /*List<VDpair> list0 = Index.get(v0);
            VDpair npair = new VDpair(v, P[v0]);
            if(list0 == null)
                list0 = new ArrayList<>();
            list0.add(npair);
            Index.put(v0, list0);*/

            List<VDpair> list0 = Index.get(v0);
            VDpair npair = new VDpair(v, P[v0]);
            list0.add(npair);
            Index.set(v0, list0);

            int v0_id = sortedList.get(v0);
            Set<Integer> neighbours = UndirectedGraph.map.get(v0_id);
            for(int nv_id : neighbours){
                int nv = idMap.get(nv_id);
                if(!visited.contains(nv)){
                    P[nv] = (byte)(P[v0] + 1);
                    visited.add(nv);
                    queue.enqueue(nv);
                }
            }

        }


        return visited;
    }

    public byte query(byte[] T, int u) { // u is the rank of a node. This function is used to accelerate the calculation during BFS
        byte distance = 100;

        /*List<VDpair> label_u = Index.get(u);
        if(label_u != null){
            for(VDpair vd : label_u){
                byte ndis = (byte)(vd.distance + T[vd.rank]);
                if(ndis < distance)
                    distance = ndis;
            }
        }*/

        List<VDpair> label_u = Index.get(u);

        for(VDpair vd : label_u){
            byte ndis = (byte)(vd.distance + T[vd.rank]);
            if(ndis < distance)
                distance = ndis;
        }

        return distance;
    }

    public int query(int s, int t) { // both s and t are id in database
        if(s == t)
            return 0;

        int distance = 100;

        // to calculate the distance between s and t, we have to transform both of them into their corresponding rank,
        // which is used to represent each entity in oracle
        int s_r = idMap.get(s);
        int t_r = idMap.get(t);

        List<VDpair> label_s = Index.get(s_r);
        List<VDpair> label_t = Index.get(t_r);

        int i = 0, j = 0;

        while(i < label_s.size() && j < label_t.size()){
            while(label_s.get(i).rank < label_t.get(j).rank){
                i ++;
                if(i >= label_s.size())
                    break;
            }
            if(i >= label_s.size()) break;
            if(label_s.get(i).rank == label_t.get(j).rank){
                int ndis = (label_s.get(i).distance & 0xFF) + (label_t.get(j).distance & 0xFF);
                if(ndis < distance)
                    distance = ndis;
                i ++;
                j ++;
            }
            else j ++;
        }

        return distance;
    }

    class VDpair implements Serializable{
        int rank;
        byte distance;

        public VDpair(int rank, byte distance){
            this.rank = rank;
            this.distance = distance;
        }
    }

    public static void SerializeOracle() {
        DistanceOracleM oracle = new DistanceOracleM();

        // ObjectOutputStream can be used to write an object into a file
        try {
            ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream((new File("oracle/yago.txt"))));

            oo.writeObject(oracle);
            System.out.println("Serialization is successful!");

            oo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static DistanceOracleM DeserializeOracle(String fileName) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(fileName)));
            DistanceOracleM oracle = (DistanceOracleM)ois.readObject();

            System.out.println("Deserialization is successful!");
            return oracle;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        System.out.println("Deserialization fails!");
        return null;
    }

    public static void main(String []args) {
        System.out.println("load undirected graph...");
        UndirectedGraph.initializeMap();

        /*long start = System.currentTimeMillis();
        SerializeOracle();
        long end = System.currentTimeMillis();

        System.out.println(end - start + " ms");

        start = System.currentTimeMillis();
        loadOracle("oracle/yago.txt");
        end = System.currentTimeMillis();

        System.out.println(end - start + " ms");

        DistanceOracleM oracle = DistanceOracleM.DisOracle;
        System.out.println(oracle.query(1, 2085397));*/

        storeIndex2DB();
    }
}
