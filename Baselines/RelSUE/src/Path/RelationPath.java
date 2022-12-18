package Path;

import java.util.*;


public class RelationPath {
	List<Integer> relations = new ArrayList<Integer>();

	public RelationPath(){
		relations = new ArrayList<>();
	}
	
	public RelationPath(List<Integer> relations){
		this.relations = relations;
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
			
		return code;
	}
	public boolean equals(Object hp){
		if(((RelationPath)hp).getRelations().equals(this.relations))
			return true;
		else
			return false;
	}
	public String toString(){
		return this.relations.toString();
	}
	public boolean isSymmetric(){
		int size = this.relations.size();
		if(size % 2 != 0)
			return false;
		for(int i = 0; i <= (size - 1)/2; i ++){
			if(Math.abs(this.relations.get(i)) != Math.abs(this.relations.get(size -1 - i)))
				return false;
		}
			
		return true;
	}
	public RelationPath getReverse(){
		List<Integer> rs = new ArrayList<Integer>();
		int size = this.relations.size();
		for(int i = 0; i < size; i ++){
			rs.add(this.relations.get(size - 1 - i)*-1);
		}
		
		return new RelationPath(rs);
	}
	public RelationPath getHalf(){
		List<Integer> rs = new ArrayList<Integer>();
		int size = this.relations.size();
		for(int i = 0; i < size/2; i ++)
			rs.add(this.relations.get(i));
		
		return new RelationPath(rs);
	}
	public RelationPath get(int i){
		List<Integer> rs = new ArrayList<Integer>();
		rs.add(this.relations.get(i));
		
		return new RelationPath(rs);
	}

	public int length(){
		return this.relations.size();
	}

	public int getLastRelation(){
		if(relations.size() > 0)
			return relations.get(relations.size() - 1);
		else
			return -1;
	}
	
	public static void main(String[] args) {
		List<Integer> rs = new ArrayList<Integer>();
		rs.add(1);
		rs.add(-2);
		rs.add(3);
		rs.add(-4);
		
		RelationPath rp = new RelationPath(rs);
		System.out.println(rp);
		System.out.println(rp.getReverse());
	}
}
