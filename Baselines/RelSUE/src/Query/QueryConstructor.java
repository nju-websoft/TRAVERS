package Query;

import GraphData.*;
import JDBCUtils.JdbcUtil;
import Path.MetaPath;
import Sig.SigCalculator;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class QueryConstructor {
    static Connection conn = JdbcUtil.getConnection();
    public static void genYago21O(MetaPath mp, List<Integer> queries, String fileName){
        List<Integer> headEntities = queries;
        System.out.println(headEntities.size());
        int i = 0;
        try {
            FileWriter fw = new FileWriter(fileName);

            for(int entity : headEntities){
                if(i == 10)
                    break;
                Map<Integer, Integer> map = SigCalculator.getAccessed(entity, mp, mp.length());
                //System.out.println(map.size());
                if(20 <= map.size()){
                    map = sortByValue(map);
                    i ++;
                    fw.write(entity + "\n");
                    for(Map.Entry<Integer, Integer> entry : map.entrySet()){
                        fw.write(entry.getKey() + ":" + entry.getValue() + "\t");
                    }
                    fw.write("\n");
                }
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void genQueries_sp(MetaPath mp, String fileName){
        List<Integer> headEntities = new ArrayList<>();
        /*String sql = "select * from objtriples where predicate=4295855 and object=3288549";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()){
                headEntities.add(rs.getInt("subject"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }*/
        headEntities.add(30830);
        headEntities.add(3085437);
        headEntities.add(3156230);
        headEntities.add(3789770);

        int i = 0;
        try {
            FileWriter fw = new FileWriter(fileName);

            for(int entity : headEntities){
                if(i == 10)
                    break;
                Map<Integer, Integer> map = SigCalculator.getAccessed(entity, mp, mp.length());
                //System.out.println(map.size());
                if(25 <= map.size()){
                    map = sortByValue(map);
                    i ++;
                    fw.write(entity + "\n");
                    for(Map.Entry<Integer, Integer> entry : map.entrySet()){
                        fw.write(entry.getKey() + ":" + entry.getValue() + "\t");
                    }
                    fw.write("\n");
                }
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void genQueries(MetaPath mp, String fileName){
        int headType = mp.getConcepts().get(0);
        List<Integer> headEntities = GraphClassInstances.getInstances(headType);
        System.out.println(headEntities.size());
        int i = 0;
        try {
            FileWriter fw = new FileWriter(fileName);

            for(int entity : headEntities){
                if(i == 10)
                    break;
                Map<Integer, Integer> map = SigCalculator.getAccessed(entity, mp, mp.length());
                //System.out.println(map.size());
                if(20 <= map.size() && 50 >= map.size()){
                    map = sortByValue(map);
                    i ++;
                    fw.write(entity + "\n");
                    for(Map.Entry<Integer, Integer> entry : map.entrySet()){
                        fw.write(entry.getKey() + ":" + entry.getValue() + "\t");
                    }
                    fw.write("\n");
                }
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void genQueries(MetaPath mp1, MetaPath mp2, String fileName){ //针对多条布尔
        int headType = mp1.getConcepts().get(0);
        List<Integer> headEntities = GraphClassInstances.getInstances(headType);
        System.out.println(headEntities.size());
        int i = 0;
        try {
            FileWriter fw = new FileWriter(fileName);

            for(int entity : headEntities){
                if(i == 10)
                    break;
                Set<Integer> set = SigCalculator.getAccessed(entity, mp1, mp1.length()).keySet();
                set.retainAll(SigCalculator.getAccessed(entity, mp2, mp2.length()).keySet());
                //System.out.println(map.size());
                if(20 <= set.size() && 50 >= set.size()){
                    i ++;
                    fw.write(entity + "\n");
                    for(int en : set){
                        fw.write(en + ":" + 1 + "\t");
                    }
                    fw.write("\n");
                }
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static <K, V> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Object>() {
            @SuppressWarnings("unchecked")
            public int compare(Object o1, Object o2) {
                return ((Comparable<V>) ((Map.Entry<K, V>) (o2)).getValue()).compareTo(((Map.Entry<K, V>) (o1)).getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<>();
        for (Iterator<Map.Entry<K, V>> it = list.iterator(); it.hasNext();) {
            Map.Entry<K, V> entry = (Map.Entry<K, V>) it.next();
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public static void dbpediaQueries(){
        List<Integer> relations = new ArrayList<>();
        List<Integer> concepts = new ArrayList<>();

        // 多步单条布尔
       /* relations.add(3480963);
        relations.add(-3480963);
        concepts.add(3481703);
        concepts.add(3481453);
        concepts.add(3481703);*/

       // 多步单条排序
        /*concepts.add(3481454);
        concepts.add(3481453);
        concepts.add(3481453);
        relations.add(-3480838);
        relations.add(3480835);*/

        // 单步单条布尔
        concepts.add(3482123);
        concepts.add(3481453);
        relations.add(3481293);

        MetaPath mp = new MetaPath(concepts, relations);
        genQueries(mp, "单步单条布尔.txt");

        relations = new ArrayList<>();
        concepts = new ArrayList<>();

        // 多步多条布尔
        /*List<Integer> relations1 = new ArrayList<>();
        List<Integer> concepts1 = new ArrayList<>();
        relations.add(3480808);
        relations.add(-3480808);
        concepts.add(3481481);
        concepts.add(3481453);
        concepts.add(3481481);
        relations1.add(3480865);
        relations1.add(-3480865);
        concepts1.add(3481481);
        concepts1.add(3481923);
        concepts1.add(3481481);

        mp = new MetaPath(concepts, relations);
        MetaPath mp1 = new MetaPath(concepts1, relations1);
        genQueries(mp, mp1, "多步多条布尔.txt");*/

        //单步多条
        /*relations = new ArrayList<>();
        concepts = new ArrayList<>();
        relations1 = new ArrayList<>();
        concepts1 = new ArrayList<>();
        relations.add(-3480836);
        concepts.add(3481454);
        concepts.add(3481513);
        relations1.add(-3480838);
        concepts1.add(3481454);
        concepts1.add(3481513);
        mp = new MetaPath(concepts, relations);
        mp1 = new MetaPath(concepts1, relations1);
        genQueries(mp, mp1, "单步多条布尔.txt");*/
    }

    public static void yago21o(String fileName){
        List<Integer> relations = new ArrayList<>();
        List<Integer> concepts = new ArrayList<>();

        relations.add(4295853);
        relations.add(-4295834);
        concepts.add(4444536);
        concepts.add(4598716);
        concepts.add(4683384);
        MetaPath mp = new MetaPath(concepts, relations);
        List<Integer> queries = new ArrayList<>();
        File file = new File(fileName);
        try {
            FileReader fr = new FileReader(file);
            BufferedReader reader = new BufferedReader(fr);

            String str;
            int i = 0;
            while((str=reader.readLine()) != null){
                i ++;
                if(i%2 == 1){
                    int query = Integer.parseInt(str);
                    queries.add(query);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

       // System.out.println(queries);
        genYago21O(mp, queries, "out/artifacts/WSDMExperiment_jar/yago/多步单条排序_new.txt");
    }

    public static void yagoQueries(){
        List<Integer> relations = new ArrayList<>();
        List<Integer> concepts = new ArrayList<>();

        // 单步单条布尔
        /*relations.add(4295834);
        concepts.add(4663898);
        concepts.add(4598716);*/

        // 多步单条排序
        /*relations.add(4295853);
        relations.add(-4295834);
        concepts.add(4444536);
        concepts.add(4598716);
        concepts.add(4845301);*/

        // 多步多条布尔
        /*List<Integer> relations1 = new ArrayList<>();
        List<Integer> concepts1 = new ArrayList<>();
        relations.add(4295855);
        relations.add(-4295855);
        concepts.add(4351015);
        concepts.add(4561253);
        concepts.add(4351015);
        relations1.add(4295854);
        relations1.add(-4295854);
        concepts1.add(4351015);
        concepts1.add(4561253);
        concepts1.add(4351015);*/

        // 多步单条布尔
        relations.add(4295855);
        relations.add(-4295855);
        relations.add(4295839);
        concepts.add(4832388);
        concepts.add(4561253);
        concepts.add(4832388);
        concepts.add(4512909);
        //concepts.add(4675051);
        MetaPath mp = new MetaPath(concepts, relations);
        //MetaPath mp1 = new MetaPath(concepts1, relations1);
        //genQueries(mp, mp1,"yagoQueries/多步多条布尔.txt");
        genQueries_sp(mp, "yagoQueries/多步单条布尔候选.txt");

    }

    public static void main(String[] args){
        //RelationIndex.initializeMap();
        GraphModelM.initializeMap();
        GraphOntGetterM.initializeMap();
        //TypedGraphModelM.initializeMap();

        //dbpediaQueries();

       // yagoQueries();
        yago21o("out/artifacts/WSDMExperiment_jar/yago/多步单条排序.txt");

    }

}
