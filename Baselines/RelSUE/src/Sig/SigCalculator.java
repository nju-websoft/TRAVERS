package Sig;

import GraphData.GraphModelM;
import GraphData.GraphOntGetterM;
import GraphData.RelationIndex;
import GraphData.TypedGraphModelM;
import JDBCUtils.JdbcUtil;
import Path.MetaPath;
import Path.RelationPath;
import PathBasedSimilarity.PCRW;

import java.util.*;

public class SigCalculator {
    static double beta = 0.9;

    public static void setBeta(double decay){
        beta = decay;
    }

    public static double getSig(RelationPath rp, int query, List<Integer> examples){
        System.out.println("hahaha: " + rp);
        Map<Integer, Integer> accessed = getAccessed(query, rp, rp.length());
       // Map<Integer, Integer> accessed = getAccessedApproximate(query, rp, 1000);

        return calculateSig(accessed, query, examples);
    }

    /**
     *  Calculate the significance for a meta path
     * @param mp
     * @param query
     * @param examples
     * @return the significance of mp. This version is based on rankings.
     */
    public static double getSig(MetaPath mp, int query, List<Integer> examples){
        Set<Integer> relations = new HashSet<>();
        relations.addAll(mp.getRelations());

        /*if(JdbcUtil.URL.contains("yago")){
            if(relations.contains(-4295828) || relations.contains(-4295829) || relations.contains(-4295840)
                    || relations.contains(-4295837) || relations.contains(-4295851)){
                return 0;
            }
        }*/
        /*else if(JdbcUtil.URL.contains("dbpedia")){
            if(relations.contains(-3480819) || relations.contains(-3480809) || relations.contains(-3480812) ||
                    relations.contains(-3480813) || relations.contains(-3480846) || relations.contains(-3480811)
                    || relations.contains(-3480933) || relations.contains(-3480810) || relations.contains(-3480860)
                    || relations.contains(-3480850) || relations.contains(-3480951)){
                return 0;
            }
        }*/

        Map<Integer, Integer> accessed = getAccessed(query, mp, mp.length());
        //Map<Integer, Integer> accessed = getAccessedApproximate(query, mp, 1000);

       // double decay = (JdbcUtil.URL.contains("yago"))?Math.pow(beta, mp.length()):1; // ########################################
        double decay = Math.pow(beta, mp.length());

        return decay*calculateSig(accessed, query, examples);

    }

    private static double calculateSig(Map<Integer, Integer> accessed, int query, List<Integer> examples){
        int numOfNonExamples = RelationIndex.map.length - examples.size() - 1; // -1因为map对一个0
        int count = 0;
        for(Map.Entry<Integer, Integer> entry : accessed.entrySet()){
            count += entry.getValue();
        }

        //System.out.println("total visit count: " + count);

        accessed = sortByValue(accessed, examples);

        int numofDefeat = 0;
        for(int e : examples){
            int i = 0;
            int j = 0;
            for(Map.Entry<Integer, Integer> entry : accessed.entrySet()){
                if(entry.getKey() == e)
                    break;
                else if(!examples.contains(entry.getKey()))
                    i ++;

                j ++;
            }

            if(j != accessed.entrySet().size())
                numofDefeat += i;
            else
                numofDefeat += numOfNonExamples;
        }

        //System.out.println("num of Defeat: " + numofDefeat);

        if(0 == numofDefeat)
            return 1;
        else return 1 - (double)(numofDefeat)/(double)(numOfNonExamples*examples.size());
        //return 1 - (double)(sumOfRank - examples.size()*(examples.size() + 1)/2)/(double)((numOfNonExamples*examples.size()));
    }

