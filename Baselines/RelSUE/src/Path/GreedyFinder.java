package Path;

import java.util.*;

import GraphData.*;
import NegativeSampler.BasicSampler;

public class GreedyFinder {
	//static final int LEN = 4;
    static final double THRESHOLD = 0.2; // 0.2 for dbpedia, 0.1 for DBLP
    static int K = 10;

    public static void setK(int num){
        K = num;
    }

    public static void setNexamples(){ //每次跑完负例要复位
        nexamples = new ArrayList<>();
    }

    public void reset(){
        OpenList = new ArrayList<>();
        paths = new HashSet<>();
    }

	public class Node{
		public MetaPath mp = null;
		public Map<Integer, Map<Integer, List<Set<Integer>>>> map0 = new HashMap<>(); //examples
		public Map<Integer, Map<Integer, Integer>> map1 = new HashMap<>(); //representative entities

		private double score = 0;
		private double inner_avg = 0;
		private double external_avg = 0;
		public double H = 0;
		public Node parent;

		public Node(MetaPath mp, Map<Integer, Map<Integer, List<Set<Integer>>>> map0, Map<Integer, Map<Integer, Integer>> map1){
		    this.map0 = map0;
		    this.map1 = map1;
		    this.mp = mp;
            if(this.map0.keySet().size() != 1){
                List<Integer> pes = new ArrayList<>();
                pes.addAll(this.map0.keySet());
                for(int i = 0; i < pes.size() - 1; i ++){
                    for(int j = i + 1; j < pes.size(); j ++){
                        this.inner_avg += weightedJaccard_internal(this.map0.get(pes.get(i)), this.map0.get(pes.get(j)));
                    }
                }
                this.inner_avg = 2*this.inner_avg/(pes.size()*(pes.size()-1));
            }
            else
                this.inner_avg = 0;

            for(int i : this.map0.keySet()){
                for(int j : this.map1.keySet()){
                    this.external_avg += weightedJaccard_external(this.map0.get(i), this.map1.get(j));
                }
            }
            if( this.map1.keySet().size() != 0){
                this.external_avg = 2*this.external_avg/(this.map0.keySet().size() * this.map1.keySet().size());
            }
            else
                this.external_avg = 0;
        }

		public boolean isLegal(){ // if there is no seed satisfy the partial meta-path, it will not be extended
			for(int i : map0.keySet()){
				if(map0.get(i).size() != 0)
					return true;
			}

			return false;
		}
		public boolean isCompleted(int query){// if one seed accesses the query, then it's completed
			//if(mp.length() >= LEN)
			//	return true;
            if(null == map0 || map0.size() == 0)
                return false;
            for(int key : map0.keySet()){
                Map<Integer, List<Set<Integer>>> m = map0.get(key);
                if(m.containsKey(query))
                    return true;
            }

            return false;

		}
		public double getScore(){
		    return  this.score;
        }
        public void setScore(double score){
		    this.score = score;
        }
        public String toString(){
		    return this.mp.toString() + this.score;
        }
	}

	public List<Node> OpenList = new ArrayList<>();
	//public List<Node> CloseList = new ArrayList<>();
	public static List<Integer> nexamples = new ArrayList<>();

