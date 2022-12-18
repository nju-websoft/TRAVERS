package Path;

import java.util.*;

import GraphData.GraphOntGetterM;
//import GraphData.GraphOntologyM;
import GraphData.Ontology;
import GraphData.RelationIndex;
import GraphData.TypedGraphModelM;
import JDBCUtils.JdbcUtil;

public class RelSimFinder {
	static int diameter = 2;
	QBSchema qbs = new QBSchema();

	public static void SetDiameter(int d){
		diameter = d;
	}
	
	public Set<MetaPath> findMetaPath(int a, int b){
		Set<MetaPath> result = new HashSet<>();
		qbs.BuildQBSchema(a, b);
		List<Integer> typea = null;
		List<Integer> typeb = null;

		if(JdbcUtil.URL.contains("dbpedia")){
			typea = GraphOntGetterM.basicClassOfEntityByID(a);
			typeb = GraphOntGetterM.basicClassOfEntityByID(b);
			/*typea = new ArrayList<>();
			typea.addAll(GraphOntGetterM.classOfEntityByID(a));
			typeb = new ArrayList<>();
			typeb.addAll(GraphOntGetterM.classOfEntityByID(b));*/

		}
		else{
			typea = new ArrayList<>();
			typea.addAll(GraphOntGetterM.classOfEntityByID_yagoSimple(a));
			typeb = new ArrayList<>();
			typeb.addAll(GraphOntGetterM.classOfEntityByID_yagoSimple(b));
		}

		//System.out.println(qbs.map);
		
		for(int ta : typea){
			for(int tb : typeb){
				result.addAll(qbs.getMP(ta, tb, diameter));
			}
		}
		
		return result;
	}
	
	public Set<MetaPath> findMetaPath(int a, List<Integer> examples){
		Set<MetaPath> result = new HashSet<>();
		for(int e : examples){
			result.addAll(findMetaPath(a, e));
		}
		
		return result;
	}

	public static void main(String []args){
		//GraphModelM.initializeMap();
		//GraphOntologyM.initializeMap();
		Ontology.Initialize();
		GraphOntGetterM.initializeMap();
		RelationIndex.initializeMap();
		TypedGraphModelM.initializeMap();
		long start = System.currentTimeMillis();
		RelSimFinder rsf = new RelSimFinder();
		Set<MetaPath> set = rsf.findMetaPath(1562340, 1924527);
		//Set<MetaPath> set = rsf.findMetaPath(490495, 2700838);
		//Set<MetaPath> set = rsf.findMetaPath(1064567, 1236147);
		//Set<MetaPath> set = rsf.findMetaPath(4103860, 2125240);
		System.out.println("time: " + (System.currentTimeMillis() - start));
		for(MetaPath mp : set){
			//if(mp.length() == 3)
				System.out.println(mp);
		}
		System.out.println(set.size());
	}
}
