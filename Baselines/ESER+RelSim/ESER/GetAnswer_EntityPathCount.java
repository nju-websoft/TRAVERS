package ESER;

import Structures.graphModel;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Vector;


public class GetAnswer_EntityPathCount {
	
	public Integer CenterNode = 0;
	public Integer CountLimit = 0;
	public Queue<Integer> q = new LinkedList<Integer>();
	public Map<Integer, Integer> dist = new HashMap<Integer, Integer>();
	public BigInteger[] dis = new BigInteger[5];
	
	public void clean()
	{
		dis[0]=dis[1]=dis[2]=dis[3]=dis[4]=BigInteger.valueOf(0);
		q.clear();dist.clear();
	}
	
	private void addInfo(Integer centernode)
	{
		CenterNode = centernode;
	}
	
	private void BFS()
	{
		dist.put(CenterNode, 0);
		q.add(CenterNode);
		while( !q.isEmpty() )
		{
			Integer x = q.poll();
			Integer d = dist.get(x);
			dis[d] = dis[d].add(BigInteger.valueOf(1));
			if( d >= CountLimit ) continue;
			if(!graphModel.G.containsKey(x))continue;
			HashMap<Integer, List<Integer>> outedges = graphModel.getNeighbours(x);
			for(Map.Entry<Integer, List<Integer>> ent : outedges.entrySet())
			{
				List<Integer> outnodes = ent.getValue();
				for(Integer p : outnodes)
				{
					if(dist.containsKey(p))continue;
					dist.put(p, d + 1);
					q.add(p);
				}
			}
		}
	}
	
	public void count(Integer centernode, Integer lim)
	{
		clean();
		Vector<Integer> ret = new Vector<Integer>();
		ret.clear();
		addInfo(centernode);
		CountLimit = lim;
		BFS();
	}
}
