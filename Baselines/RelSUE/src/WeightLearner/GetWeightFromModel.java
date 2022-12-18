package WeightLearner;

import java.io.*;
import java.util.*;
public class GetWeightFromModel {

	public static List<Double> getWeights(String modelFile, int numOfFeatures){
		double []weights = new double[numOfFeatures];
		File file = new File(modelFile);
		FileReader fr = null;
		BufferedReader reader = null;

		try {
			fr = new FileReader(file);
			reader = new BufferedReader(fr);
			String str = null;
			int line = 0;
			while((str = reader.readLine()) != null){
				line ++;
				if(line > 8){
					String[] ss = str.split(" ");
					double alpha = Double.parseDouble(ss[0]);
					for(int i = 0; i < numOfFeatures; i ++){
						String f = ss[i + 1];
						double va = Double.parseDouble(f.split(":")[1]);
						weights[i] = weights[i] + va*alpha;
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<Double> result = new ArrayList<>();
		for(double w : weights){
			if(w > 0)
				result.add(w);
			else
				result.add(0.);
		}

		return  result;
	}

    public static void printWeight(String modelFile, int numOfFeatures){
        double []weights = new double[numOfFeatures];
    	File file = new File(modelFile);
        FileReader fr = null;
        BufferedReader reader = null;

        try {
			fr = new FileReader(file);
			reader = new BufferedReader(fr);
			String str = null;
			int line = 0;
			while((str = reader.readLine()) != null){
				line ++;
				if(line > 8){
					String[] ss = str.split(" ");
					double alpha = Double.parseDouble(ss[0]);
					for(int i = 0; i < numOfFeatures; i ++){
						String f = ss[i + 1];
						double va = Double.parseDouble(f.split(":")[1]);
						weights[i] = weights[i] + va*alpha;
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        for(double w : weights)
        	System.out.println(w);
    }
    
    public static void main(String[] args) {
		System.out.println(getWeights("out/artifacts/Main_jar/viking.txt.model", 9));
	}
}
