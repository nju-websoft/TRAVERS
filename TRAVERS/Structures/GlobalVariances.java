package Structures;

import java.util.*;

public class GlobalVariances
{
    public static String Database_name = "";
    public static Map<String, Double> ClickModel = new HashMap<>();
    public static String ClickModel_Name = "";
    public static String Click_xxplicit = "Implicit";

    public static void SetPara_Database_name(String name) {Database_name = name;}

    public static void SetPara_ClickModel(Map<String, Double> cm, String nm)
    {
        ClickModel_Name = nm;
        ClickModel.clear();
        for(Map.Entry<String, Double> kv : cm.entrySet())
            ClickModel.put(kv.getKey(), kv.getValue());
    }

    public static Integer ColdStart_SC_init_num = 4;

    public static Double ColdStart_SC_Adjust_coeff_neg = 0.01;

    public static Integer repeat_times = 20;

    public static String Exp_Current_Group = "";
    public static Integer Exp_Current_Center = 0;

    public static Double Interleave_ini = 0.5;
    public static Double Interleave_lambda = 0.6;
    public static Double Interleave_epsilon_upper = 0.6;
    public static Double Interleave_epsilon_lower = 0.1;

    public static Boolean Exp_Ordered_To_Bool = true;

    public static Set<String> Evaluation = new HashSet<>(Arrays.asList("nDCG", "alpha-nDCG"));
    public static List<String> Evaluation_List = Arrays.asList("nDCG", "alpha-nDCG");

    public static Double CumulativeDiscountCoefficient = 0.6;
}
