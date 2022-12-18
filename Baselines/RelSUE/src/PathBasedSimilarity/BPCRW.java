package PathBasedSimilarity;

import GraphData.GraphModelM;
import GraphData.GraphOntGetterM;
import Path.MetaPath;
import Path.RelationPath;

import java.util.*;

public class BPCRW implements SimilarityMeasurements {
    @Override
    public double getSim(int a, int b, MetaPath mp){
        if(!GraphOntGetterM.classOfEntityByID(a).contains(mp.getConcepts().get(0)) || !GraphOntGetterM.classOfEntityByID(b).contains(mp.getConcepts().get(mp.length()))){
            return 0;
        }

        //if(GraphOntGetterM.classOfEntityByID(a).contains(mp.getConcepts().get(0)) && GraphOntGetterM.classOfEntityByID(b).contains(mp.getConcepts().get(mp.length())))
        if(GraphOntGetterM.classOfEntityByID(a).contains(mp.getConcepts().get(0)))
            return getBPCRW(a, b, mp, mp.length());
        else{
            try {
                throw new Exception("meta-path is illegal!");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return 0;
        }
    }
    @Override
    public double getSim(int a, int b, RelationPath rp) {
        return getBPCRW(a, b, rp, rp.length());
    }

    private double getBPCRW(int a, int b, MetaPath mp, int left){
        int c = mp.getConcepts().get(mp.getConcepts().size() - 1);
        if(!GraphOntGetterM.classOfEntityByID(b).contains(c))
            return 0;
        double pcrw = 0;
        if(left < 0){
            try {
                throw new Exception("bpcrw error!");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return 0;
        }
        if(0 == left){
            if(a == b)
                return 1;
            else
                return 0;
        }


        List<Integer> relations = mp.getRelations();
        List<Integer> concepts = mp.getConcepts();
        int relation = relations.get(relations.size() - left);
        int concept = concepts.get(relations.size() - left + 1);

        Map<Integer, List<Integer>> rmap = GraphModelM.map.get(a);
        List<Integer> nodes = new ArrayList<Integer>();
        if(rmap.get(relation) == null)
            return 0;
        for(int node : rmap.get(relation)){
            if(GraphOntGetterM.classOfEntityByID(node).contains(concept))
                nodes.add(node);
        }
        int nsize = nodes.size();
        for(int node : nodes){
            pcrw += getBPCRW(node, b, mp, left - 1) / Math.sqrt(nsize);
        }

        return pcrw;

    }

    private double getBPCRW(int a, int b, RelationPath rp, int left){
        double pcrw = 0;
        if(left < 0){
            try {
                throw new Exception("pcrw error!");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return 0;
        }
        if(0 == left){
            if(a == b)
                return 1;
            else
                return 0;
        }

        Map<Integer, List<Integer>> rmap = GraphModelM.map.get(a);
        List<Integer> relations = rp.getRelations();
        int relation = relations.get(relations.size() - left);
        List<Integer> nodes = rmap.get(relation);
        if(nodes == null)
            return 0;
        int nsize = nodes.size();
        for(int node : nodes){
            pcrw += getBPCRW(node, b, rp, left - 1) / Math.sqrt(nsize);
        }
        return pcrw;
    }

    public static void main(String[] args) {
        GraphModelM.initializeMap();
        System.out.println("start to calculate");
        PCRW pcrw = new PCRW();
        List<Integer> relations = new ArrayList<Integer>();
        relations.add(3480880);
        relations.add(-3480880);
        //relations.add(3480880);
        //relations.add(-3480880);

        RelationPath rp = new RelationPath(relations);

        System.out.println(pcrw.getSim(1562340, 2580708, rp));
    }

}
