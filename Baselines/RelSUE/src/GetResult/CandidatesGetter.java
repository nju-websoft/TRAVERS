package GetResult;

import GraphData.*;
import JDBCUtils.JdbcUtil;
import Path.MetaPath;
import Path.RelationPath;
import PathBasedSimilarity.HeteSim;
import PathBasedSimilarity.PCRW;
import PathBasedSimilarity.PathSim;
import PathBasedSimilarity.SimilarityMeasurements;

import java.util.*;
import java.util.Map.Entry;
import java.io.*;

public class CandidatesGetter {
    BasicRanker br = new BasicRanker();
    //SimilarityMeasurements sm = new HeteSim();
    SimilarityMeasurements sm = new PCRW();

    public Set<Integer> getCandidates(int query, List<Integer> examples, List<MetaPath> mps){
        Set<Integer> result = new HashSet<>();

        for(MetaPath mp : mps){
            //Set<Integer> accessedNodes = getAccessedNodes(query, mp);
            Set<Integer> accessedNodes = getAccessedNodes(query, mp);
            //result.addAll(br.getTopKNodes(query, mp, accessedNodes, 250, sm));
            if(accessedNodes.size() <= 200)
                result.addAll(accessedNodes);
            else{
                int i = 0;
                for(int no : accessedNodes){
                    if(200 == i)
                        break;
                    result.add(no);
                    i ++;
                }
            }

        }
        result.remove((Integer)query);
        for(Integer e : examples)
            result.remove(e);
        result.remove(query);
        return result;

    }

