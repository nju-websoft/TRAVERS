package PathBasedSimilarity;

import java.util.ArrayList;
import java.util.List;

import GraphData.GraphModelM;
import Path.MetaPath;
import Path.RelationPath;

/**
 * PathSim can only be applied under symmetric path
 * @author anonymous
 *
 */
public class PathSim implements SimilarityMeasurements {
	public static String ID = "PathSim";
	@Override
	public double getSim(int a, int b, MetaPath mp){
		SimilarityMeasurements pc = new PathCount();
		double s0 = pc.getSim(a, b, mp);
		double s1 = pc.getSim(a, a, mp);
		double s2 = pc.getSim(b, b, mp);
		if((s1 + s2) == 0)
			return 0;
		else return (2*s0 / (s1 + s2));
		
	}
	@Override
	public double getSim(int a, int b, RelationPath rp) {
		SimilarityMeasurements pc = new PathCount();
		double s0 = pc.getSim(a, b, rp);
		double s1 = pc.getSim(a, a, rp);
		double s2 = pc.getSim(b, b, rp);
		//System.out.println("aha");
		if((s1 + s2) == 0)
			return 0;
		else return (2*s0 / (s1 + s2));
	}
	
	public static void main(String[] args) {
		GraphModelM.initializeMap();
		System.out.println("start to calculate");
		PathSim ps = new PathSim();
		List<Integer> relations = new ArrayList<Integer>();
		relations.add(3480880);
		relations.add(-3480880);
		relations.add(3480880);
		relations.add(-3480880);

		RelationPath rp = new RelationPath(relations);

		System.out.println(ps.getSim(1562340, 2580709, rp));
		System.out.println(ps.getSim(1562340, 2580708, rp));
	}
}
