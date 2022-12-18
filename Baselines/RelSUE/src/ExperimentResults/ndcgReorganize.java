package ExperimentResults;

import java.io.*;

public class ndcgReorganize {
    public static void time4ESER(String dir){
        String[] files = {dir + "11b.txt", dir + "21b.txt", dir + "21o.txt", dir + "22b.txt"};
        double[] time = new double[5];

        for(String fileName : files){
            File file = new File(fileName);
            try {
                FileReader fr = new FileReader(file);
                BufferedReader reader = new BufferedReader(fr);

                String str;
                int i = 0;
                while((str = reader.readLine()) != null){
                    i ++;
                    switch(i % 5){
                        case 1: {
                            time[0] += Double.parseDouble(str)/40;
                        } break;
                        case 2: {
                            time[1] += Double.parseDouble(str)/40;
                        } break;
                        case 3: {
                            time[2] += Double.parseDouble(str)/40;
                        } break;
                        case 4: {
                            time[3] += Double.parseDouble(str)/40;
                        } break;
                        case 0: {
                            time[4] += Double.parseDouble(str)/40;
                        } break;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for(double t : time)
            System.out.println(t);
    }

    public static void printTime(String dir){
        String[] files = {dir + "/单步单条布尔.txt.result", dir + "/多步单条布尔.txt.result"
                , dir + "/多步单条排序_new.txt.result", dir + "/多步多条布尔.txt.result"};
        // String[] files = {dir + "/多步单条排序_new.txt.result", dir + "/多步单条排序_new.txt.result1"};
        double[] time = new double[5];
        for(String fileName : files){
            File filer = new File(fileName);
            try {
                FileReader fr = new FileReader(filer);
                BufferedReader reader = new BufferedReader(fr);
                String str;
                int i = 0;
                while((str = reader.readLine()) != null){
                    i ++;
                    if(i % 10 == 3){ // 路径数量为3
                        String ss[] = str.split(";");
                        double time0 = Double.parseDouble(ss[ss.length - 1]);
                        time[i/10] += time0/4.0;
                    }
                }


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for(int i = 0; i < time.length; i ++)
            System.out.println(time[i]);

    }
    public static void pathNumberStudy(int num, String dir){
        String[] files = {dir + "/单步单条布尔.txt.result", dir + "/多步单条布尔.txt.result"
                , dir + "/多步单条排序_new.txt.result", dir + "/多步多条布尔.txt.result"};
        // String[] files = {dir + "/多步单条排序_new.txt.result", dir + "/多步单条排序_new.txt.result1"};
        double ndcg10 = 0; // 3个例子，num个path的整个数据集上的均值
        for(String fileName : files){
            File filer = new File(fileName);
            try {
                FileReader fr = new FileReader(filer);
                BufferedReader reader = new BufferedReader(fr);
                String str;
                int i = 0;
                while((str = reader.readLine()) != null){
                    i ++;
                    if((num != 10 && i % 10 == num) || (num == 10 && i % 10 == 0)){
                        if(i / 10 == 3){ // 3个例子
                            //System.out.println(i);
                            String ss[] = str.split(";");
                            ndcg10 += Double.parseDouble(ss[0]);
                        }
                    }
                }


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println(ndcg10/4);
    }

    public static void relsimReorganize(String dir){
        double avg10[] = new double[5], avg20[] = new double[5];
        double time[] = new double[5];
        String[] files = {dir + "/单步单条布尔.txt.relsim.result", dir + "/多步单条布尔.txt.relsim.result"
        , dir + "/多步单条排序.txt.relsim.result", dir + "/多步多条布尔.txt.relsim.result"};

        for(String fileName : files){
            File f = new File(fileName);
            try {
                FileReader fr = new FileReader(f);
                BufferedReader reader = new BufferedReader(fr);
                String str;
                int i = 0;
                while((str = reader.readLine()) != null){
                    String[] ss = str.split(";");
                   // System.out.println(str);
                    i ++;
                    switch (i){
                        case 1: {
                            avg10[0] += Double.parseDouble(ss[0])/4;
                            avg20[0] += Double.parseDouble(ss[1])/4;

                            time[0] += Double.parseDouble(ss[ss.length - 1])/4;
                        } break;
                        case 2: {
                            avg10[1] += Double.parseDouble(ss[0])/4;
                            avg20[1] += Double.parseDouble(ss[1])/4;

                            time[1] += Double.parseDouble(ss[ss.length - 1])/4;
                        } break;
                        case 3: {
                            avg10[2] += Double.parseDouble(ss[0])/4;
                            avg20[2] += Double.parseDouble(ss[1])/4;

                            time[2] += Double.parseDouble(ss[ss.length - 1])/4;
                        } break;
                        case 4: {
                            avg10[3] += Double.parseDouble(ss[0])/4;
                            avg20[3] += Double.parseDouble(ss[1])/4;

                            time[3] += Double.parseDouble(ss[ss.length - 1])/4;
                        } break;
                        case 5: {
                            avg10[4] += Double.parseDouble(ss[0])/4;
                            avg20[4] += Double.parseDouble(ss[1])/4;

                            time[4] += Double.parseDouble(ss[ss.length - 1])/4;
                        } break;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for(int i = 0; i < 5; i ++){
            //System.out.println(avg10[i] + ";" + avg20[i]);
            System.out.println(time[i]);
        }

    }

    public static void oursReorganize(String dir){ // 就是4个文件求平均
        double avg10[] = new double[7], avg20[] = new double[7];
        String[] files = {dir + "/单步单条布尔.txt.result", dir + "/多步单条布尔.txt.result"
                , dir + "/多步单条排序.txt.result", dir + "/多步多条布尔.txt.result"};

        for(String fileName : files){
            File f = new File(fileName);
            try {
                FileReader fr = new FileReader(f);
                BufferedReader reader = new BufferedReader(fr);
                String str;
                int i = 0;
                while((str = reader.readLine()) != null){
                    String[] ss = str.split(";");
                    // System.out.println(str);
                    i ++;
                    switch (i){
                        case 1: {
                            avg10[0] += Double.parseDouble(ss[0])/4;
                            avg20[0] += Double.parseDouble(ss[1])/4;
                        } break;
                        case 2: {
                            avg10[1] += Double.parseDouble(ss[0])/4;
                            avg20[1] += Double.parseDouble(ss[1])/4;
                        } break;
                        case 3: {
                            avg10[2] += Double.parseDouble(ss[0])/4;
                            avg20[2] += Double.parseDouble(ss[1])/4;
                        } break;
                        case 4: {
                            avg10[3] += Double.parseDouble(ss[0])/4;
                            avg20[3] += Double.parseDouble(ss[1])/4;
                        } break;
                        case 5: {
                            avg10[4] += Double.parseDouble(ss[0])/4;
                            avg20[4] += Double.parseDouble(ss[1])/4;
                        } break;
                        case 6: {
                            avg10[5] += Double.parseDouble(ss[0])/4;
                            avg20[5] += Double.parseDouble(ss[1])/4;
                        } break;
                        case 7: {
                            avg10[6] += Double.parseDouble(ss[0])/4;
                            avg20[6] += Double.parseDouble(ss[1])/4;
                        } break;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for(int i = 0; i < 7; i ++){
            System.out.println(avg10[i] + ";" + avg20[i]);
        }

    }

    public static void outputOurs(String dir){
       // String[] files = {dir + "/单步单条布尔.txt.result1", dir + "/多步单条布尔.txt.result1"
       //        , dir + "/多步单条排序.txt.result1", dir + "/多步多条布尔.txt.result1"};
        String[] files = {dir + "/多步单条排序_new.txt.result", dir + "/多步单条排序_new.txt.result1"};

        for(String fileName : files){
            File filer = new File(fileName);
            try {
                FileReader fr = new FileReader(filer);
                BufferedReader reader = new BufferedReader(fr);
                String str;
                int i = 0;
                String content = "";
                while((str = reader.readLine()) != null){
                    i ++;
                    if(i % 10 == 0){
                        content += str;
                        content += "\n";
                    }
                }

                File filew = new File(fileName + ".greedy1");
                FileWriter fw = new FileWriter(filew);
                fw.write(content);
                fw.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void sbAnalyze(String fileName){
        File file = new File(fileName);
        double avg10[] = new double[5], avg20[] = new double[5];
        try {
            FileReader fr = new FileReader(file);
            BufferedReader reader = new BufferedReader(fr);
            String str;
            int i = 0;
            while((str = reader.readLine()) != null){
                if(400 > i){
                    i ++;
                    continue;
                }
                i ++;
                String ss[] = str.split(":");
                //System.out.println(ss[1]);

                switch (i%10){
                    case 1:{
                        avg10[0] += Double.parseDouble(ss[1])/40;
                    } break;
                    case 3:{
                        avg10[1] += Double.parseDouble(ss[1])/40;
                    } break;
                    case 5:{
                        avg10[2] += Double.parseDouble(ss[1])/40;
                    } break;
                    case 7:{
                        avg10[3] += Double.parseDouble(ss[1])/40;
                    } break;
                    case 9:{
                        avg10[4] += Double.parseDouble(ss[1])/40;
                    } break;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < 5; i ++){
            System.out.println(avg10[i]);
        }
    }

    public static void main(String[] args){

        //relsimReorganize("H:\\myself\\hin\\WSDM\\Queries\\relsim results\\dbpedia");

        time4ESER("H:\\myself\\hin\\WSDM\\新建压缩文件\\ESER\\yago\\");

        //按数据集汇总统计
        //oursReorganize("H:\\myself\\hin\\WSDM\\Queries\\non-diversification\\dbpedia");
        //outputOurs("H:\\myself\\hin\\WSDM\\Queries\\exhaust results\\yago");
        /////////sbAnalyze("H:\\myself\\hin\\WSDM\\dbpedia_yago.txt");

        /*for(int i = 1; i <= 10; i ++){
            pathNumberStudy(i, "H:\\\\myself\\\\hin\\\\WSDM\\\\Queries\\\\greedy results\\\\yago0.5");
        }*/
       // printTime("H:\\\\myself\\\\hin\\\\WSDM\\\\Queries\\\\exhaust results\\\\yago");
    }
}
