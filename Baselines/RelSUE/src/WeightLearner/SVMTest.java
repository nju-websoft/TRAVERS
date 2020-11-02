package WeightLearner;

import java.io.IOException;

import libsvm.svm;
import libsvm.svm_model;

public class SVMTest {
	public static void Train(String fileName){
		String[] params = {fileName};
		try {
			String modelFile = svm_train.main(params);
			svm_model model = svm.svm_load_model(modelFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String[] params = {"dblp1.data"};
		try {
			String modelFile = svm_train.main(params);
			svm_model model = svm.svm_load_model(modelFile);

			System.out.println("haha: " + modelFile);
			
			String[] params_predict = {"dblp1.data", modelFile, "dblp1.data.result"};
			svm_predict.main(params_predict);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//Train("lbj0.txt");
	}
}
