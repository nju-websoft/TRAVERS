package RelSim;

import Structures.*;

import javax.swing.*;
import java.util.*;

public class RelSim {

    public Integer MaxPathLen = 2;
    public Integer MaxNegativeSampleNumber = 10;
    public Integer MaxSample = 30;
    //public final Integer NegSampleToOne = 1;

    public Map<Integer, List<Integer>> Sample_Cluster = new HashMap<Integer, List<Integer>>();
    public List<pairModel> NegSam = new ArrayList<pairModel>();

    public MetapathFinder MF = new MetapathFinder();
    public FeatureGenerator FG = new FeatureGenerator();
    public NegativeSampleGenerator NSG = new NegativeSampleGenerator();
    public FindAnswer FA = new FindAnswer();

    public List<Integer> RelSim(List<pairModel> samples, Integer query, Integer K, Integer NegSampleToOne, Integer optimize_method)
    {

        //------------PreProcess------------
        Sample_Cluster.clear();
        for(pairModel e : samples)
        {
            Integer bg = e.valX;
            Integer ed = e.valY;
            if( !Sample_Cluster.containsKey(bg) )
            {
                List<Integer> tmp = new ArrayList<Integer>(); tmp.clear();
                Sample_Cluster.put(bg, tmp);
            }
            List<Integer> now = Sample_Cluster.get(bg);
            now.add(ed); Sample_Cluster.put(bg, now);
        }

        //------------Solve------------

        //Get MetaPath
        Map<List<Integer>, Integer> MPC_Count = new HashMap<>(); MPC_Count.clear();
        Map<List<Integer>, Set<metapathModel>> tMPC = new HashMap<List<Integer>, Set<metapathModel>>(); tMPC.clear();
        for(Map.Entry<Integer, List<Integer>> kv : Sample_Cluster.entrySet())
        {
            Integer bg = kv.getKey();
            List<Integer> que_ed = kv.getValue();
            for(Integer ed : que_ed)
            {
                Map<List<Integer>, Set<metapathModel>> nMPC = new HashMap<List<Integer>, Set<metapathModel>>(); nMPC.clear();
                MF.findPath(bg, ed, MaxPathLen, nMPC);
                for(Map.Entry<List<Integer>, Set<metapathModel>> st : nMPC.entrySet())
                {
                    // Add Count
                    List<Integer> rel = st.getKey();
                    if(!MPC_Count.containsKey(rel)) MPC_Count.put(rel, 0);
                    Integer cnt = MPC_Count.get(rel); MPC_Count.put(rel, cnt + 1);
                    // Put Together
                    if(!tMPC.containsKey(rel))
                    {
                        Set<metapathModel> tsmm = new HashSet<>(); tsmm.clear();
                        tMPC.put(rel, tsmm);
                    }
                    Set<metapathModel> smm = tMPC.get(rel);
                    smm.addAll(st.getValue()); tMPC.put(rel, smm);
                }
            }
        }
        List<metapathModel> all_metapath = new ArrayList<metapathModel>(); all_metapath.clear();

        for(Map.Entry<List<Integer>, Set<metapathModel>> kv : tMPC.entrySet())
        {
            List<Integer> rel = kv.getKey();
            if(MPC_Count.get(rel) < samples.size()) continue;
            all_metapath.add(MF.combine(kv.getValue()));
        }
        //System.out.println("All MetaPath Number : " + all_metapath.size());
        //for(metapathModel mp : all_metapath) System.out.println(mp.toString());

        //Get NegativeSamples
        NegSam.clear(); Set<Integer> tmp_NegSam = new HashSet<Integer>(); tmp_NegSam.clear();
        for(Integer bg : Sample_Cluster.keySet())
        {
            List<Integer> tmp_negsam = NSG.getNegativeSample_allMP(bg, all_metapath, Sample_Cluster.get(bg), MaxNegativeSampleNumber);
            tmp_NegSam.addAll(tmp_negsam);
            tmp_NegSam.remove(bg);
            for(Integer ed : Sample_Cluster.get(bg)) tmp_NegSam.remove(ed);
            tmp_NegSam.remove(query);
            for(Integer ed : tmp_NegSam) NegSam.add(new pairModel(bg, ed));
            //System.out.println("Begin Node : " + bg.toString() +  " ---- NegSam Number : " + tmp_negsam.size());
        }

        List< List<Integer> > pos_sample = new ArrayList<>(); pos_sample.clear();
        List< List<Integer> > neg_sample = new ArrayList<>(); neg_sample.clear();

        //Get Features
        FG.generator(all_metapath, Sample_Cluster, NegSam, NegSampleToOne, pos_sample, neg_sample);
        //System.out.println("Feature Generator OK!");

        long bg_time = System.currentTimeMillis();

        //Get Weight
        List<Double> weight = new ArrayList<Double>(); weight.clear();
        if(NegSam.size() <= 0) for(Integer id = 0; id < all_metapath.size(); ++ id) weight.add(1.0);
        else
        {
            if(pos_sample.size() > MaxSample)
            {
                Collections.shuffle(pos_sample); pos_sample = pos_sample.subList(0, MaxSample);
                Collections.shuffle(neg_sample); neg_sample = neg_sample.subList(0, MaxSample);
            }
            if(optimize_method.equals(0))
            {
                LinearProgramming LP = new LinearProgramming();
                weight = LP.getWeights(pos_sample, neg_sample, all_metapath.size());
            }
            else
            {
                LogisticRegression LR = new LogisticRegression();
                weight = LR.Sovle(pos_sample, neg_sample);
            }
        }
        //System.out.println("Weight Calculator OK!");

        long ed_time = System.currentTimeMillis();
        //System.out.println("MetaPath : " + all_metapath.size() + " --- Times : " + (ed_time - bg_time) / 1000.0 + " --- G or B : " + LinearProgramming.bad);

        //Get TopK Answer
        List<Integer> result = FA.getTopK(all_metapath, weight, query, K);

        /*
        //Test Print
        System.out.println("Test Weights : ");
        for(Integer id = 0; id < all_metapath.size(); ++ id) System.out.println(all_metapath.get(id).toString() + " --- " + weight.get(id).toString());
        //System.out.println("Test MinType : ");
        //System.out.println("566173 : " + OntologyTree.mintype[566173]);
        //System.out.println("2521462 : " + OntologyTree.mintype[2521462]);
        //System.out.println("241149 : " + OntologyTree.mintype[241149]);
        //System.out.println("522709 : " + OntologyTree.mintype[522709]);
        */

        //------------Return------------

        return result;
    }

