package Path;

import java.util.*;

public class MetaPath {
	List<Integer> concepts = new ArrayList<Integer>();
	List<Integer> relations = new ArrayList<Integer>();
	
	public MetaPath(){
		concepts = new ArrayList<>();
		relations = new ArrayList<>();
	}

	public MetaPath(List<Integer> concepts){
		if(concepts.size() == 1){
			this.concepts = concepts;
			relations = new ArrayList<>();
		}
		else{
			try {
				throw new Exception("cannot initialize metapath in this way!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public MetaPath(List<Integer> concepts, List<Integer> relations){
		if(concepts.size() == (relations.size() + 1)){
			this.relations = relations;
			this.concepts = concepts;
		}
		else
			try {
				throw new Exception("concepts and relations do not match!");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public List<Integer> getConcepts() {
		return concepts;
	}
	public void setConcepts(List<Integer> concepts) {
		this.concepts = concepts;
	}
	public List<Integer> getRelations() {
		return relations;
	}
	public void setRelations(List<Integer> relations) {
		this.relations = relations;
	}
	public int hashCode(){
		int code = 0;
		for(int i : relations){
			if(i < 0){
				i *= -1;
				code -= (i - 3480807);
			}
			code += i;
		}
		for(int i : concepts)
			code += i;
		return code;
	}
	public boolean equals(Object hp){
		if(((MetaPath)hp).getRelations().equals(this.relations) && ((MetaPath)hp).getConcepts().equals(this.concepts))
			return true;
		else
			return false;
	}
	public String toString(){
		List<Integer> items = new ArrayList<Integer>();
		for(int i = 0; i < relations.size(); i ++){
			items.add(concepts.get(i));
			items.add(relations.get(i));
		}
		items.add(concepts.get(concepts.size() - 1));
		
		return items.toString();
	}
	public boolean isLegal(){
		return concepts.size() == (relations.size() + 1);
	}
	public MetaPath getReverse(){
		List<Integer> rs = new ArrayList<Integer>();
		int sizer = this.relations.size();
		for(int i = 0; i < sizer; i ++){
			rs.add(this.relations.get(sizer - 1 - i)*-1);
		}
		List<Integer> cs = new ArrayList<Integer>();
		int sizec = this.concepts.size();
		for(int i = 0; i < sizec; i ++){
			cs.add(this.concepts.get(sizec - 1 - i));
		}
		
		return new MetaPath(cs, rs);
	}
	public MetaPath getHalf(){
		List<Integer> rs = new ArrayList<Integer>();
		int sizer = this.relations.size();
		for(int i = 0; i < sizer/2; i ++)
			rs.add(this.relations.get(i));
		List<Integer> cs = new ArrayList<Integer>();
		int sizec = this.concepts.size();
		for(int i = 0; i < (sizec+1)/2; i ++)
			cs.add(this.concepts.get(i));
		
		return new MetaPath(cs, rs);
	}
	public MetaPath get(int i){
		List<Integer> rs = new ArrayList<Integer>();
		rs.add(this.relations.get(i));
		List<Integer> cs = new ArrayList<Integer>();
		cs.add(this.concepts.get(i));
		cs.add(this.concepts.get(i + 1));
		
		return new MetaPath(cs, rs);
	}
	/**
	 * 
	 * @return the size of relations
	 */
	public int length(){
		return this.relations.size();
	}

	//如果两个meta-path relation完全相同，且concept只有开头不同，那么就视为两个相同的mp
	public boolean isSame(MetaPath mp){
		if(this.relations.equals(mp.relations)){
			for(int i = 1; i <= this.length(); i ++){
				if(this.concepts.get(i) != mp.getConcepts().get(i))
					return false;
			}
			return  true;
		}
		else
			return false;
	}

	public int getLastRelation(){
		if(relations.size() > 0)
			return relations.get(relations.size() - 1);
		else
			return -1;
	}

	public int getLastConcept(){
		if(concepts.size() > 0)
			return  concepts.get(relations.size());
		else
			return -1;
	}
	
	public static void main(String[] args) {
		List<Integer> concepts = new ArrayList<Integer>();
		concepts.add(1);
		concepts.add(2);
		concepts.add(3);
		List<Integer> relations = new ArrayList<Integer>();
		relations.add(98);
		relations.add(99);
		
		MetaPath mp = new MetaPath(concepts, relations);
		System.out.println(mp);

		List<Integer> concepts0 = new ArrayList<Integer>();
		concepts0.add(4);
		concepts0.add(2);
		concepts0.add(3);
		MetaPath mp0 = new MetaPath(concepts0, relations);

		System.out.println(mp0.isSame(mp));
	}
	
}
