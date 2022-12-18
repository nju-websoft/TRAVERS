package ESER;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;

import JDBCUtils.JdbcUtil;
import Structures.graphModel;

public class GetAnswer_MultiCenterNode {
	
	//h + k + Vector<EdgeNum> + Set<Nodes>
	
	public class SatiPath
	{
		public Integer centerNode;
		public Integer valH;
		public String pathHash;
		public Set<Integer> satiNodes;
		public Set<Integer> sampleNodes;
	}
	private SimpleDateFormat mat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
	public static Integer LimS = 0;
	public static Integer LimH = 0;
	public static Integer TopK = 0;
	public static Integer RelaxNumber = 0;
	public static Integer Smooth = 0;
	public Integer CenterNode = 0;
	public Set<Integer> SampleNodes = new HashSet<Integer>();
	public Set<Integer> CandidateNodes = new HashSet<Integer>();
	
	static {
        try {
            // 加载dbinfo.properties配置文件
            InputStream in = JdbcUtil.class.getClassLoader()
                    .getResourceAsStream("ESER.properties");
            Properties properties = new Properties();
            properties.load(in);
            
            //多中心问题，从example发散的步数
            LimS = Integer.parseInt(properties.getProperty("LimitSearchSteps"));
            //SF的长度限制
            LimH = Integer.parseInt(properties.getProperty("LimitPhiSteps"));
            //返回多少个结果
            TopK = Integer.parseInt(properties.getProperty("TopK"));
            //是否采用平滑
            Smooth = Integer.parseInt(properties.getProperty("SmoothOrNot"));
            //找SF,选择中心时最多漏覆盖的Sample数
            RelaxNumber = Integer.parseInt(properties.getProperty("RelaxNumber"));
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	public Set<Integer> AlreadyChoose = new HashSet<Integer>();
	public Set<Integer> CandidateCenters = new HashSet<Integer>();
	public Map<Integer, Integer> VisitNum = new HashMap<Integer, Integer>();
	
	public Vector<SatiPath> SP = new Vector<SatiPath>();
	public Vector<SatiPath> tmpSP = new Vector<SatiPath>();
	
	public Map<String, HashSet<Integer>> SamePath = new HashMap<String, HashSet<Integer>>();
	public Map<Integer, HashSet<Integer>> Center2OneStep = new HashMap<Integer, HashSet<Integer>>();
	public ArrayList<Integer> Tmplb2SPlb = new ArrayList<Integer>();
	
	//public Map<String, List<Integer>> Hash2Path = new HashMap<String, List<Integer>>();
	public Map<String, Integer> Path = new HashMap<String, Integer>();
	public Integer PathCnt = 0;
	
	public Map<Integer, Double> ResultScore = new HashMap<Integer, Double>();
	
	public void clean()
	{
		SampleNodes.clear();
		CandidateNodes.clear();
		AlreadyChoose.clear();
		CandidateCenters.clear();
		VisitNum.clear();
		SP.clear();tmpSP.clear();
		SamePath.clear();
		Center2OneStep.clear();
		Tmplb2SPlb.clear();
		Path.clear();
		PathCnt = 0;
		ResultScore.clear();
	}
	
	public Boolean addVisitNum(Integer x, Integer lim)
	{
		if(!VisitNum.containsKey(x))VisitNum.put(x, 0);
		Integer tmp = VisitNum.get(x);
		tmp ++;
		VisitNum.put(x, tmp);
		return tmp >= lim;
	}
	
	public void BFSFindCenter(Integer now)
	{
	    Map<Integer, Integer> dis = new HashMap<Integer, Integer>(); dis.clear();
		Queue<Integer> q = new LinkedList<Integer>(); q.clear();
		q.offer(now); dis.put(now, 0);
 		while(!q.isEmpty())
        {
            Integer x = q.poll();
            //if(CenterNode .equals() 630840) System.out.println(x);
            Integer d = dis.get(x);
            if(addVisitNum(x, SampleNodes.size() - RelaxNumber))CandidateCenters.add(x);
            if( !graphModel.G.containsKey(x) || d>=LimS ) continue;
            HashMap<Integer, List<Integer>> outedges = graphModel.getNeighbours(now);
            for(Map.Entry<Integer, List<Integer>> ent : outedges.entrySet())
            {
                List<Integer> outnodes = ent.getValue();
                for(Integer tonode : outnodes)
                {
                    if(dis.containsKey(tonode)) continue;
                    dis.put(tonode, d + 1); q.offer(tonode);
                }
            }
        }
	}
	
	public void getCenters()
	{
		CandidateCenters.clear();
		for(Integer spn : SampleNodes)
		{
			BFSFindCenter(spn);
			//System.out.println(spn.toString() + "\tEnd!");
		}
		System.out.println("Centers : " + CandidateCenters.size());
	}
	
	private Integer hash2Num(String hashnum)
	{
		Integer ret = 0;
		if(Path.containsKey(hashnum)) ret = Path.get(hashnum);
		else
		{
			ret = PathCnt ++;
			Path.put(hashnum, ret);
		}
		return ret;
	}
	
	private void addInfo(Integer centernode, Vector<Integer> samplenodes)
	{
		SampleNodes.add(centernode); CenterNode = centernode;
		for(Integer p : samplenodes)SampleNodes.add(p);
	}
	
	private String listIntegerHash(ArrayList<Integer> v)
	{
		BigInteger sum = BigInteger.valueOf(0);
		BigInteger add = BigInteger.valueOf(1);
		BigInteger mul = BigInteger.valueOf(10000000);
		mul = mul.multiply(mul);
		for(Integer pp : v)
		{
			Integer p = pp;
			if(p < 0) p = 100000000 - p;
			BigInteger tmp = BigInteger.valueOf(p);
			tmp = tmp.multiply(add);
			add = add.multiply(mul);
			sum = sum.add(tmp);
		}
		return sum.toString();
	}
	
	private String pathIntegerHash(ArrayList<Integer> v)
	{
		BigInteger sum = BigInteger.valueOf(0);
		BigInteger add = BigInteger.valueOf(1);
		BigInteger mul = BigInteger.valueOf(10000000);
		mul = mul.multiply(mul);
		for(Integer i = 1; i < v.size(); ++i)
		{
			Integer p = v.get(i);
			BigInteger tmp = BigInteger.valueOf(p);
			tmp = tmp.multiply(add);
			add = add.multiply(mul);
			sum = sum.add(tmp);
		}
		return sum.toString();
	}
	
	private void DFS_Path(Integer ct, Integer node, Integer le, ArrayList<Integer> walkedges)
	{
		if( !graphModel.G.containsKey(node) )return;
		Integer len = le + 1;AlreadyChoose.add(node);
		HashMap<Integer, List<Integer>> outedges = graphModel.getNeighbours(node);
		for(Map.Entry<Integer, List<Integer>> KV : outedges.entrySet())
		{
			ArrayList<Integer> nodepathedge = (ArrayList<Integer>) walkedges.clone();
			Integer nowedge = KV.getKey();
			List<Integer> outnode = KV.getValue();
			nodepathedge.add(nowedge);
			Integer SatiPathLabel = hash2Num( listIntegerHash( nodepathedge ));
			if(SatiPathLabel >= tmpSP.size())//add SatiPath
			{
				SatiPath tmp = new SatiPath();
				tmp.valH = len;
				tmp.centerNode = ct;
				tmp.pathHash = pathIntegerHash( nodepathedge );;
				tmp.sampleNodes = new HashSet<Integer>(); tmp.sampleNodes.clear();
				tmp.satiNodes = new HashSet<Integer>(); tmp.satiNodes.clear();
				tmpSP.add(tmp);
			}
			//Update SatiPath
			SatiPath tsp = tmpSP.elementAt(SatiPathLabel);
			for(Integer nd : outnode)
				if(SampleNodes.contains(nd))tsp.sampleNodes.add(nd);
				else tsp.satiNodes.add(nd);
			tmpSP.set(SatiPathLabel, tsp);
			
			//Record SamePath
			String pathedge = pathIntegerHash( nodepathedge );
			if(!SamePath.containsKey(pathedge))
			{
				HashSet<Integer> ss = new HashSet<Integer>();
				ss.clear();
				SamePath.put(pathedge, ss);
			}
			HashSet<Integer> sss = SamePath.get(pathedge);
			sss.add(SatiPathLabel);
			SamePath.put(pathedge, sss);
			
			if(len .equals(1) )//Record OneStep to CenterNode
			{
				if(!Center2OneStep.containsKey(ct))
				{
					HashSet<Integer> ss = new HashSet<Integer>();
					ss.clear();
					Center2OneStep.put(ct, ss);
				}
				HashSet<Integer> ss = Center2OneStep.get(ct);
				ss.add(SatiPathLabel);
				Center2OneStep.put(ct, ss);
			}
			if( len >= LimH ) continue;
			//next DFS
			for(Integer nd : outnode)
				if(!AlreadyChoose.contains(nd))DFS_Path(ct, nd, len, nodepathedge);
		}
		AlreadyChoose.remove(node);
	}
	
	private void find()
	{
		ArrayList<Integer> pt = new ArrayList<Integer>();
		for(Integer center : CandidateCenters)
		{
			pt.clear();pt.add(center);
			DFS_Path(center, center, 0, pt);
		}
	}
	
	private void getSatisfiedPath()
	{
		Integer cnt = SampleNodes.size();
		for(SatiPath sp : tmpSP)
			if(sp.valH .equals(1)  || sp.sampleNodes.size() >= cnt - RelaxNumber)
			{
				SP.add(sp);Tmplb2SPlb.add(SP.size() - 1);
				for(Integer p : sp.satiNodes)
					if(!SampleNodes.contains(p))CandidateNodes.add(p);
			}else Tmplb2SPlb.add(-1);
	}
	
	private double setCap(SatiPath s1, SatiPath s2)
	{
		Integer cnt = 0;
		for(Integer p : s2.sampleNodes) if(s1.sampleNodes.contains(p))cnt ++;
		for(Integer p : s2.satiNodes) if(s1.satiNodes.contains(p))cnt ++;
		return (double)cnt;
	}
	
	private void calculateScore()
	{
		System.out.println(CandidateNodes.size());
		for(SatiPath sp : SP)
		{
			double d = 1.0 / (double)(sp.sampleNodes.size() + sp.satiNodes.size()), r = 1.0;
			
			Integer cent = sp.centerNode;
			Map<Integer, Double> phip = new HashMap<Integer, Double>(); phip.clear();
			HashSet<Integer> tmpset = Center2OneStep.get(cent);
			for(Integer lb : tmpset)// one step
			{
				Integer label = Tmplb2SPlb.get(lb);
				if(label < 0)continue;
				if(phip.containsKey(label))continue;
				double sc = setCap(sp, SP.elementAt(label)) / (double)(sp.sampleNodes.size() + sp.satiNodes.size());
			}
			tmpset = SamePath.get( sp.pathHash );
			for(Integer lb : tmpset)// one step
			{
				Integer label = Tmplb2SPlb.get(lb);
				if(label < 0)continue;
				if(phip.containsKey(label))continue;
				double sc = setCap(sp, SP.elementAt(label)) / (double)(sp.sampleNodes.size() + sp.satiNodes.size());
			}
			
			for(Integer snd : SampleNodes)
			{
				if(sp.sampleNodes.contains(snd))continue;
				double fz = 0, fm = 0;
				if(Smooth > 0) { fz = 1; fm = 1; }
				for(Map.Entry<Integer, Double> KV : phip.entrySet())
				{
					Integer label = KV.getKey();
					Double sc = KV.getValue();
					SatiPath tsp = SP.elementAt(label);
					if(tsp.sampleNodes.contains(snd))fz += sc;
					fm += sc;
				}
				r *= (fz / fm);
			}
			
			for(Integer nd : sp.satiNodes)
			{
				if( !ResultScore.containsKey(nd) ) ResultScore.put(nd, 0.0);
				Double score = ResultScore.get(nd);
				score += d * r;
				ResultScore.put(nd, score);
			}
		}
		/*
		for(Integer i=0; i<SP.size(); ++i)
		{
			SatiPath sp = SP.elementAt(i);
			System.out.print(sp.valH + "  " + sp.sampleNodes.size() + "  " + sp.satiNodes.size() + "   ");
			System.out.println(SPScore.elementAt(i));
		}
		for(Integer i=0; i<ResultNode.size(); ++i)
		{
			System.out.print(ResultNode.elementAt(i) + "  ");
			System.out.println(ResultScore.elementAt(i));
		}
		*/
	}
	
	public Vector<Integer> findanswer(Integer centernode, Vector<Integer> relativenode)
	{
		clean();
		addInfo(centernode, relativenode);
		getCenters();
		System.out.println("GetCenters End!" + mat.format(new Date()));
		find();
		System.out.println("Find SatiPath End!" + mat.format(new Date()));
		getSatisfiedPath();calculateScore();
		
		//Get Result
		Vector<Integer> ret = new Vector<Integer>();
		ret.clear();
		AlreadyChoose.clear();
		Integer topK = TopK;
		while(topK > 0)
		{
			-- topK;
			double a1 = -1;
			Integer a2 = 0;
			for(Map.Entry<Integer, Double> KV : ResultScore.entrySet())
			{
				Integer nodenum = KV.getKey();
				if(AlreadyChoose.contains(nodenum))continue;
				double score = KV.getValue();
				if(score > a1)
				{
					a1 = score;
					a2 = nodenum;
				}
			}
			if(a1 > 0)
			{
				ret.add(a2);
				AlreadyChoose.add(a2);
			}else ret.add(0);
		}
		return ret;
	}
}
