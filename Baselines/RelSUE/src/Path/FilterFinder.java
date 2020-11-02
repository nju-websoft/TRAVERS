package Path;

import GraphData.GraphModelM;
import GraphData.GraphOntGetterM;
import GraphData.RelationIndex;
import GraphData.TypedGraphModelM;
import JDBCUtils.JdbcUtil;
import Sig.SigCalculator;

import java.io.FileWriter;
import java.util.*;

/**
 * Can be viewed as a extension of the BFSFinder or a version of GreedyFinder by set all heuristics equal to 0 (@Deprecated for this sentence.)
 *
 * Firstly, get all meta-paths connecting query and examples by bi-bfs finder, and then select them in terms of Sig.
 * (This can also be very time-consuming, so instead, we first rank the relation paths, and then select meta-paths according to their corresponding relation path)
 */

public class FilterFinder implements PathFinder {
    int length = 3; // bifinder会根据该length设置diameter
    static int K = 20; // number of paths to be return
    BiBFSFinder bifinder = new BiBFSFinder();

    public FilterFinder(){
        if(JdbcUtil.URL.contains("dbpedia"))
            length = 3;
        else
            length = 2;
    }

    public static  void setK(int numOfP){
        K = numOfP;
    }

    @Override
    public Set<RelationPath> findRelationPath(int query, List<Integer> examples) {
        bifinder.setDiameter(this.length);
        System.out.println("Start to find relation paths...");
        Set<RelationPath> rps = bifinder.findRelationPath(query, examples);
        System.out.println("#rps size: " + rps.size());
        List<RSPair> pairs = new ArrayList<>();
        //long start = System.currentTimeMillis();
        for(RelationPath rp : rps){
            double sig = SigCalculator.getSig(rp, query, examples);
            pairs.add(new RSPair(rp, sig));
        }
        //long end = System.currentTimeMillis();
       // System.out.println("rp size: " + result.size());
       // System.out.println("time for calculating significance for all rp: " + (end - start));
        Collections.sort(pairs, new Comparator<RSPair>() {
            @Override
            public int compare(RSPair o1, RSPair o2) {
                if(o2.significance > o1.significance)
                    return 1;
                else if(o2.significance == o1.significance)
                    return 0;
                else return -1;
            }
        });

        //System.out.println(pairs);
        Set<RelationPath> result = new HashSet<>();
        /**
         *
         *
         * 这部分待定，其实应该不会用到filterFinder来选relation path，直接全用就得了
         *
         */


        return result;
    }

    @Override
    public Set<MetaPath> findMetaPath(int query, List<Integer> examples) {
        bifinder.setDiameter(this.length);
        //System.out.println("Start to find meta paths...");
        //long start = System.currentTimeMillis();
        Set<MetaPath> mps = bifinder.findMetaPath(query, examples);
        if(mps.size() == 0){
            return new HashSet<>();
        }
        //long end = System.currentTimeMillis();
        System.out.println("mp size: " + mps.size());
        //System.out.println("time for finding all meta paths: " + (end - start));
        List<MSPair> pairs = new ArrayList<>();
        //start = System.currentTimeMillis();
        for(MetaPath mp : mps){
            //System.out.println(mp);
            double sig = SigCalculator.getSig(mp, query, examples);
           pairs.add(new MSPair(mp, sig));
        }
        //end = System.currentTimeMillis();
        //System.out.println("time for calculating significance for all mp: " + (end - start));

        Collections.sort(pairs, new Comparator<MSPair>() {
            @Override
            public int compare(MSPair o1, MSPair o2) {
                if(o2.significance > o1.significance)
                    return 1;
                else if(o2.significance == o1.significance)
                    return 0;
                else return -1;
            }
        });

        //start = System.currentTimeMillis();
        Set<MetaPath> result = new HashSet<>();
        result.add(pairs.get(0).mp);
        for(int i = 1; i < pairs.size(); i ++){
            if(result.size() == K)
                break;
            boolean flag = true;
            MetaPath nmp = pairs.get(i).mp;
            for(MetaPath mp : result){
                if(hasSamePaths(query, mp, nmp)){
                    flag = false;
                    break;
                }
            }

            if(flag){
                result.add(nmp);
                //System.out.println("sig: " + pairs.get(i).significance);
                System.out.println("mp: "  + nmp + ", sig: " + pairs.get(i).significance);
            }
        }
        //end = System.currentTimeMillis();

        //System.out.println("time for selection: " + (end - start));

        //System.out.println("final result: " + result);

        /**
         * output the corresponding relation path for test
         */
        /*Set<RelationPath> rps = new HashSet<>();
        for(MetaPath mp : result){
            rps.add(new RelationPath(mp.getRelations()));
        }*/
        //System.out.println("corresponding relation paths: " + rps);

        return result;
    }

