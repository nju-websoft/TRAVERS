package Factory;

import Structures.GlobalVariances;
import Structures.fileModel;
import Structures.graphModel;
import Structures.typeModel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphStatistics
{
    public static void EdgeCount()
    {
        String fold = "./datas/GraphInformation/" + GlobalVariances.Database_name + "/";
        fileModel.CreateFolder(fold);
        try
        {
            FileWriter fw = new FileWriter(new File(fold + "EdgeCount.txt"));

            Map<Integer, Integer> cnt = new HashMap<>(); cnt.clear();
            Integer sum = 0;
            for(Integer node = 1; node <= typeModel.EntityCnt; ++ node)
            {
                HashMap<Integer, List<Integer>> neigh = graphModel.getNeighbours(node);
                if(null == neigh) continue;
                for (HashMap.Entry<Integer, List<Integer>> kv : neigh.entrySet())
                {
                    Integer rela = kv.getKey();
                    if(rela < 0) continue;
                    Integer num = kv.getValue().size();
                    if(!cnt.containsKey(rela)) cnt.put(rela, 0);
                    Integer tmp = cnt.get(rela);
                    cnt.put(rela, tmp + num);
                    sum += num;
                }
            }
            fw.write("All:" + sum.toString() + "\r\n");
            for(Map.Entry<Integer, Integer> kv : cnt.entrySet())
                fw.write(kv.getKey().toString() + ":" + kv.getValue().toString() + "\r\n");
            fw.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
