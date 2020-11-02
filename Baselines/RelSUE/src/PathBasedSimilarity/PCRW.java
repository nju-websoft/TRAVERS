package PathBasedSimilarity;

import GraphData.*;
import Path.MetaPath;
import Path.RelationPath;
import org.jgrapht.Graph;

import java.util.*;

public class PCRW implements SimilarityMeasurements {
	public static String ID = "PCRW";
	@Override
	public double getSim(int a, int b, MetaPath mp){ // start from a to b
		if(!GraphOntGetterM.classOfEntityByID(a).contains(mp.getConcepts().get(0)) || !GraphOntGetterM.classOfEntityByID(b).contains(mp.getConcepts().get(mp.length()))){
			return 0;
		}

		//if(GraphOntGetterM.classOfEntityByID(a).contains(mp.getConcepts().get(0)) && GraphOntGetterM.classOfEntityByID(b).contains(mp.getConcepts().get(mp.length())))
		if(GraphOntGetterM.classOfEntityByID(a).contains(mp.getConcepts().get(0))){
			return getPcrw(a, b, mp, mp.length());
			//return  getPcrw_alpha(a, b, mp, mp.length()); // ###################################################################
		}

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
		return getPcrw(a, b, rp, rp.length());
	}

	// based on TypedGraphModel
	private double getPcrw(int a, int b, MetaPath mp, int left){
        int c = mp.getConcepts().get(mp.getConcepts().size() - 1);
        if(!GraphOntGetterM.classOfEntityByID(b).contains(c))
            return 0;
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


        List<Integer> relations = mp.getRelations();
        List<Integer> concepts = mp.getConcepts();
        int relation = relations.get(relations.size() - left);
        int concept = concepts.get(relations.size() - left + 1);

        List<Integer> nodes = TypedGraphModelM.getTypedObjects(a, relation, concept); // ###############################
		//List<Integer> nodes = GraphModelM.getTypedObjects(a, relation, concept);
        if(null == nodes || nodes.size() == 0)
        	return 0;
        else{
			int nsize = nodes.size();
			for(int node : nodes){
				pcrw += getPcrw(node, b, mp, left - 1) / (double)nsize;
			}

			return pcrw;
		}
    }

	private double getPcrw(int a, int b, RelationPath rp, int left){
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
		
		Map<Integer, List<Integer>> rmap = RelationIndex.map[a];
		List<Integer> relations = rp.getRelations();
		int relation = relations.get(relations.size() - left);
		List<Integer> nodes = rmap.get(relation);
		if(nodes == null)
			return 0;
		int nsize = nodes.size();
		for(int node : nodes){
			pcrw += getPcrw(node, b, rp, left - 1) / (double)nsize;
		}
		return pcrw;
	}

	private double getPcrw_alpha(int a, int b, MetaPath mp, int left){ // 这是针对graphmodel的版本，使用typed model会更快
		int c = mp.getConcepts().get(mp.getConcepts().size() - 1);
		if(!GraphOntGetterM.classOfEntityByID(b).contains(c))
			return 0;
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


		List<Integer> relations = mp.getRelations();
		List<Integer> concepts = mp.getConcepts();
		int relation = relations.get(relations.size() - left);
		int concept = concepts.get(relations.size() - left + 1);

		List<Integer> nodes = RelationIndex.getTypedObjects(a, relation, concept);
		//List<Integer> nodes = GraphModelM.getTypedObjects(a, relation, concept); // ##########################################
		if(null == nodes || nodes.size() == 0)
			return 0;
		else{
			int nsize = nodes.size();
			for(int node : nodes){
				pcrw += getPcrw_alpha(node, b, mp, left - 1) / (double)nsize;
			}

			return pcrw;
		}

	}
	
	public static void main(String[] args) {
		RelationIndex.initializeMap();
		//Ontology.Initialize();
		GraphOntGetterM.initializeMap();
		TypedGraphModelM.initializeMap();
		System.out.println("start to calculate");
		PCRW pcrw = new PCRW();
		List<Integer> relations = new ArrayList<Integer>();
		List<Integer> concepts = new ArrayList<>();
		//relations.add(3480880);
		//relations.add(-3480880);
		//relations.add(3480880);
		//relations.add(-3480880);

		// yago
		relations.add(4295829);
		relations.add(-4295841);
		relations.add(4295828);
		relations.add(-4295829);

		concepts.add(4832388);
		concepts.add(4832388);
		concepts.add(4832388);
		concepts.add(4832388);
		concepts.add(4832388);

		// dbpedia




		RelationPath rp = new RelationPath(relations);
		MetaPath mp = new MetaPath(concepts, relations);

		//System.out.println(pcrw.getSim(1562340, 2580708, rp));
		long start = System.currentTimeMillis();
		System.out.println(pcrw.getSim(2125240, 4103860, rp));
		long end = System.currentTimeMillis();
		System.out.println("time: " + (end - start));

		start = System.currentTimeMillis();
		System.out.println(pcrw.getPcrw(2125240, 4103860, mp, mp.length()));
		end = System.currentTimeMillis();
		System.out.println("time: " + (end - start));

		start = System.currentTimeMillis();
		System.out.println(pcrw.getPcrw_alpha(2125240, 4103860, mp, mp.length()));
		end = System.currentTimeMillis();
		System.out.println("time: " + (end - start));
	}

}
