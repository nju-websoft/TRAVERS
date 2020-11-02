package name.dxliu.bfs;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DirectedMultigraph;
/**
 * 
 * @author Daxin Liu
 * This interface is an enclosure of entity-relation graph(from database or memory). Given an vertex-id, any class implements this should answer
 * the neighborhood information of that vertex.
 * 
 */
public class GraphAgent{

	public DirectedMultigraph<Integer, IntegerEdge> graph;
	public GraphAgent(DirectedMultigraph<Integer, IntegerEdge> graph){
		this.graph=graph;
	}
	public List<int[]> getNeighborInfo(Integer id) {
		List<int[]> result = new ArrayList<>();
		Set<IntegerEdge> allEdges =	graph.edgesOf(id);
		for(IntegerEdge ie : allEdges){			
			int[] info = new int[2];//info[0] = neighbor;info[1] = interEdge
			if(ie.getSource()==id){
				info[0] = ie.getTarget();
				info[1] = ie.getEdge();
			}else{
				info[0] = ie.getSource();
				info[1] =-ie.getEdge();//inversed
			}
			result.add(info);			
		}		
		return result;
	}

}