package MainEntry;

import Factory.FormatToIR;
import Framework.Clicks;
import Experiment.Experiment_Para;
import Framework.RWR;
import JDBCUtils.JdbcUtil;
import Structures.*;
import Experiment.*;
import javafx.util.Pair;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Entry {

    public static final Map<String, Double> per = Clicks.ModelGen_2(0.0, 1.0, 0.0);
    public static final Map<String, Double> bin = Clicks.ModelGen_2(1.0, 0.9, 0.1);
    public static final Map<String, Double> rnd = Clicks.ModelGen_2(2.0, 0.6, 0.4);

    public static void initializeKG()
    {
        typeModel.clean();
        graphModel.clean();
        OntologyTree.clean();
        try {
            Connection conn = JdbcUtil.getConnection();
            typeModel.loadTypes(conn);
            graphModel.loadGraph(conn);
            OntologyTree.loadOT(conn); OntologyTree.init();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Initialize KnowledgeGraph End!");
    }

    public static void RWR_Gen(String database)
    {
        RWR rwr = new RWR();
        if(GlobalVariances.Database_name.length() <= 1) { GlobalVariances.SetPara_Database_name(database); initializeKG();}
        rwr.Entry(database);
    }

    public static void Format2IR(String database)
    {
        if(GlobalVariances.Database_name.length() <= 1) { GlobalVariances.SetPara_Database_name(database); initializeKG();}
        FormatToIR.entry(database, 2);
    }

    public static void Experiments_WarmStart(String database)
    {
        Experiment_WarmStart EHS = new Experiment_WarmStart();

        if(GlobalVariances.Database_name.length() <= 1) { GlobalVariances.SetPara_Database_name(database); initializeKG();}
        String[] mtds = {"Method_Stage", "Method_Interleave1", "RelSim", "PRA", "ESER"};
        //String[] mtds = {"Method_Interleave1"};
        //String[] mtds = {"Method_Interleave1", "RelSim", "PRA", "ESER"};
        List<Pair<String, Map<String, Double>>> clicks = new ArrayList<>(); clicks.clear();
        clicks.add(new Pair<>("per", per));
        clicks.add(new Pair<>("bin", bin));
        clicks.add(new Pair<>("rnd", rnd));

        for (Integer mid = 0; mid < mtds.length; ++mid)
            for(Integer click_plicit = 0; click_plicit < 2; ++ click_plicit)
            {
                if (click_plicit.equals(0)) GlobalVariances.Click_xxplicit = "Implicit";
                if (click_plicit.equals(1)) GlobalVariances.Click_xxplicit = "Explicit";
                for (Pair<String, Map<String, Double>> cli : clicks)
                {
                    GlobalVariances.SetPara_ClickModel(cli.getValue(), cli.getKey());
                    EHS.Expe(
                            "WarmStart",
                            mtds[mid],
                            10,
                            GlobalVariances.Click_xxplicit + "_" + cli.getKey(),
                            true
                    );
                }
            }


    }

    public static void Experiments_ColdStart(String database)
    {
        if (GlobalVariances.Database_name.length() <= 1)
        {
            GlobalVariances.SetPara_Database_name(database);
            initializeKG();
        }

        List<Pair<String, Map<String, Double>>> clicks = new ArrayList<>();
        clicks.clear();
        clicks.add(new Pair<>("per", per));
        clicks.add(new Pair<>("bin", bin));
        clicks.add(new Pair<>("rnd", rnd));

        /*

        GlobalVariances.Click_xxplicit = "Explicit";
        for (Integer click_plicit = 0; click_plicit < 2; ++click_plicit)
        {
            for (Pair<String, Map<String, Double>> cli : clicks)
            {
                GlobalVariances.SetPara_ClickModel(cli.getValue(), cli.getKey());
                Experiment.Main.Entry(
                        "ColdStart",
                        "Method_Stage",
                        "LR",
                        10,
                        GlobalVariances.Click_xxplicit + "_" + cli.getKey(),
                        true
                );
            }
        }



        for (Integer click_plicit = 1; click_plicit < 2; ++click_plicit)
        {
            if (click_plicit.equals(0)) GlobalVariances.Click_xxplicit = "Implicit";
            if (click_plicit.equals(1)) GlobalVariances.Click_xxplicit = "Explicit";
            for (Pair<String, Map<String, Double>> cli : clicks)
            {
                GlobalVariances.SetPara_ClickModel(cli.getValue(), cli.getKey());
                Experiment.Main.Entry(
                        "ColdStart",
                        "Method_Stage",
                        "SVM",
                        10,
                        GlobalVariances.Click_xxplicit + "_" + cli.getKey(),
                        true
                );
            }
        }

         */


        //String[] rankmethods = {"Pairwise", "LR", "SVM"};
        String[] rankmethods = {"Pairwise"};
        String[] mtds = {"Method_Interleave1", "Method_Stage"};
        //String[] mtds = {"Method_Stage"};
        for (Integer mid = 0; mid < mtds.length; ++mid)
            if (mtds[mid].contains("Stage"))
            {
                for (Integer click_plicit = 0; click_plicit < 2; ++click_plicit)
                {
                    if (click_plicit.equals(0)) GlobalVariances.Click_xxplicit = "Implicit";
                    if (click_plicit.equals(1)) GlobalVariances.Click_xxplicit = "Explicit";
                    for (Pair<String, Map<String, Double>> cli : clicks)
                    {
                        GlobalVariances.SetPara_ClickModel(cli.getValue(), cli.getKey());
                        Experiment_ColdStart.Entry(
                                "ColdStart",
                                mtds[mid],
                                "Pairwise",
                                10,
                                GlobalVariances.Click_xxplicit + "_" + cli.getKey(),
                                true
                        );
                    }
                }
            } else
            {
                for (Integer rkid = 0; rkid < rankmethods.length; ++ rkid)
                    for (Integer click_plicit = 0; click_plicit < 2; ++click_plicit)
                    {
                        if (click_plicit.equals(0)) GlobalVariances.Click_xxplicit = "Implicit";
                        if (click_plicit.equals(1)) GlobalVariances.Click_xxplicit = "Explicit";
                        for (Pair<String, Map<String, Double>> cli : clicks)
                        {
                            GlobalVariances.SetPara_ClickModel(cli.getValue(), cli.getKey());
                            Experiment_ColdStart.Entry(
                                    "ColdStart",
                                    mtds[mid],
                                    rankmethods[rkid],
                                    10,
                                    GlobalVariances.Click_xxplicit + "_" + cli.getKey(),
                                    true
                            );
                        }
                    }
            }

    }
    public static void UnitTest(String database)
    {
        if(GlobalVariances.Database_name.length() <= 1) { GlobalVariances.SetPara_Database_name(database); initializeKG();}
        GlobalVariances.Click_xxplicit = "Explicit";
        GlobalVariances.SetPara_ClickModel(per, "per");
        /*
        Experiment.Main.Entry(
                "Test",
                "Method_Stage",
                "Pairwise",
                10,
                GlobalVariances.Click_xxplicit + "_" + "rnd",
                true
        );
         */
        Experiment_ColdStart.Entry(
                "Test",
                "Method_Interleave1",
                "Pairwise",
                10,
                GlobalVariances.Click_xxplicit + "_" + "per",
                true
        );
    }

    public static void OtherExps(String database)
    {

        List<Pair<String, Map<String, Double>>> clicks = new ArrayList<>();
        clicks.clear();
        clicks.add(new Pair<>("per", per));
        clicks.add(new Pair<>("bin", bin));
        clicks.add(new Pair<>("rnd", rnd));

        String[] rankmethods = {"Pairwise", "LR", "SVM"};
        String[] mtds = {"Method_Interleave1", "Method_Stage"};
        for (Integer mid = 0; mid < mtds.length; ++mid)
            for (Integer rkid = 0; rkid < rankmethods.length; ++ rkid)
                for (Integer click_plicit = 0; click_plicit < 2; ++click_plicit)
                {
                    if (click_plicit.equals(0)) GlobalVariances.Click_xxplicit = "Implicit";
                    if (click_plicit.equals(1)) GlobalVariances.Click_xxplicit = "Explicit";
                    for (Pair<String, Map<String, Double>> cli : clicks)
                    {
                        GlobalVariances.SetPara_ClickModel(cli.getValue(), cli.getKey());
                        Experiment_Ablation.Entry(
                                "ColdStart",
                                mtds[mid],
                                rankmethods[rkid],
                                10,
                                GlobalVariances.Click_xxplicit + "_" + cli.getKey(),
                                true
                        );
                    }
                }

    }

    public static void main(String[] args)
    {
        String db = "dbpedia";
        if(args.length > 0) System.out.println("Get Argsss : " + args[0]);
        if(args.length > 0 && args[0].equals("dbpedia")) db = "dbpedia";
        if(args.length > 0 && args[0].equals("yago")) db = "yago";

        //UnitTest(db);

        Experiments_WarmStart(db);

        Experiments_ColdStart(db);

        /*

        if(db.equals("dbpedia"))
        {
            if (GlobalVariances.Database_name.length() <= 1)
            {
                GlobalVariances.SetPara_Database_name("dbpedia");
                initializeKG();
            }
            Experiment_Para.Entry("dbpedia");
            OtherExps(db);
        }

        */

    }

}
