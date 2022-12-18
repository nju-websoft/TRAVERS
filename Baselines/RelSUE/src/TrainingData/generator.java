package TrainingData;

import GetResult.BasicRanker;
import GetResult.CandidatesGetter;
import GraphData.GraphClassInstancesM;
import GraphData.GraphModelM;
import NegativeSampler.BasicSampler;
import NegativeSampler.TestCandidatesLoader;
import Path.BasicEnumerateFinder;
import Path.BasicSymmetricEnumerateFinder;
import Path.MetaPath;
import Path.PathFinder;
import Path.RelationPath;
import PathBasedSimilarity.HeteSim;
import PathBasedSimilarity.PCRW;
import PathBasedSimilarity.PathCount;
import PathBasedSimilarity.PathSim;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.io.*;
/**
 * to generate the features values
 * @author anonymous
 *
 */
public class generator {	
	public static void printData(int query, List<Integer> examples, List<Integer> nsamples, PathFinder pf){
		Set<RelationPath> rps = pf.findRelationPath(query, examples); 
		
		//PathSim ps = new PathSim();
		HeteSim hs = new HeteSim();
		int i = 0;
		for(RelationPath rp : rps){
			System.out.println("relation path NO." + i + "： " + rp);
			for(int psample : examples){
				System.out.print(hs.getSim(query, psample, rp) + " ");
			}
			System.out.println();
			i ++;
		}
		
		i = 0;
		for(RelationPath rp : rps){
			System.out.println("relation path NO." + i + "： " + rp);
			for(int nsample : nsamples){
				System.out.print(hs.getSim(query, nsample, rp) + " ");
			}
			System.out.println();
			i ++;
		}
	}
	
	public static void outputData(int query, List<Integer> examples, List<Integer> nsamples, PathFinder pf, String fileName) throws IOException{
		Set<RelationPath> rps = pf.findRelationPath(query, examples); 
		FileWriter writer = new FileWriter(fileName);
		//PathSim ps = new PathSim();
		HeteSim hs = new HeteSim();
		for(int psample : examples){
			String s = "1 ";
			int i = 1;
			for(RelationPath rp : rps){
				s += (i + ":");
				s += (hs.getSim(query, psample, rp));			
				if(i != rps.size())
					s += " ";
				i ++;
			}
			writer.write(s + "\n");
		}

		for(int nsample : nsamples){
			String s = "0 ";
			int i = 1;
			for(RelationPath rp : rps){
				s += (i + ":");
				s += hs.getSim(query, nsample, rp);
				if(i != rps.size())
					s += " ";
				i ++;
			}
			writer.write(s + "\n");
		}
		writer.close();
	}
	
	
	@Deprecated
	public static void printData(int query, List<Integer> examples, BasicSampler bs, PathFinder pf){
		Set<RelationPath> rps = pf.findRelationPath(query, examples); 
		
		//PathSim ps = new PathSim();
		HeteSim hs = new HeteSim();
		int i = 0;
		for(RelationPath rp : rps){
			System.out.println("relation path NO." + i + "： " + rp);
			for(int psample : examples){
				System.out.print(hs.getSim(query, psample, rp) + " ");
			}
			System.out.println();
			i ++;
		}
		//3482002 basketballleague  3481481 basketballplayer
		List<Integer> negativeSamples = bs.getSamples(3465225, query, examples, 500);
		
		i = 0;
		for(RelationPath rp : rps){
			System.out.println("relation path NO." + i + "： " + rp);
			for(int nsample : negativeSamples){
				System.out.print(hs.getSim(query, nsample, rp) + " ");
			}
			System.out.println();
			i ++;
		}
	}
	@Deprecated
	public static void outputData(int query, List<Integer> examples, BasicSampler bs, PathFinder pf, String fileName) throws IOException{
		Set<RelationPath> rps = pf.findRelationPath(query, examples); 
		FileWriter writer = new FileWriter(fileName);
		//PathSim ps = new PathSim();
		HeteSim hs = new HeteSim();
		for(int psample : examples){
			String s = "1 ";
			int i = 1;
			for(RelationPath rp : rps){
				s += (i + ":");
				s += (hs.getSim(query, psample, rp));			
				if(i != rps.size())
					s += " ";
				i ++;
			}
			writer.write(s + "\n");
		}
		List<Integer> negativeSamples = bs.getSamples(3465225, query, examples, 500);

		for(int nsample : negativeSamples){
			String s = "0 ";
			int i = 1;
			for(RelationPath rp : rps){
				s += (i + ":");
				s += hs.getSim(query, nsample, rp);
				if(i != rps.size())
					s += " ";
				i ++;
			}
			writer.write(s + "\n");
		}
		writer.close();
	}
	
