package linkedMDB;

import java.io.*;
import java.util.*;

public class test {
    public static void main(String[] args){
        File file = new File("H:\\myself\\linkedmdb-latest-dump\\linkedmdb-latest-dump.nt");
        FileReader fr = null;
        BufferedReader br = null;

        try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			String str;
			while((str = br.readLine()) != null){
				//if(str.split(" ").length != 4 && !str.contains("(Interlink)"))
				//	System.out.println(str);
				if(!str.split(" ")[1].contains("<"))
					System.out.println(str);
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