    public void TestSolve()
    {
        Scanner sc = new Scanner(System.in);
        while(true)
        {
            System.out.println("Please Input Sample Number : ");
            Integer num =  sc.nextInt();
            System.out.println("Please Input Samples : ");
            List<pairModel> samples = new ArrayList<pairModel>(); samples.clear();
            for(Integer i = 0; i < num; ++ i)
            {
                Integer x = sc.nextInt();
                Integer y = sc.nextInt();
                samples.add(new pairModel(x, y));
            }
            System.out.println("Please Input Query Entity: ");
            Integer query = sc.nextInt();
            System.out.println("Please Input K : ");
            Integer K = sc.nextInt();
            long bgtime = System.currentTimeMillis();
            System.out.println(RelSim(samples, query, K, 1, 0).toString());
            long edtime = System.currentTimeMillis();
            System.out.println("Time : " + (edtime-bgtime));
        }
    }

    public List<Integer> RelSim_Pair(List<pairModel> samples, Integer query, Integer K, Integer optmethod)
    {
        return RelSim(samples, query, K, 3, optmethod);
    }

    public List<Integer> RelSim_List(Integer query, List<Integer> samples, Integer len, Integer K, Integer optmethod)
    {
        //System.out.println("Samples : " + samples.toString());
        List<Integer> ret = new ArrayList<>(); ret.clear();
        if(samples.size() <= 0)
        {
            for(Integer id = 0; id < K; ++ id) ret.add(0);
            return ret;
        }
        MaxPathLen = len;
        List<pairModel> PM = new ArrayList<pairModel>(); PM.clear();
        for(Integer sp : samples) PM.add(new pairModel(query, sp));
        return RelSim_Pair(PM, query, K, optmethod);
    }

    public void main(String[] args)
    {
        TestSolve();
    }

}