	public static void outputData_rp(int query, List<Integer> examples, List<Integer> negativeSamples, List<RelationPath> rps, String fileName) throws IOException{
		FileWriter writer = new FileWriter(fileName);
		//PathSim ps = new PathSim();
		HeteSim hs = new HeteSim();
		PCRW pcrw = new PCRW();
		PathCount pc = new PathCount();
		PathSim ps = new PathSim();
		for(int psample : examples){
			String s = "1 ";
			int i = 1;
			for(RelationPath rp : rps){
				s += (i + ":");
				s += (hs.getSim(query, psample, rp));			
				if(i != rps.size()*4)
					s += " ";
				i ++;
			}
			for(RelationPath rp : rps){
				s += (i + ":");
				s += (pcrw.getSim(query, psample, rp));			
				if(i != rps.size()*4)
					s += " ";
				i ++;
			}
			for(RelationPath rp : rps){
				s += (i + ":");
				s += (pc.getSim(query, psample, rp));			
				if(i != rps.size()*4)
					s += " ";
				i ++;
			}
			for(RelationPath rp : rps){
				s += (i + ":");
				s += (ps.getSim(query, psample, rp));			
				if(i != rps.size()*4)
					s += " ";
				i ++;
			}
			writer.write(s + "\n");
		}

		for(int nsample : negativeSamples){
			String s = "0 ";
			int i = 1;
			for(RelationPath rp : rps){
				s += (i + ":");
				s += hs.getSim(query, nsample, rp);
				if(i != rps.size()*4)
					s += " ";
				i ++;
			}
			for(RelationPath rp : rps){
				s += (i + ":");
				s += (pcrw.getSim(query, nsample, rp));			
				if(i != rps.size()*4)
					s += " ";
				i ++;
			}
			for(RelationPath rp : rps){
				s += (i + ":");
				s += (pc.getSim(query, nsample, rp));			
				if(i != rps.size()*4)
					s += " ";
				i ++;
			}
			for(RelationPath rp : rps){
				s += (i + ":");
				s += (ps.getSim(query, nsample, rp));			
				if(i != rps.size()*4)
					s += " ";
				i ++;
			}
			writer.write(s + "\n");
		}
		writer.close();
	}
	
	public static void outputData_mp(int query, List<Integer> examples, List<Integer> negativeSamples, List<MetaPath> mps, String fileName) throws IOException{ // hetesim, pcrw, pathcount
		FileWriter writer = new FileWriter(fileName);
		//PathSim ps = new PathSim();
		HeteSim hs = new HeteSim();
		PCRW pcrw = new PCRW();
		PathCount pc = new PathCount();
		PathSim ps = new PathSim();
		for(int psample : examples){
			String s = "1 ";
			int i = 1;
			for(MetaPath mp : mps){
				s += (i + ":");
				s += (hs.getSim(query, psample, mp));			
				if(i != mps.size()*3)
					s += " ";
				i ++;
			}
			for(MetaPath mp : mps){
				s += (i + ":");
				s += (pcrw.getSim(query, psample, mp));			
				if(i != mps.size()*3)
					s += " ";
				i ++;
			}
			for(MetaPath mp : mps){
				s += (i + ":");
				s += (pc.getSim(query, psample, mp));			
				if(i != mps.size()*3)
					s += " ";
				i ++;
			}
			writer.write(s + "\n");
		}

		for(int nsample : negativeSamples){
			String s = "0 ";
			int i = 1;
			for(MetaPath mp : mps){
				s += (i + ":");
				s += hs.getSim(query, nsample, mp);
				if(i != mps.size()*3)
					s += " ";
				i ++;
			}
			for(MetaPath mp : mps){
				s += (i + ":");
				s += (pcrw.getSim(query, nsample, mp));			
				if(i != mps.size()*3)
					s += " ";
				i ++;
			}
			for(MetaPath mp : mps){
				s += (i + ":");
				s += (pc.getSim(query, nsample, mp));			
				if(i != mps.size()*3)
					s += " ";
				i ++;
			}
			writer.write(s + "\n");
		}
		writer.close();
	}

