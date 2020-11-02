package Structures;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import JDBCUtils.JdbcUtil;
import Structures.pairModel;

public class graphModel {

    public static String tripleTableName = "";
    public static String labelinfosTableName = "";
    public static String mappingTableName = "";
    public static String pre2uriTableName = "";

    static {
        try {
            // 加载dbinfo.properties配置文件
            InputStream in = JdbcUtil.class.getClassLoader()
                    .getResourceAsStream("Graph.properties");
            Properties properties = new Properties();
            properties.load(in);

            tripleTableName = properties.getProperty("TripleTable");
            labelinfosTableName = properties.getProperty("labelinfos");
            mappingTableName = properties.getProperty("mapping");
            pre2uriTableName = properties.getProperty("predicate2uri");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //inverse edge = edge + 100000000

	public static Map<Integer, HashMap<Integer, List<Integer>>> G = new HashMap<Integer, HashMap<Integer, List<Integer>>>();

    public static Map<Integer, List<pairModel> > E = new HashMap<>();

    public static Integer edgenum = 0;
    public static Map<Integer, Integer> edgecnt = new HashMap<>();

    public static Map<Integer, String> node2uri = new HashMap<>();
    //public static Integer[] node2uri = new Integer[5000005];
    public static Map<String, String> uri2name = new HashMap<>();
    public static Map<Integer, String> edge2name = new HashMap<>();

    public static Integer[] deg = new Integer[5000005];

	public static void clean()
	{
		G.clear(); E.clear();
		edgecnt.clear(); edgenum = 0;
		//for(int i = 0; i <= 5000000; ++ i) node2uri[i] = 0;
		node2uri.clear();
        uri2name.clear();
		edge2name.clear();
        for(int i = 0; i <= 5000000; ++ i) deg[i] = 0;
	}

	public static Integer parseURI(String uri)
    {
        uri = uri.replace("<http://wikidata.dbpedia.org/resource/Q", "");
        uri = uri.substring(0, uri.length() - 1);
        return Integer.parseInt(uri);
    }

    public static String shortName(String uri)
    {
        Integer pos = uri.lastIndexOf('/');
        return uri.substring(pos + 1, uri.length() - 1);
    }

	private static void addEdge(Integer sub, Integer pre, Integer obj)
	{
		if(!G.containsKey(sub))
		{
			HashMap<Integer, List<Integer>> tmp = new HashMap<Integer, List<Integer>>();
			tmp.clear();G.put(sub, tmp);
		}
		HashMap<Integer, List<Integer>> now = G.get(sub);
		if(!now.containsKey(pre))
		{
			List<Integer> tmp = new ArrayList<Integer>();
			tmp.clear();now.put(pre, tmp);
		}
		List<Integer> node = now.get(pre);
		node.add(obj);
		now.put(pre, node);G.put(sub, now);

		edgenum ++;
		if(!edgecnt.containsKey(pre)) edgecnt.put(pre, 0);
		Integer tmp = edgecnt.get(pre);
		edgecnt.put(pre, tmp + 1);
	}
	
	public static void addTriple(Integer sub, Integer pre, Integer obj)
	{
	    deg[sub] ++; deg[obj] ++;
		addEdge(sub, pre, obj);
		addEdge(obj, -pre, sub);
	}

	public static void addEdgeCluster(Integer pre, Integer sub, Integer obj)
    {
        if(!E.containsKey(pre))
        {
            List<pairModel> tmp = new ArrayList<pairModel>(); tmp.clear();
            E.put(pre, tmp);
        }
        List<pairModel> ss = E.get(pre);
        ss.add(new pairModel(sub, pre));
        E.put(pre, ss);
    }

	public static void loadGraph(Connection conn)
    {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = null;

            // Triples
            rs = stmt.executeQuery("select * from " + tripleTableName + ";");
            while(rs.next())
            {
                Integer sub = rs.getInt("subject");
                Integer pre = rs.getInt("predicate");
                Integer obj = rs.getInt("object");
                addTriple(sub, pre, obj);
                addEdgeCluster(pre, sub, obj);
            }

            // labelinfos
            rs = stmt.executeQuery("select * from " + labelinfosTableName + ";");
            while(rs.next())
            {
                String ent = rs.getString("entity");
                String name = rs.getString("label");
                uri2name.put(ent, name);
            }

            // mapping
            rs = stmt.executeQuery("select * from " + mappingTableName + ";");
            while(rs.next())
            {
                Integer id = rs.getInt("id");
                String uri = rs.getString("uri");
                node2uri.put(id, uri);
                //node2uri[id] = parseURI(uri);
            }

            // predicate2uri
            rs = stmt.executeQuery("select * from " + pre2uriTableName + ";");
            while(rs.next())
            {
                Integer id = rs.getInt("predicate");
                String uri = rs.getString("uri");
                edge2name.put(id, shortName(uri));
            }

            rs.close(); stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getEdgeName(Integer id)
    {
        String res = "";
        if(id < 0) {res += "-"; id = -id;}
        if(!edge2name.containsKey(id)) return "null";
        return res + edge2name.get(id);
    }

    public static String getNodeName(Integer id)
    {
        String ent = node2uri.get(id);
        if(null == ent) return "null";
        if(!uri2name.containsKey(ent)) return ent;
        return uri2name.get(ent);
        //if(node2uri[id] <= 0 || id > 5000000 || id < 0) return "null";
        //return uri2name.get(node2uri[id]);
    }

    public static HashMap<Integer, List<Integer> > getNeighbours(Integer entity)
    {
        if(G.containsKey(entity))return G.get(entity); else return null;
    }

    public static List<pairModel> getEdgeCluster(Integer relation)
    {
        if(!E.containsKey(relation)) return null;
        else return E.get(relation);
    }

    public static Integer degreeCount(Integer entity)
    {
        return deg[entity];
    }

    public static List<Integer> Reverse_Relation(List<Integer> relation)
    {
        List<Integer> rela = new ArrayList<Integer>(); rela.clear();
        for(Integer i = (relation.size() - 1); i >= 0; --i) rela.add( - relation.get(i) );
        return rela;
    }

    public static Integer GetEdgeCnt(Integer rel)
    {
        if(!edgecnt.containsKey(rel)) return 0;
        return edgecnt.get(rel);
    }

    public static Double GetEdgeFreq(Integer rel)
    {
        if(!edgecnt.containsKey(rel)) return 0.0;
        return (double)(GetEdgeCnt(rel)) / (double)edgenum;
    }

    public static Double GetEdgeInfo(Integer rel)
    {
        if(!edgecnt.containsKey(rel)) return 0.0;
        return -Math.log(GetEdgeFreq(rel));
    }
}
