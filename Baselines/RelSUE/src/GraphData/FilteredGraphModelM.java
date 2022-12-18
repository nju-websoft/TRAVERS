package GraphData;

import JDBCUtils.JdbcUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * Remove entities with big degree based on GraphModelM
 */
public class FilteredGraphModelM {
    public static Map<Integer, Map<Integer, List<Integer>>> map = null;
    public static List<Integer> allEntities = new ArrayList<>();

    public static void initializeMap(int maximalDegree){
        if(null == map){
            map = new HashMap<>();
            Set<Integer> illegalEntities = new HashSet<>(); // store all entities with degree greater than maximalDegree
            for(Map.Entry<Integer, Map<Integer, List<Integer>>> entry : GraphModelM.map.entrySet()){
                Map<Integer, List<Integer>> m0 = entry.getValue();
                int degree0 = 0;
                for(Map.Entry<Integer, List<Integer>> e0 : m0.entrySet()){
                    degree0 += e0.getValue().size();
                }
                if(degree0 > maximalDegree)
                    illegalEntities.add(entry.getKey());
            }

            map.putAll(GraphModelM.map);
            for(Map.Entry<Integer, Map<Integer, List<Integer>>> entry : GraphModelM.map.entrySet()){
                if(illegalEntities.contains(entry.getKey())){
                    map.remove(entry.getKey());
                }
                else{
                    Map<Integer, List<Integer>> m0 = map.get(entry.getKey());
                    for(int k : m0.keySet()){
                        List<Integer> nbs = m0.get(k);
                        List<Integer> nnbs = new ArrayList<>();
                        nnbs.addAll(nbs);
                        for(int nb : nnbs){
                            if(illegalEntities.contains(nb)){
                                nbs.remove((Integer)nb);
                            }
                        }
                    }
                }
            }
            allEntities.addAll(map.keySet());
            System.out.println("Removed entities count: " + illegalEntities.size());
            System.out.println("Remaining entities count: " + map.size());
        }
    }
    public boolean isConnected(int v0, int v1){
        if(!map.containsKey(v0) || !map.containsKey(v1))
            return false;

        Map<Integer, List<Integer>> rmap = map.get(v0);
        for(int key : rmap.keySet()){
            List<Integer> nodes = rmap.get(key);
            for(int node : nodes){
                if(node == v1)
                    return true;
            }
        }
        return false;
    }

    public static Set<Integer> getAllRelations(int id){
        Map<Integer, List<Integer>> m = map.get(id);
        Set<Integer> relations = new HashSet<>();
        if(m != null)
            relations.addAll(m.keySet());

        return  relations;
    }

    public static Set<Integer> getAllRelations(Set<Integer> ids){ //返回一个并集，这个方法没什么功能性的作用
        Set<Integer> result = new HashSet<>();
        if(null == ids || ids.size() == 0)
            return result;

        for(int id : ids){
            result.addAll(getAllRelations(id));
        }
        return result;
    }

    public static Set<Integer> getAllRelations(int id, Set<Integer> ids){ //去除会出现环路的relation
        Set<Integer> result = new HashSet<>();
        Map<Integer, List<Integer>> rmap = map.get(id);
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

    public static Map<Integer, List<Integer>> getAllPaos(int id, Set<Integer> ids){//ids用来记录路径上已经出现过的点，避免路径上形成环路
        Map<Integer, List<Integer>> result = new HashMap<Integer, List<Integer>>();
        Map<Integer, List<Integer>> rmap = map.get(id);

        if(rmap != null){
            if(ids != null){
                for(int key : rmap.keySet()){
                    List<Integer> nodes = rmap.get(key);
                    List<Integer> legalnodes = new ArrayList<Integer>();
                    for(int node : nodes){ //这里可能需要改成set，不然会很浪费时间，不过这个方法似乎只有在dfs时候会调用到
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
    public static Map<Integer, List<Integer>> get(int id){//获取所有给定实体的三元组，一般不用
        return map.get(id);
    }


    //路径去环路， list即为已经访问过的点
    public static List<Integer> getObjects(int subject, int predicate, Set<Integer> list){
        List<Integer> result = new ArrayList<>();
        Map<Integer, List<Integer>> m = map.get(subject);
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
        if(map.get(subject) != null)
            return (map.get(subject)).get(predicate);
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
    public static List<Integer> getTypedObjects(int subject, int predicate, int type){ // 这种get方式实在太慢（快的方式又存不下来，空间太大）
        List<Integer> objects = (map.get(subject)).get(predicate);
        List<Integer> result = new ArrayList<Integer>();
        if(objects != null){
            for(int object : objects){
                Set<Integer> types = GraphOntGetterM.classOfEntityByID(object);
                if(types.contains(type))
                    result.add(object);
            }
        }

        return result;
    }

    //采样计算sig时使用，如果直接用getTypedObjects当objects多的时候会非常慢，所以这里限制objects数量不超过一百
    public static List<Integer> getMPObjects(int subject, int predicate, int type, int window){
        List<Integer> objects = (map.get(subject)).get(predicate);
        List<Integer> result = new ArrayList<Integer>();

        if(null == objects) return result;
        else{
            if(objects.size() <= window){
                for(int object : objects){
                    Set<Integer> types = GraphOntGetterM.classOfEntityByID(object);
                    if(types.contains(type))
                        result.add(object);
                }
            }
            else{
                Random rand = new Random();
                int start = rand.nextInt(objects.size());
                List<Integer> nlist = new ArrayList<>();
                if((start + window - 1) < objects.size()){
                    nlist.addAll(objects.subList(start, start + window));
                }
                else{
                    int remain = start + window - 1 - objects.size();
                    nlist.addAll(objects.subList(0, remain));
                    nlist.addAll(objects.subList(start, objects.size()));
                }

                for(int object : nlist){
                    Set<Integer> types = GraphOntGetterM.classOfEntityByID(object);
                    if(types.contains(type))
                        result.add(object);
                }
            }
        }



        return result;
    }

    public static void main(String[] args) {
        GraphModelM.initializeMap();
        //System.out.println(GraphModelM.map.get(1349999));
        //System.out.println(GraphModelM.getObjects(1562340, 3480880));
        System.out.println(GraphModelM.getAllRelations(985891, new HashSet<Integer>()));
    }
}