    public Set<Integer> getAccessedNodes(int query, MetaPath mp){
        List<Integer> relations = mp.getRelations();
        List<Integer> concepts = mp.getConcepts();
        Set<Integer> set = new HashSet<>();
        if(relations.size() < 0 || relations.size() != concepts.size() - 1){
            try {
                throw new Exception("error occurred during calculate the normalized hetesim!");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if(relations.size() == 0){
            int concept = concepts.get(0);
            if(GraphOntGetterM.classOfEntityByID(query).contains(concept))
                set.add(query);
            return set;
        }
        if(relations.size() == 1){
            int rel = relations.get(0);
            int con = concepts.get(1);
            List<Integer> typedObjects = typedObjects = TypedGraphModelM.getTypedObjects(query, rel, con);

            set.addAll(typedObjects);

            return set;
        }

        List<Integer> nrelations = new ArrayList<Integer>();
        for(int i = 1; i < relations.size(); i ++)
            nrelations.add(relations.get(i));
        List<Integer> nconcepts = new ArrayList<Integer>();
        for(int i = 1; i < concepts.size(); i ++)
            nconcepts.add(concepts.get(i));
        MetaPath nmp = new MetaPath(nconcepts, nrelations);
        int rel = relations.get(0);
        int con = concepts.get(1);
        List<Integer> objects = RelationIndex.getObjects(query, rel);
        List<Integer> typedObjects = TypedGraphModelM.getTypedObjects(query, rel, con);
        for(int object : typedObjects){
            Set<Integer> s = getAccessedNodes(object, nmp);
            set.addAll(s);
        }
        //System.out.println(map);
        return set;
    }

    public Set<Integer> getAccessedNodesByRP(int query, RelationPath rp){
        List<Integer> relations = rp.getRelations();
        Set<Integer> set = new HashSet<>();
        if(relations.size() < 0){
            try {
                throw new Exception("error occurred during calculate the normalized hetesim!");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if(relations.size() == 0){

            set.add(query);
            return set;
        }
        if(relations.size() == 1){
            int rel = relations.get(0);
            List<Integer> objects = RelationIndex.getObjects(query, rel);
            if(objects != null){
                for(int object : objects){
                    set.add(object);
                }
            }
            return set;
        }

        List<Integer> nrelations = new ArrayList<Integer>();
        for(int i = 1; i < relations.size(); i ++)
            nrelations.add(relations.get(i));

        RelationPath nrp = new RelationPath(nrelations);
        int rel = relations.get(0);
        List<Integer> objects = RelationIndex.getObjects(query, rel);

        if(objects != null){
            for(int object : objects){
                Set<Integer> s = getAccessedNodesByRP(object, nrp);
                set.addAll(s);
            }
        }
        //System.out.println(map);
        return set;
    }


    public Map<Integer, Double> getTopK(int query, MetaPath mp, List<Integer> candidates, int k, SimilarityMeasurements sm){
        Map<Integer, Double> map = new HashMap<Integer, Double>();
        for(int entity : candidates){
            double sim = sm.getSim(query, entity, mp);
            map.put(entity, sim);
        }
        map = sortByValue(map);
        Map<Integer, Double> result = new LinkedHashMap<Integer, Double>();
        int i = 0;
        for(Map.Entry<Integer, Double> entry : map.entrySet()){
            if(i < k){
                result.put(entry.getKey(), entry.getValue());
                i ++;
                System.out.println(entry.getValue());
            }
            else
                break;
        }

        return result;
    }
    private static <K, V> Map<K, V> sortByValue(Map<K, V> map) {
        List<Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Object>() {
            @SuppressWarnings("unchecked")
            public int compare(Object o1, Object o2) {
                return ((Comparable<V>) ((Map.Entry<K, V>) (o2)).getValue()).compareTo(((Map.Entry<K, V>) (o1)).getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<>();
        for (Iterator<Entry<K, V>> it = list.iterator(); it.hasNext();) {
            Map.Entry<K, V> entry = (Map.Entry<K, V>) it.next();
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
    private static double SumOfList(List<Double> list){
        double result = 0;
        for(double d : list)
            result += d;

        return result;
    }

    private static void output(Map<Integer, Double> map, String fileName){
        File file = new File(fileName);
        FileWriter fw = null;
        BufferedWriter writer = null;
        try {
            fw = new FileWriter(file);
            writer = new BufferedWriter(fw);

            for(Map.Entry<Integer, Double> entry : map.entrySet()){
                writer.write(entry.getKey() + " : " + entry.getValue());
                writer.newLine();
            }

            writer.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void KDDTest(){
        GraphModelM.initializeMap();
        //GraphClassInstancesM.initializeMap();
        System.out.println("begin to load ontgetter!");
        GraphOntGetterM.initializeMap();

        List<Integer> concepts = new ArrayList<>();
        List<Integer> relations = new ArrayList<>();

        concepts.add(3481481);
        concepts.add(3481923);
        concepts.add(3481481);
        concepts.add(3482002);
        concepts.add(3481481);

        relations.add(3480865);
        relations.add(3480928);
        relations.add(3480880);
        relations.add(-3480880);

        MetaPath mp = new MetaPath(concepts, relations);

        List<MetaPath> mps = new ArrayList<>();
        mps.add(mp);

        List<Integer> examples = new ArrayList<>();
        System.out.println("begin........");
        CandidatesGetter cg = new CandidatesGetter();
        cg.getCandidates(1562340, examples, mps);
        //System.out.println(cg.getAccessedNodes(1562340, mp).size());
    }

    /**
     * test0810
     * 测试
     */
    public static void test(){
        RelationIndex.initializeMap();
        GraphOntGetterM.initializeMap();
        TypedGraphModelM.initializeMap();

        List<Integer> concepts = new ArrayList<>();
        List<Integer> relations = new ArrayList<>();

        concepts.add(3481453);
        concepts.add(3481453);
        concepts.add(3481453);
        concepts.add(3481453);
        concepts.add(3481453);
        relations.add(3480809);
        relations.add(-3480809);
        relations.add(3480808);
        relations.add(-3480808);

        MetaPath mp = new MetaPath(concepts, relations);
        List<MetaPath> mps = new ArrayList<>();
        mps.add(mp);
        int query = 12041;
        List<Integer> examples = new ArrayList<>();
        CandidatesGetter cg = new CandidatesGetter();
        long start = System.currentTimeMillis();
        cg.getCandidates(query, examples, mps);
        long end = System.currentTimeMillis();
        System.out.println("time0: " + (end - start));
        start = System.currentTimeMillis();
        System.out.println(cg.getAccessedNodes(query, mp).size());
        end = System.currentTimeMillis();

        System.out.println("time1: " + (end - start));
    }

    public static void main(String[] args) {
        test();
    }
}
