package Main;

import GraphData.*;
import Path.GreedyFinder;
import Path.RelSimFinder;

import java.io.*;
import java.util.*;

public class Experiments {
    public static void outputPrecision(List<Integer> examples, String fileName, int numOfE){
        File file = new File(fileName);
        FileReader fr = null;
        BufferedReader reader = null;
        Set<Integer> answers = new HashSet<>();
        try {
            fr = new FileReader(file);
            reader = new BufferedReader(fr);
            String str = reader.readLine();
            int query = Integer.parseInt(str);
            if(null == examples){
                examples = new ArrayList<>();
                str = reader.readLine();
                String[] ss = str.split("\t");

                if(numOfE >=1)
                    examples.add(Integer.parseInt(ss[0]));
                if(numOfE >= 2)
                    examples.add(Integer.parseInt(ss[4]));
                if(numOfE >= 3)
                    examples.add(Integer.parseInt(ss[7]));
                if(numOfE >= 4)
                    examples.add(Integer.parseInt(ss[2]));
                if(numOfE >= 5)
                    examples.add(Integer.parseInt(ss[5]));


                for(String sans : ss){
                    answers.add(Integer.parseInt(sans));
                }
            }
            //System.out.println(answers);
            File wfile = new File(fileName + "." + numOfE + ".prec");
            FileWriter fw = new FileWriter(wfile);
            BufferedWriter writer = new BufferedWriter(fw);
            String ws = "";
            //for(int num = 1; num <= 20 ; num ++){
            for(int num = 5; num <= 5 ; num ++){
                GreedyFinder.setK(num);
                long start = System.currentTimeMillis();
                List<Integer> result = Main.topK_relevance(query, examples, 20);
                long end = System.currentTimeMillis();
                int count = 0;
                /*if(result.size() < 9){
                    System.out.println(result);

                    System.exit(-1);
                }*/
                for(int i = 0; i < result.size(); i ++){
                    if(answers.contains(result.get(i)))
                        count ++;
                    if(9 == i)
                        ws += (count + ",");
                }
                ws += count + " " + (end - start) + ";";
            }
            writer.write(ws);

            writer.close();
            fw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void outputPrecision_random(List<Integer> examples, String fileName, int numOfE){
        File file = new File(fileName);
        FileReader fr = null;
        BufferedReader reader = null;
        Set<Integer> answers = new HashSet<>();
        try {
            fr = new FileReader(file);
            reader = new BufferedReader(fr);
            String str = reader.readLine();
            int query = Integer.parseInt(str);
            if(null == examples){
                examples = new ArrayList<>();
                str = reader.readLine();
                String[] ss = str.split("\t");

                if(numOfE >=1)
                    examples.add(Integer.parseInt(ss[0]));
                if(numOfE >= 2)
                    examples.add(Integer.parseInt(ss[4]));
                if(numOfE >= 3)
                    examples.add(Integer.parseInt(ss[7]));
                if(numOfE >= 4)
                    examples.add(Integer.parseInt(ss[2]));
                if(numOfE >= 5)
                    examples.add(Integer.parseInt(ss[5]));


                for(String sans : ss){
                    answers.add(Integer.parseInt(sans));
                }
            }
            //System.out.println(answers);
            File wfile = new File(fileName + "." + numOfE + ".random.prec");
            FileWriter fw = new FileWriter(wfile);
            BufferedWriter writer = new BufferedWriter(fw);
            String ws = "";
            //for(int num = 1; num <= 20 ; num ++){
            for(int num = 5; num <= 5 ; num ++){
                GreedyFinder.setK(num);
                long start = System.currentTimeMillis();
                List<Integer> result = Main.topK_relevance_random(query, examples, 20);
                long end = System.currentTimeMillis();
                int count = 0;
                /*if(result.size() < 9){
                    System.out.println(result);

                    System.exit(-1);
                }*/
                for(int i = 0; i < result.size(); i ++){
                    if(answers.contains(result.get(i)))
                        count ++;
                    if(9 == i)
                        ws += (count + ",");
                }
                ws += count + " " + (end - start) + ";";
            }
            writer.write(ws);

            writer.close();
            fw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void outputPrecision_RelSim(List<Integer> examples, String fileName, int numOfE){
        File file = new File(fileName);
        FileReader fr = null;
        BufferedReader reader = null;
        Set<Integer> answers = new HashSet<>();
        try {
            fr = new FileReader(file);
            reader = new BufferedReader(fr);
            String str = reader.readLine();
            int query = Integer.parseInt(str);
            if(null == examples){
                examples = new ArrayList<>();
                str = reader.readLine();
                String[] ss = str.split("\t");

                if(numOfE >=1)
                    examples.add(Integer.parseInt(ss[0]));
                if(numOfE >= 2)
                    examples.add(Integer.parseInt(ss[4]));
                if(numOfE >= 3)
                    examples.add(Integer.parseInt(ss[7]));
                if(numOfE >= 4)
                    examples.add(Integer.parseInt(ss[2]));
                if(numOfE >= 5)
                    examples.add(Integer.parseInt(ss[5]));

                for(String sans : ss){
                    answers.add(Integer.parseInt(sans));
                }
            }
            //System.out.println(answers);
            File wfile = new File(fileName + "."  + numOfE + ".relsim.prec");
            FileWriter fw = new FileWriter(wfile);
            BufferedWriter writer = new BufferedWriter(fw);
            String ws = "";
            for(int num = 2; num <= 4 ; num += 2){
                RelSimFinder.SetDiameter(num);
                long start = System.currentTimeMillis();
                List<Integer> result = Main.topK_byRelsim(query, examples, 20);
                long end = System.currentTimeMillis();
                int count = 0;
                for(int i = 0; i < result.size(); i ++){
                    if(answers.contains(result.get(i)))
                        count ++;
                    if(9 == i)
                        ws += (count + ",");
                }
                ws += count + " " + (end - start) + ";";
            }
            writer.write(ws);

            writer.close();
            fw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void outputPrecision_Mixed(List<Integer> examples, String fileName){
        File file = new File(fileName);
        FileReader fr = null;
        BufferedReader reader = null;
        Set<Integer> answers = new HashSet<>();
        try {
            fr = new FileReader(file);
            reader = new BufferedReader(fr);
            String str = reader.readLine();
            int query = Integer.parseInt(str);
            if(null == examples){
                examples = new ArrayList<>();
                str = reader.readLine();
                String[] ss = str.split("\t");

                examples.add(Integer.parseInt(ss[0]));
                //examples.add(Integer.parseInt(ss[4]));
                //examples.add(Integer.parseInt(ss[7]));
                //examples.add(Integer.parseInt(ss[2]));
                //examples.add(Integer.parseInt(ss[5]));

                for(String sans : ss){
                    answers.add(Integer.parseInt(sans));
                }
            }
            //System.out.println(answers);
            File wfile = new File(fileName + ".mixed.prec");
            FileWriter fw = new FileWriter(wfile);
            BufferedWriter writer = new BufferedWriter(fw);
            String ws = "";
            for(int num = 5; num <= 5 ; num ++){
                GreedyFinder.setK(num);
                long start = System.currentTimeMillis();
                List<Integer> result = Main.topK_byMixed(query, examples, 20);
                long end = System.currentTimeMillis();
                int count = 0;
                for(int i = 0; i < result.size(); i ++){
                    if(answers.contains(result.get(i)))
                        count ++;
                    if(9 == i)
                        ws += (count + ",");
                }
                ws += count + " " + (end - start) + ";";
            }
            writer.write(ws);

            writer.close();
            fw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        GraphClassInstancesM.initializeMap();
        //RelationsFreq.initialize("../../../dbpedia.freq");
        RelationsFreq.initialize("../../../dblp.freq");
        GraphModelM.initializeMap();
        GraphOntGetterM.initializeMap();
        Ontology.Initialize();
        System.out.println("begin test in dblp!");

        int[] list = {1,2,3,5,6,7,10,12,16};
        for(int i : list){
            String file = "../../../dataset/dblp/";
            for(int num = 2; num <= 4; num += 2){ //例子数量
                outputPrecision_random(null, file + i + ".txt", num);
                GreedyFinder.setNexamples();
            }
        }
        /*for(int i = 0; i <= 49; i ++){
            String file = "../../../dataset/dbpedia/";
            if(i != 2 && i != 4 && i != 5 && i != 21 && i != 30){
                for(int num = 1; num <= 5; num ++){
                    outputPrecision_random(null, file+ i + ".txt", num);
                    GreedyFinder.setNexamples();
                }
            }
        }*/
        /*for(int i = 8;i <=17 ; i ++){
            if(i != 8 && i != 9 && i != 13 && i != 14 && i != 15 && i != 11 && i != 17){
                String file = "../../../dataset/dblp/";
                for(int num = 2; num <= 5; num ++){ //例子数量
                    outputPrecision(null, file + i + ".txt", num);
                    GreedyFinder.setNexamples();
                }
            }
        }*/

        /*for(int i = 0;i <=19 ; i ++){
            if(i != 8 && i != 9 && i != 13 && i != 14 && i != 15 && i != 11 && i != 17){
                //String file = "../../../dataset/dblp/";
                String file = "dataset/dblp/";
                for(int num = 1; num <= 5; num ++){
                    outputPrecision_RelSim(null, file + i + ".txt", num);
                }
            }

        }*/

       /* for(int i = 0;i <=49 ; i ++){
            String file = "../../../dataset/";
            if(i != 4 && i != 2 && i != 21 && i != 5 && i != 30){
                outputPrecision_Mixed(null, file + i + ".txt");
                GreedyFinder.setNexamples();
            }
        }*/
    }
}
