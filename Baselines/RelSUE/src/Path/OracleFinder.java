package Path;

import GraphData.*;
import JDBCUtils.JdbcUtil;
import Sig.SigCalculator;
import oracle.OracleDB;
import oracle.OracleFromFile;

import java.util.*;

/**
 * Greedy Search utilizing heuristic based on oracle distance and discriminability (i.e. num of entities in each search state/node)
 */

public class OracleFinder {
    static int K = 20;
    static double threshold = 0.5; // WSDM setting 两个数据集保持一致
    //static double threshold = 0.6;
    public List<Node> OpenList = new ArrayList<>();
    public Set<MetaPath> paths = new HashSet<>();
    long start_time_stamp;


    public static void setK(int num){
        K = num;
    }

    public static void setThreshold(double tau){
        threshold = tau;
    }

    public void reset(){
        OpenList = new ArrayList<>();
        paths = new HashSet<>();
    }

    public class Node{
        public MetaPath mp = null;
        public Map<Integer, Set<List<Integer>>> map; // key-> visited entity v, value->set of paths connecting query and v
        //The aim of storing those path instances is to eliminate the cycles and to determine whether two meta-paths share
        // the same path instances.

        private double score = 0; // for expanding
        private double distance = 200;
        // 优先选距离小的，距离同样小的，优先选size小的

        //private double significance = 0; // for selection

        public Node(MetaPath mp, Map<Integer, Set<List<Integer>>> map){
            this.mp = mp;
            this.map = map;
        }

        public boolean isCompleted(List<Integer> examples){// if any example is visited, then it's finished
            //if(mp.length() >= LEN)
            //	return true;
            if(null == map || map.size() == 0)
                return false;
            for(int example : examples){
                if(map.keySet().contains(example))
                    return true;
            }

            return false;
        }

        public boolean equals(Object node){
            return map.keySet().equals(((Node)node).map.keySet());
            //return map.equals(((Node)node).map);
        }

        public int hashCode(){
            /*int count = 0;
            for(Map.Entry<Integer, Set<List<Integer>>> entry : map.entrySet()){
                count += entry.getValue().size();
            }

            int mod = map.size() % 12306;
            return mod*mod + count;*/

            int code = 0;
            for(int key : map.keySet()){
                code += key;
            }
            return code;
        }

        public double getScore(){
            return  this.score;
        }
        public void setScore(double score){
            this.score = score;
        }

        public void setDistance(double distance){
            this.distance = distance;
        }

        public String toString(){
            return this.mp.toString() + this.distance + "\nentities size: " + map.keySet().size() + " degree sum: " + sumOfDegree(map.keySet());
        }
    }

    /**
     *
     * @param query
     * @param examples
     * @return k meta paths (k <= K)
     */
    public Set<MetaPath> findMetaPath_K(int query, List<Integer> examples) {
       start_time_stamp = System.currentTimeMillis();

        // System.out.println("begin initialize...");
        OpenList.add(initialize(query, examples));
       // System.out.println(OpenList);
       // System.out.println("Begin finding...");
        while(OpenList.size() > 0 && paths.size() < K){
            //Node max = MaxS(OpenList);
            Node max = OptimalNode(OpenList);
            //System.out.println("open list size: " + OpenList.size());
            //System.out.println(paths);
            //System.out.println("max: " + max);
            //System.out.println("examples size: " + examples.size());
            OpenList.remove(max);
           /* if(max.isCompleted(examples)){
                paths.add(max.mp); // this will change paths.size()
            }

            if(paths.size() < K){ // if unfinished, we have to continue our searching
                long start = System.currentTimeMillis();
                OpenList.addAll(Extend(max, examples));
                long end = System.currentTimeMillis();
                System.out.println("time for extending: " + (end - start));

            }*/
           long start = System.currentTimeMillis();
           List<Node> nodes = Extend(max, examples);
           long end = System.currentTimeMillis();
          // System.out.println("time for extending: " + (end - start));
           if(System.currentTimeMillis() - start_time_stamp > 120000){
                break;
            }
           for(int i = 0; i < nodes.size() && paths.size() < K; i ++){
               //double threshold = (JdbcUtil.URL.contains("yago"))?0.6:0.5;
               if(nodes.get(i).isCompleted(examples) && SigCalculator.getSig(nodes.get(i).mp, query, examples) > threshold){
                   paths.add(nodes.get(i).mp);
               }
               OpenList.add(nodes.get(i));
           }

        }

       // System.out.println("final paths: " + paths);
        for(MetaPath mp : paths){
            System.out.println(mp + ",  sig:" + SigCalculator.getSig(mp, query, examples));
        }
        return paths;
    }


    public Set<Integer> nextRelations(Node node){
        return TypedGraphModelM.getAllRelations(node.map.keySet());
    }