	public Set<MetaPath> paths = new HashSet<>();
	/**
	 * 
	 * @param query
	 * @param examples
	 * @return k meta paths (k <= K)
	 */
	public Set<MetaPath> findMetaPath_K(int query, List<Integer> examples) {
	    System.out.println("begin initialize...");
		OpenList.addAll(initialize(examples, query)); //负样本也在initialize过程中被指定
        System.out.println(OpenList);
        System.out.println("Begin finding...");
        long start = System.currentTimeMillis();
        while(OpenList.size() > 0){
            /*System.out.println("oplenlist: " + OpenList);
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            Node max = MaxS(OpenList, examples);

            if(null == max)
                break;
            Set<Integer> relations = nextRelations(max);
            OpenList.remove(max);

            for(int r : relations){
                if(paths.size() < K){
                    long start_ext = System.currentTimeMillis();
                    OpenList.addAll(Extend(max, r, query, examples)); //慢的地方
                    long end_ext = System.currentTimeMillis();
                    if((end_ext - start_ext) > 1000){
                        System.out.println("node: " + max);
                        int size = 0;
                        for(int pe : max.map0.keySet()){
                            size += max.map0.get(pe).size();
                        }
                        System.out.println("size: " + size);
                        System.out.println("relation: " + LabelGetter.get(Math.abs(r)) + " " + r);
                    }
                }
                else break;
            }
        }
		System.out.println("time: " + (System.currentTimeMillis()-start));
        System.out.println("oplenlist left: " + OpenList.size());
		return paths;
	}

    public Set<MetaPath> findMetaPath(int query, List<Integer> examples) {
        ////不要求所有样本都具有这些metapath，但是要求所有样本都具有这些metapath对应的relationpath,但是不要求所有样本
        //均能经由该relation path达到query， 只要有一个到达即可
        OpenList.addAll(initialize(examples, query)); //负样本也在initialize过程中被指定
        System.out.println(OpenList);
        System.out.println("Begin finding...");
        long start = System.currentTimeMillis();
        while(OpenList.size() > 0){
            Node max = MaxS(OpenList, examples);
            if(null == max)
                break;

            if(max.isCompleted(query)){
                paths.add(max.mp);
                System.out.println(max.mp);
                System.out.println(max.getScore());
            }

            Set<Integer> relations = nextRelations(max);
            //System.out.println(relations);
            OpenList.remove(max);

            for(int r : relations){
                //System.out.println("max: " + max.mp);
                //System.out.println("r: " + r);
                OpenList.addAll(Extend(max, r, query, examples)); //慢的地方
            }
        }
        System.out.println("time: " + (System.currentTimeMillis()-start));
        return paths;
    }

	public Set<Integer> nextRelations(Node node){
        /**
         * 获取当前node下一步扩展的边
         * 目前方式是直接穷举保留所有的边，这种方式有待改进（即可以为不同relation计算一个打分，超过某一个阈值的才保留）
         * 可以考虑的方式比如：1）对于某一边，考虑各例子所到达点集合的点经过该边转移的到的点集间的相似性
         * （这里的关键在于，在某种表示下经过边转移后两点表示的差异会发生变化）
         * 2）可以考虑该边是否使目前到达点更接近目标点（即查询实体）
         * 关于第2）点，也可以放到下一步体现，即选定边扩展后得到的了新的partial meta-path，可以基于第二点对这个meta-path
         * 进行打分。因为计算由边到达的点与目标点的相似度更容易，具体来说，因为如果想直接基于出发点（即当前node的到达点）
         * 与选择的边来打分的话，需要预测由改变到达的点，目前这个信息似乎很难直接编码进来
         *
         * R可以很好地反映第一点，但问题在于基于R得到的转移后的表示似乎并不直接适用于得到与目标点的关系
         */
        Map<Integer, Map<Integer, List<Set<Integer>>>> map0 = node.map0;
        Set<Integer> relations = new HashSet<>();
        /*if(node.mp.getLastRelation() == 3480866){
            relations.add(-3480866);
            return relations;
        }
        if(node.mp.getLastRelation() == -3480866){
            relations.add(3480832);
            return relations;
        }*/
        for(int pe : map0.keySet()){
            Map<Integer, List<Set<Integer>>> m = map0.get(pe);
            //System.out.println("keyset: " + m.keySet());
            for(int i : m.keySet()){
                for(Set<Integer> l : m.get(i)){
                    relations.addAll(GraphModelM.getAllRelations(i, l));
                }

            }

        }

        relations.remove(-3480819);
        relations.remove(-3480809);
        relations.remove(-3480812);
        relations.remove(-3480813);
        relations.remove(-3480846);
        relations.remove(-3480811);
        relations.remove(-3480933);
        relations.remove(-3480810);
        relations.remove(-3480860);
        relations.remove(-3480850);
        relations.remove(-3480951);

	    return relations;
    }

	public Node MaxS(List<Node> nodes, List<Integer> examples){
	   if(nodes.size() > 0){
           Node max = nodes.get(0);
           for(int i = 1; i < nodes.size(); i ++){
               if(nodes.get(i).getScore() > max.getScore()){
                   max = nodes.get(i);
               }
               else if (nodes.get(i).getScore() == max.getScore()){
                   if(RelationsFreq.getFreq(nodes.get(i).mp.getLastRelation()) < RelationsFreq.getFreq(max.mp.getLastRelation()))
                       max = nodes.get(i);
               }
               /*else if(nodes.get(i).getScore() == max.getScore()){
                   if(max.mp.getReverse().getConcepts().get(0) == 3481856){
                       if(nodes.get(i).mp.getReverse().getConcepts().get(0) != 3481856){
                           max = nodes.get(i);
                       }
                   }
               }*/
               /*if(nodes.get(i).getScore() > (max.getScore() + 0.02)){
                  max = nodes.get(i);
               }
               else if(nodes.get(i).getScore() >= max.getScore()){
                   //优先选点少的
                   int sizei = 0, sizem = 0;
                   for(int pe : examples){
                       sizei += nodes.get(i).map0.get(pe).size();
                       sizem += max.map0.get(pe).size();
                   }
                   if(sizei < sizem)
                       max = nodes.get(i);
               }*/

           }
           return max;
       }
       else return null;
    }

	public List<Node> Extend(Node parent, int relation, int query, List<Integer> examples) {
	    List<Node> nodes = new ArrayList<>();
	    //if(parent.mp.length() >= LEN)/////////////////////////////////////////////////////////////////////////
	    //    return  nodes;

        //if(parent.getScore() < THRESHOLD/0.6)
        //    return  nodes;

        Map<Integer, Map<Integer, List<Set<Integer>>>> map0OfP = parent.map0;
        Map<Integer, Map<Integer, Integer>> map1OfP = parent.map1;



        //以下这段代码都是为了获取下一步有可能到达的基础类，效率比较低下，后面可以考虑直接从schema获取
        /*Set<Integer> allEntities = new HashSet<>(); // 所有可以由map0原有基础上再由relation到达的实体
        for(int i : map0OfP.keySet()){
            Map<Integer, List<Set<Integer>>> m = map0OfP.get(i);
            for(int j : m.keySet()){
                for(Set<Integer> l : m.get(j)){
                    List<Integer> objs = GraphModelM.getObjects(j, relation, l);
                    if(objs != null)
                        allEntities.addAll(objs);
                }

            }
        }
        Set<Integer> types = new HashSet<>(); // all candidate types
        for(int e : allEntities){
            types.addAll(GraphOntGetterM.basicClassOfEntityByID(e));
        }*/
        /*if(types.size() > 10){
            System.out.println("relation:" + LabelGetter.get(Math.abs(relation)));
            System.out.println("relation id:" + relation);
            System.out.println(types);
        }*/
        Map<Integer, Map<Integer, List<Set<Integer>>>> allObjs0 = new HashMap<>();
        for(int i : map0OfP.keySet()){
            Map<Integer, List<Set<Integer>>> m = map0OfP.get(i);
            Map<Integer, List<Set<Integer>>> newm = new HashMap<>();
            for(int j : m.keySet()){
                for(Set<Integer> l : m.get(j)){
                    List<Integer> objs = GraphModelM.getObjects(j, relation, l);
                    for(int obj : objs){
                        if(!newm.containsKey(obj)){
                            Set<Integer> set = new HashSet<>();
                            set.addAll(l);
                            set.add(obj);
                            List<Set<Integer>> list = new LinkedList<>();
                            list.add(set);
                            newm.put(obj, list);
                        }
                        else{
                            List<Set<Integer>> list = newm.get(obj);
                            Set<Integer> set = new HashSet<>();
                            set.addAll(l);
                            set.add(obj);
                            list.add(set);
                            newm.put(obj, list);
                        }
                    }

                }

            }
            allObjs0.put(i, newm);
        }

        Map<Integer, Map<Integer, Integer>> allObjs1 = new HashMap<>();
        for(int i : map1OfP.keySet()){
            Map<Integer, Integer> m = map1OfP.get(i);
            Map<Integer, Integer> newm = new HashMap<>();
            for(int j : m.keySet()){
                List<Integer> objs = GraphModelM.getObjects(j, relation);
                if(objs != null){
                    for(int obj : objs){
                        if(newm.containsKey(obj)){
                            int count = newm.get(obj);
                            count += m.get(j);
                            newm.put(obj, count);
                        }
                        else{
                            newm.put(obj, m.get(j));
                        }
                    }
                }

            }
            allObjs1.put(i, newm);
        }

        Set<Integer> types = new HashSet<>(); // all candidate types
        for(int key : allObjs0.keySet()){
            for(int e : allObjs0.get(key).keySet())
                types.addAll(GraphOntGetterM.basicClassOfEntityByID(e));
        }

        //For multi types
        Set<Integer> validTypes = Ontology.getallValidTypes(types);

        //For single types
        //Set<Integer> validTypes = new HashSet<>();
        //validTypes.addAll(types);


        //获取所有可能的type后进行扩展
        for(int type : validTypes){

            List<Integer> concepts = new ArrayList<>();
            List<Integer> relations = new ArrayList<>();
            concepts.addAll(parent.mp.getConcepts());
            relations.addAll(parent.mp.getRelations());

            concepts.add(type);
            relations.add(relation);
            MetaPath mp = new MetaPath(concepts, relations);

            Map<Integer, Map<Integer, List<Set<Integer>>>> map0 = new HashMap<>();
            for(int pe : map0OfP.keySet()){
                Map<Integer, List<Set<Integer>>> objm = allObjs0.get(pe);
                Map<Integer, List<Set<Integer>>> newm = new HashMap<>();

                for(int obj : objm.keySet()){
                    if(GraphOntGetterM.classOfEntityByID(obj).contains(type)){
                        newm.put(obj, objm.get(obj));
                    }
                }

                map0.put(pe, newm);
            }

            Map<Integer, Map<Integer, Integer>> map1 = new HashMap<>();
            for(int pe : map1OfP.keySet()){
                Map<Integer, Integer> objm = allObjs1.get(pe);
                Map<Integer, Integer> newm = new HashMap<>();

                for(int obj : objm.keySet()){
                    if(GraphOntGetterM.classOfEntityByID(obj).contains(type)){
                        newm.put(obj, objm.get(obj));
                    }
                }

                map1.put(pe, newm);
            }

            Node node = new Node(mp, map0, map1);
            double score = calculateNodeScore(node, query, examples);
            node.setScore(score);

            /*if(relation == 3480866){
                System.out.println("3480866: " + node );
            }
            if(relation == -3480866){
                System.out.println("-3480866: " + node );
            }*/
            //System.out.println("score: " + score);
            //System.out.println(node.mp);
            //if((score >= THRESHOLD && node.mp.length() < LEN) || (node.mp.length() == LEN && node.isCompleted(query)))////////////////////////////////////////////
            //    nodes.add(node);

            if(node.isCompleted(query) && paths.size() < K){
                int a = 0;
                for(int pe : node.map0.keySet()){
                    if(node.map0.get(pe).keySet().contains(query))
                        a ++;
                }

                if((double)a/(double)node.map0.size() >= 0.5){
                    paths.add(node.mp);
                    System.out.println("*" + node.mp);
                    System.out.println("*" + node.getScore());

                    if(paths.size() >= K)
                        break;
                }
            }

            if(score >= THRESHOLD)
                nodes.add(node);

        }

        return  nodes;
	}

	public List<Node> initialize(List<Integer> pexamples, int query){
		List<Node> nodes = new ArrayList<>();
	    Set<Integer> types = new HashSet<>();
		for(int example : pexamples){
		    System.out.println("example: " + example);
			types.addAll(GraphOntGetterM.basicClassOfEntityByID(example));
		}
		this.nexamples = getNexamples(types, pexamples, query);
		System.out.println("types:" + types);
        //For multi types
		//Set<Integer> validTypes = Ontology.getallValidTypes(types);

        //For single types
        Set<Integer> validTypes = new HashSet<>();
        validTypes.addAll(types);


        System.out.println("validTypes:" + validTypes);

		for(int type : validTypes){
		    List<Integer> concept = new ArrayList<>();
		    concept.add(type);
		    MetaPath mp = new MetaPath(concept);

		    Map<Integer, Map<Integer, List<Set<Integer>>>> map0 = new HashMap<>();
		    Map<Integer, Map<Integer, Integer>> map1 = new HashMap<>();

		    for(int p : pexamples){
                Map<Integer, List<Set<Integer>>> mm = new HashMap<>();
		        if(GraphOntGetterM.HasType(p, type)){
		            List<Set<Integer>> ll = new LinkedList<>();
		            Set<Integer> l = new HashSet<>();
		            l.add(p);
		            ll.add(l);
		            mm.put(p, ll);
                }
                map0.put(p, mm);
            }
            for(int n : nexamples){
                Map<Integer, Integer> mm = new HashMap<>();
                if(GraphOntGetterM.HasType(n, type)){
                    mm.put(n, 1);
                    System.out.println(LabelGetter.get(n));
                }
                map1.put(n, mm);
            }

            Node node = new Node(mp, map0, map1);
            node.setScore(calculateNodeScore(node, query, pexamples));
            nodes.add(node);
        }
        return nodes;

	}

	public double calculateNodeScore(Node node, int query, List<Integer> examples){
	    double result = 0;
	    /*int validCount = 0;
            int len = node.mp.length();
            for(int pe : node.map0.keySet()){
                if(node.map0.get(pe).size() != 0)
                    validCount ++;
            }*/


        //System.out.println(validCount + " " + innerJac_avg + " " + externalJac_avg + " " + len);
        //double result = (double)validCount/examples.size() * (0.4*innerJac_avg + 0.6*(1-externalJac_avg))*Math.pow(0.6, len);
        double innerJac_avg = node.inner_avg, externalJac_avg = node.external_avg;
        result = (0.4*innerJac_avg + 0.6*(1-externalJac_avg))*Math.pow(0.6, node.mp.length());
        //System.out.println(result);

        return result;
    }

	public List<Integer> getNexamples(Set<Integer> types, List<Integer> pexamples, int query){
		if(nexamples.size() > 0)
		    return nexamples;
		else{
            List<Integer> btypes = GraphOntGetterM.basicClassOfEntityBySet(types);
            System.out.println("btypes:::" + btypes);
            BasicSampler bs = new BasicSampler();
            List<Integer> list = new ArrayList<>();
            for(int i : btypes){
                if(list.size() <= 5){
                    list.addAll(bs.getSamples(i,query, pexamples, 5));
                }
                else break;
            }

            System.out.println("nexamples::::" + list);

            return list;
        }
	}

	public double weightedJaccard_internal(Map<Integer, List<Set<Integer>>> a, Map<Integer, List<Set<Integer>>> b){
	    if(a.size() == 0 || b.size() == 0)
            return 0;

        double result;
        double top = 0;
        double bottom = 0;

        Set<Integer> keySet = new HashSet<>();
        keySet.addAll(a.keySet());
        keySet.addAll(b.keySet());
        for(int i : keySet){
            if(a.containsKey(i) && b.containsKey(i)){
                if(a.get(i).size() >= b.get(i).size()){
                    top += (double)b.get(i).size();
                    bottom += (double)a.get(i).size();
                }
                /**
                 * fixed a bug here
                 */
                else{
                    top += (double)a.get(i).size();
                    bottom += (double)b.get(i).size();
                }
            }
            else if (b.containsKey(i)){ // only b
                top += (double)0;
                bottom += (double)b.get(i).size();
            }
            else{ // only a
                top += (double)0;
                bottom += (double)a.get(i).size();
            }
        }

        result = top/bottom;

        /*if(a.containsKey(2821978)){
            System.out.println("map a:" + a);
            System.out.println("map b:" + b);
            System.out.println("jac: " + result);
        }*/
        return result;
    }
    public double weightedJaccard_external(Map<Integer, List<Set<Integer>>> a, Map<Integer, Integer> b){
        if(a.size() == 0 || b.size() == 0)
            return 0;

        double result;
        double top = 0;
        double bottom = 0;

        Set<Integer> keySet = new HashSet<>();
        keySet.addAll(a.keySet());
        keySet.addAll(b.keySet());
        for(int i : keySet){
            if(a.containsKey(i) && b.containsKey(i)){
                if(a.get(i).size() >= b.get(i)){
                    top += (double)b.get(i);
                    bottom += (double)a.get(i).size();
                }
                /**
                 * fixed a bug here
                 */
                else{
                    top += (double)a.get(i).size();
                    bottom += (double)b.get(i);
                }
            }
            else if (b.containsKey(i)){ // only b
                top += (double)0;
                bottom += (double)b.get(i);
            }
            else{ // only a
                top += (double)0;
                bottom += (double)a.get(i).size();
            }
        }

        result = top/bottom;
        return result;
    }

	public double weightedJaccard(Map<Integer, Integer> a, Map<Integer, Integer> b){
		//System.out.println("a: " + a);
		//System.out.println("b: " + b);
	    if(a.size() == 0 || b.size() == 0)
		    return 0;

	    double result;
		double top = 0;
		double bottom = 0;

		Set<Integer> keySet = new HashSet<>();
		keySet.addAll(a.keySet());
		keySet.addAll(b.keySet());
		for(int i : keySet){
			if(a.containsKey(i) && b.containsKey(i)){
				if(a.get(i) >= b.get(i)){
					top += (double)b.get(i);
					bottom += (double)a.get(i);
				}
                /**
                 * fixed a bug here
                 */
                else{
                    top += (double)a.get(i);
                    bottom += (double)b.get(i);
                }
			}
			else if (b.containsKey(i)){ // only b
				top += (double)0;
				bottom += (double)b.get(i);
			}
			else{ // only a
				top += (double)0;
				bottom += (double)a.get(i);
			}
		}

		result = top/bottom;
		return result;
	}
	
	public static void main(String[] args) {
		GraphModelM.initializeMap();
		//GraphOntologyM.initializeMap();
        GraphClassInstancesM.initializeMap();
		Ontology.Initialize();
		RelationsFreq.initialize("dbpedia.freq");
       // RelationsFreq.initialize("dblp.freq");
		GraphOntGetterM.initializeMap();
        //GraphOntGetterM.initializeMap(3465220);
	    //int query = 219426;
		int query = 1125862;
        List<Integer> examples = new ArrayList<>();
        examples.add(2732463);
        examples.add(2071298);
        examples.add(2888488);
		//examples.add(2580708); // Jae Crowder
        //examples.add(450031);
		//examples.add(1924527); // KI
        //examples.add(1045885); // Waiters
        GreedyFinder gf = new GreedyFinder();
        System.out.println(gf.findMetaPath_K(query, examples));
	}


}