    /**
     * determine that start from entity a, whether following mp1 and mp2 will meet the same path instances
     * @param a
     * @param mp1
     * @param mp2
     * @return
     */
    public boolean hasSamePaths(int a, MetaPath mp1, MetaPath mp2){
        if(!mp1.getRelations().equals(mp2.getRelations()))
            return false;
        else{ // mp1 and mp2 share the same relation path
            return hasSamePaths(a, mp1, mp2, mp1.length());
        }
    }

    /**
     * recursive method
     * @param a
     * @param mp1
     * @param mp2 share the same relation path with mp1
     * @param left
     * @return
     */
    private boolean hasSamePaths(int a, MetaPath mp1, MetaPath mp2, int left){
        if(0 == left)
            return true;
        else{
            int relation = mp1.getRelations().get(mp1.length() - left);
            int concept1 = mp1.getConcepts().get(mp1.length() - left + 1);
            int concept2 = mp2.getConcepts().get(mp2.length() - left + 1);

            List<Integer> nodes1 = TypedGraphModelM.getTypedObjects(a, relation, concept1);
            Collections.sort(nodes1);
            List<Integer> nodes2 = TypedGraphModelM.getTypedObjects(a, relation, concept2);
            Collections.sort(nodes2);

            if(!nodes1.equals(nodes2))
                return false;
            else{ // nodes1 = nodes2
                for(int i = 0; i < nodes1.size(); i ++){
                    if(!hasSamePaths(nodes1.get(i), mp1, mp2, left - 1))
                        return false;
                }

                return true;
            }
        }
    }

    public void setLength(int length){
        this.length = length;
    }

    /**
     * Pair of meta-path and significance, which can be used to rank meta-path in terms of significance
     */
    class MSPair{
        MetaPath mp;
        double significance;

        public MSPair(MetaPath mp, double significance){
            this.mp = mp;
            this.significance = significance;
        }

    }

    class RSPair{
        RelationPath rp;
        double significance;

        public RSPair(RelationPath rp, double significance){
            this.rp = rp;
            this.significance = significance;
        }

        public String toString(){
            return rp.toString() + " : " + significance;
        }
    }

    public static void yagoTest(){
        RelationIndex.initializeMap();
        GraphOntGetterM.initializeMap();
        TypedGraphModelM.initializeMap();

       // GraphModelM.initializeMap();

        FilterFinder ff = new FilterFinder();
        List<Integer> examples = new ArrayList<>();
        examples.add(3617892);
        examples.add(1701576);
        examples.add(408457);


        long start = System.currentTimeMillis();
        //Set<RelationPath> rps = ff.findRelationPath(2125240, examples);
        Set<MetaPath> mps = ff.findMetaPath(4180137, examples);
        long end = System.currentTimeMillis();
        System.out.println("time: " + (end - start));
    }

    public static void dbpediaTest(){
        RelationIndex.initializeMap();
        GraphOntGetterM.initializeMap();
        TypedGraphModelM.initializeMap();

        FilterFinder ff = new FilterFinder();
        List<Integer> examples = new ArrayList<>();
        //examples.add(2668);
        //examples.add(2857301);
        examples.add(1277626);
        examples.add(2507712);

        //2593030	1748585	725709
        /*examples.add(2593030);
        examples.add(1748585);
        examples.add(725709);*/


        long start = System.currentTimeMillis();
        Set<MetaPath> mps = ff.findMetaPath(1344463, examples);
        //Set<MetaPath> mps = ff.findMetaPath(630840, examples);
        long end = System.currentTimeMillis();
        System.out.println("time: " + (end - start));
    }

    public static void main(String[] args){
        System.out.println("begin...");
        dbpediaTest();
        //yagoTest();
    }
}
