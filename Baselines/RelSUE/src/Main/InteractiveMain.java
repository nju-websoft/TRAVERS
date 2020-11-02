package Main;

import GetResult.CandidatesGetter;
import GetResult.getFinals;
import NegativeSampler.CandidatesSampler;
import Path.FilterFinder;
import Path.MetaPath;
import Path.OracleFinder;
import Path.RelSimFinder;
import Sig.SigCalculator;
import TrainingData.generator;
import TrainingData.generatorForRelsim;
import WeightLearner.GetWeightFromModel;
import WeightLearner.LPforRelSim;
import WeightLearner.SVMTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class InteractiveMain {
    OracleFinder of = new OracleFinder();
    FilterFinder fif = new FilterFinder();
    CandidatesGetter cg = new CandidatesGetter();

    /**
     *
     * @param query
     * @param pexamples examples selected by user as postive
     * @param nexamples examples selected by user as negative
     * @param K
     * @param numOfP
     * @return
     */
    public ResultBean topK_relevance(int query, List<Integer> pexamples, List<Integer> nexamples, int K, int numOfP){
        OracleFinder.setK(numOfP);
        if(null == pexamples || pexamples.size() == 0){
            return new ResultBean(new ArrayList<Integer>(), new ArrayList<Integer>(), 0, 0, 0, 0);
        }
        long start = System.currentTimeMillis();
        Set<MetaPath> paths = of.findMetaPath_K(query, pexamples);
        long end = System.currentTimeMillis();
        long time4FindingMP = end - start;
        //System.out.println("time for finding meta paths; " + time4FindingMP);

        if(paths.size() == 0){
            ResultBean resultBean = new ResultBean(new ArrayList<Integer>(), new ArrayList<Integer>(),time4FindingMP, 0, 0, 0);
            return resultBean;
        }

        List<MetaPath> pathslist = new ArrayList<>();
        pathslist.addAll(paths);


        start = System.currentTimeMillis();
        Set<Integer> candidates = cg.getCandidates(query, pexamples, pathslist);

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
        List<Integer> rexamples = CandidatesSampler.getNsampes(candidates, 50*pexamples.size()); // 50倍的随机负例

        candidateList.addAll(pexamples); // This is different from our original setting in RelSUE

        String trainFile = "train_data.txt";
        try {
            generator.outputPCRW_mp(query, pexamples, rexamples, nexamples, pathslist, trainFile);
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
        //System.out.println("time for training: " + time4Train);

        List<Double> weights = GetWeightFromModel.getWeights(trainFile+".model", pathslist.size());

        List<Double> weights1 = new ArrayList<>();
        for(MetaPath mp : pathslist){
            weights1.add(SigCalculator.getSig(mp, query, pexamples));
        }

        start = System.currentTimeMillis();
        List<Integer> results = getFinals.getTopK(weights, testFile, candidateList, K);
        end = System.currentTimeMillis();

        List<Integer>  results1 = getFinals.getTopK(weights1, testFile, candidateList, K);

        long time4Predict = end - start;
        //System.out.println("time for predicting: " + time4Predict);

        ResultBean resultBean = new ResultBean(results, results1, time4FindingMP, time4GenCandidates, time4Train, time4Predict);

        of.reset();
        //System.out.println("top k: " + resultBean.getResults());
        return resultBean;
    }
    public ResultBean topK_relevance(int query, List<Integer> examples, int K, int numOfP){
        OracleFinder.setK(numOfP);
        // FilterFinder.setK(numOfP);
        if(null == examples || examples.size() == 0){
            return new ResultBean(new ArrayList<Integer>(), new ArrayList<Integer>(), 0, 0, 0, 0);
        }
        long start = System.currentTimeMillis();
        Set<MetaPath> paths = of.findMetaPath_K(query, examples);
        // Set<MetaPath> paths = fif.findMetaPath(query, examples);
        long end = System.currentTimeMillis();
        long time4FindingMP = end - start;
        //System.out.println("time for finding meta paths; " + time4FindingMP);

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
        //System.out.println("candidates size: " + candidates.size());

        List<Integer> candidateList = new ArrayList<>();
        candidateList.addAll(candidates);

        List<Integer> nexamples = CandidatesSampler.getNsampes(candidates, 50*examples.size()); // 50倍的负例

        candidateList.addAll(examples); // This is different from our original setting in RelSUE

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

        ResultBean resultBean = new ResultBean(results, results1, time4FindingMP, time4GenCandidates, time4Train, time4Predict);

        of.reset();
        //System.out.println("top k: " + resultBean.getResults());
        return resultBean;
    }

    public ResultBean topK_byRelsim(int query, List<Integer> examples, int K){
        if(null == examples || examples.size() == 0){
            return new ResultBean(new ArrayList<Integer>(), null, 0, 0, 0, 0);
        }
        long start = System.currentTimeMillis();
        RelSimFinder rf = new RelSimFinder();
        Set<MetaPath> paths = rf.findMetaPath(query, examples);
        List<MetaPath> pathslist = new ArrayList<>();
        for(MetaPath mp : paths){
            pathslist.add(mp);
        }
        long time4FindingMP = System.currentTimeMillis() - start;
        long a = System.currentTimeMillis();
        Set<Integer> candidates = cg.getCandidates(query, examples, pathslist); // use the same candidates as our method
        if(null == candidates || candidates.size() == 0)
            return new ResultBean(examples, null, 0, 0, 0, 0);
        List<Integer> nexamples = new ArrayList<>();
        nexamples.addAll(candidates);
        long b = System.currentTimeMillis();
        long time4GenCandidates = b - a;

        //String fileName = "relsim/Loire_relsim.txt";
        String fileName = "wtf.txt";
        //generatorForRelsim.outputData(query, examples, nexamples, pathslist, fileName);
        generatorForRelsim.outputData(query, examples, nexamples, pathslist, fileName);
        a = System.currentTimeMillis();
        List<Double> weights = LPforRelSim.getWeights(fileName);
        b = System.currentTimeMillis();
        long time4Train = b - a;
        List<Integer> candidatesList = new ArrayList<>();
        candidatesList.addAll(candidates);
        candidatesList.addAll(examples);
        a = System.currentTimeMillis();
        List<Integer> results = getFinals.getTopKForRelSim(query, weights, pathslist, examples, candidatesList, K);
        long time4Predict = a - b;

        ResultBean resultBean = new ResultBean(results, null, time4FindingMP, time4GenCandidates, time4Train, time4Predict);
        return resultBean;
    }

}
