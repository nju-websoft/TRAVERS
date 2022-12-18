package WeightLearner;

import java.io.*;

public class Predict_Tester {
	public void Predict(String fileName){
		try {
			FileReader reader = new FileReader(fileName);
			BufferedReader br = new BufferedReader(reader);
			String str;
			while((str = br.readLine()) != null){
				String[] features = str.split(" ");
				double x1 = Double.parseDouble(features[1].split(":")[1]);
				double x2 = Double.parseDouble(features[2].split(":")[1]);
				double x3 = Double.parseDouble(features[3].split(":")[1]);
				
				System.out.println((14.367*x1 + 13.568*x2 + 9.168*x3 - 18.3596) > 0);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		Predict_Tester pt = new Predict_Tester();
		pt.Predict("dblp1.data");
	}
}
