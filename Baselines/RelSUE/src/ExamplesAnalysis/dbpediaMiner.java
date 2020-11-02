//package ExamplesAnalysis;
//
//import GraphData.GraphModelM;
//import GraphData.GraphOntGetterM;
//import GraphData.GraphOntologyM;
//
//import java.io.*;
//import java.util.*;
//
//public class dbpediaMiner {
//
//    private static boolean islegal(Set<Integer> types){
//        List<Integer> list = new ArrayList<Integer>();
//        list.addAll(types);
//        for(int i = 0; i < list.size() - 1; i ++){
//            for(int j = i + 1; j < list.size(); j ++){
//                if(!GraphOntologyM.isDescendantOf(list.get(i), list.get(j)) && !GraphOntologyM.isDescendantOf(list.get(j), list.get(i)))
//                    return true;
//            }
//        }
//
//        return false;
//    }
//    public static void OneStep() throws IOException{
//        int count = 0;
//        File file = new File("oneStep.txt");
//        FileWriter fw = null;
//        BufferedWriter writer = null;
//        fw = new FileWriter(file);
//        writer = new BufferedWriter(fw);
//        writer.write("one step:\n");
//        for(int i = 1;i <= 3480806; i ++){
//            if(count >= 50)
//                break;
//            Map<Integer, List<Integer>> map = GraphModelM.get(i);
//            if(map != null){
//                for(int predicate : map.keySet()){
//                     List<Integer> list = map.get(predicate);
//                     if(list.size() >= 50){
//                         Set<Integer> types = new HashSet<Integer>();
//                         for(int obj : list){
//                             if(GraphOntGetter.classOfEntityByID(obj) != null)
//                                 types.addAll(GraphOntGetter.classOfEntityByID(obj));
//                         }
//                         if (islegal(types)){
//                             count ++;
//                             System.out.println("count: " + count);
//                             writer.write(i + " " + predicate);
//                             writer.newLine();
//                         }
//                     }
//                }
//            }
//        }
//        writer.close();
//        fw.close();
//    }
//
//    public static void TwoStep() throws IOException{
//        int count = 0;
//        File file = new File("twoStep.txt");
//        FileWriter fw = null;
//        BufferedWriter writer = null;
//        fw = new FileWriter(file);
//        writer = new BufferedWriter(fw);
//        writer.write("two step:\n");
//        for(int i = 1;i <= 3480806; i ++){
//            if(count >= 50)
//                break;
//            Map<Integer, List<Integer>> map = GraphModelM.get(i);
//            if(map != null){
//                for(int predicate0 : map.keySet()){
//                    List<Integer> list = map.get(predicate0);
//                    List<Integer> predicates1 = new ArrayList<Integer>();
//                    for(int obj : list){// 获取第二步所有可能的predicate
//                        if(GraphModelM.get(obj) != null){
//                            predicates1.addAll(GraphModelM.get(obj).keySet());
//                        }
//                    }
//                    for(int predicate1 : predicates1){
//                        Set<Integer> entities = new HashSet<Integer>();
//                        for(int obj : list){
//                            if(GraphModelM.getObjects(obj, predicate1) != null)
//                                entities.addAll(GraphModelM.getObjects(obj, predicate1));
//                        }
//                        if(entities.size() > 200){
//                            Set<Integer> types = new HashSet<Integer>();
//                            for(int obj : entities){
//                                if(GraphOntGetter.classOfEntityByID(obj) != null)
//                                    types.addAll(GraphOntGetter.classOfEntityByID(obj));
//                            }
//                            if (islegal(types)){
//                                count ++;
//
//                                writer.write(i + " " + predicate0 + " " + predicate1);
//                                writer.newLine();
//                            }
//
//                        }
//                    }
//                }
//            }
//        }
//        writer.close();
//        fw.close();
//    }
//
//    public static void main(String[] args){
//        System.out.println("Miner is running");
//        GraphModelM.initializeMap();
//        GraphOntGetterM.initializeMap(3480806);
//        GraphOntologyM.initializeMap();
//
//
//        try {
//            OneStep();
//            TwoStep();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
