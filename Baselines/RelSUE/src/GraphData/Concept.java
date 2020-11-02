package GraphData;

import java.util.*;

import GraphData.Ontology.Pair;

public class Concept {
	public int id;// the index of class in database
	public Concept upperClass;// concept is subclass of upperClass
	public Set<Concept> lowerClasses;
	public Set<Concept> descendants;
	public Concept(int id){
		this.id = id;
		upperClass = null;
		lowerClasses = new HashSet<Concept>();
		descendants = new HashSet<Concept>();
	}
	
	public int hashCode() {
        return id;
    }
	
	public boolean equals(Object obj) {
        if (obj instanceof Concept) {
            if(this.id == ((Concept)obj).id)
            	return true;
        }
        return false;
    }
}
