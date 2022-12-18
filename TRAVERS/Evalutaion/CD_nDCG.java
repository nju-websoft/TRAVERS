package Evalutaion;

import Structures.GlobalVariances;

import java.util.List;

public class CD_nDCG
{
    public static Double calculate(List<Double> ndcgs)
    {
        Double sum = 0.0;
        Double base = 1.0;
        for(Double val : ndcgs)
        {
            sum += base * val;
            base *= GlobalVariances.CumulativeDiscountCoefficient;
        }
        return sum;
    }
}
