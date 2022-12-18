package yago;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class coreTypesInfo {
	static Map<String, Integer> map = getMap.get();
	static Set<String> entities = new HashSet<String>();
	public static void main(String[] args) {
		FileReader reader;
		try {
			reader = new FileReader("H:/myself/yago/yagoTransitiveType.ttl");
			BufferedReader br = new BufferedReader(reader);
			String str = null;
			int i = 0;
			File file = new File("coreType.ttl");
			FileWriter fw = null;
			BufferedWriter writer = null;
			fw = new FileWriter(file);
			writer = new BufferedWriter(fw);
			while((str = br.readLine()) != null){
				if(str.contains("rdf:type\t")){
					String[] ss = str.split("\t");
					String entity = ss[0];
					String type = ss[2];
					if(entity.contains("\\u"))
						entity = entity.replace("\\u", "u");
					if(entity.contains("\\\\"))
						entity = entity.replace("\\\\", "\\");
					
					
					if(map.get(entity) != null){
						writer.write(entity + "\t" + type);
						writer.newLine();
						entities.add(entity);
						i ++;
					}
				}
				
			}
			writer.close();
			fw.close();
			System.out.println(i);
			System.out.println(entities.size());
			//System.out.println(types.size());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
