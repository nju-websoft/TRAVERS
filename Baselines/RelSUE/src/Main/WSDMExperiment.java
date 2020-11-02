package Main;

import GraphData.*;
import JDBCUtils.JdbcUtil;
import oracle.OracleFromFile;

import java.io.*;
import java.util.*;


public class WSDMExperiment {

    /**
     * 输出文件格式：ndcg@10;ndcg@20;time;
     * @param fileName
     */
    public static void relsimExperiments(String fileName){
        try {
            File outputFile = new File(fileName + ".relsim.result");
            FileWriter fw = new FileWriter(outputFile);
            String content = "";

            for(int numOfE = 1; numOfE <= 5; numOfE ++){
                double avg_ndcg10 = 0, avg_ndcg20 = 0;
                long avg_time = 0;

                File file = new File(fileName);
                FileReader fr = new FileReader(file);
                BufferedReader reader = new BufferedReader(fr);
                String str;
                int query = -1;
                int i = 0;
                while((str = reader.readLine()) != null){
                    if((i + 2) % 2 == 0){
                        query = Integer.parseInt(str);
                    }
                    else{
                        Map<Integer, Integer> anserMap = new HashMap<>();
                        List<Integer> answerList = new ArrayList<>();
                        String[] ss = str.split("\t");
                        for(String s : ss){
                            //System.out.println(s);
                            if(s.contains(":")){
                                String[] pair = s.split(":");
                                anserMap.put(Integer.parseInt(pair[0]), Integer.parseInt(pair[1]));
                                answerList.add(Integer.parseInt(pair[0]));
                            }
                            else{
                                anserMap.put(Integer.parseInt(s), 1);
                                answerList.add(Integer.parseInt(s));
                            }
                        }
                        anserMap = sortByValue(anserMap);
                        //System.out.println(anserMap);
                        //System.out.println(answerList);

                        List<Integer> examples = new ArrayList<>();
                        examples.addAll(answerList.subList(0, numOfE));
                        long start = System.currentTimeMillis();
                        List<Integer> results = Main.topK_byRelsim(query, examples, 20);
                        long end = System.currentTimeMillis();

                        long time = end - start;

                        double ndcg10 = (results.size() != 0)?calculateNDCG(examples, results, anserMap, 10):0;
                        double ndcg20 = (results.size() != 0)?calculateNDCG(examples, results, anserMap, 20):0;

                        avg_ndcg10 += ndcg10;
                        avg_ndcg20 += ndcg20;

                        avg_time += time;


                    }

                    i ++;
                }

                content += (avg_ndcg10/10 + ";" + avg_ndcg20/10 + ";" + avg_time/10 + ";" );
                content += "\n";
            }

            fw.write(content);
            fw.close();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 固定例子数量为3，yago路径数量固定为10，dbpedia固定为3， tau固定为0.5，beta从tau到1（计时掐断）
     * @param fileName
     */
    public static void betaExperiments(String fileName){
        try {
            File outputFile = new File(fileName + ".result.beta");
            FileWriter fw = new FileWriter(outputFile);
            String content = "";

            for(double beta = 0.5; beta <= 1; beta += 0.1){
                if(beta >= 0.5){
                    double avg_ndcg10 = 0, avg_ndcg20 = 0;
                    long avg_time1 = 0, avg_time2 = 0, avg_time3 = 0, avg_time4 = 0;

                    File file = new File(fileName);
                    FileReader fr = new FileReader(file);
                    BufferedReader reader = new BufferedReader(fr);
                    String str;
                    int query = -1;
                    int i = 0;
                    while((str = reader.readLine()) != null){
                        if((i + 2) % 2 == 0){
                            query = Integer.parseInt(str);
                            //System.out.println(query);
                        }

                        else{
                            Map<Integer, Integer> anserMap = new HashMap<>();
                            List<Integer> answerList = new ArrayList<>();
                            String[] ss = str.split("\t");
                            for(String s : ss){
                                String[] pair = s.split(":");
                                anserMap.put(Integer.parseInt(pair[0]), Integer.parseInt(pair[1]));
                                answerList.add(Integer.parseInt(pair[0]));
                            }
                            anserMap = sortByValue(anserMap);
                            //System.out.println(anserMap);
                            //System.out.println(answerList);

                            List<Integer> examples = new ArrayList<>();
                            examples.addAll(answerList.subList(0, 3)); // 从5改为3，所有的parameter都固定3个例子

                            System.out.println("query: " + query + ", examples: " + examples);

                            WSDMMain wsdmMain = new WSDMMain();
                            ResultBean resultBean = wsdmMain.betaStudy(query, examples, beta, 20);
                            List<Integer> results = resultBean.getResults();

                            double ndcg10 = (results.size() != 0)?calculateNDCG(examples, results, anserMap, 10):0;
                            double ndcg20 = (results.size() != 0)?calculateNDCG(examples, results, anserMap, 20):0;

                            avg_ndcg10 += ndcg10;
                            avg_ndcg20 += ndcg20;
                            avg_time1 += resultBean.getTime4FindingMP();
                            avg_time2 += resultBean.getTime4GenCandidates();
                            avg_time3 += resultBean.getTime4Train();
                            avg_time4 += resultBean.getTime4Predict();
                        }

                        i ++;
                    }

                    content += (avg_ndcg10/10 + ";" + avg_ndcg20/10 + ";" +
                            (avg_time1/10 + avg_time2/10 + avg_time3/10 + avg_time4/10) + ";");
                    content += "\n";
                }
                else{
                    content += (0.0 + ";" + 0.0 + ";" +
                            120000 + ";");
                    content += "\n";
                }

            }

            fw.write(content);
            fw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 固定例子数量为3，yago路径数量固定为10，dbpedia固定为3，阈值从0.0测到0.7 （yago可能0.7测不起来，那就统一到0.6）
     * @param fileName
     */
    public static void thresholdExperiments(String fileName){
        try {
            File outputFile = new File(fileName + ".result.tau");
            FileWriter fw = new FileWriter(outputFile);
            String content = "";

            for(double tau = 0.0; tau <= 0.6; tau += 0.1){
                //System.out.println("num of Paths: " + numOfP);
                double avg_ndcg10 = 0, avg_ndcg20 = 0;
                long avg_time1 = 0, avg_time2 = 0, avg_time3 = 0, avg_time4 = 0;

                File file = new File(fileName);
                FileReader fr = new FileReader(file);
                BufferedReader reader = new BufferedReader(fr);
                String str;
                int query = -1;
                int i = 0;
                while((str = reader.readLine()) != null){
                    if((i + 2) % 2 == 0){
                        query = Integer.parseInt(str);
                        //System.out.println(query);
                    }

                    else{
                        Map<Integer, Integer> anserMap = new HashMap<>();
                        List<Integer> answerList = new ArrayList<>();
                        String[] ss = str.split("\t");
                        for(String s : ss){
                            String[] pair = s.split(":");
                            anserMap.put(Integer.parseInt(pair[0]), Integer.parseInt(pair[1]));
                            answerList.add(Integer.parseInt(pair[0]));
                        }
                        anserMap = sortByValue(anserMap);
                        //System.out.println(anserMap);
                        //System.out.println(answerList);

                        List<Integer> examples = new ArrayList<>();
                        examples.addAll(answerList.subList(0, 3)); // 从5改为3，所有的parameter都固定3个例子

                        System.out.println("query: " + query + ", examples: " + examples);

                        WSDMMain wsdmMain = new WSDMMain();
                        ResultBean resultBean = wsdmMain.tauStudy(query, examples, tau, 20);
                        List<Integer> results = resultBean.getResults();

                        double ndcg10 = (results.size() != 0)?calculateNDCG(examples, results, anserMap, 10):0;
                        double ndcg20 = (results.size() != 0)?calculateNDCG(examples, results, anserMap, 20):0;

                        avg_ndcg10 += ndcg10;
                        avg_ndcg20 += ndcg20;
                        avg_time1 += resultBean.getTime4FindingMP();
                        avg_time2 += resultBean.getTime4GenCandidates();
                        avg_time3 += resultBean.getTime4Train();
                        avg_time4 += resultBean.getTime4Predict();
                    }

                    i ++;
                }

                content += (avg_ndcg10/10 + ";" + avg_ndcg20/10 + ";" +
                        (avg_time1/10 + avg_time2/10 + avg_time3/10 + avg_time4/10) + ";");
                content += "\n";

            }

            fw.write(content);
            fw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 对每个查询，要根据例子数量1-5，贪心方法另需根据路径数量进行实验
     * 输出结果包括top10 ndcg， top20 ndcg， 找路径时间，生成candidate时间，训练时间，预测时间
     * 输出格式(每行,表示一种配置下10个查询的平均表现)：ndcg@10;ndcg@20;time0;time1;time2;time3;totaltime;
     * greedy一个文件共5*10=50行 (每组输出两个文件，分别为svm的和直接用sig做权重的)
     */
    public static void conductExperiments(String fileName){
        try {
//            File outputFile = new File(fileName + ".result");
//            File outputFile1 = new File(fileName + ".result1");
            File outputFile_sb = new File(fileName + ".all.result");
//            FileWriter fw = new FileWriter(outputFile);
//            FileWriter fw1 = new FileWriter(outputFile1);
            FileWriter fw_sb = new FileWriter(outputFile_sb);
            String content = "";
            String content1 = "";
            String content_sb = "";

            // 例子固定在3个，路径数量yago隔10从10取到50
            for(int numOfE = 1; numOfE <= 5; numOfE ++){
                //System.out.println("num of examples: " + numOfE);
                for(int numOfP = 10; numOfP <= 10; numOfP +=5){
                    //System.out.println("num of Paths: " + numOfP);
                    double avg_ndcg10 = 0, avg_ndcg20 = 0, avg_ndcg10_1 = 0, avg_ndcg20_1 = 0;
                    long avg_time1 = 0, avg_time2 = 0, avg_time3 = 0, avg_time4 = 0;

                    File file = new File(fileName);
                    FileReader fr = new FileReader(file);
                    BufferedReader reader = new BufferedReader(fr);
                    String str;
                    int query = -1;
                    int i = 0;
                    while((str = reader.readLine()) != null){
                        if((i + 2) % 2 == 0){
                            query = Integer.parseInt(str);
                            //System.out.println(query);
                        }

                        else{
                            Map<Integer, Integer> anserMap = new HashMap<>();
                            List<Integer> answerList = new ArrayList<>();
                            String[] ss = str.split("\t");
                            for(String s : ss){
                                if(s.contains(":")){
                                    String[] pair = s.split(":");
                                    anserMap.put(Integer.parseInt(pair[0]), Integer.parseInt(pair[1]));
                                    answerList.add(Integer.parseInt(pair[0]));
                                }
                                else{
                                    anserMap.put(Integer.parseInt(s), 1);
                                    answerList.add(Integer.parseInt(s));
                                }
                            }
                            anserMap = sortByValue(anserMap);
                            //System.out.println(anserMap);
                            //System.out.println(answerList);

                            List<Integer> examples = new ArrayList<>();
                            examples.addAll(answerList.subList(0, numOfE));

                            System.out.println("query: " + query + ", examples: " + examples + ", numOfP: " + numOfP);

                            WSDMMain wsdmMain = new WSDMMain();
                            ResultBean resultBean = wsdmMain.topK_relevance(query, examples, 20, numOfP);
                            List<Integer> results = resultBean.getResults();
                            List<Integer> results1 = resultBean.getResults1();

                            double ndcg10 = (results.size() != 0)?calculateNDCG(examples, results, anserMap, 10):0;
                            double ndcg20 = (results.size() != 0)?calculateNDCG(examples, results, anserMap, 20):0;
                            double ndcg10_1 = (results.size() != 0)?calculateNDCG(examples, results1, anserMap, 10):0;
                            double ndcg20_1 = (results.size() != 0)?calculateNDCG(examples, results1, anserMap, 20):0;

                            avg_ndcg10 += ndcg10;
                            avg_ndcg20 += ndcg20;
                            avg_ndcg10_1 += ndcg10_1;
                            avg_ndcg20_1 += ndcg20_1;
                            avg_time1 += resultBean.getTime4FindingMP();
                            avg_time2 += resultBean.getTime4GenCandidates();
                            avg_time3 += resultBean.getTime4Train();
                            avg_time4 += resultBean.getTime4Predict();
                            content_sb += (ndcg10 + ";" + ndcg20 + ";"  + resultBean.getTime4FindingMP() + ";");
                            content_sb += "\n";

                           // content += (ndcg10 + ";" + ndcg20 + ";" + "\n"); // for test

                        }

                        i ++;
                    }

                    content += (avg_ndcg10/10 + ";" + avg_ndcg20/10 + ";" + avg_time1/10 + ";" + avg_time2/10 + ";" + avg_time3/10 + ";" + avg_time4/10 + ";" +
                            (avg_time1/10 + avg_time2/10 + avg_time3/10 + avg_time4/10) + ";");
                    content += "\n";

                    content1 += (avg_ndcg10_1/10 + ";" + avg_ndcg20_1/10 + ";" + avg_time1/10 + ";" + avg_time2/10 + ";" + avg_time3/10 + ";" + avg_time4/10 + ";" +
                            (avg_time1/10 + avg_time2/10 + avg_time4/10) + ";");
                    content1 += "\n";

                }

            }

//            fw.write(content);
//            fw.close();
//
//            fw1.write(content1);
//            fw1.close();

            fw_sb.write(content_sb);
            fw_sb.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static double calculateNDCG(List<Integer> examples, List<Integer> results, Map<Integer, Integer> answerMap, int K){
        double dcg = 0, idcg = 0;
        int j = 0;

      // List<Integer> resRel = new ArrayList<>();
        for(int i = 0; i < K; i ++){
            if(results.size() > i){
                if(answerMap.containsKey(results.get(i))){
                    dcg += (answerMap.get(results.get(i)))/(Math.log10(i+2)/Math.log10(2));

                    //resRel.add(answerMap.get(results.get(i)));
                }
               // else resRel.add(0);
            }
            else break;
        }
       // System.out.println(resRel);
      //  List<Integer> idealRel = new ArrayList<>();
        int i = 0;
        for(Map.Entry<Integer, Integer> entry : answerMap.entrySet()){
            if(K == i)
                break;
            if(!examples.contains(entry.getKey())){
                idcg += (entry.getValue())/(Math.log10(i+2)/Math.log10(2));
                i ++;
              //  idealRel.add(entry.getValue());
            }
        }
       // System.out.println(idealRel);
        /*if(dcg > idcg){
            System.out.println("what the fuck??");
            System.out.println(results.subList(0, K));
        }*/
        double ndcg = dcg/idcg;
        System.out.println("ndcg: " + ndcg);
        return ndcg;
    }

    public static <K, V> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Object>() {
            @SuppressWarnings("unchecked")
            public int compare(Object o1, Object o2) {
                return ((Comparable<V>) ((Map.Entry<K, V>) (o2)).getValue()).compareTo(((Map.Entry<K, V>) (o1)).getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<>();
        for (Iterator<Map.Entry<K, V>> it = list.iterator(); it.hasNext();) {
            Map.Entry<K, V> entry = it.next();
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public static void nDCGTest(){
        List<Integer> list = new ArrayList<>();
        list.add(3);
        list.add(4);
        list.add(7);

        Map<Integer, Integer> map = new HashMap();
        map.put(3, 1);
        map.put(4, 1);
        map.put(7, 1);

        map = sortByValue(map);

        System.out.println(calculateNDCG(new ArrayList<Integer>(), list, map, 1));
    }

    public static void ourExpMain(){
        System.out.println("Begin experiments...");
        RelationIndex.initializeMap();
        GraphOntGetterM.initializeMap();
        TypedGraphModelM.initializeMap();
        OracleFromFile.initialize();
        //String dir = "V2/";
         String dir = "yago/";
        //String dir = "dbpedia/";
        //String dir = "";
        conductExperiments(dir + "多步单条排序_new.txt");
        //conductExperiments(dir + "多步单条排序.txt");
        conductExperiments(dir + "单步单条布尔.txt");
        conductExperiments(dir + "多步单条布尔.txt");
        conductExperiments(dir + "多步多条布尔.txt");
        //conductExperiments(dir + "V2_filtered.txt");
    }

    public static void tauMain(){
        System.out.println("Begin tau study... 0-0.6 dbpedia");
        RelationIndex.initializeMap();
        GraphOntGetterM.initializeMap();
        TypedGraphModelM.initializeMap();
        OracleFromFile.initialize();
       // String dir = "yago/";
        String dir = "dbpedia/";
        //String dir = "";
        thresholdExperiments(dir + "多步单条排序.txt");
        thresholdExperiments(dir + "单步单条布尔.txt");
        thresholdExperiments(dir + "多步单条布尔.txt");
        thresholdExperiments(dir + "多步多条布尔.txt");
    }

    public static void betaMain(){
        System.out.println("Begin beta study... 0.5-1 dbpedia");
        RelationIndex.initializeMap();
        GraphOntGetterM.initializeMap();
        TypedGraphModelM.initializeMap();
        OracleFromFile.initialize();
        //String dir = "yago/";
        String dir = "dbpedia/";
        //String dir = "";
        betaExperiments(dir + "多步单条排序.txt");
        betaExperiments(dir + "单步单条布尔.txt");
        betaExperiments(dir + "多步单条布尔.txt");
        betaExperiments(dir + "多步多条布尔.txt");
    }

    public static void relsimExpMain(){
        Ontology.Initialize(); // only for relsim
       // GraphClassInstancesM.initializeMap(); // only for relsim
        RelationIndex.initializeMap();
        GraphOntGetterM.initializeMap();
        TypedGraphModelM.initializeMap();

        String dir = "yago/";
        //String dir = "dbpedia/";
       // String dir = "";
        relsimExperiments(dir + "多步单条排序_new.txt");
//        relsimExperiments(dir + "多步单条排序.txt");
//        relsimExperiments(dir + "单步单条布尔.txt");
//        relsimExperiments(dir + "多步单条布尔.txt");
//        relsimExperiments(dir + "多步多条布尔.txt");
    }


    public static void main(String[] args){
        System.out.println("output for sb");
        ourExpMain();
       //  tauMain();
        // betaMain();
      // relsimExpMain();
    }
}
