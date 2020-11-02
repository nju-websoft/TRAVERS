package GraphData;

import java.util.*;
import java.sql.*;

import JDBCUtils.JdbcUtil;

public class Ontology {
	static Connection conn = JdbcUtil.getConnection();
	private static Ontology instance;
	public static Concept root;
	//public static List<Concept> allConcepts = new ArrayList<Concept>();
	public static Map<Integer, Concept> allConcepts = new HashMap<>();
	public static Map<Pair, Integer> LCS = new HashMap<>(); // least common subsumer. Note that it is not feasible to calculate LCS for yago in advance
	static int size = 0;
	
	public static void Initialize(){
		if(null == instance){
			instance = new Ontology();
		}
	}
	
	protected Ontology(){
		int rid = -1;
		if(JdbcUtil.URL.contains("dbpedia"))
			rid = 3481453; // dbpedia
		else if(JdbcUtil.URL.contains("yago"))
			rid = 4832388;
		//int rid = 4832388; // yago
		//int rid = ; // dblp
		//int rid = ; // linkedmdb
		root = new Concept(rid); 
		
		allConcepts.put(rid, root);
		size = 1;
		
		Construct();

		/**
		 * 如果是完整的yago taxonomy的话，没有办法提前计算好，因为太多了
		 */
		if(!JdbcUtil.URL.contains("yago")){
			Descendant(root);
			calculateLCSByDFS(root);
		}
		else{
			Descendant4Yago(root);
		}
		System.out.println("Ontology size: " + size);

	}
	
	protected static void Construct(){
		try {
			Statement stmt = conn.createStatement();
			String sql = "select * from ontinfos";
			ResultSet rs = stmt.executeQuery(sql);
			while(rs.next()){
				int child = rs.getInt("child");
				int parent = rs.getInt("parent");
				
				constructByTuple(parent, child);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void constructByTuple(int parent, int child){
		Concept parc = returnById(parent);
		if(parc == null){
			parc = new Concept(parent);
			allConcepts.put(parent, parc);
			size ++;
		}
		
		Concept chic = returnById(child);
		if(chic == null){
			chic = new Concept(child);
			allConcepts.put(child, chic);
			size ++;
		}
		
		parc.lowerClasses.add(chic);
		chic.upperClass = parc;
		
		
	}

	/**
	 *
	 * @param root
	 * @return the depth of the tree whose root is root
	 */
	public static int  getDepth(Concept root){ // 7 for dbpedia, 72 for yago
		if(root.lowerClasses.size() == 0)
			return 1;
		int max = 0;
		for(Concept c : root.lowerClasses){
			int cdepth = getDepth(c);
			if(cdepth > max)
				max = cdepth;
		}
		return max+1;
	}
	
	public static Concept returnById(int id){
		if(allConcepts.containsKey(id))
			return allConcepts.get(id);
		
		else return null;
	}
	
	public void calculateLCSByDFS(Concept root){
		Pair pr = new Pair(root.id, root.id);
		LCS.put(pr, root.id);
		
		for(Concept c : root.lowerClasses){
			calculateLCSByDFS(c);
			Pair p = new Pair(root.id, c.id);
			LCS.put(p, root.id);
			for(Concept d : c.descendants){
				Pair pd = new Pair(root.id, d.id);
				LCS.put(pd, root.id);
			}
		}
		
		for(Concept c0 : root.descendants){
			for(Concept c1 : root.descendants){
				Pair p = new Pair(c0.id, c1.id);
				if(!LCS.containsKey(p)){
					LCS.put(p, root.id);
				}
			}
		}
	}
	
	public void Descendant(Concept root){
		for(Concept c : root.lowerClasses){
			Descendant(c);
			root.descendants.add(c);
			root.descendants.addAll(c.descendants);
		}
	}

	public void Descendant4Yago(Concept root){
		root.descendants.addAll(root.lowerClasses);
		for(Concept c : root.lowerClasses){
			root.descendants.addAll(c.lowerClasses);
			c.descendants.addAll(c.lowerClasses);
		}

	}
	
	/**
    *
    * @param a
    * @param b
    * @return whether a is a descendant of b
    */
	 public static boolean isDescendantOf(int a, int b){
		 Concept cb = returnById(b);
		 Concept ca = returnById(a);
		 try{
			 if(cb.descendants.contains(ca))
				 return true;
		 } catch (Exception e){
			 System.out.println("Illegal type ID");
			 e.printStackTrace();
		 }
		 
		 return false;
	 }

	/**
	 *
	 * @param a  type id
	 * @param b  type id
	 * @return id of the lcs of a and b
	 */
	public static int getLCS(int a, int b){ // offline version. Calculate lcs for each pair before hand.
		Pair p = new Pair(a, b);
		if(LCS.containsKey(p)){
			return LCS.get(p);
		}
		else{
			try {
				throw new Exception("Illegal type in getLCS!");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return -1;
		}
	}

	public static int getLCSOnline(int a, int b){ // online version.
		if(isDescendantOf(a, b))
			return b;
		else if(isDescendantOf(b, a))
			return a;
		else{
			int ancestor = a;
			Concept concepta = returnById(a);
			while(!isDescendantOf(b, concepta.upperClass.id)){
				concepta = concepta.upperClass;
				ancestor = concepta.id;
			}

			return ancestor;
		}
	}
	
	public static Set<Integer> getallValidTypes(Set<Integer> types){
		Set<Integer> result = new HashSet<>();
		for(int t0 : types){
			for(int t1 : types){
				result.add(getLCS(t0, t1));
			}
		}
		
		return result;
	}
	
	public static class Pair{
		int a;
		int b;
		
		public Pair(int a, int b){
			this.a = a;
			this.b = b;
		}
		
		public int hashCode() {
	        return a + b + Math.abs(a - b);
	    }
		
		public boolean equals(Object obj) {
	        if (obj instanceof Pair) {
	            if((this.a == ((Pair)obj).a && this.b == ((Pair)obj).b)
	            		|| this.b == ((Pair)obj).a && this.a == ((Pair)obj).b)
	            	return true;
	        }
	        return false;
	    }
		
	}
	
	public static void main(String[] args) {
		Ontology.Initialize();
		/*System.out.println(LabelGetter.get(3482184));
		System.out.println(LabelGetter.get(3482185));
		
		System.out.println(LabelGetter.get(getLCS(3482184, 3482185)));
		System.out.println(LabelGetter.get(getLCS(3482185, 3482184)));
		
		System.out.println(LabelGetter.get(getLCS(3482184, 3482184)));
		System.out.println(LabelGetter.get(getLCS(3482185, 3482185)));*/

		System.out.println("depth: " + getDepth(allConcepts.get(3481453)));
		
		System.out.println(isDescendantOf(3481454,3481653));
		Set<Integer> types = new HashSet<>();
		types.add(3482184);
		types.add(3482185);
		//types.add(3481790);
		
		System.out.println(getallValidTypes(types));
	}

}
