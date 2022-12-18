package Path;

import java.util.*;

import GraphData.GraphOntGetterM;
import GraphData.RelationIndex;
import JDBCUtils.JdbcUtil;

public class QBSchema {
	int D = 4;
	
	public Map<Integer, Map<Integer, Set<Integer>>> map = new HashMap<>();
	
	/*public Map<MetaPath, Set<Integer>> getHalfIndexMap(int typea, int len){
		//System.out.println("type: " + typea);
		Map<MetaPath, Set<Integer>> result = new HashMap<MetaPath, Set<Integer>>();
		Map<MetaPath, Set<Integer>> paths_l = new HashMap<MetaPath, Set<Integer>>();
		List<Integer> concepts = new ArrayList<>();
		concepts.add(typea);
		List<Integer> relations = new ArrayList<>();
		MetaPath mp0 = new MetaPath(concepts, relations);
		paths_l.add(mp0);
		result.add(mp0);
		for(int i = 0; i < len; i ++){
			Set<MetaPath> paths_l0 = new HashSet<>();
			for(MetaPath p : paths_l){
				int endType = p.getConcepts().get(p.length());
				//System.out.println("endType: " + endType);
				Map<Integer, Set<Integer>> m = map.get(endType);
				//System.out.println("m: " + m);
				if(m != null){
					List<Integer> con_p = p.getConcepts();
					List<Integer> rel_p = p.getRelations();
					for(int r : m.keySet()){
						List<Integer> rel0 = new ArrayList<>();
						rel0.addAll(rel_p);
						rel0.add(r);
						for(int t : m.get(r)){
							List<Integer> con0 = new ArrayList<>();
							con0.addAll(con_p);
							con0.add(t);
							
							MetaPath mp = new MetaPath(con0, rel0);
							paths_l0.add(mp);
							result.add(mp);
						}
					}
				}
			}
			paths_l = paths_l0;
		}
		
		return result;
	}*/
	
	public Set<MetaPath> getHalfIndex(int typea, int len){
		//System.out.println("type: " + typea);
		Set<MetaPath> result = new HashSet<>();
		Set<MetaPath> paths_l = new HashSet<>();
		List<Integer> concepts = new ArrayList<>();
		concepts.add(typea);
		List<Integer> relations = new ArrayList<>();
		MetaPath mp0 = new MetaPath(concepts, relations);
		paths_l.add(mp0);
		result.add(mp0);
		for(int i = 0; i < len; i ++){
			Set<MetaPath> paths_l0 = new HashSet<>();
			for(MetaPath p : paths_l){
				int endType = p.getConcepts().get(p.length());
				//System.out.println("endType: " + endType);
				Map<Integer, Set<Integer>> m = map.get(endType);
				//System.out.println("m: " + m);
				if(m != null){
					List<Integer> con_p = p.getConcepts();
					List<Integer> rel_p = p.getRelations();
					for(int r : m.keySet()){
						List<Integer> rel0 = new ArrayList<>();
						rel0.addAll(rel_p);
						rel0.add(r);
						for(int t : m.get(r)){
							List<Integer> con0 = new ArrayList<>();
							con0.addAll(con_p);
							con0.add(t);
							
							MetaPath mp = new MetaPath(con0, rel0);
							paths_l0.add(mp);
							result.add(mp);
						}
					}
				}
			}
			paths_l = paths_l0;
		}
		//System.out.println("index size: " + result.size());
		//System.out.println(result);
		return result;
	}
	
	public Set<MetaPath> getMP(int typea, int typeb, int len){
		Set<MetaPath> pathsa;
		if(len % 2 == 0){
			pathsa = getHalfIndex(typea, len/2);
		}
		else
			pathsa = getHalfIndex(typea, len/2 + 1);
		//System.out.println(pathsa);
		Set<MetaPath> pathsb = getHalfIndex(typeb, len/2);
		//System.out.println(pathsb);
		Set<MetaPath> result = new HashSet<>();
		for(MetaPath pa : pathsa){
			List<Integer> cona = pa.getConcepts();
			for(MetaPath pb : pathsb){
				List<Integer> conb = pb.getConcepts();
				if(cona.get(cona.size() - 1).equals(conb.get(conb.size() - 1))){
					//System.out.println("haha:" + pa + " " + pb);
					List<Integer> concepts = new ArrayList<>();
					List<Integer> relations = new ArrayList<>();
					List<Integer> rela = pa.getRelations();
					MetaPath pb_r = pb.getReverse();
					List<Integer> relb_r = pb_r.getRelations();
					List<Integer> conb_r = pb_r.getConcepts();
					relations.addAll(rela);
					relations.addAll(relb_r);
					for(int i = 0; i < cona.size() - 1; i ++){
						concepts.add(cona.get(i));
					}
					concepts.addAll(conb_r);
					MetaPath mp = new MetaPath(concepts, relations);
					
					result.add(mp);
				}
			}
		}
		
		return result;
	}
	
	public void BuildQBSchema(int a, int b){
		BuildQBSchema(a);
		BuildQBSchema(b);
	}
	
	public void BuildQBSchema(int a){
		Set<Integer> ea_l = new HashSet<>(); // a bfs 遇到的实体队列
		ea_l.add(a);
		for(int i = 0; i < D/2; i ++){
			Set<Integer> ea_l0 = new HashSet<>();
			for(int a0 : ea_l){
				List<Integer> typea; //= GraphOntGetterM.basicClassOfEntityByID(a0); // 只保留了基础类
				//List<Integer> typea = new ArrayList<>();
				//typea.addAll(GraphOntGetterM.classOfEntityByID(a0));
				if(JdbcUtil.URL.contains("dbpedia")){
					typea = GraphOntGetterM.basicClassOfEntityByID(a0);

				}
				else{
					typea = new ArrayList<>();
					typea.addAll(GraphOntGetterM.classOfEntityByID_yagoSimple(a0));
				}

				//System.out.println("typea: " + typea);
				Map<Integer, List<Integer>> paos = RelationIndex.get(a0);
				
				for(int r : paos.keySet()){
					for(int obj : paos.get(r)){
						ea_l0.add(obj);
						List<Integer> typeobj; //= GraphOntGetterM.basicClassOfEntityByID(obj);
						//List<Integer> typeobj = new ArrayList<>();
						//typeobj.addAll(GraphOntGetterM.classOfEntityByID(obj));
						if(JdbcUtil.URL.contains("dbpedia")){
							typeobj = GraphOntGetterM.basicClassOfEntityByID(obj);
						}
						else{
							typeobj = new ArrayList<>();
							typeobj.addAll(GraphOntGetterM.classOfEntityByID_yagoSimple(obj));
						}

						for(int ta : typea){
							if(map.containsKey(ta)){
								Map<Integer, Set<Integer>> m = map.get(ta);
								if(m.containsKey(r)){
									Set<Integer> ots = m.get(r);
									ots.addAll(typeobj);
									m.put(r, ots);
								}
								else{
									Set<Integer> ots = new HashSet<>();
									ots.addAll(typeobj);
									m.put(r, ots);
								}
								map.put(ta, m);
							}
							else{
								Map<Integer, Set<Integer>> m = new HashMap<>();
								Set<Integer> ots = new HashSet<>();
								ots.addAll(typeobj);
								m.put(r, ots);
								
								map.put(ta, m);
							}
						}
					}
				}
			}
			
			ea_l = ea_l0;
			
			
		}
	}

}
