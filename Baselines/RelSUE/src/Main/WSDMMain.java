package Main;

import GetResult.CandidatesGetter;
import GetResult.getFinals;
import GraphData.GraphOntGetterM;
import GraphData.RelationIndex;
import GraphData.TypedGraphModelM;
import JDBCUtils.JdbcUtil;
import NegativeSampler.CandidatesSampler;
import Path.BiBFSFinder;
import Path.FilterFinder;
import Path.MetaPath;
import Path.OracleFinder;
import Sig.SigCalculator;
import TrainingData.generator;
import WeightLearner.GetWeightFromModel;
import WeightLearner.SVMTest;
import oracle.OracleFromFile;

import javax.xml.transform.Result;
import java.io.IOException;
import java.net.StandardSocketOptions;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WSDMMain {
    OracleFinder of = new OracleFinder();
    FilterFinder fif = new FilterFinder();
    CandidatesGetter cg = new CandidatesGetter();

    public ResultBean betaStudy(int query, List<Integer> examples, double beta, int K){
        if(JdbcUtil.URL.contains("dbpedia"))
            OracleFinder.setK(3);
        else OracleFinder.setK(10);

        SigCalculator.setBeta(beta);
        System.out.println("beta: " + beta);
        long start = System.currentTimeMillis();
        Set<MetaPath> paths = of.findMetaPath_K(query, examples);
        long end = System.currentTimeMillis();
        long time4FindingMP = end - start;
        System.out.println("time for finding meta paths; " + time4FindingMP);

        if(paths.size() == 0){
            ResultBean resultBean = new ResultBean(new ArrayList<Integer>(), new ArrayList<Integer>(),time4FindingMP, 0, 0, 0);
            return resultBean;
        }

        List<MetaPath> pathslist = new ArrayList<>();
        pathslist.addAll(paths);


        start = System.currentTimeMillis();
        Set<Integer> candidates = cg.getCandidates(query, examples, pathslist);

        if(candidates.size() == 0){ // 这种异常情况只会在greedy只选极少个meta-path时发生
            ResultBean resultBean = new ResultBean(new ArrayList<Integer>(), new ArrayList<Integer>(),time4FindingMP, 0, 0, 0);
            return resultBean;
        }

        end = System.currentTimeMillis();
        long time4GenCandidates = end - start;
        System.out.println("time for generating candidates: " + time4GenCandidates);
        System.out.println("candidates size: " + candidates.size());

        List<Integer> candidateList = new ArrayList<>();
        candidateList.addAll(candidates);
        List<Integer> nexamples = CandidatesSampler.getNsampes(candidates, 50*examples.size()); // 50倍的负例

        String trainFile = "train_data.txt";
        try {
            generator.outputPCRW_mp(query, examples, nexamples, pathslist, trainFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String testFile = "test_data.txt";
        try {
            generator.outputPCRW_mp(query, new ArrayList<Integer>(), candidateList, pathslist, testFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        start = System.currentTimeMillis();
        SVMTest.Train(trainFile);
        end = System.currentTimeMillis();
        long time4Train = end - start;
        System.out.println("time for training: " + time4Train);

        List<Double> weights = GetWeightFromModel.getWeights(trainFile+".model", pathslist.size());

        start = System.currentTimeMillis();
        List<Integer> results = getFinals.getTopK(weights, testFile, candidateList, K);
        end = System.currentTimeMillis();

        long time4Predict = end - start;
        System.out.println("time for predicting: " + time4Predict);

        ResultBean resultBean = new ResultBean(results, new ArrayList<Integer>(), time4FindingMP, time4GenCandidates, time4Train, time4Predict);
        return resultBean;
    }

    public ResultBean tauStudy(int query, List<Integer> examples, double tau, int K){
        if(JdbcUtil.URL.contains("dbpedia"))
            OracleFinder.setK(3);
        else OracleFinder.setK(10);

        OracleFinder.setThreshold(tau);
        System.out.println("tau: " + tau);
        long start = System.currentTimeMillis();
        Set<MetaPath> paths = of.findMetaPath_K(query, examples);
        // Set<MetaPath> paths = fif.findMetaPath(query, examples);
        long end = System.currentTimeMillis();
        long time4FindingMP = end - start;
        System.out.println("time for finding meta paths; " + time4FindingMP);

        if(paths.size() == 0){
            ResultBean resultBean = new ResultBean(new ArrayList<Integer>(), new ArrayList<Integer>(),time4FindingMP, 0, 0, 0);
            return resultBean;
        }

        List<MetaPath> pathslist = new ArrayList<>();
        pathslist.addAll(paths);


        start = System.currentTimeMillis();
        Set<Integer> candidates = cg.getCandidates(query, examples, pathslist);

        if(candidates.size() == 0){ // 这种异常情况只会在greedy只选极少个meta-path时发生
            ResultBean resultBean = new ResultBean(new ArrayList<Integer>(), new ArrayList<Integer>(),time4FindingMP, 0, 0, 0);
            return resultBean;
        }

        end = System.currentTimeMillis();
        long time4GenCandidates = end - start;
        System.out.println("time for generating candidates: " + time4GenCandidates);
        System.out.println("candidates size: " + candidates.size());

        List<Integer> candidateList = new ArrayList<>();
        candidateList.addAll(candidates);
        List<Integer> nexamples = CandidatesSampler.getNsampes(candidates, 50*examples.size()); // 50倍的负例

        String trainFile = "train_data.txt";
        try {
            generator.outputPCRW_mp(query, examples, nexamples, pathslist, trainFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String testFile = "test_data.txt";
        try {
            generator.outputPCRW_mp(query, new ArrayList<Integer>(), candidateList, pathslist, testFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        start = System.currentTimeMillis();
        SVMTest.Train(trainFile);
        end = System.currentTimeMillis();
        long time4Train = end - start;
        System.out.println("time for training: " + time4Train);

        List<Double> weights = GetWeightFromModel.getWeights(trainFile+".model", pathslist.size());

        start = System.currentTimeMillis();
        List<Integer> results = getFinals.getTopK(weights, testFile, candidateList, K);
        end = System.currentTimeMillis();

        long time4Predict = end - start;
        System.out.println("time for predicting: " + time4Predict);

        ResultBean resultBean = new ResultBean(results, new ArrayList<Integer>(), time4FindingMP, time4GenCandidates, time4Train, time4Predict);
        return resultBean;
    }

    public ResultBean topK_relevance(int query, List<Integer> examples, int K, int numOfP){
        OracleFinder.setK(numOfP);
       // FilterFinder.setK(numOfP);

        long start = System.currentTimeMillis();
        Set<MetaPath> paths = of.findMetaPath_K(query, examples);
       // Set<MetaPath> paths = fif.findMetaPath(query, examples);
        long end = System.currentTimeMillis();
        long time4FindingMP = end - start;
        System.out.println("time for finding meta paths; " + time4FindingMP);

        if(paths.size() == 0){
            ResultBean resultBean = new ResultBean(new ArrayList<Integer>(), new ArrayList<Integer>(),time4FindingMP, 0, 0, 0);
            return resultBean;
        }

        List<MetaPath> pathslist = new ArrayList<>();
        pathslist.addAll(paths);


        start = System.currentTimeMillis();
        Set<Integer> candidates = cg.getCandidates(query, examples, pathslist);

        if(candidates.size() == 0){ // 这种异常情况只会在greedy只选极少个meta-path时发生

            ResultBean resultBean = new ResultBean(new ArrayList<Integer>(), new ArrayList<Integer>(),time4FindingMP, 0, 0, 0);
            return resultBean;
        }

        end = System.currentTimeMillis();
        long time4GenCandidates = end - start;
        System.out.println("time for generating candidates: " + time4GenCandidates);
        System.out.println("candidates size: " + candidates.size());

        List<Integer> candidateList = new ArrayList<>();
        candidateList.addAll(candidates);
        List<Integer> nexamples = CandidatesSampler.getNsampes(candidates, 50*examples.size()); // 50倍的负例

        String trainFile = "train_data.txt";
        try {
            generator.outputPCRW_mp(query, examples, nexamples, pathslist, trainFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String testFile = "test_data.txt";
        try {
            generator.outputPCRW_mp(query, new ArrayList<Integer>(), candidateList, pathslist, testFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        start = System.currentTimeMillis();
        SVMTest.Train(trainFile);
        end = System.currentTimeMillis();
        long time4Train = end - start;
        System.out.println("time for training: " + time4Train);

        List<Double> weights = GetWeightFromModel.getWeights(trainFile+".model", pathslist.size());

        List<Double> weights1 = new ArrayList<>();
        for(MetaPath mp : pathslist){
            weights1.add(SigCalculator.getSig(mp, query, examples));
        }

        start = System.currentTimeMillis();
        List<Integer> results = getFinals.getTopK(weights, testFile, candidateList, K);
        end = System.currentTimeMillis();

        List<Integer>  results1 = getFinals.getTopK(weights1, testFile, candidateList, K);

        long time4Predict = end - start;
        System.out.println("time for predicting: " + time4Predict);

        ResultBean resultBean = new ResultBean(results, results1, time4FindingMP, time4GenCandidates, time4Train, time4Predict);

        //System.out.println("top k: " + resultBean.getResults());
        return resultBean;
    }

    public static void svmTest(){
        String trainFile = "multitypesTest.txt";
        SVMTest.Train(trainFile);
        List<Double> weights = GetWeightFromModel.getWeights(trainFile+".model", 3);
        System.out.println(weights);
    }

    public static void dbpediaTest(){
        RelationIndex.initializeMap();
        GraphOntGetterM.initializeMap();
        TypedGraphModelM.initializeMap();
       // OracleM.initialize();

        OracleFinder of = new OracleFinder();
        List<Integer> examples = new ArrayList<>();
        /*examples.add(2732463);
        examples.add(2071298);
        examples.add(2888488);*/

        examples.add(716722);
        examples.add(835865);
        examples.add(703093);

        //int query = 1125862;
        int query = 1608898;

        WSDMMain wsdmMain = new WSDMMain();
        wsdmMain.topK_relevance(query, examples, 20, 20);
    }

    public static void main(String[] args){
        //dbpediaTest();

        svmTest();
    }
}
