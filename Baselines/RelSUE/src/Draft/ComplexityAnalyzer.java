package Draft;

import GraphData.GraphModelM;
import JDBCUtils.JdbcUtil;
import oracle.UndirectedGraph;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * conducting dfs in yago is much slower than in dbpedia when visiting the entity United States
 * So I want to output the sum of degree of the neighbours of United States in two different graphs.
 *
 */

public class ComplexityAnalyzer {

    public static void analyzeByTriples(){
        GraphModelM.initializeMap();

        int USA_id = -1;

        if(JdbcUtil.URL.contains("dbpedia")){
            USA_id = 630840;
        }
        else if(JdbcUtil.URL.contains("yago")){
            USA_id = 2291382;
        }
        System.out.println("United States ID: " + USA_id);
        Set<Integer> ids = new HashSet<>();
        ids.add(USA_id);
        for(int i = 3; i <= 3; i ++){
            System.out.println(i + " 步总访问空间： " + getNumber(USA_id, i, ids) );
        }



    }

    public static long getNumber(int start, int step, Set<Integer> ids){
        if(0 == step)
            return 1;

        //Map<Integer, List<Integer>> paos = GraphModelM.getAllPaos(start, ids);
        Map<Integer, List<Integer>> paos = GraphModelM.map.get(start);

        if(1 == step){
            long number = 0;
            for(int key : paos.keySet()){
                number += (long)paos.get(key).size();
            }

            return number;
        }
        else{
            long number = 0;
            for(Map.Entry<Integer, List<Integer>> entry : paos.entrySet()){
                for(int i : entry.getValue()){
                    Set<Integer> ids0 = new HashSet<>();
                    ids0.addAll(ids);
                    ids0.add(i);
                    //System.out.println(ids0);
                    long number0 = getNumber(i, step - 1, ids0);
                    number += number0;

                    //if(2 == step && number0 > 100000 && i != 2291382){ //看看为什么第二步到第三步一下子暴涨
                    //    System.out.println("id: " + i + " count " + number0);
                    //
                    // }
                    //最终的结论是：因为yago中有几个超大度数的比如male，因为male和usa距离刚好是2，所以3步就爆炸了
                }
            }

            return number;
        }

    }

    public static void analyzeByUndirectedGraph(){
        UndirectedGraph.initializeMap();

        int USA_id = -1;

        if(JdbcUtil.URL.contains("dbpedia")){
            USA_id = 630840;
        }
        else if(JdbcUtil.URL.contains("yago")){
            USA_id = 2291382;
        }

        Set<Integer> neighbours = UndirectedGraph.map.get(USA_id);
        Set<Integer> nOfn = new HashSet<>(); // neighbours of neighbours
        Set<Integer> nOfnn = new HashSet<>(); // 3-step neighbours
        int sumOfDegree = 0; // sum of the degree of neighbours 5,050,000 for yago, 1,920,000 for dbpedia
        for(int nb : neighbours){
            sumOfDegree += UndirectedGraph.map.get(nb).size();
        }

        for(int nb : neighbours){
            nOfn.addAll(UndirectedGraph.map.get(nb));
        }

        System.out.println(sumOfDegree);

        int sumOfDegree0 = 0; // sum of the degree of neighbours of neighbours  17,000,000 for yago, 9,750,000 for dbpedia
        for(int nn : nOfn){
            sumOfDegree0 += UndirectedGraph.map.get(nn).size();
        }

        System.out.println(sumOfDegree0);

        for(int nn : nOfn){
            nOfnn.addAll(UndirectedGraph.map.get(nn));
        }

        int sumOfDegree1 = 0; // sum of the degree fo 3-step neighbours 22,000,000 for yago 21,000,000 for dbpedia
        for(int n3 : nOfnn){
            sumOfDegree1 += UndirectedGraph.map.get(n3).size();
        }

        System.out.println(sumOfDegree1);

    }

    public static void main(String[] args){
        //analyzeByUndirectedGraph();
        analyzeByTriples();
    }

}
