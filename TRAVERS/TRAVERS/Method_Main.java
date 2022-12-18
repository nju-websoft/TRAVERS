package TRAVERS;

import JDBCUtils.JdbcUtil;
import SVM.svm_scale;
import SVM.svm_train;
import Structures.GlobalVariances;
import Structures.LogisticRegression;
import Structures.TimeCounter;
import Structures.fileModel;
import javafx.util.Pair;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class Method_Main
{

    public final DecimalFormat decifm = new DecimalFormat("#0.00");

    public Integer PathLen = 0;
    public Integer topK = 0;
    public Integer Iterations = 0;

    public FeatureGenerator FG = new FeatureGenerator();

    public Double eps = 0.00001;

    public BitSet Classifier_Features = new BitSet();

    {
        try {
            InputStream in = JdbcUtil.class.getClassLoader()
                    .getResourceAsStream("Interactive.properties");
            Properties properties = new Properties();
            properties.load(in);

            PathLen = Integer.parseInt(properties.getProperty("PathLen"));
            topK = Integer.parseInt(properties.getProperty("TOPK"));
            Iterations = Integer.parseInt(properties.getProperty("Iterations"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Pair< List<List<Integer>>, Map<Integer, Map<Integer, Integer>> > features;
    public Map<Integer, BitSet> fea_map = new HashMap<>();
    public Integer bs_size = 0;
    public Set<Integer> cumu_Pos = new HashSet<>();
    public Set<Integer> cumu_Neg = new HashSet<>();
    public Boolean hasRight = false;
    public Double Interleave_P = 0.0;

    public void clear()
    {
        features = null;
        fea_map.clear();
        bs_size = 0;
        cumu_Neg.clear(); cumu_Pos.clear();
        hasRight = false;
        Classifier_Features.clear();
        for(Integer i = 0; i < 3; ++ i) sum_his[i] = cnt_his[i] = 0.0;
    }

    public Map<Integer, BitSet> MapFromNodeToBitSet()
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

        bs_size = new_cnt;

        for(Integer node : fea.keySet())
        {
            List<Integer> ff = new ArrayList<>(); ff.clear();
            ff.addAll(fea.get(node).keySet()); Collections.sort(ff);
            Integer len = ff.size();
            BitSet bt = new BitSet(bs_size); bt.clear();

            // 1
            for(Integer i = 0; i < len; ++ i)
            {
                List<Integer> fe = new ArrayList<>(); fe.clear();
                fe.add(ff.get(i));
                bt.set(new_fea.get(fe));
            }

            new_fb.put(node, bt);
        }

        return new_fb;
    }

    public List<Map<String, Double>> Stage(
            Pair<Integer, Map<Integer, Integer>> data,
            String csmethod,
            String rankmethod,
            TimeCounter TC
    )
    {
        // Init
        clear();
        List<Map<String, Double>> ret = new ArrayList<>(); ret.clear();
        features = FG.getFeatures(data.getKey(), PathLen);
        fea_map = MapFromNodeToBitSet();
        Classifier_Features = new BitSet(bs_size);
        ColdStart.init(features);
        List<Integer> cs = new ArrayList<>(); cs.clear();
        Pair<Set<Integer>, Set<Integer>> click_res = null;

        for(Integer ite = 0; ite <= Iterations; ++ ite)
        {
            long bg_time = System.currentTimeMillis();

            if(cumu_Pos.size() <= 0)
            {
                if(csmethod.equals("SC"))cs = ColdStart.Coldstart_SC(features.getKey(), features.getValue(), fea_map, FG, topK);
                if(csmethod.equals("RD"))cs = ColdStart.Coldstart_Random(features.getKey(), features.getValue(), fea_map, topK);
                if(csmethod.equals("RI"))cs = ColdStart.Coldstart_RelInfo(features.getKey(), features.getValue(), fea_map, topK);
                if(csmethod.equals("PI"))cs = ColdStart.Coldstart_PathInfo(features.getKey(), features.getValue(), fea_map, topK);
                if(csmethod.equals("PN"))cs = ColdStart.Coldstart_Pathnum(features.getKey(), features.getValue(), fea_map, topK);
                if(csmethod.equals("RWR"))cs = ColdStart.Coldstart_RWR(features.getKey(), features.getValue(), fea_map, topK);
            }
            else
            {
                if(rankmethod.equals("Pairwise")) cs = Rerank_Pairwise(features.getValue(), fea_map, topK, cumu_Pos, cumu_Neg);
                if(rankmethod.equals("LR")) cs = Rerank_LR(features.getValue(), fea_map, topK, cumu_Pos, cumu_Neg);
                if(rankmethod.equals("SVM")) cs = Rerank_SVM(features.getValue(), fea_map, topK, cumu_Pos, cumu_Neg);
            }
            click_res = Clicks.ClickModel(data.getValue(), cs);
            ColdStart.UpdateResult(click_res, cumu_Pos, cumu_Neg);
            for(Integer node : click_res.getKey()) Classifier_Features.or(fea_map.get(node));
            Map<String, Double> tmp;
            tmp = new HashMap<>(); tmp.clear();
            if(GlobalVariances.Evaluation.contains("nDCG")) tmp.put("nDCG", Evalutaion.CalNDCG.calculateNDCG(cs, data.getValue(), topK));
            if(GlobalVariances.Evaluation.contains("alpha-nDCG")) tmp.put("alpha-nDCG", Evalutaion.alpha_nDCG.Calculate(cs, topK));
            ret.add(tmp);
            if(csmethod.equals("SC"))ColdStart.Adjust(click_res, fea_map);

            long ed_time = System.currentTimeMillis();
            double timecost = (ed_time - bg_time) / 1000.0;
            TC.addTime(ite.toString(), timecost);
        }

        return ret;
    }

    public List<Integer> GetBitSetPos(BitSet bs)
    {
        List<Integer> pos = new ArrayList<>(); pos.clear();
        Integer p = bs.nextSetBit(0);
        while(p >= 0)
        {
            pos.add(p);
            p = bs.nextSetBit(p + 1);
        }
        //System.out.println("Featuress : " + bs.cardinality());
        return pos;
    }

    public Integer GetMapValue(Map<Integer, Integer> mp, Integer key)
    {
        if(!mp.containsKey(key)) return 0; else return mp.get(key);
    }

    public Double calculate_score(
            Map<Integer, Map<Integer, Integer>> fea,
            List<Integer> label_pos,
            List<Double> weights,
            Integer node
    )
    {
        if(node.equals(GlobalVariances.Exp_Current_Center)) return 0.0;
        Double res = 0.0;
        for(Integer id = 0; id < label_pos.size(); ++ id)
        {
            Double f = (double)GetMapValue(fea.get(node), label_pos.get(id));
            Double w = weights.get(id);
            res += f * w;
        }
        return res;
    }

    public Double Score(Double sco_1, Double sco_2, Double num)
    {
        Double ret = 0.0;

        ret = sco_1 + sco_2 * (Math.exp(num) - 1.0);

        return ret;
    }

    public List<Integer> Rerank_LR(
            Map<Integer, Map<Integer, Integer>> fea,
            Map<Integer, BitSet> fea_bit,
            Integer topK,
            Set<Integer> cumu_Pos,
            Set<Integer> cumu_Neg
    )
    {
        List<Integer> result = new ArrayList<>(); result.clear();

        List<Integer> label_pos = GetBitSetPos(Classifier_Features);
        fileModel.CreateFolder("./datas/Method_Main(Classifier)/");

        // Classifier Data Gen
        Set<Integer> cp = new HashSet<>(); cp.clear();
        Set<Integer> cn = new HashSet<>(); cn.clear();
        cp.addAll(cumu_Pos); cn.addAll(cumu_Neg);
        cp.remove(GlobalVariances.Exp_Current_Center); cn.remove(GlobalVariances.Exp_Current_Center);
        if(cn.size() <= 0)
        {
            List<Integer> tmp = new ArrayList<>(); tmp.clear();
            tmp.addAll(fea.keySet());
            Random rd = new Random();
            while(cn.size() <= topK)
            {
                Integer nd = tmp.get(rd.nextInt(tmp.size()));
                while(cn.contains(nd)) nd = tmp.get(rd.nextInt(tmp.size()));
                cn.add(nd);
            }
        }

        List< List<Integer> > pos = new ArrayList<>(); pos.clear();
        for(Integer posi_node : cp)
        {
            if(posi_node.equals(GlobalVariances.Exp_Current_Center)) continue;
            Map<Integer, Integer> pmap = fea.get(posi_node);
            List<Integer> feat = new ArrayList<>(); feat.clear();
            for(Integer id = 0; id < label_pos.size(); ++ id )
            {
                Integer p = label_pos.get(id);
                Integer val = GetMapValue(pmap, p);
                feat.add(val);
            }
            pos.add(feat);
        }
        List< List<Integer> > neg = new ArrayList<>(); neg.clear();
        for(Integer nega_node : cn)
        {
            if(nega_node.equals(GlobalVariances.Exp_Current_Center)) continue;
            Map<Integer, Integer> nmap = fea.get(nega_node);
            List<Integer> feat = new ArrayList<>(); feat.clear();
            for(Integer id = 0; id < label_pos.size(); ++ id )
            {
                Integer p = label_pos.get(id);
                Integer val = GetMapValue(nmap, p);
                feat.add(val);
            }
            neg.add(feat);
        }

        // Parse Model File
        LogisticRegression LR = new LogisticRegression();
        List<Double> weights = LR.Sovle(pos, neg);
        // List<Double> weights = LinearProgramming.getWeights(pos, neg);
        if(!((Integer)weights.size()).equals((Integer)label_pos.size())) System.out.println("Weights Length Problem!");
        //Double coef_of_l2r = calculate_coefficient(fea, cumu_Pos, cumu_Neg, label_pos, weights);

        // Sort
        Map<Integer, Double> scomp = new HashMap<>(); scomp.clear();
        for(Integer node : fea.keySet())
        {
            BitSet node_bs = (BitSet) fea_bit.get(node).clone(); node_bs.and(Classifier_Features);
            Double sco_1 = (double) node_bs.cardinality();
            Double sco_2 = calculate_score(fea, label_pos, weights, node);
            scomp.put(node, Score(sco_1, sco_2, (double)cumu_Pos.size()));
        }
        List< Map.Entry<Integer, Double> > list = new ArrayList<>(); list.clear();
        list.addAll(scomp.entrySet()); Collections.shuffle(list);
        Collections.sort(list, new Comparator< Map.Entry<Integer, Double> >() {
            @Override
            public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        for(Integer i = 0; i < topK && i < list.size(); ++ i)
            result.add(list.get(i).getKey());
        while(result.size() < topK) result.add(0);
        return result;
    }

    public List<Double> getWeights(String modelFile, int numOfFeatures){
        double []weights = new double[numOfFeatures];
        File file = new File(modelFile);
        FileReader fr = null;
        BufferedReader reader = null;

        try {
            fr = new FileReader(file);
            reader = new BufferedReader(fr);
            String str = null;
            int line = 0;
            while((str = reader.readLine()) != null){
                line ++;
                if(line > 9){
                    String[] ss = str.split(" ");
                    double alpha = Double.parseDouble(ss[0]);
                    for(int i = 0; i < numOfFeatures; i ++){
                        String f = ss[i + 1];
                        double va = Double.parseDouble(f.split(":")[1]);
                        weights[i] = weights[i] + va*alpha;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        List<Double> result = new ArrayList<>();
        for(double w : weights){
            if(w > 0)
                result.add(w);
            else
                result.add(0.);
        }

        return  result;
    }

    public List<Integer> Rerank_SVM(
            Map<Integer, Map<Integer, Integer>> fea,
            Map<Integer, BitSet> fea_bit,
            Integer topK,
            Set<Integer> cumu_Pos,
            Set<Integer> cumu_Neg
    )
    {
        List<Integer> result = new ArrayList<>(); result.clear();

        List<Integer> label_pos = GetBitSetPos(Classifier_Features);
        fileModel.CreateFolder("./datas/Method_Main(Classifier)/");

        Set<Integer> cp = new HashSet<>(); cp.clear();
        Set<Integer> cn = new HashSet<>(); cn.clear();
        cp.addAll(cumu_Pos); cn.addAll(cumu_Neg);
        cp.remove(GlobalVariances.Exp_Current_Center); cn.remove(GlobalVariances.Exp_Current_Center);
        if(cn.size() <= 0)
        {
            List<Integer> tmp = new ArrayList<>(); tmp.clear();
            tmp.addAll(fea.keySet());
            Random rd = new Random();
            while(cn.size() <= topK)
            {
                Integer nd = tmp.get(rd.nextInt(tmp.size()));
                while(cn.contains(nd)) nd = tmp.get(rd.nextInt(tmp.size()));
                cn.add(nd);
            }
        }

        // Classifier Data Gen
        try
        {
            FileWriter fw = new FileWriter(new File("./datas/Method_Main(Classifier)/Train.txt"));

            // Positive
            for(Integer node : cp)
            {
                if(node.equals(GlobalVariances.Exp_Current_Center)) continue;
                fw.write("1");
                for(Integer i = 0; i < label_pos.size(); ++ i)
                {
                    Integer id = i + 1;
                    Integer val = GetMapValue(fea.get(node), label_pos.get(i));
                    fw.write(" " + id.toString() + ":" + val.toString());
                }
                fw.write("\r\n");
            }

            // Negative
            for(Integer node : cn)
            {
                if(node.equals(GlobalVariances.Exp_Current_Center)) continue;
                fw.write("0");
                for(Integer i = 0; i < label_pos.size(); ++ i)
                {
                    Integer id = i + 1;
                    Integer val = GetMapValue(fea.get(node), label_pos.get(i));
                    fw.write(" " + id.toString() + ":" + val.toString());
                }
                fw.write("\r\n");
            }

            fw.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        try
        {
            svm_scale s1= new svm_scale();
            String[] s1arg = {"./datas/Method_Main(Classifier)/Scale.txt" , "./datas/Method_Main(Classifier)/Train.txt"};
            s1.main(s1arg);

            svm_train t = new svm_train();
            String[] targ = { //"-s","0","-t","2",
                              "-q",
                              "./datas/Method_Main(Classifier)/Scale.txt",
                              "./datas/Method_Main(Classifier)/Model.txt"};
            t.main(targ);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        // Parse Model File
        List<Double> weights = getWeights("./datas/Method_Main(Classifier)/Model.txt", label_pos.size());
        // List<Double> weights = LinearProgramming.getWeights(pos, neg);
        if(!((Integer)weights.size()).equals((Integer)label_pos.size())) System.out.println("Weights Length Problem!");
        //Double coef_of_l2r = calculate_coefficient(fea, cumu_Pos, cumu_Neg, label_pos, weights);

        // Sort
        Map<Integer, Double> scomp = new HashMap<>(); scomp.clear();
        for(Integer node : fea.keySet())
        {
            BitSet node_bs = (BitSet) fea_bit.get(node).clone(); node_bs.and(Classifier_Features);
            Double sco_1 = (double) node_bs.cardinality();
            Double sco_2 = calculate_score(fea, label_pos, weights, node);
            scomp.put(node, Score(sco_1, sco_2, (double)cumu_Pos.size()));
        }
        List< Map.Entry<Integer, Double> > list = new ArrayList<>(); list.clear();
        list.addAll(scomp.entrySet()); Collections.shuffle(list);
        Collections.sort(list, new Comparator< Map.Entry<Integer, Double> >() {
            @Override
            public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        for(Integer i = 0; i < topK && i < list.size(); ++ i)
            result.add(list.get(i).getKey());
        while(result.size() < topK) result.add(0);
        return result;
    }

    public List<Integer> Rerank_Pairwise(
            Map<Integer, Map<Integer, Integer>> fea,
            Map<Integer, BitSet> fea_bit,
            Integer topK,
            Set<Integer> cumu_Pos,
            Set<Integer> cumu_Neg
    )
    {
        List<Integer> result = new ArrayList<>(); result.clear();

        List<Integer> label_pos = GetBitSetPos(Classifier_Features);
        fileModel.CreateFolder("./datas/Method_Main(Classifier)/");

        // Classifier Data Gen
        try
        {
            FileWriter fw = new FileWriter(new File("./datas/Method_Main(Classifier)/Train.txt"));

            //System.out.println("Pos : " + ((Integer)cumu_Pos.size()).toString() + " --- Neg : " + ((Integer)cumu_Neg.size()).toString());

            Set<Integer> cp = new HashSet<>(); cp.clear();
            Set<Integer> cn = new HashSet<>(); cn.clear();
            cp.addAll(cumu_Pos); cn.addAll(cumu_Neg);
            cp.remove(GlobalVariances.Exp_Current_Center); cn.remove(GlobalVariances.Exp_Current_Center);
            if(cn.size() <= 0)
            {
                List<Integer> tmp = new ArrayList<>(); tmp.clear();
                tmp.addAll(fea.keySet());
                Random rd = new Random();
                while(cn.size() <= topK)
                {
                    Integer nd = tmp.get(rd.nextInt(tmp.size()));
                    while(cn.contains(nd)) nd = tmp.get(rd.nextInt(tmp.size()));
                    cn.add(nd);
                }
            }

            for(Integer pos : cp)
                for(Integer neg : cn)
                {
                    if(pos.equals(GlobalVariances.Exp_Current_Center) || neg.equals(GlobalVariances.Exp_Current_Center)) continue;
                    fw.write("1");
                    for(Integer i = 0; i < label_pos.size(); ++ i)
                    {
                        Integer id = i + 1;
                        Integer pval = GetMapValue(fea.get(pos), label_pos.get(i));
                        Integer nval = GetMapValue(fea.get(neg), label_pos.get(i));
                        Integer cha = pval - nval;
                        fw.write(" " + id.toString() + ":" + cha.toString());
                    }
                    fw.write("\r\n");

                    fw.write("0");
                    for(Integer i = 0; i < label_pos.size(); ++ i)
                    {
                        Integer id = i + 1;
                        Integer pval = GetMapValue(fea.get(pos), label_pos.get(i));
                        Integer nval = GetMapValue(fea.get(neg), label_pos.get(i));
                        Integer cha = nval - pval;
                        fw.write(" " + id.toString() + ":" + cha.toString());
                    }
                    fw.write("\r\n");
                }

            fw.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        try
        {
            svm_scale s1= new svm_scale();
            String[] s1arg = {"./datas/Method_Main(Classifier)/Scale.txt" , "./datas/Method_Main(Classifier)/Train.txt"};
            s1.main(s1arg);

            svm_train t = new svm_train();
            String[] targ = { //"-s","0","-t","2",
                    "-q",
                    "./datas/Method_Main(Classifier)/Scale.txt",
                    "./datas/Method_Main(Classifier)/Model.txt"};
            t.main(targ);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        // Parse Model File
        List<Double> weights = getWeights("./datas/Method_Main(Classifier)/Model.txt", label_pos.size());
        // List<Double> weights = LinearProgramming.getWeights(pos, neg);
        if(!((Integer)weights.size()).equals((Integer)label_pos.size())) System.out.println("Weights Length Problem!");
        //Double coef_of_l2r = calculate_coefficient(fea, cumu_Pos, cumu_Neg, label_pos, weights);

        // Sort
        Map<Integer, Double> scomp = new HashMap<>(); scomp.clear();
        for(Integer id = 0; id < label_pos.size(); ++ id)
        {
            Integer rela = label_pos.get(id);
            for (Integer node : FG.rela2node.get(rela))
            {
                if (!scomp.containsKey(node)) scomp.put(node, 0.0);
                Double now = scomp.get(node);
                now += weights.get(id) * fea.get(node).get(rela);
                scomp.put(node, now);
            }
        }
        for(Integer node : scomp.keySet())
        {
            BitSet node_bs = (BitSet) fea_bit.get(node).clone(); node_bs.and(Classifier_Features);
            Double sco_1 = (double) node_bs.cardinality();
            Double sco_2 = scomp.get(node);
            scomp.put(node, Score(sco_1, sco_2, (double)cumu_Pos.size()));
        }
        //System.out.println("New Candidates : " + scomp.size());
        List< Map.Entry<Integer, Double> > list = new ArrayList<>(); list.clear();
        list.addAll(scomp.entrySet());
        Collections.sort(list, new Comparator< Map.Entry<Integer, Double> >() {
            @Override
            public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        for(Integer i = 0; i < topK && i < list.size(); ++ i)
            result.add(list.get(i).getKey());
        while(result.size() < topK) result.add(0);
        return result;
    }

    /*
    public Double calculate_coefficient(
            Map<Integer, Map<Integer, Integer>> fea,
            Set<Integer> cumu_Pos,
            Set<Integer> cumu_Neg,
            List<Integer> label_pos,
            List<Double> weights
    )
    {
        Integer cnt = 0;
        for(Integer posi_node : cumu_Pos)
            for(Integer nega_node : cumu_Neg)
            {
                Double score_pos = calculate_score(fea, label_pos, weights, posi_node);
                Double score_neg = calculate_score(fea, label_pos, weights, nega_node);
                if(score_pos < score_neg) cnt ++;
            }
        if((cnt << 1) > cumu_Pos.size() * cumu_Neg.size() ) return -1.0; else return 1.0;
    }
    */

    public Pair< List<Integer>, List<Integer> > Interleave1(
            List<Integer> res_cs, List<Integer> res_rr,
            Double p // p is the possbility of choosing from res_cs
    )
    {
        List<Integer> res = new ArrayList<>(); res.clear();
        List<Integer> from = new ArrayList<>(); from.clear();
        if(null == res_rr)
        {
            for(Integer node : res_cs)
            {
                res.add(node);
                from.add(1);
            }
            return new Pair<>(res, from);
        }
        Integer id_cs = 0, id_rr = 0;
        Integer target_cs = Math.toIntExact(Math.round(Interleave_P * (double) topK));
        Integer target_rr = topK - target_cs;
        Random rd = new Random();
        Integer bg = 0;
        for(Integer id = 1; id <= topK; ++ id)
        {
            bg = 1 - bg;
            if ((bg.equals(0) && target_cs > 0) || target_rr.equals(0) || res_rr.get(id_rr).equals(0))
            {
                while (res.contains(res_cs.get(id_cs))) ++ id_cs;
                target_cs --; res.add(res_cs.get(id_cs)); from.add(1);
            }else
            {
                while (res_rr.get(id_rr) > 0 && res.contains(res_rr.get(id_rr))) ++ id_rr;
                if(res_rr.get(id_rr).equals(0)) {id --; continue;}
                target_rr --; res.add(res_rr.get(id_rr)); from.add(2);
            }
        }
        /*
        while(res.size() < topK)
        {
            double poss = rd.nextDouble();
            if(poss <= p)
            {
                while(res.contains(res_cs.get(id_cs))) ++ id_cs;
                res.add(res_cs.get(id_cs)); from.add(1);
            }else
            {
                while(res.contains(res_rr.get(id_rr))) ++ id_rr;
                res.add(res_rr.get(id_rr)); from.add(2);
            }
        }
        */
        return new Pair<>(res, from);
    }

    public double[] sum_his = new double[3];
    public double[] cnt_his = new double[3];

    public void CHANGE_P(Double coeff, Double para1, Double para2)
    {
        Double tmp = coeff * GlobalVariances.Interleave_lambda * Math.pow(Math.E, para1) / Math.pow(Math.E, para2);
        Interleave_P += tmp;
        Interleave_P = Math.max(Interleave_P, GlobalVariances.Interleave_epsilon_lower);
        Interleave_P = Math.min(Interleave_P, GlobalVariances.Interleave_epsilon_upper);
        //Interleave_P = 0.95;
    }

    public Double Get_Interleave_Metric(
            Pair<Set<Integer>, Set<Integer>> click_res,
            Pair< List<Integer>, List<Integer> > inter_res,
            Integer label
    )
    {
        double ret = 0.0;
        double cnt = 0.0;
        for(Integer node : click_res.getKey())
        {
            Integer pos = inter_res.getKey().indexOf(node);
            if(inter_res.getValue().get(pos).equals( label )) ret += 1.0;
        }
        for(Integer lb : inter_res.getValue()) if(lb.equals(label)) cnt += 1.0;
        /*
        if(cnt < 0.5) return 1.0 / (double)topK;
        else
        {
            sum_his[label] += ret / cnt;
            cnt_his[label] += 1.0;
            return ret / cnt;
        }
        */
        return ret;
    }

    public void Adjust_P(
            Pair<Set<Integer>, Set<Integer>> click_res,
            Pair< List<Integer>, List<Integer> > inter_res
    )
    {
        Double[] score = new Double[3];
        score[1] = score[2] = 0.0;
        for(Integer id = 1; id <= 2; ++ id) score[id] = Get_Interleave_Metric(click_res, inter_res, id);
        Double score_max = score[1] + score[2];
        Double coeff = 0.0;
        if(Math.abs(score[1] - score[2]) < eps) coeff = 0.0; else coeff = (score[1] - score[2]) / Math.abs(score[1] - score[2]);
        CHANGE_P(coeff, Math.abs(score[1] - score[2]), score_max);
        //System.out.print(decifm.format(Interleave_P) + "(" + decifm.format(score[1]) + "-" + decifm.format(score[2]) + ")  ");
    }

    public List<Map<String, Double>> Solve_Interleave1(
            Pair<Integer, Map<Integer, Integer>> data,
            String csmethod,
            String rankmethod,
            TimeCounter TC
    )
    {
        // Init
        clear();
        List<Map<String, Double>> ret = new ArrayList<>(); ret.clear();
        features = FG.getFeatures(data.getKey(), PathLen);
        fea_map = MapFromNodeToBitSet();
        Classifier_Features = new BitSet(bs_size);
        //System.out.println("Nodes : " + features.getValue().size());
        ColdStart.init(features);
        List<Integer> cs = new ArrayList<>(); cs.clear();
        Pair<Set<Integer>, Set<Integer>> click_res = null;
        Interleave_P = GlobalVariances.Interleave_ini;

        for(Integer ite = 0; ite <= Iterations; ++ ite)
        {
            long bg_time = System.currentTimeMillis();

            // Gen
            List<Integer> res_cs = new ArrayList<>();
            if(csmethod.equals("SC"))res_cs = ColdStart.Coldstart_SC(features.getKey(), features.getValue(), fea_map, FG, topK);
            if(csmethod.equals("RD"))res_cs = ColdStart.Coldstart_Random(features.getKey(), features.getValue(), fea_map, topK);
            if(csmethod.equals("RI"))res_cs = ColdStart.Coldstart_RelInfo(features.getKey(), features.getValue(), fea_map, topK);
            if(csmethod.equals("PI"))res_cs = ColdStart.Coldstart_PathInfo(features.getKey(), features.getValue(), fea_map, topK);
            if(csmethod.equals("PN"))res_cs = ColdStart.Coldstart_Pathnum(features.getKey(), features.getValue(), fea_map, topK);
            if(csmethod.equals("RWR"))res_cs = ColdStart.Coldstart_RWR(features.getKey(), features.getValue(), fea_map, topK);
            List<Integer> res_rr = null;
            if(cumu_Pos.size() > 0)
            {
                if(rankmethod.equals("Pairwise")) res_rr = Rerank_Pairwise(features.getValue(), fea_map, topK, cumu_Pos, cumu_Neg);
                if(rankmethod.equals("LR")) res_rr = Rerank_LR(features.getValue(), fea_map, topK, cumu_Pos, cumu_Neg);
                if(rankmethod.equals("SVM")) res_rr = Rerank_SVM(features.getValue(), fea_map, topK, cumu_Pos, cumu_Neg);
            }

            Pair< List<Integer>, List<Integer> > inter_res = Interleave1(res_cs, res_rr, Interleave_P);
            cs = inter_res.getKey();

            // Click
            click_res = Clicks.ClickModel(data.getValue(), cs);

            // Adjust
            if(cumu_Pos.size() > 0)
            {
                //System.out.print(ite.toString() + ":");
                Adjust_P(click_res, inter_res);
            }
            if(csmethod.equals("SC")) ColdStart.Adjust(click_res, fea_map);
            ColdStart.UpdateResult(click_res, cumu_Pos, cumu_Neg);
            for(Integer node : click_res.getKey()) Classifier_Features.or(fea_map.get(node));

            // Statistics
            Map<String, Double> tmp;
            tmp = new HashMap<>(); tmp.clear();
            if(GlobalVariances.Evaluation.contains("nDCG")) tmp.put("nDCG", Evalutaion.CalNDCG.calculateNDCG(cs, data.getValue(), topK));
            if(GlobalVariances.Evaluation.contains("alpha-nDCG")) tmp.put("alpha-nDCG", Evalutaion.alpha_nDCG.Calculate(cs, topK));
            ret.add(tmp);

            long ed_time = System.currentTimeMillis();
            double timecost = (ed_time - bg_time) / 1000.0;
            TC.addTime(ite.toString(), timecost);
        }

        return ret;
    }

    public List<Integer> GetListByScore(Integer topK)
    {
        List< Pair<Integer, Double> > ret = new ArrayList<>(); ret.clear();

        for(Integer node : fea_map.keySet())
        {
            BitSet ub = fea_map.get(node);
            double sco = 0;
            Integer pos = ub.nextSetBit(0);
            while(pos >= 0)
            {
                Integer cnt = 0;
                if(features.getValue().get(node).containsKey(pos)) cnt = features.getValue().get(node).get(pos);
                sco += (double)cnt * ColdStart.Feature_Weight.get(pos);
                pos = ub.nextSetBit(pos + 1);
            }
            ret.add(new Pair<>(node, sco));
        }

        Collections.sort(ret, new Comparator<Pair<Integer, Double>>()
        {
            @Override
            public int compare(Pair<Integer, Double> o1, Pair<Integer, Double> o2)
            {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        List<Integer> ans = new ArrayList<>(); ans.clear();
        for(Integer i = 0; i < topK; ++ i) ans.add(ret.get(i).getKey());
        return ans;
    }

    public List<Map<String, Double>> Solve_Interleave2(
            Pair<Integer, Map<Integer, Integer>> data,
            String csmethod,
            String rankmethod,
            TimeCounter TC
    )
    {
        // Init
        clear();
        List<Map<String, Double>> ret = new ArrayList<>(); ret.clear();
        features = FG.getFeatures(data.getKey(), PathLen);
        fea_map = MapFromNodeToBitSet();
        ColdStart.init(features);
        List<Integer> cs = new ArrayList<>(); cs.clear();
        Pair<Set<Integer>, Set<Integer>> click_res = null;
        Interleave_P = GlobalVariances.Interleave_ini;

        for(Integer ite = 0; ite <= Iterations; ++ ite)
        {
            long bg_time = System.currentTimeMillis();

            // Gen
            List<Integer> res_cs = new ArrayList<>();
            if(cumu_Pos.size() <= 0 || !csmethod.equals("SC"))
            {
                if (csmethod.equals("SC"))
                    res_cs = ColdStart.Coldstart_SC(features.getKey(), features.getValue(), fea_map, FG, topK);
                if (csmethod.equals("RD"))
                    res_cs = ColdStart.Coldstart_Random(features.getKey(), features.getValue(), fea_map, topK);
                if (csmethod.equals("RI"))
                    res_cs = ColdStart.Coldstart_RelInfo(features.getKey(), features.getValue(), fea_map, topK);
                if (csmethod.equals("PI"))
                    res_cs = ColdStart.Coldstart_PathInfo(features.getKey(), features.getValue(), fea_map, topK);
                if (csmethod.equals("PN"))
                    res_cs = ColdStart.Coldstart_Pathnum(features.getKey(), features.getValue(), fea_map, topK);
                if (csmethod.equals("RWR"))
                    res_cs = ColdStart.Coldstart_RWR(features.getKey(), features.getValue(), fea_map, topK);
            }else
                res_cs = GetListByScore(topK);
            List<Integer> res_rr = null;
            if(cumu_Pos.size() > 0)
            {
                if(rankmethod.equals("Pairwise")) res_rr = Rerank_Pairwise(features.getValue(), fea_map, topK, cumu_Pos, cumu_Neg);
                if(rankmethod.equals("LR")) res_rr = Rerank_LR(features.getValue(), fea_map, topK, cumu_Pos, cumu_Neg);
                if(rankmethod.equals("SVM")) res_rr = Rerank_SVM(features.getValue(), fea_map, topK, cumu_Pos, cumu_Neg);
            }

            Pair< List<Integer>, List<Integer> > inter_res = Interleave1(res_cs, res_rr, Interleave_P);
            cs = inter_res.getKey();

            // Click
            click_res = Clicks.ClickModel(data.getValue(), cs);

            // Adjust
            if(cumu_Pos.size() > 0) Adjust_P(click_res, inter_res);
            if(csmethod.equals("SC")) ColdStart.Adjust(click_res, fea_map);
            ColdStart.UpdateResult(click_res, cumu_Pos, cumu_Neg);
            for(Integer node : click_res.getKey()) Classifier_Features.or(fea_map.get(node));

            // Statistics
            Map<String, Double> tmp;
            tmp = new HashMap<>(); tmp.clear();
            if(GlobalVariances.Evaluation.contains("nDCG")) tmp.put("nDCG", Evalutaion.CalNDCG.calculateNDCG(cs, data.getValue(), topK));
            if(GlobalVariances.Evaluation.contains("alpha-nDCG")) tmp.put("alpha-nDCG", Evalutaion.alpha_nDCG.Calculate(cs, topK));
            ret.add(tmp);

            long ed_time = System.currentTimeMillis();
            double timecost = (ed_time - bg_time) / 1000.0;
            TC.addTime(ite.toString(), timecost);
        }

        return ret;
    }

}
