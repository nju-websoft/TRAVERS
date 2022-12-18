package Path;

import java.util.*;

import GraphData.GraphModelM;
import GraphData.GraphOntGetterM;
//import GraphData.GraphOntologyM;
import GraphData.LabelGetter;
import GraphData.Ontology;
import NegativeSampler.BasicSampler;

/**
 * 用搜索树的结构来遍历，检测性能问题
 */

public class GreedyTester {
    static final int LEN = 3;
    static final double THRESHOLD = 0;
    public class Node{
        public MetaPath mp = null;
        Map<Integer, Set<Integer>> map = new HashMap<>();
        Map<Integer, Set<Integer>> ids = new HashMap<>();

        public Node(MetaPath mp, Map<Integer, Set<Integer>> map, Map<Integer, Set<Integer>> ids){
            this.mp = mp;
            this.map = map;
            this.ids = ids;
        }

        private double score = 0;
        public double H = 0;
        public Node parent;


        public boolean isCompleted(int query){// if one seed accesses the query, then it's completed
            for(int pe : map.keySet()){
                if(map.get(pe).contains(query))
                    return true;
            }
            return false;

        }
        public double getScore(){
            return  this.score;
        }
        public void setScore(double score){
            this.score = score;
        }
        public String toString(){
            return this.mp.toString() + this.score;
        }
    }

    public List<Node> OpenList = new ArrayList<>();
    //public List<Node> CloseList = new ArrayList<>();
    public List<Integer> nexamples;

    /**
     *
     * @param query
     * @param examples
     * @param K the maximum number of meta paths to return
     * @return k meta paths (k <= K)
     */
    public Set<MetaPath> findMetaPath(int query, List<Integer> examples, int K) {
        ////不要求所有样本都具有这些metapath，但是要求所有样本都具有这些metapath对应的relationpath,但是不要求所有样本
        //均能经由该relation path达到query， 只要有一个到达即可
        Set<MetaPath> paths = new HashSet<>();
        OpenList.addAll(initialize(examples, query)); //负样本也在initialize过程中被指定
        System.out.println(OpenList);
        System.out.println("Begin finding...");
        while(paths.size() < K){
            if(OpenList.size() > 0){

                Node max = MaxS(OpenList);
                while(!max.isCompleted(query)){
                    Set<Integer> relations = nextRelations(max);
                    //System.out.println(relations);
                    OpenList.remove(max);

                    for(int r : relations){
                        //System.out.println("max: " + max.mp);
                        //System.out.println("r: " + r);
                        OpenList.addAll(Extend(max, r, query, examples)); //慢的地方
                    }

                    System.out.println(OpenList);
                    max = MaxS(OpenList);
                    System.out.println(max);
                }
                paths.add(max.mp);
                System.out.println(max.mp);
                System.out.println(max.getScore());
                if(paths.size() < K){
                    Set<Integer> relations = nextRelations(max);
                    //System.out.println(relations);
                    OpenList.remove(max);
                    for(int r : relations){
                        //System.out.println("max: " + max.mp);
                        //System.out.println("r: " + r);
                        OpenList.addAll(Extend(max, r, query, examples));
                    }
                    System.out.println(OpenList);
                }
            }
            else break;
        }
        return paths;
    }

    public Set<Integer> nextRelations(Node node){ // 需要是所有seed都能扩展的relation
        Map<Integer, Set<Integer>> map = node.map;
        List<Set<Integer>> list = new ArrayList<>();
        for(int pe : map.keySet()){
            Set<Integer> s = map.get(pe);
            Set<Integer> relations = new HashSet<>();
            for(int i : s){
                relations.addAll(GraphModelM.getAllRelations(i));
            }
            list.add(relations);
        }
        Set<Integer> result = list.get(0);
        for(int i = 1; i < list.size(); i ++){
            result.retainAll(list.get(i));
        }

        return result;
    }

    public Node MaxS(List<Node> nodes){
        Node max = nodes.get(0);
        for(int i = 0; i < nodes.size(); i ++){
            if(nodes.get(i).getScore() >= max.getScore()){
                max = nodes.get(i);
            }
        }
        return max;
    }

