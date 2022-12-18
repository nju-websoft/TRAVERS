package Test;

import java.util.StringTokenizer;

public class stringtoken
{
    public static String line = "1 1:1 2:1 3:1 4:1 5:1 6:0 7:1 8:1 9:1 10:1 11:1 12:0 13:0 14:0 15:1 16:1 17:1 18:1 19:1 20:0 21:0 22:0 23:0 24:1\r\n";

    public static void main(String[] args)
    {
        StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");
        System.out.println(st.nextToken());
        while(st.hasMoreElements())
        {
            Integer index = Integer.parseInt(st.nextToken());
            Double value = Double.parseDouble(st.nextToken());
            System.out.println(index.toString() + " " + value.toString());
        }
    }
}
