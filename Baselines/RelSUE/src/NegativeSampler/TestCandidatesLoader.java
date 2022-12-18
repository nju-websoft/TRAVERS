package NegativeSampler;

import java.io.*;
import java.util.*;

public class TestCandidatesLoader {
	public List<Integer> getSamples(int query, List<Integer> examples, List<String> fileNames){
		List<Integer> samples = new ArrayList<Integer>();
		Set<Integer> set = new HashSet<Integer>();
		FileReader reader;
		for(String file : fileNames){
			try {
				reader = new FileReader(file);
				BufferedReader br = new BufferedReader(reader);
				String str;
				while((str = br.readLine()) != null){
					//System.out.println(str);
					int nsample = Integer.parseInt(str);
					if(nsample != query && !examples.contains(nsample)){
						set.add(nsample);
					}
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		samples.addAll(set);
		return samples;
	}
	
	public static void main(String[] args) {
		TestCandidatesLoader tcl = new TestCandidatesLoader();
		int query = 219426;
		List<String> fileNames = new ArrayList<String>();
		fileNames.add("candidates/candidates");
		fileNames.add("candidates/candidates1");
		fileNames.add("candidates/candidates2");
		System.out.println(tcl.getSamples(query, new ArrayList<Integer>(), fileNames).size());
	}
}
