package Main;

import GetResult.BasicRanker;
import GetResult.CandidatesGetter;
import GetResult.getFinals;
import GraphData.*;
//import GraphData.GraphOntologyM;
import NegativeSampler.BasicSampler;
import Path.GreedyFinder;
import Path.MetaPath;
import Path.RelSimFinder;
import TrainingData.generator;
import TrainingData.generatorForRelsim;
import WeightLearner.GetWeightFromModel;
import WeightLearner.LPforRelSim;
import WeightLearner.SVMTest;

import java.io.IOException;
import java.util.*;

public class Main {
    static GreedyFinder gf = new GreedyFinder();
    static CandidatesGetter cg = new CandidatesGetter();
    //static BasicRanker br = new BasicRanker();
    static BasicSampler bs = new BasicSampler();
    public static List<Integer> topK_relevance(int query, List<Integer> examples, int K){
        Set<MetaPath> paths = gf.findMetaPath_K(query, examples);
        List<MetaPath> pathslist = new ArrayList<>();
        for(MetaPath mp : paths){
            MetaPath mpr = mp.getReverse();
            boolean flag = true;
            for(MetaPath mpp : pathslist){
                if(mpr.isSame(mpp))
                    flag = false;
            }
            if(flag)
                pathslist.add(mp.getReverse());
        }
        System.out.println(pathslist);
        Set<Integer> candidates = cg.getCandidates(query, examples, pathslist);
        List<Integer> nexamples = new ArrayList<>();
        nexamples.addAll(candidates);
        System.out.println(nexamples.size());
        System.out.println(nexamples);
        String fileName = "Google.txt";
        try {
            generator.outputData_mp(query, examples, nexamples, pathslist, fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Begin training...");
        SVMTest.Train(fileName);
        List<Double> weights = GetWeightFromModel.getWeights(fileName+".model", pathslist.size()*3);

        List<Integer> result = getFinals.getTopK(weights, fileName, examples, nexamples, K);
        gf.reset();
        return result;
    }

    public static List<Integer> topK_relevance_random(int query, List<Integer> examples, int K){ // random是用来体现负样本价值的
        Set<MetaPath> paths = gf.findMetaPath_K(query, examples);
        List<MetaPath> pathslist = new ArrayList<>();
        for(MetaPath mp : paths){
            MetaPath mpr = mp.getReverse();
            boolean flag = true;
            for(MetaPath mpp : pathslist){
                if(mpr.isSame(mpp))
                    flag = false;
            }
            if(flag)
                pathslist.add(mp.getReverse());
        }
        System.out.println(pathslist);
        Set<Integer> candidates = cg.getCandidates(query, examples, pathslist);
        List<Integer> randomExamples = bs.getSamples(3465225, query, examples, 200);/////////////random sample dblp author as negative samples
        List<Integer> nexamples = new ArrayList<>();
        List<Integer> candidates0 = new ArrayList<>();
        //nexamples.addAll(candidates);
        nexamples.addAll(randomExamples);
        candidates0.addAll(candidates);
        System.out.println(nexamples.size());
        System.out.println(nexamples);
        String fileName = "Google.txt";
        try {
            generator.outputData_mp(query, examples, nexamples, pathslist, fileName);
            generator.outputData_mp(query, examples, candidates0, pathslist, "candidates.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }

        SVMTest.Train(fileName);
        List<Double> weights = GetWeightFromModel.getWeights(fileName+".model", pathslist.size()*3);

        List<Integer> result = getFinals.getTopK(weights, "candidates.txt", examples, candidates0, K);
        gf.reset();
        return result;
    }

    public static List<Integer> topK_byRelsim(int query, List<Integer> examples, int K){
        long start = System.currentTimeMillis();
        RelSimFinder rf = new RelSimFinder();
        Set<MetaPath> paths = rf.findMetaPath(query, examples);
        List<MetaPath> pathslist = new ArrayList<>();
        for(MetaPath mp : paths){
            pathslist.add(mp);
        }
        System.out.println("time for paths finding: " + (System.currentTimeMillis() - start));
        System.out.println("paths number: " + pathslist.size());

        Set<Integer> candidates = cg.getCandidates(query, examples, pathslist); // use the same candidates as our method

        /**
         * 暂时用随机的样本来看能不能跑得起来（因为按路径采样本，路径太多了）
         */
        //int type = GraphOntGetterM.basicClassOfEntityByID(examples.get(0)).get(0);
       // int type = GraphOntGetterM.LCA(examples).get(0);
       // List<Integer> candidates = GraphClassInstancesM.getInstances(type);

        System.out.println("candidates num: " + candidates.size());
        List<Integer> nexamples = new ArrayList<>();
        nexamples.addAll(candidates);
        //String fileName = "relsim/Loire_relsim.txt";
        String fileName = "wtf.txt";
        //generatorForRelsim.outputData(query, examples, nexamples, pathslist, fileName);
        generatorForRelsim.outputData(query, examples, nexamples, pathslist, fileName);

        List<Double> weights = LPforRelSim.getWeights(fileName);

        List<Integer> result = getFinals.getTopKForRelSim(query, weights, pathslist, examples, nexamples, K);


        return result;
    }

    public static List<Integer> topK_byMixed(int query, List<Integer> examples, int K){
        Set<MetaPath> paths = gf.findMetaPath_K(query, examples);
        List<MetaPath> pathslist = new ArrayList<>();
        for(MetaPath mp : paths){
            MetaPath mpr = mp.getReverse();
            boolean flag = true;
            for(MetaPath mpp : pathslist){
                if(mpr.isSame(mpp))
                    flag = false;
            }
            if(flag)
                pathslist.add(mp.getReverse());
        }
        System.out.println(pathslist);
        Set<Integer> candidates = cg.getCandidates(query, examples, pathslist);
        List<Integer> nexamples = new ArrayList<>();
        nexamples.addAll(candidates);
        System.out.println(nexamples.size());
        System.out.println(nexamples);

        String fileName = "wtf.txt";
        generatorForRelsim.outputData(query, examples, nexamples, pathslist, fileName);

        List<Double> weights = LPforRelSim.getWeights(fileName);

        List<Integer> result = getFinals.getTopKForRelSim(query, weights, pathslist, examples, nexamples, K);
        gf.reset();
        return result;
    }




    public static void main(String[] args){
        GraphModelM.initializeMap();
        GraphClassInstancesM.initializeMap(); //有些方法没用到注意注释掉
        Ontology.Initialize();
        RelationsFreq.initialize("dbpedia.freq");
        GraphOntGetterM.initializeMap();

        int query = 490495;
        List<Integer> examples = new ArrayList<>();
        examples.add(2700838);
        examples.add(1544354);


        //List<Integer> result0 = topK_relevance(query, examples, 20);
        List<Integer> result0 = topK_byRelsim(query, examples, 20);
        //List<Integer> result1 = topK_relevance_random(query, examples, 20);

        System.out.println("result: " + result0);
       // System.out.println("result: " + result1);
        //System.out.println("result relsim: " + topK_byRelsim(query, examples, 20));
    }
}