    public Set<Integer> nextConcepts(Node node, int relation){
        Set<Integer> result = new HashSet<>();
        for(int entity : node.map.keySet()){
            if(TypedGraphModelM.map[entity].get(relation) != null)
                result.addAll(TypedGraphModelM.map[entity].get(relation).keySet());
        }

        return result;
    }

    @Deprecated
    public Node MaxS(List<Node> nodes){
        if(nodes.size() > 0){
            Node max = nodes.get(0);
            for(int i = 1; i < nodes.size(); i ++){
                if(nodes.get(i).getScore() > max.getScore()) {
                    max = nodes.get(i);
                }
            }
            return max;
        }
        else return null;
    }

    public Node OptimalNode(List<Node> nodes){ //优先选距离小，距离一样小优先选点少的
        if(nodes.size() > 0){
            Node opt = nodes.get(0);
            for(int i = 1; i < nodes.size(); i ++){
                if(nodes.get(i).distance < opt.distance){
                    opt = nodes.get(i);
                }
                //else if(nodes.get(i).distance == opt.distance && nodes.get(i).map.size() < opt.map.size()){
                else if(nodes.get(i).distance == opt.distance && sumOfDegree(nodes.get(i).map.keySet()) < sumOfDegree(opt.map.keySet())){
                    opt = nodes.get(i);
                }
            }

            return opt;
        }
        else return null;
    }

    public int sumOfDegree(Set<Integer> set){
        int sum = 0;
        for(int entity : set){
            sum += RelationIndex.getDegree(entity);
        }

        return sum;
    }

    public List<Node> Extend(Node parent, List<Integer> examples) {
        List<Node> result = new ArrayList<>();

        if(JdbcUtil.URL.contains("yago") && sumOfDegree(parent.map.keySet()) > 100000)
            return  result;

        Set<Integer> relations = nextRelations(parent);
        int count = 0;
        for(int relation : relations){
            if(legalRelation(parent, relation)){
                Set<Node> relationResult = new HashSet<>(); // 通过集合的方式去重（包含相同map的node）， 重写Node类的equals和hashCode方法
//                 List<Node> relationResult = new ArrayList<>(); // 这里不做去重，可以用来说明保留重复的影响
                //注意这里只能保证从一个parent扩展出来的没有重复
                Set<Integer> concepts = nextConcepts(parent, relation);
                for(int concept : concepts){
                    count ++;
                    Node newNode = Extend(parent, relation, concept);
                    if(newNode != null)
                        relationResult.add(Extend(parent, relation, concept));

                   /* if(System.currentTimeMillis() - start_time_stamp > 60000){ // yago两分钟， dbpedia一分钟
                        return result;
                    }*/
                }

                result.addAll(relationResult);
                /*for(Node no : relationResult){
                    if(no.map.size() < 5000)
                        result.add(no);
                }*/
            }
        }
       // System.out.println("count: " + count);
        /*for(Node node : result){
            node.setScore(calculateNodeScore(node, examples));
        }*/

        for(Node node : result){
            node.setDistance(calculateDistance(node, examples));
        }


        return result;
    }


    public Node Extend(Node parent, int relation, int concept){
        Map<Integer, Set<List<Integer>>> map = parent.map;
        Map<Integer, Set<List<Integer>>> nmap = new HashMap<>();
        for(Map.Entry<Integer, Set<List<Integer>>> entry : map.entrySet()){
            List<Integer> visitedlist = TypedGraphModelM.getTypedObjects(entry.getKey(), relation, concept);
            //if(visitedlist.size() > 1000) // little trick to accelerate the searching process
            //    return null;
            for(int visited : visitedlist){
                if(nmap.containsKey(visited)){
                    for(List<Integer> path : entry.getValue()){
                        if(!path.contains(visited)){ // 去环路
                            List<Integer> npath = new ArrayList<>();
                            npath.addAll(path);
                            npath.add(visited);

                            nmap.get(visited).add(npath);
                        }
                    }
                }
                else{
                    Set<List<Integer>> set = new HashSet<>();
                    for(List<Integer> path : entry.getValue()){
                        if(!path.contains(visited)){ // indicate no circles
                            List<Integer> npath = new ArrayList<>();
                            npath.addAll(path);
                            npath.add(visited);

                            set.add(npath);
                        }
                    }

                    if(set.size() != 0)
                        nmap.put(visited, set);
                }
            }
        }
        if(nmap.size() != 0){
            List<Integer> nconcepts = new ArrayList<>();
            nconcepts.addAll(parent.mp.getConcepts());
            nconcepts.add(concept);
            List<Integer> nrelations = new ArrayList<>();
            nrelations.addAll(parent.mp.getRelations());
            nrelations.add(relation);
            MetaPath nmp = new MetaPath(nconcepts, nrelations);

            Node node = new Node(nmp, nmap);

            return node;
        }
        else return null;
    }

