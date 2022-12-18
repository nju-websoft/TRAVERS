package Structures;

import JDBCUtils.JdbcUtil;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class OntologyTree {

    //public String mintypeTableName = "";
    public static String ontologyTableName = "";
    public static final Integer limit = 5000000;

    static {
        try {
            // 加载dbinfo.properties配置文件
            InputStream in = JdbcUtil.class.getClassLoader()
                    .getResourceAsStream("Graph.properties");
            Properties properties = new Properties();
            properties.load(in);

            //mintypeTableName = properties.getProperty("MinTypeTable");
            ontologyTableName = properties.getProperty("OntologyTable");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Integer[] mintype = new Integer[limit + 5];
    public static Integer[] typedep = new Integer[limit + 5];
    public static List<Integer>[] sons = new ArrayList[limit + 5];
    public static Integer[] typefat = new Integer[limit + 5];
    public static final Integer root = 3481453;
    public static final Integer type_lower = 3481453;
    public static final Integer type_upper = 3482185;
    public static List<Integer>[] cnt = new ArrayList[limit + 5];

    public static void clean()
    {
        for(Integer i = 0; i <= limit; ++ i) mintype[i] = 0;
        for(Integer i = 0; i <= limit; ++ i)
        {
            typedep[i] = typefat[i] = 0;
            sons[i] = new ArrayList<Integer>(); sons[i].clear();
            cnt[i] = new ArrayList<Integer>(); cnt[i].clear();
        }
    }

    public static void loadOT(Connection conn)
    {
        try {
            Statement stmt = conn.createStatement();
            String sql = "select * from " + ontologyTableName + ";";
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next())
            {
                Integer chi = rs.getInt("child");
                Integer par = rs.getInt("parent");
                sons[par].add(chi);
                typefat[chi] = par; cnt[chi].add(par);
                //if(par > chi) System.out.println(chi.toString() + " --- " + par.toString());
            }
            //for(Integer i = type_lower; i <= type_upper; ++ i) if(cnt[i].size() > 1) { System.out.print(i.toString() + " : "); for(Integer p : cnt[i]) System.out.print(p.toString() + " "); System.out.println();}
            rs.close(); stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void init()
    {
        for(Integer i = type_lower; i <= type_upper; ++ i) if(typefat[i].equals(0) && !i.equals(root)) { typefat[i] = root; sons[root].add(i); }
        typefat[root] = 0; typedep[0] = 0;
        Queue<Integer> queue = new LinkedList<Integer>(); queue.clear();
        queue.offer(root);
        while(!queue.isEmpty())
        {
            Integer x = queue.poll(); typedep[x] = typedep[ typefat[x] ] + 1;
            for(Integer son : sons[x])
            {
                queue.offer(son);
                //if(typedep[son] > 0) System.out.println(son.toString() + " --- " + x.toString());
            }
        }
        for(Integer i = 1; i <= typeModel.EntityCnt; ++ i)
        {
            Integer ans = 0;
            if(typeModel.getTypes(i) == null) { mintype[i] = root; continue; }
            for(Integer p : typeModel.getTypes(i))
                if(ans <= 0 || typedep[p] > typedep[ans]) ans = p;
            mintype[i] = ans;
        }
    }

    public static Integer getTypeLCA(Integer x, Integer y)
    {
        boolean flag = false;
        //if(x.equals()561 && y.equals()561) flag=true;
        if(x .equals(0) ) return y;
        if(y .equals(0) ) return x;
        if(flag)System.out.println("Find Truth : " + x.toString() + " *** " + y.toString());
        while(typedep[x] > typedep[y]) x = typefat[x];
        if(flag)System.out.println("Find Truth : " + x.toString() + " *** " + y.toString());
        while(typedep[y] > typedep[x]) y = typefat[y];
        if(flag)System.out.println("Find Truth : " + x.toString() + " *** " + y.toString());
        while( !x.equals(y) ) { x = typefat[x]; y = typefat[y]; }
        if(flag)System.out.println("Find Truth : " + x.toString() + " *** " + y.toString());
        if(flag)System.out.println("---");
        return x;
    }

    public static Integer getNodeSetLCA(Set<Integer> nodeSet)
    {
        Integer ret = 0;
        for(Integer s : nodeSet)
        {
            Integer tmp1 = ret, tmp2 = mintype[s];
            ret = getTypeLCA(tmp1, tmp2);
        }
        //System.out.println("Fuck : " + nodeSet.toString() + " --- " + ret.toString());
        return ret;
    }
}
