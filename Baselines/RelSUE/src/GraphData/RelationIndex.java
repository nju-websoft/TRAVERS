package GraphData;

import JDBCUtils.JdbcUtil;
import Path.RelationPath;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * The old implementation of GraphModelM utilize a hashmap to index each entity, which consumes too much memory.
 * Instead, RelationIndex exploit the index of an array to index entities since each entity has an numerical id in the database
 */

public class RelationIndex {

    static Connection conn = JdbcUtil.getConnection();
    public static Map<Integer, List<Integer>>[] map = null;

    public static void initializeMap(){
        if(null == map){
            if(JdbcUtil.URL.contains("yago")){
                map = (Map<Integer, List<Integer>>[])new HashMap[4295826];
        }
            else if(JdbcUtil.URL.contains("dbpedia")){
                map = (Map<Integer, List<Integer>>[])new HashMap[3480807];
            }
            else{

            }
            String sql = "select * from objtriples";
            try {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                while(rs.next()){
                    int subject = rs.getInt("subject");
                    int predicate = rs.getInt("predicate");
                    int object = rs.getInt("object");

                    if(null == map[subject])
                        map[subject] = new HashMap<>();

                    if(map[subject].size() == 0){
                        Map<Integer, List<Integer>> rmap = new HashMap<Integer, List<Integer>>();
                        List<Integer> nodes = new ArrayList<Integer>();
                        nodes.add(object);
                        rmap.put(predicate, nodes);
                        map[subject] = rmap;
                    }
                    else{
                        Map<Integer, List<Integer>> rmap = map[subject];
                        if(rmap.containsKey(predicate)){
                            List<Integer> nodes = rmap.get(predicate);
                            nodes.add(object);
                            rmap.put(predicate, nodes);
                        }
                        else{
                            List<Integer> nodes = new ArrayList<Integer>();
                            nodes.add(object);
                            rmap.put(predicate, nodes);
                        }
                        map[subject] = rmap;

                    }

                    if(null == map[object])
                        map[object] = new HashMap<>();

                    if(map[object].size() == 0){
                        Map<Integer, List<Integer>> rmap = new HashMap<Integer, List<Integer>>();
                        List<Integer> nodes = new ArrayList<Integer>();
                        nodes.add(subject);
                        rmap.put(predicate*-1, nodes);
                        map[object] = rmap;
                    }
                    else{
                        Map<Integer, List<Integer>> rmap = map[object];
                        if(rmap.containsKey(predicate*-1)){
                            List<Integer> nodes = rmap.get(predicate*-1);
                            nodes.add(subject);
                            rmap.put(predicate*-1, nodes);
                        }
                        else{
                            List<Integer> nodes = new ArrayList<Integer>();
                            nodes.add(subject);
                            rmap.put(predicate*-1, nodes);
                        }
                        map[object] = rmap;
                    }
                }

                System.out.println("triples loaded!");
                stmt.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        else
            System.out.println("Graph model has already be initialized");
    }
    public boolean isConnected(int v0, int v1){
        if(v0 >= map.length || v1 >= map.length)
            return false;

        Map<Integer, List<Integer>> rmap = map[v0];
        for(int key : rmap.keySet()){
            List<Integer> nodes = rmap.get(key);
            for(int node : nodes){
                if(node == v1)
                    return true;
            }
        }
        return false;
    }

    public static int getDegree(int id){
        Map<Integer, List<Integer>> m = map[id];
        int degree = 0;
        for(Map.Entry<Integer, List<Integer>> entry : m.entrySet()){
            degree += entry.getValue().size();
        }

        return degree;
    }

    public static Set<Integer> getAllRelations(int id){
        Map<Integer, List<Integer>> m = map[id];
        Set<Integer> relations = new HashSet<>();
        if(m != null)
            relations.addAll(m.keySet());

        return  relations;
    }

    public static Set<Integer> getAllRelations(Set<Integer> ids){ //????????????????????????????????????????????????????????????
        Set<Integer> result = new HashSet<>();
        if(null == ids || ids.size() == 0)
            return result;

        for(int id : ids){
            result.addAll(getAllRelations(id));
        }
        return result;
    }

    public static Set<Integer> getAllRelations(int id, Set<Integer> ids){ //????????????????????????relation
        Set<Integer> result = new HashSet<>();
        Map<Integer, List<Integer>> rmap = map[id];
        if(rmap != null){
            if(ids != null){
                for(int key : rmap.keySet()){
                    List<Integer> nodes = rmap.get(key);
                    for(int node : nodes){
                        if(!ids.contains(node)){
                            result.add(key);
                            break;
                        }

                    }
                }
            }
            else
                return rmap.keySet();
        }

        return result;
    }

    public static Map<Integer, List<Integer>> getAllPaos(int id, Set<Integer> ids){//ids????????????????????????????????????????????????????????????????????????
        Map<Integer, List<Integer>> result = new HashMap<Integer, List<Integer>>();
        Map<Integer, List<Integer>> rmap = map[id];

        if(rmap != null){
            if(ids != null){
                for(int key : rmap.keySet()){
                    List<Integer> nodes = rmap.get(key);
                    List<Integer> legalnodes = new ArrayList<Integer>();
                    for(int node : nodes){ //????????????????????????set???????????????????????????????????????????????????????????????dfs??????????????????
                        if(!ids.contains(node))
                            legalnodes.add(node);
                    }

                    result.put(key, legalnodes);
                }
            }
            else
                return rmap;
        }

        return result;
    }

    @Deprecated
    public static Map<Integer, List<Integer>> get(int id){//???????????????????????????????????????????????????
        return map[id];
    }


    //?????????????????? list???????????????????????????
    public static List<Integer> getObjects(int subject, int predicate, Set<Integer> list){
        List<Integer> result = new ArrayList<>();
        Map<Integer, List<Integer>> m = map[subject];
        if(m != null){
            List<Integer> objs = m.get(predicate);
            if(objs != null){
                result.addAll(objs);
            }
        }
        else
            return null;

        for(Integer i : list){
            result.remove(i);
        }

        return result;
    }
    /**
     *
     * @param subject
     * @param predicate
     * @return all objects satisfy the subject and predicate
     */
    public static List<Integer> getObjects(int subject, int predicate){
        if(subject < map.length)
            return (map[subject]).get(predicate);
        else
            return null;
    }
    /**
     *
     * @param subject
     * @param predicate
     * @param type
     * @return all objects have type type satisfy the subject and predicate
     */
    public static List<Integer> getTypedObjects(int subject, int predicate, int type){
        List<Integer> objects = (map[subject]).get(predicate);
        List<Integer> result = new ArrayList<Integer>();
        for(int object : objects){
            Set<Integer> types = GraphOntGetterM.classOfEntityByID(object);
            if(types.contains(type))
                result.add(object);
        }

        return result;
    }

    public static void main(String[] args) {
        RelationIndex.initializeMap();

       // System.out.println(RelationIndex.getAllRelations(985891, new HashSet<Integer>()));
        int count = 0; // ????????????????????????relation????????????10000?????????????????????
        Set<Integer> relations = new HashSet<>(); // ??????????????????relation
        int num = 0;
        for(int i = 1; i < map.length; i ++){ // ??????i???1?????????0????????????
            for(Map.Entry<Integer, List<Integer>> entry : map[i].entrySet()){
                if(entry.getValue().size() > 5000){
                    count ++;
                    System.out.println("id: " + i);
                    System.out.println("relation: " + entry.getKey());
                    relations.add(entry.getKey());
                    num += entry.getValue().size();
                }
            }
        }
        System.out.println(count);
        System.out.println(relations);
        System.out.println(num);
    }
}
