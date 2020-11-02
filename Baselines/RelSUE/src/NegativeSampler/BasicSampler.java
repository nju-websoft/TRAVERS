package NegativeSampler;

import java.util.*;

import GraphData.GraphClassInstances;
import GraphData.GraphClassInstancesM;

public class BasicSampler {
	public List<Integer> getSamples(int type, int query, List<Integer> examples, int number){
		List<Integer> candidates = GraphClassInstancesM.getInstances(type);
		//List<Integer> candidates = GraphClassInstances.getInstances(type);
		candidates.remove((Integer)query);
		for(int example : examples)
			candidates.remove((Integer)example);

		if(candidates.size() <= number){
			return candidates;
		}
		else{
			Set<Integer> set = new HashSet<Integer>();
			while(set.size() < number){
				int rand = (int)(Math.random()*(candidates.size() - 1));
				//System.out.println("rand: " + rand);
				set.add(candidates.get(rand));
			}
			List<Integer> result = new ArrayList<Integer>();
			result.addAll(set);
			
			return result;
		}
		
	}
	
	public static void main(String[] args){
		GraphClassInstancesM.initializeMap();
		System.out.println("Start to sample");
		BasicSampler bs = new BasicSampler();
		int type = 3481461;
		int query = 1;
		List<Integer> examples = new ArrayList<Integer>();
		examples.add(880652);
		System.out.println(bs.getSamples(type, query, examples, 150000).size());
	}
}
