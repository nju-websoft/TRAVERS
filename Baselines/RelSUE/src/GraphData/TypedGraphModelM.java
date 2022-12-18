package GraphData;

import java.util.*;

public class TypedGraphModelM {
    // index -> Map<relation, Map<type, List<neighbours>>>
    public static Map<Integer, Map<Integer, List<Integer>>>[] map = null;

    public static void initializeMap(){
        if(null == map){
            if(null == RelationIndex.map || null == GraphOntGetterM.map){
                try {
                    throw new Exception("error! please load relationIndex and graphontgetter first!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else{
                map = (Map<Integer, Map<Integer, List<Integer>>>[])new HashMap[RelationIndex.map.length];
                for(int i = 0; i < RelationIndex.map.length; i ++){
                    Map<Integer, List<Integer>> mapR = RelationIndex.map[i];
                    if(mapR != null){
                        Map<Integer, Map<Integer, List<Integer>>> nitem = new HashMap<>();
                        for(Map.Entry<Integer, List<Integer>> entry : mapR.entrySet()){
                            List<Integer> nodes = entry.getValue();
                            Map<Integer, List<Integer>> typedMap = new HashMap<>();
                            for(int node : nodes){
                                Set<Integer> types = GraphOntGetterM.classOfEntityByID(node);
                                for(int type : types){
                                    if(typedMap.containsKey(type)){
                                        typedMap.get(type).add(node);
                                    }
                                    else{
                                        List<Integer> neighbours = new ArrayList<>();
                                        neighbours.add(node);
                                        typedMap.put(type, neighbours);
                                    }
                                }
                            }
                            nitem.put(entry.getKey(), typedMap);
                        }

                        map[i] = nitem;
                    }
                }

                System.out.println("typed objtriples loaded!");
            }

        }
    }

    public static Set<Integer> getAllRelations(int id){
        Map<Integer, Map<Integer, List<Integer>>> m = map[id];
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
        Map<Integer, Map<Integer, List<Integer>>> rmap = map[id];
        if(rmap != null){
            if(ids != null){
                for(int key : rmap.keySet()){
                    for(int type : rmap.get(key).keySet()){
                        List<Integer> nodes = rmap.get(key).get(type);
                        for(int node : nodes){
                            if(!ids.contains(node)){
                                result.add(key);
                                break;
                            }

                        }
                    }
                }
            }
            else
                return rmap.keySet();
        }

        return result;
    }

    public static Map<Integer, Map<Integer, List<Integer>>> getAllTypedPaos(int id, Set<Integer> ids){//ids用来记录路径上已经出现过的点，避免路径上形成环路
        Map<Integer, Map<Integer, List<Integer>>> result = new HashMap<>();
        Map<Integer, Map<Integer, List<Integer>>> rmap = map[id];

        if(rmap != null){
            if(ids != null){
                for(int key : rmap.keySet()){
                    Map<Integer, List<Integer>> nitem = new HashMap<>();
                    Map<Integer, List<Integer>> typedMap = rmap.get(key);

                    for(Map.Entry<Integer, List<Integer>> entry : typedMap.entrySet()){
                        List<Integer> nodes = entry.getValue();
                        List<Integer> nnodes = new ArrayList<>();
                        for(int node : nodes){
                            if(!ids.contains(node))
                                nnodes.add(node);
                        }

                        nitem.put(entry.getKey(), nnodes);
                    }


                    result.put(key, nitem);
                }
            }
            else
                return rmap;
        }

        return result;
    }



    //路径去环路， list即为已经访问过的点
    public static List<Integer> getTypedObjects(int subject, int predicate, int type, Set<Integer> list){
        List<Integer> result = new ArrayList<>();
        Map<Integer, List<Integer>> m = map[subject].get(predicate);
        if(m != null){
            List<Integer> objs = m.get(type);
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
     * @param type
     * @return all objects have type type satisfy the subject and predicate
     */
    public static List<Integer> getTypedObjects(int subject, int predicate, int type){
        if(map[subject] != null){
            if(map[subject].get(predicate) != null){
                List<Integer> result = map[subject].get(predicate).get(type);
                if(null == result)
                    return new ArrayList<>();
                else
                    return result;
            }
            else return  new ArrayList<>();
        }
        else return new ArrayList<>();
    }


    public static void main(String[] args){
        long a = System.currentTimeMillis();
        RelationIndex.initializeMap();
        long b = System.currentTimeMillis();
        System.out.println("time0: " + (b - a));
        GraphOntGetterM.initializeMap();
        a = System.currentTimeMillis();
        System.out.println("time1: " + (a - b));
        TypedGraphModelM.initializeMap();
        b = System.currentTimeMillis();
        System.out.println("time2: " + (b - a));

       // System.out.println(TypedGraphModelM.getTypedObjects(2125240, -4295853, 4342597));

        //yago
        a = System.currentTimeMillis();
        for(int i = 0; i < 100; i ++)
            RelationIndex.getTypedObjects(2291382, -4295828, 4832388);
        b = System.currentTimeMillis();
        System.out.println("non-typed version: " + (b - a));

        a = System.currentTimeMillis();
        for(int i = 0; i < 10000; i ++)
            TypedGraphModelM.getTypedObjects(2291382, -4295828, 4832388);
        b = System.currentTimeMillis();
        System.out.println("typed version: " + (b - a));

        //dbpedia
        /*a = System.currentTimeMillis();
        List<Integer> wtf = new ArrayList<>();
        for(int i = 0; i < 10000; i ++)
           wtf = RelationIndex.getTypedObjects(630840, -3480809, 3481453);
        b = System.currentTimeMillis();
        System.out.println("non-typed version: " + (b - a));
        System.out.println(wtf.size());

        a = System.currentTimeMillis();
        List<Integer> wtf0 = new ArrayList<>();
        for(int i = 0; i < 10000; i ++)
           wtf0 =  TypedGraphModelM.getTypedObjects(630840, -3480809, 3481453);
        b = System.currentTimeMillis();
        System.out.println("typed version: " + (b - a));
        System.out.println(wtf0.size());*/
    }
}
