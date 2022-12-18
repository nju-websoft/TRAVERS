package Structures;

import Jama.Matrix;

import java.util.ArrayList;
import java.util.List;

public class LogisticRegression {

    //0, 0.1, 0.5, 1, 2, 5

    public double lambda = 0.05;

    public Matrix para;
    public Matrix Y;
    public Matrix theta;

    public List<Integer> mi = new ArrayList<>();
    public List<Integer> ma = new ArrayList<>();
    public List<Integer> su = new ArrayList<>();

    public void AdjustData(List<List<Integer>> pos_sample, List< List<Integer> > neg_sample)
    {
        if(pos_sample.size() <= 0) return;

        Integer sz_p = pos_sample.size();
        Integer sz_n = neg_sample.size();
        Integer len = pos_sample.get(0).size();
        Integer base = 0;
        Y = new Matrix(sz_p + sz_n, 1);
        para = new Matrix(sz_n + sz_p, len + 1);
        ma.clear(); mi.clear(); su.clear();
        for(Integer i = 0; i < len; ++ i)
        {
            ma.add(-1000000000);
            mi.add(1000000000);
            su.add(0);
        }

        for(Integer id = 0; id < sz_p; ++ id)
        {
            List<Integer> tmp_pos = pos_sample.get(id);
            for(Integer i = 0; i < len; ++ i)
            {
                Integer a = tmp_pos.get(i);

                Integer tmp_ma = ma.get(i);
                ma.set(i, Math.max( tmp_ma, a ));

                Integer tmp_mi = mi.get(i);
                mi.set(i, Math.min( tmp_mi, a ));

                Integer tmp_su = su.get(i);
                su.set(i, tmp_su + a);
            }
        }
        for(Integer id = 0; id < sz_n; ++ id)
        {
            List<Integer> tmp_neg = neg_sample.get(id);
            for(Integer i = 0; i < len; ++ i)
            {
                Integer a = tmp_neg.get(i);

                Integer tmp_ma = ma.get(i);
                ma.set(i, Math.max( tmp_ma, a ));

                Integer tmp_mi = mi.get(i);
                mi.set(i, Math.min( tmp_mi, a ));
            }
        }

        for(Integer id = 0; id < sz_p; ++ id)
        {
            List<Integer> tmp_pos = pos_sample.get(id);

            Y.set(base + id, 0, 1); para.set(base + id, 0, 1);

            for(Integer i = 0; i < len; ++ i)
            {
                if(ma.get(i).equals(mi.get(i)))
                {
                    para.set(base + id, i + 1, 0.0);
                    continue;
                }
                double a = tmp_pos.get(i);
                double cha = ma.get(i) - mi.get(i); cha *= 0.5;
                double val = a - su.get(i) / (double)(sz_p + sz_n);
                para.set(base + id, i + 1, val / cha);
            }
        }
        base = sz_p;
        for(Integer id = 0; id < sz_n; ++ id)
        {
            List<Integer> tmp_neg = neg_sample.get(id);

            Y.set(base + id, 0, 1); para.set(base + id, 0, 1);

            for(Integer i = 0; i < len; ++ i)
            {
                if(ma.get(i).equals(mi.get(i)))
                {
                    para.set(base + id, i + 1, 0.0);
                    continue;
                }
                double a = tmp_neg.get(i);
                double cha = ma.get(i) - mi.get(i); cha *= 0.5;
                double val = a - su.get(i) / (double)(sz_p + sz_n);
                para.set(base + id, i + 1, val / cha);
            }
        }
    }

    public double H_value(Matrix x)
    {
        double ret = x.times(theta).get(0,0);
        return 1.0 / ( 1.0 + Math.exp( -ret ) );
    }

    public double G_value(double x)
    {
        double ret = x;
        return 1.0 / ( 1.0 + Math.exp( -ret ) );
    }

    public Matrix H_matrix(Matrix x)
    {
        Matrix ret = x.times(theta);
        Matrix ans = new Matrix(ret.getRowDimension(), ret.getColumnDimension());
        for(Integer i = 0; i < ret.getRowDimension(); ++ i)
            for(Integer j = 0; j < ret.getColumnDimension(); ++ j)
                ans.set(i, j, 1.0 / ( 1.0 + Math.exp( -ret.get(i, j) ) ));
        return ans;
    }

    public Matrix G_matrix(Matrix x)
    {
        Matrix ret = x;
        Matrix ans = new Matrix(ret.getRowDimension(), ret.getColumnDimension());
        for(Integer i = 0; i < ret.getRowDimension(); ++ i)
            for(Integer j = 0; j < ret.getColumnDimension(); ++ j)
                ans.set(i, j, 1.0 / ( 1.0 + Math.exp( -ret.get(i, j) ) ));
        return ans;
    }

    public void Optimize()
    {
        Integer len = para.getColumnDimension();
        Matrix iden = Matrix.identity(len, len);
        iden.set(0, 0, 0.0);
        theta = ( para.transpose().times(para).plus( iden.times(lambda) ) ).inverse().times( para.transpose() ).times(Y);
        //theta = ( para.transpose().times(para) ).inverse().times( para.transpose() ).times(Y);
        //System.out.println("para  : " + para.getRowDimension() + " * " + para.getColumnDimension());
        //System.out.println("Y     : " + Y.getRowDimension() + " * " + Y.getColumnDimension());
        //System.out.println("theta : " + theta.getRowDimension() + " * " + theta.getColumnDimension());
    }

    public List<Double> Sovle(List<List<Integer>> pos_sample, List< List<Integer> > neg_sample)
    {
        AdjustData(pos_sample, neg_sample);

        //theta = Matrix.random(para.getColumnDimension(), 1);

        Optimize();

        List<Double> weights = new ArrayList<Double>(); weights.clear();
        for(Integer i = 1; i < theta.getRowDimension(); ++ i) weights.add( theta.get(i, 0) );

        //System.out.println("Sample Feature : " + pos_sample.get(0).size() + " --- Weight Feature : " + weights.size());

        return weights;
    }

}
