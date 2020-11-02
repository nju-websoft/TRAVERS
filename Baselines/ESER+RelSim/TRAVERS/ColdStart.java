package TRAVERS;

import JDBCUtils.JdbcUtil;
import Structures.GlobalVariances;
import Structures.graphModel;
import javafx.util.Pair;

import java.io.*;
import java.util.*;

public class ColdStart
{

    public static Integer PathLen = 0;
    public static Integer Iterations = 0;

    {
        try {
            InputStream in = JdbcUtil.class.getClassLoader()
                    .getResourceAsStream("Interactive.properties");
            Properties properties = new Properties();
            properties.load(in);

            PathLen = Integer.parseInt(properties.getProperty("PathLen"));
            Iterations = Integer.parseInt(properties.getProperty("Iterations"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<Integer, Double> Feature_Weight = new HashMap<>();
    public static Integer bs_size = 0;
    public static Set<Integer> canuse = new HashSet<>();
    public static Integer cs_relinfo_record = 0;
    public static List<Integer> Relinfo_List = new ArrayList<>();
    public static Integer cs_pathinfo_record = 0;
    public static List<Integer> Pathinfo_List = new ArrayList<>();
    public static Integer cs_pathnum_record = 0;
    public static List<Integer> Pathnum_List = new ArrayList<>();
    public static Integer cs_random_record = 0;
    public static List<Integer> Random_List = new ArrayList<>();
    public static Integer cs_rwr_record = 0;
    public static List<Integer> RWR_List = new ArrayList<>();

    public static void init(
            Pair< List<List<Integer>>, Map<Integer, Map<Integer, Integer>> > f
    )
    {
        Feature_Weight = null;
        bs_size = f.getKey().size();
        canuse.clear(); canuse.addAll(f.getValue().keySet());
        Relinfo_List.clear(); cs_relinfo_record = 0;
        Pathinfo_List.clear(); cs_pathinfo_record = 0;
        Pathnum_List.clear(); cs_pathnum_record = 0;
        Random_List.clear(); cs_random_record = 0;
        RWR_List.clear(); cs_rwr_record = 0;
    }

    public static void Adjust(
            Pair<Set<Integer>, Set<Integer>> cr,
            Map<Integer, BitSet> fm
    )
    {
        Integer pos = 0;
        BitSet ub = new BitSet(bs_size);

        Double coeff_neg = GlobalVariances.ColdStart_SC_Adjust_coeff_neg;

        // Positive
        ub.clear();
        for(Integer node : cr.getKey())
            if(!fm.containsKey(node))
                System.out.println("BitSet Error : Center : " + GlobalVariances.Exp_Current_Center + "Node : " + node);
            else ub.or(fm.get(node));
        pos = ub.nextSetBit(0);
        while(pos >= 0)
        {
            Double val = Feature_Weight.get(pos);
            Feature_Weight.put(pos, val / coeff_neg);
            pos = ub.nextSetBit(pos + 1);
        }


        // Negative

        ub.clear();
        for(Integer node : cr.getValue())
            if(!fm.containsKey(node))
                System.out.println("BitSet Error : Center : " + GlobalVariances.Exp_Current_Center + "Node : " + node);
            else ub.or(fm.get(node));
        pos = ub.nextSetBit(0);
        while(pos >= 0)
        {
            Double val = Feature_Weight.get(pos);
            Feature_Weight.put(pos, val * coeff_neg);
            pos = ub.nextSetBit(pos + 1);
        }

        for(Integer node : cr.getValue()) canuse.remove(node);

    }

    public static void SC_init(
            List< List<Integer> > paths,
            Map<Integer, Map<Integer, Integer>> fea,
            Map<Integer, BitSet> fea_bit,
            FeatureGenerator FG
    )
    {
        Feature_Weight = new HashMap<>(); Feature_Weight.clear();
        for(Integer id = 0; id < bs_size; ++ id) Feature_Weight.put(id, 0.0);
        if(GlobalVariances.ColdStart_SC_init_num.equals(1))
        {
            for(Integer id = 0; id < bs_size; ++ id) Feature_Weight.put(id, 1.0);
        }
        if(GlobalVariances.ColdStart_SC_init_num.equals(2))
        {
            for(Map.Entry<Integer, Map<Integer, Integer>> kv : fea.entrySet())
                for(Integer id : kv.getValue().keySet())
                {
                    Double tmp = Feature_Weight.get(id);
                    Feature_Weight.put(id, tmp + 1.0);
                }
        }
        if(GlobalVariances.ColdStart_SC_init_num.equals(3))
        {
            for(Map.Entry<Integer, Map<Integer, Integer>> kv : fea.entrySet())
                for(Integer id : kv.getValue().keySet())
                {
                    Double tmp = Feature_Weight.get(id);
                    Feature_Weight.put(id, tmp + 1.0);
                }
            for(Integer id = 0; id < bs_size; ++ id)
            {
                Double tmp = Feature_Weight.get(id);
                Double sco = Math.log(tmp + 1.0);
                //if(paths.get(id).size() <= 1) sco *= 2.0 / Math.sqrt(GlobalVariances.ColdStart_SC_Adjust_coeff_neg); // sqrt(1 / coeff) * Math.log((tmp + 1.0)^2) for length 1 path
                Feature_Weight.put(id, sco);
            }
        }
        if(GlobalVariances.ColdStart_SC_init_num.equals(4))
        {
            Integer[] cnt = new Integer[3]; cnt[1] = cnt[2] = 0;
            for(Map.Entry<Integer, Map<Integer, Integer>> kv : fea.entrySet())
                for(Integer id : kv.getValue().keySet())
                {
                    Double tmp = Feature_Weight.get(id);
                    Feature_Weight.put(id, tmp + 1.0);
                    cnt[paths.get(id).size()] ++;
                }
            for(Integer id = 0; id < bs_size; ++ id)
            {
                Double tmp = Feature_Weight.get(id);
                Double sco = Math.log(tmp + 1.0);
                for(Integer len = paths.get(id).size() + 1; len <= FG.PathLen; ++ len) sco += Math.log(FG.AvgDegOfLen(len));
                Feature_Weight.put(id, sco);
            }
        }
    }

    public static List<Integer> Coldstart_SC(
            List< List<Integer> > paths,
            Map<Integer, Map<Integer, Integer>> fea,
            Map<Integer, BitSet> fea_bit,
            FeatureGenerator FG,
            Integer topK
    )
    {
        if(null == Feature_Weight) SC_init(paths, fea, fea_bit, FG);

        List<Integer> result = new ArrayList<>(); result.clear();
        Set<Integer> used = new HashSet<>(); used.clear();
        Set<Integer> usedpos = new HashSet<>(); usedpos.clear();
        //System.out.println("!!! " + vis.cardinality() + " " + vis);

        for(Integer id = 1; id <= topK; ++ id)
        {
            // Initialize
            Double std = -1.0; Integer now = 0;

            // Calculate
            for(Integer node : canuse)
            {
                if(used.contains(node)) continue;
                BitSet tmp = fea_bit.get(node);
                Double sum = 0.0;
                Integer pos = tmp.nextSetBit(0);
                while(pos >= 0)
                {
                    if(!usedpos.contains(pos)) sum += Feature_Weight.get(pos);
                    pos = tmp.nextSetBit(pos + 1);
                }
                if(sum > std) {std = sum; now = node;}
            }
            result.add(now); used.add(now);

            // Update
            BitSet tmp = fea_bit.get(now);
            Integer pos = tmp.nextSetBit(0);
            while(pos >= 0)
            {
                usedpos.add(pos);
                pos = tmp.nextSetBit(pos + 1);
            }
        }
        //System.out.print(" " + decifm.format((double)(bs_size - vis.cardinality()) / (double)bs_size));
        return result;
    }

    public static void Random_ListGen(
            List< List<Integer> > paths,
            Map<Integer, Map<Integer, Integer>> fea
    )
    {
        Random_List.clear();
        Random_List.addAll(fea.keySet());
        Collections.shuffle(Random_List);
    }

    public static List<Integer> Coldstart_Random(
            List< List<Integer> > paths,
            Map<Integer, Map<Integer, Integer>> fea,
            Map<Integer, BitSet> fea_bit,
            Integer topK
    )
    {
        if(Random_List.size() <= 0) Random_ListGen(paths, fea);
        List<Integer> result = new ArrayList<>(); result.clear();
        /*for(Integer node : Random_List)
        {
            if(!canuse.contains(node)) continue;
            result.add(node);
            if(result.size() >= topK) break;
        }*/
        while(result.size() < topK)
        {
            result.add(Random_List.get(cs_random_record));
            cs_random_record ++;
            if(cs_random_record + 1 >= Random_List.size()) cs_random_record = 0;
        }
        return result;
    }

    public static void Relinfo_ListGen(
            List< List<Integer> > paths,
            Map<Integer, Map<Integer, Integer>> fea
    )
    {
        List<Double> infos = new ArrayList<>(); infos.clear();
        List< Pair<Integer, Double> > scores = new ArrayList<>(); scores.clear();
        for(Integer id = 0; id < paths.size(); ++ id)
        {
            List<Integer> p = paths.get(id);
            double sum = 0;
            for(Integer rel : p) sum += graphModel.GetEdgeInfo(rel);
            infos.add(sum / (double)p.size());
        }
        for(Map.Entry<Integer, Map<Integer, Integer>> kv : fea.entrySet())
        {
            double sum = 0;
            for(Integer rel : kv.getValue().keySet()) sum += infos.get(rel);
            sum /= (double)kv.getValue().keySet().size();
            scores.add(new Pair<>(kv.getKey(), sum));
        }
        Collections.sort(scores, new Comparator< Pair<Integer, Double> >() {
            public int compare(Pair<Integer, Double> o1,
                               Pair<Integer, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        for(Pair<Integer, Double> kv : scores) Relinfo_List.add(kv.getKey());
    }

    public static List<Integer> Coldstart_RelInfo(
            List< List<Integer> > paths,
            Map<Integer, Map<Integer, Integer>> fea,
            Map<Integer, BitSet> fea_bit,
            Integer topK
    )
    {
        if(Relinfo_List.size() <= 0) Relinfo_ListGen(paths, fea);
        List<Integer> result = new ArrayList<>(); result.clear();
        /*for(Integer node : Relinfo_List)
        {
            if(!canuse.contains(node)) continue;
            result.add(node);
            if(result.size() >= topK) break;
        }*/
        while(result.size() < topK)
        {
            result.add(Relinfo_List.get(cs_relinfo_record));
            cs_relinfo_record ++;
            if(cs_relinfo_record + 1 >= Relinfo_List.size()) cs_relinfo_record = 0;
        }
        return result;
    }

    public static void Pathinfo_ListGen(
            List< List<Integer> > paths,
            Map<Integer, Map<Integer, Integer>> fea
    )
    {
        Integer pathsum = 0;
        Map<Integer, Integer> pathcnt = new HashMap<>(); pathcnt.clear();
        List<Double> infos = new ArrayList<>(); infos.clear();
        List< Pair<Integer, Double> > scores = new ArrayList<>(); scores.clear();
        for(Map.Entry<Integer, Map<Integer, Integer>> f : fea.entrySet())
            for(Map.Entry<Integer, Integer> kv : f.getValue().entrySet())
            {
                Integer rel = kv.getKey();
                if(!pathcnt.containsKey(rel)) pathcnt.put(rel, 0);
                Integer tmp = pathcnt.get(rel);
                pathcnt.put(rel, tmp + kv.getValue());
                pathsum += kv.getValue();
            }
        for(Integer id = 0; id < paths.size(); ++ id)
        {
            Double freq = (double)pathcnt.get(id) / (double)pathsum;
            infos.add(-Math.log(freq));
        }
        for(Map.Entry<Integer, Map<Integer, Integer>> kv : fea.entrySet())
        {
            double sum = 0;
            for(Integer rel : kv.getValue().keySet()) sum += infos.get(rel);
            sum /= (double)kv.getValue().keySet().size();
            scores.add(new Pair<>(kv.getKey(), sum));
        }
        Collections.sort(scores, new Comparator< Pair<Integer, Double> >() {
            public int compare(Pair<Integer, Double> o1,
                               Pair<Integer, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        for(Pair<Integer, Double> kv : scores) Pathinfo_List.add(kv.getKey());
    }

    public static List<Integer> Coldstart_PathInfo(
            List< List<Integer> > paths,
            Map<Integer, Map<Integer, Integer>> fea,
            Map<Integer, BitSet> fea_bit,
            Integer topK
    )
    {
        if(Pathinfo_List.size() <= 0) Pathinfo_ListGen(paths, fea);
        List<Integer> result = new ArrayList<>(); result.clear();
        /*for(Integer node : Pathinfo_List)
        {
            if(!canuse.contains(node)) continue;
            result.add(node);
            if(result.size() >= topK) break;
        }*/
        while(result.size() < topK)
        {
            result.add(Pathinfo_List.get(cs_pathinfo_record));
            cs_pathinfo_record ++;
            if(cs_pathinfo_record + 1 >= Pathinfo_List.size()) cs_pathinfo_record = 0;
        }
        return result;
    }

    public static void Pathnum_ListGen(
            List< List<Integer> > paths,
            Map<Integer, Map<Integer, Integer>> fea
    )
    {
        List< Pair<Integer, Double> > scores = new ArrayList<>(); scores.clear();
        for(Map.Entry<Integer, Map<Integer, Integer>> kv : fea.entrySet())
        {
            Integer sum = 0;
            for(Integer num : kv.getValue().values()) sum += num;
            scores.add(new Pair<>(kv.getKey(), (double)sum));
        }
        Collections.sort(scores, new Comparator< Pair<Integer, Double> >() {
            public int compare(Pair<Integer, Double> o1,
                               Pair<Integer, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        for(Pair<Integer, Double> kv : scores) Pathnum_List.add(kv.getKey());
    }

    public static List<Integer> Coldstart_Pathnum(
            List< List<Integer> > paths,
            Map<Integer, Map<Integer, Integer>> fea,
            Map<Integer, BitSet> fea_bit,
            Integer topK
    )
    {
        if(Pathnum_List.size() <= 0) Pathnum_ListGen(paths, fea);
        List<Integer> result = new ArrayList<>(); result.clear();
        /*for(Integer node : Pathnum_List)
        {
            if(!canuse.contains(node)) continue;
            result.add(node);
            if(result.size() >= topK) break;
        }*/
        while(result.size() < topK)
        {
            result.add(Pathnum_List.get(cs_pathnum_record));
            cs_pathnum_record ++;
            if(cs_pathnum_record + 1 >= Pathnum_List.size()) cs_pathnum_record = 0;
        }
        return result;
    }

    public static Map<Integer, BitSet> MapFromNodeToBitSet(
            Pair< List<List<Integer>>, Map<Integer, Map<Integer, Integer>> > features
    )
    {
        Map<Integer, Map<Integer, Integer>> fea = features.getValue();
        Map<Integer, BitSet> new_fb = new HashMap<>(); new_fb.clear();
        Map< List<Integer>, Integer > new_fea = new HashMap<>(); new_fea.clear();
        Integer new_cnt = 0;

        for(Integer i = 0; i < features.getKey().size(); ++ i)
        {
            List<Integer> fe = new ArrayList<>(); fe.clear(); fe.add(i);
            new_fea.put(fe, new_cnt);
            new_cnt ++;
        }

        for(Integer node : fea.keySet())
        {
            List<Integer> ff = new ArrayList<>(); ff.clear();
            ff.addAll(fea.get(node).keySet()); Collections.sort(ff);
            Integer len = ff.size();
            BitSet bt = new BitSet(); bt.clear();

            // 1
            for(Integer i = 0; i < len; ++ i)
            {
                List<Integer> fe = new ArrayList<>(); fe.clear();
                fe.add(ff.get(i));
                bt.set(new_fea.get(fe));
            }

            new_fb.put(node, bt);
        }

        bs_size = new_cnt;
        return new_fb;
    }

    public static void RWR_ListGen(
            List< List<Integer> > paths,
            Map<Integer, Map<Integer, Integer>> fea
    )
    {
        try
        {
            Scanner sc = new Scanner(new FileInputStream( new File("./datas/Semantic/RWR/" + GlobalVariances.Exp_Current_Group + ".txt")));
            while(sc.hasNextLine())
            {
                String tmp = sc.nextLine();
                tmp = tmp.substring(0, tmp.indexOf(':'));
                if(!tmp.equals(GlobalVariances.Exp_Current_Center.toString())) {tmp = sc.nextLine(); continue;}
                tmp = sc.nextLine();
                String[] nodes = tmp.split("\t");
                for(Integer id = 0; id < nodes.length; ++ id) if(nodes[id].length() >= 1) RWR_List.add(Integer.parseInt(nodes[id]));
                break;
            }
            sc.close();
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public static List<Integer> Coldstart_RWR(
            List< List<Integer> > paths,
            Map<Integer, Map<Integer, Integer>> fea,
            Map<Integer, BitSet> fea_bit,
            Integer topK
    )
    {
        if(RWR_List.size() <= 0) RWR_ListGen(paths, fea);
        List<Integer> result = new ArrayList<>(); result.clear();
        /*for(Integer node : RWR_List)
        {
            if(!canuse.contains(node)) continue;
            result.add(node);
            if(result.size() >= topK) break;
        }*/
        while(result.size() < topK)
        {
            result.add(RWR_List.get(cs_rwr_record));
            cs_rwr_record ++;
            if(cs_rwr_record + 1 >= RWR_List.size()) cs_rwr_record = 0;
        }
        return result;
    }

    public static Integer Times_Count(
            Pair<Integer, Map<Integer, Integer>> data,
            Integer para1, Integer para2,
            Integer topK
    )
    {
        FeatureGenerator FG = new FeatureGenerator(); FG.clear();
        Pair< List<List<Integer>>, Map<Integer, Map<Integer, Integer>> > features =
                FG.getFeatures(data.getKey(), PathLen);
        Map<Integer, BitSet> fea_map = MapFromNodeToBitSet(features);
        init(features);
        List<Integer> cs = new ArrayList<>(); cs.clear();
        Pair<Set<Integer>, Set<Integer>> click_res = null;
        Set<Integer> cumu_Pos = new HashSet<>(); cumu_Pos.clear();
        Set<Integer> cumu_Neg = new HashSet<>(); cumu_Neg.clear();
        if(para1.equals(1)) GlobalVariances.ColdStart_SC_init_num = para2;
        Integer cnt = 0;
        while(cumu_Pos.size() <= 0 && cnt <= 10)
        {
            cnt ++;
            //System.out.println(cnt.toString() + " : ");
            if(para1.equals(1)) cs = Coldstart_SC(features.getKey(), features.getValue(), fea_map, FG, topK);
            if(para1.equals(2)) cs = Coldstart_Random(features.getKey(), features.getValue(), fea_map, topK);
            if(para1.equals(3)) cs = Coldstart_Pathnum(features.getKey(), features.getValue(), fea_map, topK);
            if(para1.equals(4)) cs = Coldstart_PathInfo(features.getKey(), features.getValue(), fea_map, topK);
            if(para1.equals(5)) cs = Coldstart_RelInfo(features.getKey(), features.getValue(), fea_map, topK);
            click_res = Clicks.ClickModel(data.getValue(), cs);
            //cumu_Pos.addAll(click_res.getKey()); cumu_Neg.addAll(click_res.getValue());
            UpdateResult(click_res, cumu_Pos, cumu_Neg);
            if(para1.equals(1)) Adjust(click_res, fea_map);
        }
        //System.out.println(cs.toString());
        //System.out.println(data.getValue().keySet());
        //System.out.println(cumu_Pos.toString());
        //System.out.println();
        //System.out.println(cnt);
        if(cnt > 10) cnt = 100;
        return cnt;
    }

    public static Pair< List<Integer>, Pair< Pair< Set<Integer>, Set<Integer> >, Pair<Set<Integer>, Map< List<Integer>, Double > > > > Gen(
            Pair<Integer, Map<Integer, Integer>> data,
            Integer para1, Integer para2,
            Integer topK
    )
    {
        FeatureGenerator FG = new FeatureGenerator(); FG.clear();
        Pair< List<List<Integer>>, Map<Integer, Map<Integer, Integer>> > features =
                FG.getFeatures(data.getKey(), PathLen);
        Map<Integer, BitSet> fea_map = MapFromNodeToBitSet(features);
        init(features);
        List<Integer> cs = new ArrayList<>(); cs.clear();
        Pair<Set<Integer>, Set<Integer>> click_res = null;
        Set<Integer> cumu_Pos = new HashSet<>(); cumu_Pos.clear();
        Set<Integer> cumu_Neg = new HashSet<>(); cumu_Neg.clear();
        Set<Integer> his_neg = new HashSet<>(); his_neg.clear();
        Map<List<Integer>, Double> scores = new HashMap<>(); scores.clear();
        if(para1.equals(1)) GlobalVariances.ColdStart_SC_init_num = para2;
        Integer cnt = 0;
        while(cumu_Pos.size() <= 0)
        {
            cnt ++;
            //System.out.println(cnt.toString() + " : ");
            if(para1.equals(1)) cs = Coldstart_SC(features.getKey(), features.getValue(), fea_map, FG, topK);
            if(para1.equals(2)) cs = Coldstart_Random(features.getKey(), features.getValue(), fea_map, topK);
            if(para1.equals(3)) cs = Coldstart_Pathnum(features.getKey(), features.getValue(), fea_map, topK);
            if(para1.equals(4)) cs = Coldstart_PathInfo(features.getKey(), features.getValue(), fea_map, topK);
            if(para1.equals(5)) cs = Coldstart_RelInfo(features.getKey(), features.getValue(), fea_map, topK);
            if(para1.equals(6)) cs = Coldstart_RWR(features.getKey(), features.getValue(), fea_map, topK);
            //if(cnt > 1) his_neg.addAll(click_res.getValue());
            click_res = Clicks.ClickModel(data.getValue(), cs);
            //cumu_Pos.addAll(click_res.getKey()); cumu_Neg.addAll(click_res.getValue());
            UpdateResult(click_res, cumu_Pos, cumu_Neg);
            if(para1.equals(1)) Adjust(click_res, fea_map);
        }
        if(para1.equals(1))
        {
            for (Map.Entry<Integer, Double> kv : Feature_Weight.entrySet())
                scores.put(features.getKey().get(kv.getKey()), kv.getValue());
        }
        return new Pair<>(cs, new Pair<>(click_res, new Pair(his_neg, scores)));
    }

    public static void UpdateResult(Pair<Set<Integer>, Set<Integer>> click_res, Set<Integer> cumu_pos, Set<Integer> cumu_neg)
    {
        Set<Integer> need_del = new HashSet<>(); need_del.clear();
        for(Integer node : cumu_pos) if(click_res.getKey().contains(node) || click_res.getValue().contains(node)) need_del.add(node);
        for(Integer node : need_del) cumu_pos.remove(node);
        need_del.clear();
        for(Integer node : cumu_neg) if(click_res.getKey().contains(node) || click_res.getValue().contains(node)) need_del.add(node);
        for(Integer node : need_del) cumu_neg.remove(node);
        need_del.clear();
        cumu_pos.addAll(click_res.getKey()); cumu_neg.addAll(click_res.getValue());
    }
}
