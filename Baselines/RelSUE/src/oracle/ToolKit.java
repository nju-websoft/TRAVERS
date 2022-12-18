package oracle;

import GraphData.GraphModelM;

import java.util.*;

public class ToolKit {

    protected static int getDegree(int id){
        Map<Integer, Set<Integer>> map = UndirectedGraph.map;

        return map.get(id).size();
}

    protected static List<Tuple> getTuples(){
        List<Tuple> tuples = new ArrayList<>();
        for(int key : UndirectedGraph.map.keySet()){
            Tuple tuple = new Tuple(key, getDegree(key));
            tuples.add(tuple);
        }

        return tuples;
    }

    public static List<Integer> getSortedList(){
        List<Tuple> tuples = getTuples();
        Collections.sort(tuples, new Comparator<Tuple>() {
            public int compare(Tuple o1, Tuple o2) {
                return (o2.degree - o1.degree);
            }
        });
        List<Integer> sortedList = new ArrayList<>();
        for(Tuple t : tuples){
            sortedList.add(t.id);
        }

        return sortedList;
    }

    static class Tuple{
        int id;
        int degree;

        public Tuple(int id, int degree){
            this.id = id;
            this.degree = degree;
        }
    }

    public static void main(String[] args){
        UndirectedGraph.initializeMap();
        List<Integer> sl = getSortedList();
        for(int i = 0; i < 100; i ++){
            System.out.println(sl.get(i+2500000) + " " + getDegree(sl.get(i+2500000)));
        }
    }
}