    public static Map<Integer, Integer> getAccessed(int a, RelationPath rp, int left){
       // System.out.println("relation path: " + rp);
        Map<Integer, Integer> result = new HashMap<>();
        if(left == 0){
            result.put(a, 1);
            return result;

        }
        else{
            int relation = rp.getRelations().get(rp.length() - left);
            List<Integer> nodes = RelationIndex.getObjects(a, relation);
            if(nodes != null){
                for(int node : nodes){
                    Map<Integer, Integer> map = getAccessed(node, rp, left - 1);
                    for(int key : map.keySet()){
                        if(result.containsKey(key)){
                            result.put(key, result.get(key) + map.get(key));
                        }
                        else{
                            result.put(key, map.get(key));
                        }
                    }
                }
            }


            return result;
        }
    }
    /**
     *
     * @param a starting node
     * @param mp meta path
     * @param  left initialize as the length of mp
     * @return key->accessed node, value->count of accessed
     */
    public static Map<Integer, Integer> getAccessed(int a, MetaPath mp, int left){
        if(!GraphOntGetterM.classOfEntityByID(a).contains(mp.getConcepts().get(mp.length() - left))){
            System.out.println(a);
            try {
                throw new Exception("Error: the first class of the meta path is inconsistent with the class of " + a);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Map<Integer, Integer> result = new HashMap<>();
        if(left == 0){
            if(GraphOntGetterM.classOfEntityByID(a).contains(mp.getConcepts().get(mp.length()))){
                result.put(a, 1);
            }
            return result;

        }
        else{
            int concept = mp.getConcepts().get(mp.length() - left + 1);
            int relation = mp.getRelations().get(mp.length() - left);
            List<Integer> nodes = TypedGraphModelM.getTypedObjects(a, relation, concept); //#################################################

           // List<Integer> nodes = GraphModelM.getTypedObjects(a, relation, concept);

            for(int node : nodes){
                Map<Integer, Integer> map = getAccessed(node, mp, left - 1);
                for(int key : map.keySet()){
                    if(result.containsKey(key)){
                        result.put(key, result.get(key) + map.get(key));
                    }
                    else{
                        result.put(key, map.get(key));
                    }
                }
            }


            return result;
        }

    }

    public static Map<Integer, Integer> getAccessedApproximate(int a, RelationPath rp, int k){
        Random rand = new Random();
        Map<Integer, Integer> result = new HashMap<>();

        for(int i = 0; i < k; i ++){
            int visited = a;
            for(int j = 0; j < rp.length(); j ++){
                int relation = rp.getRelations().get(j);

                List<Integer> entities = RelationIndex.getObjects(visited, relation);
                //System.out.println("relation: " + relation);
                //System.out.println("entities: " + entities);
                if(entities != null && entities.size() > 0 ){
                    int id = rand.nextInt(entities.size());
                    visited = entities.get(id);
                    // System.out.println("visited: " + visited);
                }
                else{
                    visited = -1;
                    break;
                }
            }

            if(visited != -1){
                if(result.containsKey(visited)){
                    result.put(visited, result.get(visited) + 1);
                }
                else result.put(visited, 1);
            }
        }

        return result;
    }

    // 随机设k个walkers来近似，减小计算开销
    public static Map<Integer, Integer> getAccessedApproximate(int a, MetaPath mp, int k){
       // System.out.println("metapath: " + mp);
        Random rand = new Random();
        Map<Integer, Integer> result = new HashMap<>();

        for(int i = 0; i < k; i ++){
            int visited = a;
            for(int j = 0; j < mp.length(); j ++){
                int relation = mp.getRelations().get(j);
                int concept = mp.getConcepts().get(j + 1);

                //List<Integer> entities = GraphModelM.getTypedObjects(visited, relation, concept);
                //List<Integer> entities = GraphModelM.getMPObjects(visited, relation, concept, 100);
                List<Integer> entities = TypedGraphModelM.getTypedObjects(visited, relation, concept);

                if(entities.size() > 0 ){
                    int id = rand.nextInt(entities.size());
                    visited = entities.get(id);
                   // System.out.println("visited: " + visited);
                }
                else{
                    visited = -1;
                    break;
                }
            }

            if(visited != -1){
                if(result.containsKey(visited)){
                    result.put(visited, result.get(visited) + 1);
                }
                else result.put(visited, 1);
            }
        }

        return result;
    }

    private static <K, V> Map<K, V> sortByValue(Map<K, V> map, final List<Integer> examples) {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Object>() {
            @SuppressWarnings("unchecked")
            public int compare(Object o1, Object o2) {
                int result = ((Comparable<V>) ((Map.Entry<K, V>) (o2)).getValue()).compareTo(((Map.Entry<K, V>) (o1)).getValue());
                if(result != 0)
                    return result;
                else{
                    if(examples.contains(((Map.Entry<K, V>)o2).getKey()) && !examples.contains(((Map.Entry<K, V>)o1).getKey()))
                        return -1;
                    else if(!examples.contains(((Map.Entry<K, V>)o2).getKey()) && examples.contains(((Map.Entry<K, V>)o1).getKey()))
                        return 1;
                    else return 0;
                }
            }
        });

        Map<K, V> result = new LinkedHashMap<>();
        for (Iterator<Map.Entry<K, V>> it = list.iterator(); it.hasNext();) {
            Map.Entry<K, V> entry = (Map.Entry<K, V>) it.next();
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public static void test0(){
        RelationIndex.initializeMap();
        GraphOntGetterM.initializeMap();

        List<Integer> relations = new ArrayList<>();
        List<Integer> concepts = new ArrayList<>();

        //relations.add(4295834);
        //relations.add(-4295853);

        /*relations.add(4295828);
        relations.add(4295836);
        relations.add(4295851);
        relations.add(-4295851);*/

        relations.add(-4295853);
        relations.add(4295828);
        relations.add(-4295829);

        concepts.add(4832388);
        concepts.add(4342597);
        concepts.add(4521609);
        concepts.add(4723919);
        // concepts.add(4832388);

        MetaPath mp = new MetaPath(concepts, relations);
        //RelationPath rp = new RelationPath(relations);
        long start = System.currentTimeMillis();
        Map<Integer, Integer> map = getAccessed(2125240, mp, mp.length());
        long end = System.currentTimeMillis();
        System.out.println("time0 : " + (end - start));

//        for(Map.Entry<Integer, Integer> entry : map.entrySet()){
//            System.out.println(entry.getKey() + ": " + entry.getValue());
//        }

       /* List<Integer> examples = new ArrayList<>();
        examples.add(1889736);
        examples.add(989135);
        System.out.println(getSig(mp, 627504, examples));

        examples.remove((Integer)989135);
        System.out.println(getSig(mp, 627504, examples));*/

        List<Integer> random = new ArrayList<>();
        random.add(250);
        random.add(2501);
        start = System.currentTimeMillis();
        System.out.println(getSig(mp, 2125240, random));
        end = System.currentTimeMillis();
        System.out.println("time1: " + (end - start));


        // random.add(1889736);
        //for(int i = 0; i < 1000; i ++)
        //    getSig(mp, 627504, random);
        // System.out.println(getSig(mp, 627504, random));

        // concepts.add();

        start = System.currentTimeMillis();
        Map<Integer, Integer> map0 = getAccessedApproximate(2125240, mp, 100);
        end = System.currentTimeMillis();
        System.out.println("time2: " + (end - start));

        System.out.println(map0);
    }

    public static void test1(){
        RelationIndex.initializeMap();

        PCRW pcrw = new PCRW();
        List<Integer> relations = new ArrayList<Integer>();
        //relations.add(3480880);
        //relations.add(-3480880);
        //relations.add(3480880);
        //relations.add(-3480880);

        // yago
        relations.add(4295829);
        relations.add(-4295841);
        relations.add(4295828);
        relations.add(-4295829);

        RelationPath rp = new RelationPath(relations);

        //System.out.println(pcrw.getSim(1562340, 2580708, rp));
        long start = System.currentTimeMillis();
        System.out.println(pcrw.getSim(2125240, 4103860, rp));
        long end = System.currentTimeMillis();
        System.out.println("time: " + (end - start));
        start = System.currentTimeMillis();
        getAccessed(2125240, rp, rp.length());
        end = System.currentTimeMillis();
        System.out.println("time1: " + (end - start));
    }

    public static void getAccessedTest(){
        RelationIndex.initializeMap();
        GraphOntGetterM.initializeMap();
        TypedGraphModelM.initializeMap();

        List<Integer> relations = new ArrayList<>();
        List<Integer> concepts = new ArrayList<>();

        // 多步单条
        relations.add(3480963);
        relations.add(-3480963);
        concepts.add(3481703);
        concepts.add(3481453);
        concepts.add(3481703);

        MetaPath mp = new MetaPath(concepts, relations);

        System.out.println(getAccessed(492, mp, mp.length()));
    }

    public static void main(String[] args){
        //test1();
        getAccessedTest();
    }
}
