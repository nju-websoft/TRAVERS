package Main;

import GraphData.*;
import Path.GreedyFinder;
import Path.RelSimFinder;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class V2Test {
    public static void outputPrecision(String fileName, int numOfE){
        File file = new File(fileName);
        FileReader fr = null;
        BufferedReader reader = null;
        Set<Integer> answers = new HashSet<>(); // 标记答案
        try {
            fr = new FileReader(file);
            reader = new BufferedReader(fr);
            String str;
            int line = 0;
            int query = -1;
            List<Integer> examples = null;
            while((str = reader.readLine()) != null){

                if(line % 2 == 0){ // query
                    query = Integer.parseInt(str);
                }
                else{ // answers
                    examples = new ArrayList<>();
                    String[] ss = str.split("\t");

                    if(numOfE >=1)
                        examples.add(Integer.parseInt(ss[0]));
                    if(numOfE >= 2)
                        examples.add(Integer.parseInt(ss[1]));
                    if(numOfE >= 3)
                        examples.add(Integer.parseInt(ss[2]));
                    if(numOfE >= 4)
                        examples.add(Integer.parseInt(ss[3]));
                    if(numOfE >= 5)
                        examples.add(Integer.parseInt(ss[4]));

                    for(String sans : ss){
                        answers.add(Integer.parseInt(sans));
                    }

                    //System.out.println(answers);
                    File wfile = new File("V2Test0/" + fileName + "." + line/2 + ".prec");
                    FileWriter fw = new FileWriter(wfile);
                    BufferedWriter writer = new BufferedWriter(fw);
                    String ws = "";
                    //for(int num = 1; num <= 20 ; num ++){
                    for(int num = 5; num <= 5 ; num ++){
                        GreedyFinder.setK(num);
                        long start = System.currentTimeMillis();
                        List<Integer> result = Main.topK_relevance(query, examples, 20);
                        long end = System.currentTimeMillis();
                        int count = 0; // 记录正确的数量

                        for(int i = 0; i < result.size(); i ++){
                            if(answers.contains(result.get(i)))
                                count ++;
                            if(9 == i)
                                ws += (count + ",");
                        }
                        ws += count + " " + (end - start) + ";";
                        //每一行表示10个对几个，20个对几个，时间
                    }
                    writer.write(ws);

                    writer.close();
                    fw.close();
                    GreedyFinder.setNexamples();
                }
                line ++;
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args){
        GraphClassInstancesM.initializeMap();
        RelationsFreq.initialize("dbpedia.freq");
        GraphModelM.initializeMap();
        Ontology.Initialize();
        GraphOntGetterM.initializeMap();
        System.out.println("begin test V2!");

        outputPrecision("V2.txt", 3); // 取前三个作为例子

    }
}
