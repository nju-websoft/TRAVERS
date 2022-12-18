package WeightLearner;

import com.joptimizer.exception.JOptimizerException;
import com.joptimizer.optimizers.LPOptimizationRequest;
import com.joptimizer.optimizers.LPPrimalDualMethod;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LPforRelSim {
    public static double param_c = 1;
    public static List<Double> getWeights(String fileName){
        List<Double> result = new ArrayList<Double>();
        File file = new File(fileName);
        FileReader fr = null;
        BufferedReader reader = null;

        try {
            fr = new FileReader(file);
            reader = new BufferedReader(fr);
            String str;
            int k = 0, m = 0;
            List<List<Double>> X = new ArrayList<List<Double>>();
            List<List<Double>> NX = new ArrayList<List<Double>>();
            while((str = reader.readLine()) != null){
                String[] ss = str.split(":");
                //System.out.println(ss[0]);
                //System.out.println(ss[1]);
                String[] xk = ss[0].split(" ");
                m = xk.length;
                String[] nxk = ss[1].split(" ");
                List<Double> xkl = new ArrayList<Double>();
                List<Double> nxkl = new ArrayList<Double>();
                for(int i = 0; i < m; i ++){
                    double a = Double.parseDouble(xk[i]);
                    double b = Double.parseDouble(nxk[i]);

                    if(a != 0 || b != 0){
                        xkl.add(a/(Math.sqrt(a*a + b*b)));
                        nxkl.add(b/(Math.sqrt(a*a + b*b)));
                    }
                    else{
                        xkl.add(0.);
                        nxkl.add(0.);
                    }
                }
                X.add(xkl);
                NX.add(nxkl);
                k ++;
            }
            //System.out.println(X);
            //System.out.println(NX);
            //System.out.println(k);
            double[] c = new double[m + k];
            for(int i = 0; i < m; i ++)
                c[i] = 0.;
            for(int i = m; i < m + k; i ++)
                c[i] = 1;
            //equality constraint: sum of w = 1
            double[][] A = new double[1][m + k];
            double[] b = new double[] {1.};
            for(int i = 0; i < m; i ++)
                A[0][i] = 1.;
            for(int i = m; i < m + k; i ++)
                A[0][i] = 0.;

            //inequality constraint
            double[][] G = new double[k][m + k];
            double[] h = new double[k];
            for(int i = 0; i < k; i ++)
                h[i] = -1*param_c;
            for(int i = 0; i < k; i ++){
                for(int j = 0; j < m; j ++){
                    G[i][j] = (NX.get(i).get(j) - X.get(i).get(j));
                }
                for(int j = m; j < m + k; j ++){
                    if(j == m + i)
                        G[i][j] = -1;
                    else
                        G[i][j] = 0;
                }
            }

            //Bounds on variables
            double[] lb = new double[m + k];
            for(int i = 0; i < m + k; i ++)
                lb[i] = 0;
            double[] ub = new double[m + k];
            for(int i = 0; i < m; i ++)
                ub[i] = 1;
            for(int i = m; i < m + k; i ++)
                ub[i] = 1000000;


            //optimization problem
            LPOptimizationRequest or = new LPOptimizationRequest();
            or.setC(c);
            or.setA(A);
            or.setB(b);
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
                    if(solution[i] >= 0.9999)
                        solution[i] = 1.0;
                    if(solution[i] <= 0.000001)
                        solution[i] = 0.0;
                    result.add(solution[i]);
                }

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
        List<Double> weights = getWeights("relsim/Vagn_Ekman_relsim.txt");
        System.out.println(weights);
    }
}
