package yago;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class entitiesChecker {
	static Set<String> set0 = new HashSet<String>();
	static Set<String> set1 = new HashSet<String>();
	
	public static void main(String[] args) {
		FileReader reader;
		try {
			reader = new FileReader("H:/myself/yago/yagoFacts.ttl");
			BufferedReader br = new BufferedReader(reader);
			String str = null;
			while((str = br.readLine()) != null){
				if(!str.contains("#")){
					String[] ss = str.split("\t");
					String sub = ss[0];
					String obj = ss[2].split(" ")[0];
					if(sub.contains("\\u"))
						sub = sub.replace("\\u", "u");
					if(sub.contains("\\\\"))
						sub = sub.replace("\\\\", "\\");
					if(obj.contains("\\u"))
						obj = obj.replace("\\u", "u");
					if(obj.contains("\\\\"))
						obj = obj.replace("\\\\", "\\");
					set0.add(sub);
					set0.add(obj);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			reader = new FileReader("H:/myself/yago/coreType.ttl");
			BufferedReader br = new BufferedReader(reader);
			String str = null;
			while((str = br.readLine()) != null){
				if(!str.contains("#")){
					String[] ss = str.split("\t");
					String ent = ss[0];
					if(ent.contains("\\u"))
						ent = ent.replace("\\u", "u");
					if(ent.contains("\\\\"))
						ent = ent.replace("\\\\", "\\");
					set1.add(ent);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(set0.size());//4295825
		System.out.println(set1.size());//3966370
		
		for(String s : set1){
			if(!set0.contains(s)){
				System.out.println("error: " + s);
				return;
			}
		}
		
		for(String s : set0){
			if(!set1.contains(s))
				System.out.println(s);
		}
		
	}
}
