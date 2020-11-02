package WeightLearner;

import com.joptimizer.exception.JOptimizerException;
import com.joptimizer.optimizers.LPOptimizationRequest;
import com.joptimizer.optimizers.LPPrimalDualMethod;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class NNLearner {
    public static List<Double> getWeights(String fileName){
        List<Double> result = new ArrayList<Double>();
        File file = new File(fileName);
        FileReader fr = null;
        BufferedReader reader = null;

        try {
            fr = new FileReader(file);
            reader = new BufferedReader(fr);
            String str;
            int kp = 0, kn = 0, m = 0;
            List<List<Double>> X = new ArrayList<List<Double>>();
            List<List<Double>> NX = new ArrayList<List<Double>>();
            while((str = reader.readLine()) != null){
                String[] ss = str.split(" ");
                m = ss.length - 1;
                if(ss[0].equals("1")){
                    List<Double> xkl = new ArrayList<>();
                    for(int i = 1; i < ss.length; i ++){
                        String s = ss[i];
                        String[] index_f = s.split(":");
                        xkl.add(Double.parseDouble(index_f[1]));
                    }
                    X.add(xkl);
                    kp ++;
                }
                else if(ss[0].equals("0")){
                    List<Double> nxkl = new ArrayList<>();
                    for(int i = 1; i < ss.length; i ++){
                        String s = ss[i];
                        String[] index_f = s.split(":");
                        nxkl.add(Double.parseDouble(index_f[1]));
                    }
                    NX.add(nxkl);
                    kn ++;
                }
            }
            System.out.println("kp: " + kp);
            System.out.println(X);
            System.out.println(NX);

            double[] c = new double[m + kp + kn];
            for(int i = 0; i < m; i ++)
                c[i] = 0;
            for(int i = m; i < m + kp; i ++)
                c[i] = 50;
            for(int i = m + kp; i < m + kp + kn; i ++)
                c[i] = 1;

            //inequality constraint
            double[][] G = new double[kp + kn][m + kp + kn];
            double[] h = new double[kp + kn];
            for(int i = 0; i < kp + kn; i ++)
                h[i] = -1;
            for(int i = 0; i < kp; i ++){
                for(int j = 0; j < m; j ++){
                    G[i][j] = -1 * X.get(i).get(j);
                }
                for(int j = m; j < m + kp; j ++){
                    if(j == m + i)
                        G[i][j] = -1;
                    else
                        G[i][j] = 0;
                }
            }
            for(int i = 0; i < kn; i ++){
                for(int j = 0; j < m; j ++){
                    G[kp + i][j] = NX.get(i).get(j);
                }
                for(int j = m + kp; j < m + kp + kn; j ++){
                    if(j == m + kp + i)
                        G[kp + i][j] = -1;
                    else
                        G[kp + i][j] = 0;
                }
            }

            //Bounds on variables
            double[] lb = new double[m + kp + kn];
            for(int i = 0; i < m + kp + kn; i ++)
                lb[i] = 0;
            double[] ub = new double[m + kp + kn];
            for(int i = 0; i < m; i ++)
                ub[i] = 1;
            for(int i = m; i < m + kp + kn; i ++)
                ub[i] = 1000000;


            //optimization problem
            LPOptimizationRequest or = new LPOptimizationRequest();
            or.setC(c);
            or.setG(G);
            or.setH(h);
            or.setLb(lb);
            or.setUb(ub);
            or.setDumpProblem(true);

            //optimization
            LPPrimalDualMethod opt = new LPPrimalDualMethod();

            opt.setLPOptimizationRequest(or);
            try {
                opt.optimize();
                double[] solution = opt.getOptimizationResponse().getSolution();
                for(int i = 0; i < m; i ++){
                    result.add(solution[i]);
                }
                for(int i = m; i < m + kp + kn; i ++)
                    System.out.println(solution[i]);

            } catch (JOptimizerException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static void main(String []args){
        System.out.println(getWeights("lbj0.txt"));
    }
}
