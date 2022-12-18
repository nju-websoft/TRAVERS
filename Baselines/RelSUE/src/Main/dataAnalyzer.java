package Main;

import java.io.*;
import java.util.*;

public class dataAnalyzer {
    static int[] number = {20, 16, 20, 12, 20, 20, 12, 15, 13, 20, 20, 10, 20, 10, 18, 18, 12, 16, 20, 20, 20, 20, 20, 14,
    20, 20, 12, 20, 18, 11, 18, 20, 15, 20, 14, 10, 11, 12, 20, 20, 20, 10, 20, 11, 20};
    static int[] ndcg10 = new int[20];
    static int[] ndcg20 = new int[20];
    static int[] time = new int[20];

    public static int sum(int[] number){
        int result = 0;
        for(int i : number)
            result += i;

        return result;
    }
    public static void analyze(String fileName, int id) {
        File file = new File(fileName);
        FileReader fr = null;
        BufferedReader reader = null;

        try {
            fr = new FileReader(file);
            reader = new BufferedReader(fr);
            String str;
            while((str = reader.readLine()) != null){
                String[] ss = str.split(";");
                int i = 0;
                for(String s : ss){
                    //System.out.println(s);
                    String[] count = s.split(" ")[0].split(",");
                    ndcg10[i] += Integer.parseInt(count[0]);
                    ndcg20[i] += Integer.parseInt(count[1]);
                    time[i] += Integer.parseInt(s.split(" ")[1]);

                    i ++;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args){
        //String directory = "H:\\myself\\hin\\2018KDD\\ground truth\\2\\";
        String directory = "H:\\myself\\hin\\2018KDD\\ground truth\\dblp\\random\\2\\";
        //String directory = "H:\\myself\\hin\\2018KDD\\ground truth\\random\\5\\";
        int id = 0;
        /*for(int i = 0; i < 50; i ++){
            if(i!=2 && i!=4 && i!=5 && i!=21 && i!=30){
                analyze(directory + i + ".txt.5.prec", id);
                //System.out.println(id);
                id ++;
            }
        }*/
        int[] list = {0,1,2,3,5,6,7,10,12,16};
        for(int i : list){
            analyze(directory + i + ".txt.2.random.prec", id);
            id ++;
        }

        /*for(int i = 0; i < ndcg10.length; i ++)
            System.out.print((double)ndcg10[i]/450 + ",");
        for(int i = 0; i < ndcg20.length; i ++)
            System.out.print((double)ndcg20[i]/sum(number) + ",");*/
        //for(int i = 0; i < time.length; i ++)
        //    System.out.println((double)time[i]/45);

       for(int i = 0; i < ndcg10.length; i ++)
            System.out.print((double)ndcg10[i]/100 + ",");
        System.out.println();
        for(int i = 0; i < ndcg20.length; i ++)
            System.out.print((double)ndcg20[i]/200 + ",");
    }
}
