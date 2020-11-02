package GraphData;

import java.util.*;
import java.io.*;

import JDBCUtils.JdbcUtil;

public class RelationsFreq {
	public static Map<Integer, Double> freq = null;
	
	public static void initialize(String fileName){
		if(null == freq){
			freq = new HashMap<Integer, Double>();
			File file = new File(fileName);
			
			FileReader fr = null;
			BufferedReader reader = null;
			
			try {
				fr = new FileReader(file);
				reader = new BufferedReader(fr);
				String str;
				while((str = reader.readLine()) != null){
					String[] ss = str.split(" ");
					int relation = Integer.parseInt(ss[0]);
					double fre = Double.parseDouble(ss[1]);
					//System.out.println(relation + "     " + fre);
					freq.put(relation, fre);
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static double getFreq(int relation){
		if(freq.get(relation) != null){
			return freq.get(relation);
		}
		else
			return 0;
	}
	
	public static void main(String[] args) {
		RelationsFreq.initialize("dbpedia.freq");
		System.out.println(RelationsFreq.freq);
	}

}
