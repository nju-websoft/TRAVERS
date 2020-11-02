package Path;

import GraphData.*;
import NegativeSampler.BasicSampler;
import WeightLearner.LARS;
import org.ejml.simple.SimpleMatrix;

import java.util.*;

public class GreedyTree {
    static final double THRESHOLD = 0.2;
    static int K = 5;

    public static void setK(int num){
        K = num;
    }

    public static void setNexamples(){
        nexamples = new ArrayList<>();
    }

    public void reset(){
        OpenList = new ArrayList<>();
        paths = new HashSet<>();
    }

    public class Node{
        public MetaPath mp = null;
        public Map<Integer, Map<Integer, List<Set<Integer>>>> map0 = new LinkedHashMap<>();
        public Map<Integer, Map<Integer, Integer>> map1 = new LinkedHashMap<>();
        public List<Double> mk = new ArrayList<>(); // valid only when it is completed

        private double score = 0;

        public Node(MetaPath mp, Map<Integer, Map<Integer, List<Set<Integer>>>> map0, Map<Integer, Map<Integer, Integer>> map1){
            this.map0 = map0;
            this.map1 = map1;
            this.mp = mp;
        }

        public boolean isCompleted(int query){// if one seed accesses the query, then it's completed
            //if(mp.length() >= LEN)
            //	return true;
            if(null == map0 || map0.size() == 0)
                return false;
            for(int key : map0.keySet()){
                Map<Integer, List<Set<Integer>>> m = map0.get(key);
                if(m.containsKey(query))
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
        public void setMk(List<Double> mk){
            this.mk = mk;
        }
        public String toString(){
            return this.mp.toString() + this.score;
        }
    }

    public List<Node> OpenList = new ArrayList<>();
    public static List<Integer> nexamples = new ArrayList<>();
    public Set<MetaPath> paths = new HashSet<>();

    public Set<MetaPath> findMetaPath(int query, List<Integer> examples) {
        ////不要求所有样本都具有这些metapath，但是要求所有样本都具有这些metapath对应的relationpath,但是不要求所有样本
        //均能经由该relation path达到query， 只要有一个到达即可
        List<Double> residual = new ArrayList<>();
        for(int i = 0; i < examples.size(); i ++)
            residual.add(1.);
        for(int i = examples.size(); i < 2*examples.size(); i ++)
            residual.add(-1.);
        OpenList.addAll(initialize(examples, query, residual)); //负样本也在initialize过程中被指定

        System.out.println("Begin finding...");
        long start = System.currentTimeMillis();

        while(OpenList.size() > 0){
            //System.out.println("pre Openlist: " + OpenList);
            Node max = MaxS(OpenList, examples);
            //System.out.println("pre Max: " + max);
            Set<Integer> relations = nextRelations(max);
            OpenList.remove(max);

            for(int r : relations){
                OpenList.addAll(Extend(max, r, query, examples, residual));
            }
            if(null == max)
                break;

            if(max.isCompleted(query)){
                paths.add(max.mp);
                System.out.println(max.mp);
                System.out.println(max.getScore());
                System.out.println(max.mk);

                LARS.initialize(getSimpleVector(max.mk), examples.size(), examples.size());

                residual = LARS.getR();


                break;
            }
        }
        System.out.println("residual: " + residual);

        while(OpenList.size() > 0 && magnitude(residual) > 0.01){
            Node max = MaxS(OpenList, examples);
            //System.out.println("max: " + max);
            if(null == max)
                break;

            if(max.isCompleted(query)){
                paths.add(max.mp);
                System.out.println(max.mp);
                System.out.println(max.getScore());
                System.out.println("mk: " + max.mk);

                LARS.calculate(getSimpleVector(max.mk));

                residual = LARS.getR();
                System.out.println("residual: " + residual);
            }

            Set<Integer> relations = nextRelations(max);
            //System.out.println(relations);
            OpenList.remove(max);

            for(int r : relations){
                OpenList.addAll(Extend(max, r, query, examples, residual)); //慢的地方
            }
        }
        System.out.println("time: " + (System.currentTimeMillis()-start));
        System.out.println(LARS.getW());
        return paths;
    }

    public double magnitude(List<Double> r){
        double result = 0;
        for(double i : r){
            result += i*i;
        }
        result = Math.sqrt(result);
        return result;
    }

    public Set<Integer> nextRelations(Node node){
        Map<Integer, Map<Integer, List<Set<Integer>>>> map0 = node.map0;
        Set<Integer> relations = new HashSet<>();
        /*if(node.mp.getLastRelation() == 3480866){
            relations.add(-3480866);
            return relations;
        }
        if(node.mp.getLastRelation() == -3480866){
            relations.add(3480832);
            return relations;
        }*/
        for(int pe : map0.keySet()){
            Map<Integer, List<Set<Integer>>> m = map0.get(pe);
            //System.out.println("keyset: " + m.keySet());
            for(int i : m.keySet()){
                for(Set<Integer> l : m.get(i)){
                    relations.addAll(GraphModelM.getAllRelations(i, l));
                }

            }

        }

        /*relations.remove(-3480819);
        relations.remove(-3480809);
        relations.remove(-3480812);
        relations.remove(-3480813);
        relations.remove(-3480846);
        relations.remove(-3480811);
        relations.remove(-3480933);
        relations.remove(-3480810);
        relations.remove(-3480860);
        relations.remove(-3480850);
        relations.remove(-3480951);*/

        return relations;
    }

    public double getCosine(List<Double> mk, List<Double> residual){
        if(mk.size() != residual.size()){
            try {
                throw new Exception("mk doesn't match residual!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        double result = 0;
        for(int i = 0; i < mk.size(); i ++){
            result += mk.get(i)*residual.get(i);
        }

        result = result/(magnitude(mk)*magnitude(residual));

        return result;
    }

    public Node MaxS(List<Node> nodes, List<Integer> examples){
        if(nodes.size() > 0){
            Node max = nodes.get(0);
            for(int i = 1; i < nodes.size(); i ++){
                if(nodes.get(i).getScore() > max.getScore()){
                    max = nodes.get(i);
                }
                else if (nodes.get(i).getScore() == max.getScore()){
                    if(RelationsFreq.getFreq(nodes.get(i).mp.getLastRelation()) < RelationsFreq.getFreq(max.mp.getLastRelation()))
                        max = nodes.get(i);
                }

            }
            return max;
        }
        else return null;
    }

    public List<Node> Extend(Node parent, int relation, int query, List<Integer> examples, List<Double> residual) {
        List<Node> nodes = new ArrayList<>();
        //if(parent.mp.length() >= LEN)/////////////////////////////////////////////////////////////////////////
        //    return  nodes;

        //if(parent.getScore() < THRESHOLD/0.6)
        //    return  nodes;

        Map<Integer, Map<Integer, List<Set<Integer>>>> map0OfP = parent.map0;
        Map<Integer, Map<Integer, Integer>> map1OfP = parent.map1;

        Map<Integer, Map<Integer, List<Set<Integer>>>> allObjs0 = new LinkedHashMap<>();
        for(int i : map0OfP.keySet()){
            Map<Integer, List<Set<Integer>>> m = map0OfP.get(i);
            Map<Integer, List<Set<Integer>>> newm = new HashMap<>();
            for(int j : m.keySet()){
                for(Set<Integer> l : m.get(j)){
                    List<Integer> objs = GraphModelM.getObjects(j, relation, l);
                    for(int obj : objs){
                        if(!newm.containsKey(obj)){
                            Set<Integer> set = new HashSet<>();
                            set.addAll(l);
                            set.add(obj);
                            List<Set<Integer>> list = new LinkedList<>();
                            list.add(set);
                            newm.put(obj, list);
                        }
                        else{
                            List<Set<Integer>> list = newm.get(obj);
                            Set<Integer> set = new HashSet<>();
                            set.addAll(l);
                            set.add(obj);
                            list.add(set);
                            newm.put(obj, list);
                        }
                    }

                }

            }
            allObjs0.put(i, newm);
        }

        Map<Integer, Map<Integer, Integer>> allObjs1 = new LinkedHashMap<>();
        for(int i : map1OfP.keySet()){
            Map<Integer, Integer> m = map1OfP.get(i);
            Map<Integer, Integer> newm = new HashMap<>();
            for(int j : m.keySet()){
                List<Integer> objs = GraphModelM.getObjects(j, relation);
                if(objs != null){
                    for(int obj : objs){
                        if(newm.containsKey(obj)){
                            int count = newm.get(obj);
                            count += m.get(j);
                            newm.put(obj, count);
                        }
                        else{
                            newm.put(obj, m.get(j));
                        }
                    }
                }

            }
            allObjs1.put(i, newm);
        }

        Set<Integer> types = new HashSet<>(); // all candidate types
        for(int key : allObjs0.keySet()){
            for(int e : allObjs0.get(key).keySet())
                types.addAll(GraphOntGetterM.basicClassOfEntityByID(e));
        }

        Set<Integer> validTypes = Ontology.getallValidTypes(types);


        //获取所有可能的type后进行扩展
        for(int type : validTypes){

            List<Integer> concepts = new ArrayList<>();
            List<Integer> relations = new ArrayList<>();
            concepts.addAll(parent.mp.getConcepts());
            relations.addAll(parent.mp.getRelations());

            concepts.add(type);
            relations.add(relation);
            MetaPath mp = new MetaPath(concepts, relations);

            Map<Integer, Map<Integer, List<Set<Integer>>>> map0 = new LinkedHashMap<>();
            for(int pe : map0OfP.keySet()){
                Map<Integer, List<Set<Integer>>> objm = allObjs0.get(pe);
                Map<Integer, List<Set<Integer>>> newm = new HashMap<>();

                for(int obj : objm.keySet()){
                    if(GraphOntGetterM.classOfEntityByID(obj).contains(type)){
                        newm.put(obj, objm.get(obj));
                    }
                }

                map0.put(pe, newm);
            }

            Map<Integer, Map<Integer, Integer>> map1 = new LinkedHashMap<>();
            for(int pe : map1OfP.keySet()){
                Map<Integer, Integer> objm = allObjs1.get(pe);
                Map<Integer, Integer> newm = new HashMap<>();

                for(int obj : objm.keySet()){
                    if(GraphOntGetterM.classOfEntityByID(obj).contains(type)){
                        newm.put(obj, objm.get(obj));
                    }
                }

                map1.put(pe, newm);
            }

            Node node = new Node(mp, map0, map1);
            List<Double> mk = calculateMk(node, query);
            node.setMk(mk);
            if(!node.isCompleted(query)){
                double score = calculateNodeScore(node, query, examples, residual);
                node.setScore(score);
            }
            else{
                //System.out.println("mk: " + mk);
                //node.setMk(mk);
                double score = calculateCompletedNodeScore(mk, residual);
                node.setScore(score);
            }

            nodes.add(node);

        }

        return  nodes;
    }

    public SimpleMatrix getSimpleVector(List<Double> list){
        double[][] da = new double[list.size()][1];
        for(int i = 0; i < da.length; i ++)
            da[i][0] = list.get(i);

        return new SimpleMatrix(da);

    }
    public SimpleMatrix getSimpleMatrix(List<List<Double>> list){
        double[][] matrix = new double[list.size()][list.get(0).size()];
        for(int j = 0; j < list.get(0).size(); j ++){
            for(int i = 0; i < list.size(); i ++){
                matrix[i][j] = list.get(j).get(i);
            }
        }

        return new SimpleMatrix(matrix);
    }

    public List<Node> initialize(List<Integer> pexamples, int query, List<Double> residual){
        List<Node> nodes = new ArrayList<>();
        Set<Integer> types = new HashSet<>();
        for(int example : pexamples){
            System.out.println("example: " + example);
            types.addAll(GraphOntGetterM.basicClassOfEntityByID(example));
        }
        this.nexamples = getNexamples(types, pexamples, query);
        System.out.println("types:" + types);

        Set<Integer> validTypes = Ontology.getallValidTypes(types);
        System.out.println("validTypes:" + validTypes);

        for(int type : validTypes){
            List<Integer> concept = new ArrayList<>();
            concept.add(type);
            MetaPath mp = new MetaPath(concept);

            Map<Integer, Map<Integer, List<Set<Integer>>>> map0 = new HashMap<>();
            Map<Integer, Map<Integer, Integer>> map1 = new HashMap<>();

            for(int p : pexamples){
                Map<Integer, List<Set<Integer>>> mm = new HashMap<>();
                if(GraphOntGetterM.HasType(p, type)){
                    List<Set<Integer>> ll = new LinkedList<>();
                    Set<Integer> l = new HashSet<>();
                    l.add(p);
                    ll.add(l);
                    mm.put(p, ll);
                }
                map0.put(p, mm);
            }
            for(int n : nexamples){
                Map<Integer, Integer> mm = new HashMap<>();
                if(GraphOntGetterM.HasType(n, type)){
                    mm.put(n, 1);
                    System.out.println(LabelGetter.get(n));
                }
                map1.put(n, mm);
            }

            Node node = new Node(mp, map0, map1);
            node.setScore(calculateNodeScore(node, query, pexamples, residual));
            nodes.add(node);
        }
        return nodes;

    }

    public int pathNum(Map<Integer, List<Set<Integer>>> map){
        int result = 0;
        for(int key : map.keySet()){
            result += map.get(key).size();
        }

        return result;
    }

    public int npathNum(Map<Integer, Integer> map){
        int result = 0;
        for(int key : map.keySet()){
            result += map.get(key);
        }

        return result;
    }

    public double calculateNodeScore(Node node, int query, List<Integer> examples, List<Double> residual){
        double result = 0;
        double numerator = 0;
        double denominator = 0;
        int i = 0;
        for(int pe : node.map0.keySet()){
            int size = pathNum(node.map0.get(pe));
            //numerator += residual.get(i)*Math.sqrt(size);
            if(size > 0)
                numerator += residual.get(i);

            i ++;
        }
        double intermediate = 0;
        for(int pe : node.map0.keySet()){
            int size = pathNum(node.map0.get(pe));
            for(int key : node.map0.get(pe).keySet()){
                //intermediate += (Math.pow((double)node.map0.get(pe).get(key).size(), 2)/size);
                intermediate += (Math.pow((double)node.map0.get(pe).get(key).size(), 2)/(size*size));
            }
        }
        for(int ne : node.map1.keySet()){
            int size = npathNum(node.map1.get(ne));
            for(int key : node.map1.get(ne).keySet()){
                //intermediate += (Math.pow((double)node.map1.get(ne).get(key), 2)/size);
                intermediate += (Math.pow((double)node.map1.get(ne).get(key), 2)/(size*size));
            }
        }

        denominator = Math.sqrt(intermediate)*magnitude(residual);


        //result = numerator*Math.pow(0.6, node.mp.length())/denominator;
        result = numerator*Math.pow(0.6, node.mp.length());
        //System.out.println(result);

        return result;
    }

    public List<Double> calculateMk(Node node, int query){
        List<Double> mk = new ArrayList<>();
        for(int pe : node.map0.keySet()){
            if(node.map0.get(pe).containsKey(query)){
                int queryCount = node.map0.get(pe).get(query).size();
                int size = pathNum(node.map0.get(pe));

                //mk.add((double)queryCount/Math.sqrt(size));
                mk.add((double)queryCount/size);
            }
            else
                mk.add(0.);
        }
        for(int ne : node.map1.keySet()){
            if(node.map1.get(ne).containsKey(query)){
                int queryCount = node.map1.get(ne).get(query);
                int size = npathNum(node.map1.get(ne));

                //mk.add((double)queryCount/Math.sqrt(size));
                mk.add((double)queryCount/size);
            }
            else
                mk.add(0.);
        }

        return mk;
    }

    public double calculateCompletedNodeScore(List<Double> mk, List<Double> residual){

        return getCosine(mk, residual);
    }

    public List<Integer> getNexamples(Set<Integer> types, List<Integer> pexamples, int query){
        if(nexamples.size() > 0)
            return nexamples;
        else{
            List<Integer> btypes = GraphOntGetterM.basicClassOfEntityBySet(types);
            System.out.println("btypes:::" + btypes);
            BasicSampler bs = new BasicSampler();
            List<Integer> list = new ArrayList<>();
            list.addAll(bs.getSamples(btypes.get(0), query, pexamples, pexamples.size()));
            System.out.println("nexamples::::" + list);

            return list;
        }
    }

    public double weightedJaccard_internal(Map<Integer, List<Set<Integer>>> a, Map<Integer, List<Set<Integer>>> b){
        if(a.size() == 0 || b.size() == 0)
            return 0;

        double result;
        double top = 0;
        double bottom = 0;

        Set<Integer> keySet = new HashSet<>();
        keySet.addAll(a.keySet());
        keySet.addAll(b.keySet());
        for(int i : keySet){
            if(a.containsKey(i) && b.containsKey(i)){
                if(a.get(i).size() >= b.get(i).size()){
                    top += (double)b.get(i).size();
                    bottom += (double)a.get(i).size();
                }
            }
            else if (b.containsKey(i)){ // only b
                top += (double)0;
                bottom += (double)b.get(i).size();
            }
            else{ // only a
                top += (double)0;
                bottom += (double)a.get(i).size();
            }
        }

        result = top/bottom;

        /*if(a.containsKey(2821978)){
            System.out.println("map a:" + a);
            System.out.println("map b:" + b);
            System.out.println("jac: " + result);
        }*/
        return result;
    }
    public double weightedJaccard_external(Map<Integer, List<Set<Integer>>> a, Map<Integer, Integer> b){
        if(a.size() == 0 || b.size() == 0)
            return 0;

        double result;
        double top = 0;
        double bottom = 0;

        Set<Integer> keySet = new HashSet<>();
        keySet.addAll(a.keySet());
        keySet.addAll(b.keySet());
        for(int i : keySet){
            if(a.containsKey(i) && b.containsKey(i)){
                if(a.get(i).size() >= b.get(i)){
                    top += (double)b.get(i);
                    bottom += (double)a.get(i).size();
                }
            }
            else if (b.containsKey(i)){ // only b
                top += (double)0;
                bottom += (double)b.get(i);
            }
            else{ // only a
                top += (double)0;
                bottom += (double)a.get(i).size();
            }
        }

        result = top/bottom;
        return result;
    }

    public double weightedJaccard(Map<Integer, Integer> a, Map<Integer, Integer> b){
        //System.out.println("a: " + a);
        //System.out.println("b: " + b);
        if(a.size() == 0 || b.size() == 0)
            return 0;

        double result;
        double top = 0;
        double bottom = 0;

        Set<Integer> keySet = new HashSet<>();
        keySet.addAll(a.keySet());
        keySet.addAll(b.keySet());
        for(int i : keySet){
            if(a.containsKey(i) && b.containsKey(i)){
                if(a.get(i) >= b.get(i)){
                    top += (double)b.get(i);
                    bottom += (double)a.get(i);
                }
            }
            else if (b.containsKey(i)){ // only b
                top += (double)0;
                bottom += (double)b.get(i);
            }
            else{ // only a
                top += (double)0;
                bottom += (double)a.get(i);
            }
        }

        result = top/bottom;
        return result;
    }
    public static void main(String[] args) {
        GraphModelM.initializeMap();
        //GraphOntologyM.initializeMap();
        Ontology.Initialize();
        RelationsFreq.initialize("dbpedia.freq");
        GraphOntGetterM.initializeMap();
        //GraphOntGetterM.initializeMap(3465220);
        //int query = 219426;
        int query = 1562340;
        List<Integer> examples = new ArrayList<>();
        //examples.add(656521);
        //examples.add(629711);
        //examples.add(2580708); // Jae Crowder
        //examples.add(450031);
        examples.add(1924527); // KI
        examples.add(1045885); // Waiters
        GreedyTree gt = new GreedyTree();
        System.out.println(gt.findMetaPath(query, examples));
    }
}
