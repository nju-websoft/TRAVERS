package name.dxliu.bfs;

import java.util.ArrayList;
/**
 * Path. the father field is the previous node of a path while the sons field keep all the possible 
 * succeeding node.
 * @author LiuXin
 *
 */
public class Path {
	public int length = 0;
	public 	Path father=null;
	public 	int id;	
	public ArrayList<Path> sons=new ArrayList<Path>();	
	public int relation=0;//the relation lays on the current path.	
	
	
	public Path(int i){ id=i;}
	public Path(int i,Path father){this.id=i;this.father=father; this.length=father.length+1;}
	public Path(int id,int relate,Path father){this.relation = relate;this.id=id;this.father=father;this.length=father.length+1;}

	public Path clone(){Path n=new Path(id);n.relation = relation;n.father=father;n.sons=sons;n.length=length;return n;}

	public int getLength(){
		return length;
	}
	
	public void recursivePrint(){
		System.out.print(id+"-> "+ relation+"-> ");
		if(father!=null){
			father.recursivePrint();
		}
	}
}