    public List<Node> Extend(Node parent, int relation, int query, List<Integer> examples) {
        System.out.println("mp tp be extended:" + parent.mp);
        List<Node> nodes = new ArrayList<>();
        Map<Integer, Set<Integer>> mapOfP = parent.map;
        Map<Integer, Set<Integer>> idsOfP = parent.ids;


        //以下这段代码都是为了获取下一步有可能到达的基础类，效率比较低下，后面可以考虑直接从schema获取
        Set<Integer> allEntities = new HashSet<>(); // 所有可以由map0原有基础上再由relation到达的实体
        for(int i : mapOfP.keySet()){
            Set<Integer> set = mapOfP.get(i);
            for(int j : set){
                List<Integer> objs = GraphModelM.getObjects(j, relation, idsOfP.get(i));
                if(objs != null)
                    allEntities.addAll(objs);
            }
        }
        Set<Integer> types = new HashSet<>(); // all candidate types
        for(int e : allEntities){
            types.addAll(GraphOntGetterM.basicClassOfEntityByID(e));
        }
        /*if(types.size() > 10){
            System.out.println("relation:" + LabelGetter.get(Math.abs(relation)));
            System.out.println("relation id:" + relation);
            System.out.println(types);
        }*/
        //获取所有可能的type后进行扩展
        for(int type : types){

            List<Integer> concepts = new ArrayList<>();
            List<Integer> relations = new ArrayList<>();
            concepts.addAll(parent.mp.getConcepts());
            relations.addAll(parent.mp.getRelations());

            concepts.add(type);
            relations.add(relation);
            MetaPath mp = new MetaPath(concepts, relations);

            Map<Integer, Set<Integer>> map = new HashMap<>();
            Map<Integer, Set<Integer>> ids = new HashMap<>();
            for(int pe : mapOfP.keySet()){
                Set<Integer> set = mapOfP.get(pe);
                Set<Integer> newset = new HashSet<>();
                Set<Integer> peids = idsOfP.get(pe);
                Set<Integer> newpeids = new HashSet<>();
                newpeids.addAll(peids);
                //System.out.println(m.keySet().size());
                for(int e : set){
                    long start = System.currentTimeMillis();
                    List<Integer> objs = GraphModelM.getObjects(e, relation, peids);
                    if(objs != null){
                        for(int obj : objs){
                            if(GraphOntGetterM.classOfEntityByID(obj).contains(type)){
                                if(!newpeids.contains(obj)){
                                    newpeids.add(obj);
                                }
                                if(!newset.contains(obj)){
                                    newset.add(obj);
                                }
                            }
                        }
                    }
                    long end = System.currentTimeMillis();
                    // System.out.println("time: " + (end-start));
                    // System.out.println("objs size:" + objs.size());
                }

                map.put(pe, newset);
                ids.put(pe, newpeids);
            }

            Node node = new Node(mp, map, ids); /////////////////////////////////////////////
            double score = calculateNodeScore(node, query, examples);
            node.setScore(score);
            if(score >= THRESHOLD && node.mp.length() <= LEN)
                nodes.add(node);

        }
        System.out.println("extending done！");
        return  nodes;
    }

    public List<Node> initialize(List<Integer> pexamples, int query){
        List<Node> nodes = new ArrayList<>();
        Set<Integer> types = new HashSet<>();
        for(int example : pexamples){
            types.addAll(GraphOntGetterM.basicClassOfEntityByID(example));
        }
        //this.nexamples = getNexamples(types, pexamples, query);

        for(int type : types){
            List<Integer> concept = new ArrayList<>();
            concept.add(type);
            MetaPath mp = new MetaPath(concept);

            Map<Integer, Set<Integer>> map = new HashMap<>();
            Map<Integer, Set<Integer>> ids = new HashMap<>();
            for(int p : pexamples){
                Set<Integer> set = new HashSet<>();
                if(GraphOntGetterM.HasType(p, type)){
                    set.add(p);
                }
                map.put(p, set);

                Set<Integer> id = new HashSet<>();
                id.add(p);
                ids.put(p, id);
            }


            Node node = new Node(mp, map, ids);
            node.setScore(calculateNodeScore(node, query, pexamples));
            nodes.add(node);
        }
        return nodes;

    }

    public double calculateNodeScore(Node node, int query, List<Integer> examples){
        return 0;
    }

    public List<Integer> getNexamples(Set<Integer> types, List<Integer> pexamples, int query){
        BasicSampler bs = new BasicSampler();
        List<Integer> list = new ArrayList<>();
        for(int i : types){
            list.addAll(bs.getSamples(i,query, pexamples, 5));
        }
        return list;
    }


    public static void main(String[] args) {
        GraphModelM.initializeMap();
        //GraphOntologyM.initializeMap();
        Ontology.Initialize();
        GraphOntGetterM.initializeMap(3480806);
        //int query = 219426;
        //int query = 1562340;
        int query = 2580708;
        List<Integer> examples = new ArrayList<>();
        //examples.add(656521);
        //examples.add(629711);
        examples.add(1562340);
        //examples.add(2580708); // Jae Crowder
        //examples.add(2161684); // DW3
        //examples.add(1924527); // KI
        //examples.add(1045885); // Waiters
        GreedyTester gf = new GreedyTester();
        System.out.println(gf.findMetaPath(query, examples, 3));
    }


}
