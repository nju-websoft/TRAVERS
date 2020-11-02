package Factory;

import Framework.FeatureGenerator;
import Structures.fileModel;
import Structures.pairModel;
import javafx.util.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class FormatToIR
{
    public static final String ou_prefix = "./datas/IR/";

    public static void doit(String groupname, Integer number, Integer pathlen)
    {
        List<Pair<Integer, Map<Integer, Integer>>> ret = ReadInput.ParseData(groupname, number);
        fileModel.CreateFolder(ou_prefix + groupname + "/");
        FileWriter[] fw = new FileWriter[4];
        try {
            Integer max_f = 0;
            List< Pair<Integer, List<Integer>> > samples = ReadInput.ParseColdStart_Sample(groupname, number);
            for(Integer id = 1; id <= number; ++ id)
            {
                String folds = ou_prefix + groupname + "/Fold" + id.toString() + "/";
                fileModel.CreateFolder(folds);
                List<Integer> sample = samples.get(id - 1).getValue();
                fw[0] = new FileWriter(folds + "train.txt");
                fw[1] = new FileWriter(folds + "vali.txt");
                fw[2] = new FileWriter(folds + "test.txt");
                fw[3] = new FileWriter(folds + "cold.txt");
                Integer center = ret.get(id - 1).getKey();
                Map<Integer, Integer> ans = ret.get(id - 1).getValue();
                FeatureGenerator FG = new FeatureGenerator(); FG.clear();
                Pair< List< List<Integer> >, Map<Integer, Map<Integer, Integer>> > fs = FG.getFeatures(center, pathlen);
                max_f = Math.max(max_f, fs.getKey().size());
                Integer docid = -1;
                Map<Integer, Integer> node2doc = new HashMap<>(); node2doc.clear();
                for(Map.Entry<Integer, Map<Integer, Integer>> tmp : fs.getValue().entrySet())
                {
                    Integer node = tmp.getKey();
                    docid ++; node2doc.put(node, docid);
                    Integer sco = 0;
                    if(ans.containsKey(node)) sco = ans.get(node);
                    if(sco > 0) sco = 1;
                    for(Integer ind = 0; ind <= 2; ++ ind)
                    {
                        fw[ind].write(sco.toString() + " ");
                        fw[ind].write("qid:1 ");
                        List<pairModel> list = new ArrayList<>(); list.clear();
                        for(Map.Entry<Integer, Integer> pm : tmp.getValue().entrySet())
                            list.add(new pairModel(pm.getKey() + 1, pm.getValue()));
                        Collections.sort(list,new Comparator<pairModel>() {
                            public int compare(pairModel o1,
                                               pairModel o2) {
                                return o1.valX.compareTo(o2.valX);
                            }
                        });
                        for(pairModel p : list) fw[ind].write(p.valX.toString() + ":" + p.valY.toString() + " ");
                        fw[ind].write("#\r\n");
                    }
                }
                for(Integer node : sample) fw[3].write(node2doc.get(node).toString() + "\r\n");
                for(Integer ind = 0; ind <= 3; ++ ind) fw[ind].close();
            }
            System.out.println(groupname + " : " + max_f.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void entry(String database, Integer len)
    {
        String[] suf = {"11b", "21b", "21o", "22b", "rs1b", "rs2b", "rs3b", "rs4b", "rs5b"};
        for(Integer id = 0; id < suf.length; ++ id) doit(database + "_" + suf[id], 10, len);
    }
}
