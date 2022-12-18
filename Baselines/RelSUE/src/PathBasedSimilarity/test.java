package PathBasedSimilarity;

import java.util.*;

public class test {
	public static void main(String[] args) {
		Map<Integer, Double> map = new HashMap<Integer, Double>();
		map.put(1, 0.5);
		map.put(1, 2.0);
		map.put(2, 9.0);
		
		System.out.println(map);
		for(Map.Entry<Integer, Double> entry : map.entrySet()){
			entry.setValue(entry.getValue() * 0.3);
		}
		System.out.println(map);
	}
}
