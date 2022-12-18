package ExperimentResults;

import Main.WSDMExperiment;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class embNDCG {

    public static void test(){
        File file = new File("H:\\myself\\hin\\WSDM\\Hash2Node.txt");
        Map<Integer, Integer> map = new HashMap<>();
        Map<Integer, Integer> answerMap = new HashMap<>();
        List<Integer> examples = new ArrayList<>();
        examples.add(3458003);
        examples.add(2258901);
        examples.add(960394);
        String line = "3458003:5\t2258901:2\t960394:2\t1929471:2\t2589219:2\t622228:2\t2430074:2\t2754518:2\t2607755:2\t1151062:2\t1889890:2\t1428391:1\t3078913:1\t1425758:1\t972396:1\t2683902:1\t1762356:1\t3553091:1\t2907344:1\t3842622:1\t3954659:1\t783552:1\t2954446:1\t3385084:1\t2828222:1\t789162:1\t2666591:1\t1153479:1\t3529362:1\t4197134:1\t1794516:1\t403610:1\t1821431:1\t3734969:1\t2339072:1\t3225365:1\t4163074:1\t244843:1\t2332401:1\t620876:1\t3036611:1\t2227326:1\t2781886:1\t1973192:1\t3674622:1\t3280058:1\t";
       // String res = "497377, 291932, 234372, 177480, 347654, 320652, 522753, 695, 547433, 446301, 356871, 364800, 301977, 511618, 513117, 497485, 204664, 273378, 242922, 301937";
        String res = "562103, 522753, 511618, 497485, 497377, 489450, 373941, 320652, 291932, 234372, 204664, 177480, 347654, 695, 502522, 406028, 280066, 301937, 513117, 496410";
        answerMap = mapByString(line);
        answerMap = WSDMExperiment.sortByValue(answerMap);
        try {
            FileReader fr = new FileReader(file);
            BufferedReader reader = new BufferedReader(fr);
            String str;
            while((str = reader.readLine()) != null){
                String[] ss = str.split("\t");
                map.put(Integer.parseInt(ss[0]), Integer.parseInt(ss[1]));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Integer> results = resultsByString(res, map);

        System.out.println(WSDMExperiment.calculateNDCG(examples, results, answerMap, 20));
    }

    public static Map<Integer, Integer> mapByString(String line){
        Map<Integer, Integer> map = new HashMap<>();
        String[] ss = line.split("\t");
        for(String s : ss){
            String pair[] = s.split(":");
            //System.out.println(pair[0]);
            map.put(Integer.parseInt(pair[0]), Integer.parseInt(pair[1]));
        }

        return map;
    }

    public static List<Integer> resultsByString(String res, Map<Integer, Integer> map){
        String[] ss = res.split(", ");
        List<Integer> list = new ArrayList<>();
        for(String s : ss){
            list.add(map.get(Integer.parseInt(s)));
        }

        return  list;
    }

    public static void main(String[] args){
        test();
    }
}