    public boolean legalRelation(Node node, int relation){
        /*if(JdbcUtil.URL.contains("yago")){
            for(int key : node.map.keySet()){
                if(RelationIndex.map[key].get(relation) != null && RelationIndex.map[key].get(relation).size() > 5000)
                    return false;
            }
        }*/

        return true;
    }


    public Node initialize( int query, List<Integer> examples){
        int thingID = -1;
        if(JdbcUtil.URL.contains("yago")){
            thingID = 4832388;
        }
        else if(JdbcUtil.URL.contains("dbpedia")){
            thingID = 3481453;
        }
        List<Integer> concepts = new ArrayList<>();
        concepts.add(thingID);
        MetaPath mp = new MetaPath(concepts);
        List<Integer> path = new ArrayList<>();
        path.add(query);
        Set<List<Integer>> paths = new HashSet<>();
        paths.add(path);
        Map<Integer, Set<List<Integer>>> map = new HashMap<>();
        map.put(query, paths);

        Node start = new Node(mp, map);
        //start.setScore(calculateNodeScore(start, examples));
        start.setDistance(calculateDistance(start, examples));

        return start;
    }

    public double calculateDistance(Node node, List<Integer> examples){ // node中的点与example最小距离（0以外）的平均值
        if(node.map.size() > 5000)
            return 1000;

        double avg_min = 0;
        for(int example : examples){
            int min_distance = 100;
            for(int entity : node.map.keySet()){
                int dis = OracleFromFile.query(example, entity);
                //int dis = OracleDB.query(example, entity);
                if(dis < min_distance && dis != 0)
                    min_distance = dis;
            }

            avg_min += (double)min_distance/(double)examples.size();
        }

        return avg_min + node.mp.length();
    }

    public double calculateNodeScore(Node node, List<Integer> examples){
        double result = 0;
        double discriminability = 1.0/(double)node.map.size();
        /*double avgDistance = 0;
        if(node.map.size() < 1000){ // 小于1000就全部算一下距离，大于1000的话随机采1000个算距离
            for(int entity : node.map.keySet()){
                for(int example : examples){
                     avgDistance += (double) OracleM.query(entity, example);
                }
            }
            avgDistance /= (double)(node.map.size()*examples.size());
            avgDistance += (double)node.mp.length();
        }
        else{
            int i = 0;
            for(int entity : node.map.keySet()){
                if(1000 == i)
                    break;
                i ++;
                for(int example : examples){
                     avgDistance += (double)OracleM.query(entity, example);
                }
            }

            avgDistance /= (double)(1000*examples.size());
            avgDistance += (double)node.mp.length();
        }

        result = discriminability/(avgDistance + 1.0);*/



        result = discriminability;

        return result;
    }

    public static void yagoTest(){
        long start = System.currentTimeMillis();
        RelationIndex.initializeMap();
        GraphOntGetterM.initializeMap();
        TypedGraphModelM.initializeMap();
        //OracleM.initialize();
        OracleFromFile.initialize();
        long end = System.currentTimeMillis();
        System.out.println("time for initializing: " + (end - start));

        OracleFinder of = new OracleFinder();
        List<Integer> examples = new ArrayList<>();
        examples.add(4103860);
        examples.add(2761610);
        examples.add(899483);


        start = System.currentTimeMillis();
        Set<MetaPath> mps = of.findMetaPath_K(2125240, examples);
        end = System.currentTimeMillis();
        System.out.println("time for finding: " + (end - start));
    }

    public static void dbpediaTest(){
        long start = System.currentTimeMillis();
        RelationIndex.initializeMap();
        GraphOntGetterM.initializeMap();
        TypedGraphModelM.initializeMap();
        //OracleM.initialize();
       // OracleFromFile.initialize();
        long end = System.currentTimeMillis();
        System.out.println("time for initializing: " + (end - start));

        OracleFinder of = new OracleFinder();
        List<Integer> examples = new ArrayList<>();
        /*examples.add(2732463);
        examples.add(2071298);
        examples.add(2888488);*/

       // examples.add(716722);
       // examples.add(835865);
       // examples.add(703093);

        examples.add(2668);

        //int query = 1125862;
        //int query = 1608898;
        int query = 2668;
        start = System.currentTimeMillis();
        Set<MetaPath> mps = of.findMetaPath_K(query, examples);
        end = System.currentTimeMillis();
        System.out.println("time for finding: " + (end - start));
    }

    public static void main(String[] args){
        System.out.println("oracle finder test\n" + JdbcUtil.URL);
       // yagoTest();
        dbpediaTest();
    }

}
