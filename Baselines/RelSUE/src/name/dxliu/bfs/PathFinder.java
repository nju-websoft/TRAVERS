package name.dxliu.bfs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.jgrapht.graph.DirectedMultigraph;

import JDBCUtils.JdbcUtil;

public class PathFinder {
	
	public static GraphAgent graphAgent = null;		
	public static List<Path> findAllPaths(int start, int end, int length){
		if(start == end||length<=0) return null;
		
		List<Path> allPaths = new ArrayList<>();
		
		Path root = new Path(start);
		Queue<Path> bfsQueue = new LinkedList<>();		
		bfsQueue.offer(root);
		while(!bfsQueue.isEmpty()){
			Path currentPath = bfsQueue.poll();
			
			if(currentPath.id == end){//find a path.
				allPaths.add(currentPath);
				continue;
			}
			
			List<int[]> neighbors = graphAgent.getNeighborInfo(currentPath.id);
			for(int[] neighbor : neighbors){
				if(isLoop(currentPath,neighbor[0])) continue;//有环
				if(currentPath.length<length){
					Path extendedPath = new Path(neighbor[0],neighbor[1],currentPath);
					bfsQueue.add(extendedPath);
				}
			}			
		}		
		return allPaths;
	}

	private static boolean isLoop(Path currentPath, int node) {
		if(currentPath==null) return false;
		return currentPath.id==node||isLoop(currentPath.father, node);
	}
	
	
	public static void main(String[] args) {
//		DirectedMultigraph<Integer, IntegerEdge> graph = new DirectedMultigraph<>(IntegerEdge.class);
//		graph.addVertex(1);
//		graph.addVertex(2);
//		graph.addVertex(3);
//		graph.addVertex(4);
//		
//		graph.addEdge(1, 2, new IntegerEdge(1, 2, 11));
//		graph.addEdge(1, 2, new IntegerEdge(1, 2, 12));
//		graph.addEdge(2, 4, new IntegerEdge(2, 4, 11));
//		graph.addEdge(1, 3, new IntegerEdge(1, 3, 11));
//		graph.addEdge(3, 4, new IntegerEdge(3, 4, 11));
//		graph.addEdge(1, 4, new IntegerEdge(1, 4, 11));
		
//		graph.addVertex(1); graph.addVertex(2);
//		graph.addVertex(3); graph.addVertex(4);
//		graph.addVertex(5); graph.addVertex(6);
//		graph.addVertex(7); graph.addVertex(8);
//		
//		graph.addEdge(1, 2, new IntegerEdge(1, 2, 11));graph.addEdge(1, 2, new IntegerEdge(1, 2, 12));		
//		graph.addEdge(1, 3, new IntegerEdge(1, 3, 11));
//		graph.addEdge(1, 4, new IntegerEdge(1, 4, 11));
//		graph.addEdge(2, 7, new IntegerEdge(2, 7, 11));graph.addEdge(2, 7, new IntegerEdge(2, 7, 12));
//		graph.addEdge(2, 6, new IntegerEdge(2, 6, 11));
//		graph.addEdge(3, 7, new IntegerEdge(3, 7, 11));
//		graph.addEdge(3, 5, new IntegerEdge(3, 5, 11));
//		graph.addEdge(4, 6, new IntegerEdge(4, 6, 11));
//		graph.addEdge(4, 8, new IntegerEdge(4, 8, 11));
//		graph.addEdge(5, 8, new IntegerEdge(5, 8, 11));
//		graph.addEdge(6, 8, new IntegerEdge(6, 8, 11));
//		graph.addEdge(7, 8, new IntegerEdge(7, 8, 11));
		
		PathFinder.graphAgent = new GraphAgent(loadGraph());
		System.out.println("load complete");
		long startTime = System.currentTimeMillis();
		
		List<Path> allPaths =  PathFinder.findAllPaths(1562340, 2580708, 4);
		
		
		System.out.println("finished in " + (System.currentTimeMillis()-startTime)+" ms");
		System.out.println(allPaths.size());
		for(Path path : allPaths){
			path.recursivePrint();
			System.out.println();
		}


//		-3480865 3480865
//		-3480865 3480807
//		-3480808 3480808
//		-3480880 3480880
		
//				[3480865, -3480865]
//				[3480807, -3480865]
//				[3480808, -3480808]
//				[3480880, -3480880]
		
		
		
//		for(Path path : allPaths){
//			path.recursivePrint();
//			System.out.println();
//		}
	}
	
	public static DirectedMultigraph<Integer, IntegerEdge> loadGraph(){
		DirectedMultigraph<Integer, IntegerEdge> graph = new DirectedMultigraph<>(IntegerEdge.class);
		
		try {
			Connection connection = JdbcUtil.getConnection();
			PreparedStatement preparedStatement = connection.prepareStatement("select * from objtriples");
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				int subject = resultSet.getInt("subject");
				int predicate = resultSet.getInt("predicate");
				int object = resultSet.getInt("object");
				if(subject == object) continue;
				graph.addVertex(subject);
				graph.addVertex(object);
				graph.addEdge(subject, object, new IntegerEdge(subject, object, predicate));
			}
			resultSet.close();
			preparedStatement.close();			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return graph;
	}
	
}
