package WeightLearner;

import org.ejml.data.SingularMatrixException;
import org.ejml.simple.SimpleMatrix;
import java.util.*;

public class LARS {
    /**
     * r,u,w都是列向量，即列数（column）为 1
     */
    static SimpleMatrix r;
    static SimpleMatrix u;
    static SimpleMatrix w;
    static SimpleMatrix X;

//    public LARS(SimpleMatrix m0,int numPositive, int numNegative){
//        SimpleMatrix positive = new SimpleMatrix(numPositive,1);
//        SimpleMatrix negitive = new SimpleMatrix(numNegative,1);
//        positive.set(1);
//        negitive.set(-1);
//        LARS.r = positive.combine(numPositive,0,negitive);
//        LARS.w = new SimpleMatrix(1,1);
//
//        r.print();
//        w.print();
//    }

    public static void initialize (SimpleMatrix m0,int numPositive, int numNegative){
        SimpleMatrix positive = new SimpleMatrix(numPositive,1);
        SimpleMatrix negitive = new SimpleMatrix(numNegative,1);
        positive.set(1);
        negitive.set(-1);
        LARS.r = positive.combine(numPositive,0,negitive);
        LARS.w = new SimpleMatrix(1,1);
        X = new SimpleMatrix(m0);

        r.print();
        w.print();
        X.print();

    }
    /**
     * 根据特征向量mk与特征矩阵X（包含mk，即不需要再做X=XU{mk}了）以及上一次迭代的残差r0，计算r,u,w
     * @param mk
     */
    public static boolean calculate(SimpleMatrix mk){
        SimpleMatrix oldX = new SimpleMatrix(X);
        X = X.combine(0,X.numCols(),mk);
        System.out.println("X: " + X);
        SimpleMatrix XTX = X.transpose().mult(X);
        System.out.println("XTX: " + XTX);
        SimpleMatrix OneVector = new SimpleMatrix(X.numCols(),1);
        OneVector.set(1);
        SimpleMatrix OneMatrix;
        try{
            System.out.println("OneVector: " + OneVector );
            OneMatrix = OneVector.transpose().mult(XTX.invert()).mult(OneVector);
            System.out.println("invert: " + XTX.invert());
            System.out.println("OneMatrix: " + OneMatrix);
        }catch (SingularMatrixException e){
            OneMatrix = OneVector.transpose().mult(XTX.pseudoInverse()).mult(OneVector);
//            OneMatrix.print();
        }
        assert OneMatrix.numRows()==1&&OneMatrix.numCols()==1;
        double scale = OneMatrix.get(0,0);
        if (scale < 0){
            X = oldX;
            return false;
        }
        scale = Math.sqrt(scale);
        try {
            LARS.u = XTX.transpose().invert().scale(scale).mult(OneVector);
//            LARS.u.print();
        }catch (SingularMatrixException e){
            LARS.u = XTX.transpose().pseudoInverse().scale(scale).mult(OneVector);
//            LARS.u.print();
        }
        double corr = cosine(mk,LARS.r);
        ArrayList<Double> Gama = new ArrayList<Double>();
        for (int j=0;j < X.numCols()-1;j++){
            double cos = cosine(X.cols(j,j+1),LARS.r);
            SimpleMatrix MjTU = X.cols(j,j+1).transpose().mult(X.mult(LARS.u));
            assert MjTU.numRows()==1&&MjTU.numCols()==1;
            double mjTu = MjTU.get(0,0);
            double minus = (corr-cos)/(scale-mjTu);
            double plus  = (corr+cos)/(scale+mjTu);

            double min = Math.min(minus,plus);
            if (min > 0){
                Gama.add(min);
//                System.out.println("add a min:"+min);
            }
        }
        double gama;
        if (Gama.size()>0){
            gama = Collections.min(Gama);
//            System.out.println("gama = "+gama);
        }else {
            gama = 0;
//            System.out.println("gama = "+gama);
        }
        LARS.r = LARS.r .minus(X.mult(LARS.u).scale(gama));
        LARS.w = LARS.w.combine(LARS.w.numRows(),0,new SimpleMatrix(1,1));
        LARS.w = LARS.w .plus(LARS.u.scale(gama));
//        System.out.println("***************");
//        u.print();
//        r.print();
//        w.print();
//        X.print();
//        System.out.println("***************");
        return true;
    }
    public static double cosine(SimpleMatrix vector1, SimpleMatrix vector2){
        assert vector1.numCols()==1&&vector2.numCols()==1&&vector1.numRows()==vector2.numRows();
        double dot = 0;
        for(int i = 0; i < vector1.numRows();i++){
            dot +=vector1.get(i)*vector2.get(i);
        }
        return dot/(vector1.normF()*vector2.normF());
    }

    /**
     *
     * 将r中元素以list的形式返回
     */
    public static List<Double> getR(){
        ArrayList<Double> R = new ArrayList<Double>();
        for(int i = 0; i < LARS.r.numRows();i++ ){
            R.add(LARS.r.get(i));
        }
        return R;
    }

    /**
     *
     * 将w中元素以list的形式返回
     */
    public static List<Double> getW(){
        ArrayList<Double> W = new ArrayList<Double>();
        for(int i = 0; i < LARS.w.numRows();i++ ){
            W.add(LARS.w.get(i));
        }
        return W;
    }
    public static List<List<Double>> getX(){
        ArrayList<List<Double>> listX = new ArrayList<List<Double>>();
        for(int i = 0;i < LARS.X.numCols();i++){
            ArrayList<Double> column = new  ArrayList<Double>();
            for (int j = 0;j < LARS.X.numRows();j++){
                column.add(LARS.X.get(j,i));
            }
            listX.add(column);
        }
        return listX;
    }
    public static void main(String[] args){
        double[][] mk0 = {{0.0014903129657228018}, {0.0014903129657228018}, {0.0}, {0.0}};
        SimpleMatrix m0 = new SimpleMatrix(mk0);
        LARS.initialize(m0,2,2);

        double[][] mk1 = {{0.6014903129657228018}, {0.6014903129657228018}, {0.0}, {0.0}};
        SimpleMatrix m1 = new SimpleMatrix(mk1);
        System.out.println(LARS.calculate(m1));
        System.out.println(getR());

//        LARS.r.print();
//        System.out.println(LARS.getX());
    }
}