	public static void outputPCRW_mp(int query, List<Integer> examples, List<Integer> negativeSamples, List<MetaPath> mps, String fileName) throws IOException{ // pcrw
		FileWriter writer = new FileWriter(fileName);
		PCRW pcrw = new PCRW();
		for(int psample : examples){
			String s = "1 ";
			int i = 1;
			for(MetaPath mp : mps){
				s += (i + ":");
				s += (pcrw.getSim(query, psample, mp));
				if(i != mps.size()*3)
					s += " ";
				i ++;
			}
			writer.write(s + "\n");
		}

		for(int nsample : negativeSamples){
			String s = "0 ";
			int i = 1;
			for(MetaPath mp : mps){
				s += (i + ":");
				s += (pcrw.getSim(query, nsample, mp));
				if(i != mps.size()*3)
					s += " ";
				i ++;
			}
			writer.write(s + "\n");
		}
		writer.close();
	}

	public static void outputPCRW_mp(int query, List<Integer> pexamples, List<Integer> rexamples, List<Integer> nexamples, List<MetaPath> mps, String fileName) throws IOException{ // pcrw
		FileWriter writer = new FileWriter(fileName);
		PCRW pcrw = new PCRW();
		for(int pexample : pexamples){
			for(int rexample : rexamples){
				String s = "1 ";
				int i = 1;
				for(MetaPath mp : mps){
					s += (i + ":");
					s += (pcrw.getSim(query, pexample, mp) - pcrw.getSim(query, rexample, mp));
					if(i != mps.size()*3)
						s += " ";
					i ++;
				}
				writer.write(s + "\n");

				s = "0 ";
				i = 1;
				for(MetaPath mp : mps){
					s += (i + ":");
					s += (pcrw.getSim(query, rexample, mp) - pcrw.getSim(query, pexample, mp));
					if(i != mps.size()*3)
						s += " ";
					i ++;
				}
				writer.write(s + "\n");
			}
		}

		for(int pexample : pexamples){
			for(int nexample : nexamples){
				String s = "1 ";
				int i = 1;
				for(MetaPath mp : mps){
					s += (i + ":");
					s += (pcrw.getSim(query, pexample, mp) - pcrw.getSim(query, nexample, mp));
					if(i != mps.size()*3)
						s += " ";
					i ++;
				}
				writer.write(s + "\n");

				s = "0 ";
				i = 1;
				for(MetaPath mp : mps){
					s += (i + ":");
					s += (pcrw.getSim(query, nexample, mp) - pcrw.getSim(query, pexample, mp));
					if(i != mps.size()*3)
						s += " ";
					i ++;
				}
				writer.write(s + "\n");
			}
		}

		for(int rexample : rexamples){
			for(int nexample : nexamples){
				String s = "1 ";
				int i = 1;
				for(MetaPath mp : mps){
					s += (i + ":");
					s += (pcrw.getSim(query, rexample, mp) - pcrw.getSim(query, nexample, mp));
					if(i != mps.size()*3)
						s += " ";
					i ++;
				}
				writer.write(s + "\n");

				s = "0 ";
				i = 1;
				for(MetaPath mp : mps){
					s += (i + ":");
					s += (pcrw.getSim(query, nexample, mp) - pcrw.getSim(query, rexample, mp));
					if(i != mps.size()*3)
						s += " ";
					i ++;
				}
				writer.write(s + "\n");
			}
		}

		writer.close();
	}

	private static List<Integer> readExamplesFromFile(String fileName){
		List<Integer> examples = new ArrayList<Integer>();
		File file = new File(fileName);
		FileReader fr = null;
		BufferedReader reader = null;
		try {
			fr = new FileReader(file);
			reader = new BufferedReader(fr);
			String str = null;
			while((str = reader.readLine()) != null){
				examples.add(Integer.parseInt(str));
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return examples;
	}
	
	private static List<RelationPath> setRelationPaths(){
		List<RelationPath> result = new ArrayList<RelationPath>();
		List<Integer> list0 = new ArrayList<Integer>();
		list0.add(3465221);
		list0.add(-3465221);
		RelationPath rp0 = new RelationPath(list0);
		result.add(rp0);
		
		List<Integer> list1 = new ArrayList<Integer>();
		list1.add(3465221);
		list1.add(3465222);
		list1.add(-3465222);
		list1.add(-3465221);
		RelationPath rp1 = new RelationPath(list1);
		result.add(rp1);
		
		List<Integer> list2 = new ArrayList<Integer>();
		list2.add(3465221);
		list2.add(-3465221);
		list2.add(3465221);
		list2.add(-3465221);
		RelationPath rp2 = new RelationPath(list2);
		result.add(rp2);
		
		return result;
	}
	
	public static void main(String[] args) {
		GraphModelM.initializeMap();
		//GraphClassInstancesM.initializeMap();
		/* dbpedia
		int query = 1562340;// LeBron James
		List<Integer> examples = new ArrayList<Integer>();
		examples.add(2580708);// Jae Crowder
		examples.add(1924527);// Kyrie Irving
		//examples.add(2161684);// Dwyane Wade
		//examples.add(1853308);// NBA
		//examples.add(70299); // a cba player
		//examples.add(53352); // a kbl player
		  
		*/
		CandidatesGetter cg = new CandidatesGetter();
		BasicRanker br = new BasicRanker();
		int[] queries = {219426, 656521, 155765, 630014, 629711, 398220, 643612, 60387, 566628, 31383};
		System.out.println("begin 1...");
		for(int query : queries){
			List<Integer> relations = new ArrayList<>();
			relations.add(3465221);
			relations.add(-3465221);
			RelationPath rp = new RelationPath(relations);
			List<Integer> candidates = new ArrayList<Integer>();
			candidates.addAll(cg.getAccessedNodesByRP(query, rp));
			List<Integer> examples = new ArrayList<>();
			examples.addAll(br.getTopK(query, rp, candidates, 20, new PathCount()).keySet());
			
			
			List<Integer> nrelations = new ArrayList<>();
			nrelations.add(3465221);
			nrelations.add(3465222);
			nrelations.add(-3465222);
			nrelations.add(-3465221);
			RelationPath nrp = new RelationPath(nrelations);
			List<Integer> ncandidates = new ArrayList<Integer>();
			ncandidates.addAll(cg.getAccessedNodesByRP(query, nrp));
			List<Integer> nexamples = new ArrayList<>();
			nexamples.addAll(br.getTopK(query, nrp, ncandidates, 40, new HeteSim()).keySet());
		
			List<Integer> nsamples = new ArrayList<>();
			int i = 0;
			for(Integer ne : nexamples){
				if(i < 20){
					if(!examples.contains(ne)){
						nsamples.add(ne);
						i ++;
					}
				}
				else
					break;
			}
			
			try {
				outputData_rp(query, examples, nsamples, setRelationPaths(), "semantics1." + query + ".data");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("begin 2...");
		for(int query : queries){
			List<Integer> relations = new ArrayList<>();
			relations.add(3465221);
			relations.add(-3465221);
			RelationPath rp = new RelationPath(relations);
			List<Integer> candidates = new ArrayList<Integer>();
			candidates.addAll(cg.getAccessedNodesByRP(query, rp));
			List<Integer> examples = new ArrayList<>();
			examples.addAll(br.getTopK(query, rp, candidates, 20, new HeteSim()).keySet());
			
			
			List<Integer> nrelations = new ArrayList<>();
			nrelations.add(3465221);
			nrelations.add(3465222);
			nrelations.add(-3465222);
			nrelations.add(-3465221);
			RelationPath nrp = new RelationPath(nrelations);
			List<Integer> ncandidates = new ArrayList<Integer>();
			ncandidates.addAll(cg.getAccessedNodesByRP(query, nrp));
			List<Integer> nexamples = new ArrayList<>();
			nexamples.addAll(br.getTopK(query, nrp, ncandidates, 40, new HeteSim()).keySet());
		
			List<Integer> nsamples = new ArrayList<>();
			int i = 0;
			for(Integer ne : nexamples){
				if(i < 20){
					if(!examples.contains(ne)){
						nsamples.add(ne);
						i ++;
					}
				}
				else
					break;
			}
			
			try {
				outputData_rp(query, examples, nsamples, setRelationPaths(), "semantics2." + query + ".data");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("begin 3...");
		for(int query : queries){
			List<Integer> relations = new ArrayList<>();
			relations.add(3465221);
			relations.add(-3465221);
			relations.add(3465221);
			relations.add(-3465221);
			RelationPath rp = new RelationPath(relations);
			List<Integer> candidates = new ArrayList<Integer>();
			candidates.addAll(cg.getAccessedNodesByRP(query, rp));
			List<Integer> examples = new ArrayList<>();
			examples.addAll(br.getTopK(query, rp, candidates, 20, new PathCount()).keySet());
			
			List<Integer> nrelations = new ArrayList<>();
			nrelations.add(3465221);
			nrelations.add(3465222);
			nrelations.add(-3465222);
			nrelations.add(-3465221);
			RelationPath nrp = new RelationPath(nrelations);
			List<Integer> ncandidates = new ArrayList<Integer>();
			ncandidates.addAll(cg.getAccessedNodesByRP(query, nrp));
			List<Integer> nexamples = new ArrayList<>();
			nexamples.addAll(br.getTopK(query, nrp, ncandidates, 40, new HeteSim()).keySet());
		
			List<Integer> nsamples = new ArrayList<>();
			int i = 0;
			for(Integer ne : nexamples){
				if(i < 20){
					if(!examples.contains(ne)){
						nsamples.add(ne);
						i ++;
					}
				}
				else
					break;
			}
			
			try {
				outputData_rp(query, examples, nsamples, setRelationPaths(), "semantics3." + query + ".data");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("begin 4...");
		for(int query : queries){
			List<Integer> relations = new ArrayList<>();
			relations.add(3465221);
			relations.add(-3465221);
			relations.add(3465221);
			relations.add(-3465221);
			RelationPath rp = new RelationPath(relations);
			List<Integer> candidates = new ArrayList<Integer>();
			candidates.addAll(cg.getAccessedNodesByRP(query, rp));
			List<Integer> examples = new ArrayList<>();
			examples.addAll(br.getTopK(query, rp, candidates, 20, new HeteSim()).keySet());
			
			List<Integer> nrelations = new ArrayList<>();
			nrelations.add(3465221);
			nrelations.add(3465222);
			nrelations.add(-3465222);
			nrelations.add(-3465221);
			RelationPath nrp = new RelationPath(nrelations);
			List<Integer> ncandidates = new ArrayList<Integer>();
			ncandidates.addAll(cg.getAccessedNodesByRP(query, nrp));
			List<Integer> nexamples = new ArrayList<>();
			nexamples.addAll(br.getTopK(query, nrp, ncandidates, 40, new HeteSim()).keySet());
		
			List<Integer> nsamples = new ArrayList<>();
			int i = 0;
			for(Integer ne : nexamples){
				if(i < 20){
					if(!examples.contains(ne)){
						nsamples.add(ne);
						i ++;
					}
				}
				else
					break;
			}
			
			try {
				outputData_rp(query, examples, nsamples, setRelationPaths(), "semantics4." + query + ".data");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("begin 5...");
		for(int query : queries){
			List<Integer> relations = new ArrayList<>();
			relations.add(3465221);
			relations.add(3465222);
			relations.add(-3465222);
			relations.add(-3465221);
			RelationPath rp = new RelationPath(relations);
			List<Integer> candidates = new ArrayList<Integer>();
			candidates.addAll(cg.getAccessedNodesByRP(query, rp));
			List<Integer> examples = new ArrayList<>();
			examples.addAll(br.getTopK(query, rp, candidates, 20, new HeteSim()).keySet());
			
			List<Integer> nrelations = new ArrayList<>();
			nrelations.add(3465221);
			nrelations.add(-3465221);
			RelationPath nrp = new RelationPath(nrelations);
			List<Integer> ncandidates = new ArrayList<Integer>();
			ncandidates.addAll(cg.getAccessedNodesByRP(query, rp));
			List<Integer> nexamples = new ArrayList<>();
			nexamples.addAll(br.getTopK(query, nrp, ncandidates, 40, new HeteSim()).keySet());
		
			List<Integer> nsamples = new ArrayList<>();
			int i = 0;
			for(Integer ne : nexamples){
				if(i < 20){
					if(!examples.contains(ne)){
						nsamples.add(ne);
						i ++;
					}
				}
				else
					break;
			}
			
			try {
				outputData_rp(query, examples, nsamples, setRelationPaths(), "semantics5." + query + ".data");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		/*printData(query, examples, bs, pf);
		try {
			outputData(query, examples, bs, pf, "dblp.data");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
}
