package Path;

import GraphData.GraphOntGetterM;
import GraphData.Ontology;
import GraphData.RelationIndex;
import JDBCUtils.JdbcUtil;

import java.util.*;

/**
 * Find paths (both mp and rp) based on bi-directional BFS.
 *Intuitively, it should be faster than DFS and BFS in small-world graphs with nodes of tremendous degree, e.g., the
 * entity of USA
 */

public class BiBFSFinder implements PathFinder{
    int diameter = 4;

    public void setDiameter(int diameter){
        this.diameter = diameter;
    }

    @Override
    public Set<RelationPath> findRelationPath(int query, List<Integer> examples) {
        if(examples != null && examples.size() > 0){
            Set<RelationPath> set = rpFind(query, examples.get(0), this.diameter);
            for(int i = 1; i < examples.size(); i ++){
                set.retainAll(rpFind(query, examples.get(i), this.diameter));
            }
            return set;
        }
        else
            return null;
    }

    @Override
    public Set<MetaPath> findMetaPath(int query, List<Integer> examples) { // meta path的交并集问题仍然待定，先不实现这一部分
        Set<MetaPath> result = new HashSet<>();
        for(int example : examples){
            result.addAll(getMP(getPathsMap(query, example, diameter)));
        }

        return result;
    }



    private static Set<MetaPath> getMPByRP(RelationPath rp, Set<List<Integer>> paths){
        Set<MetaPath> result = new HashSet<>();

        for(List<Integer> path : paths){
            List<List<Integer>> conceptss_i = new ArrayList<>();
            for(int entity : path){
                if(conceptss_i.size() == 0){ // 起点的类直接给owl:Thing就行
                    List<Integer> concepts = new ArrayList<>();
                    if(JdbcUtil.URL.contains("yago")){
                        concepts.add(4832388);
                    }
                    else if(JdbcUtil.URL.contains("dbpedia")){
                        concepts.add(3481453);
                    }

                    conceptss_i.add(concepts);
                }
                else{
                    List<List<Integer>> nconceptss = new ArrayList<>();
                    Set<Integer> types = null;
                    /*if(JdbcUtil.URL.contains("dbpedia")){
                        types = GraphOntGetterM.classOfEntityByID(entity);
                    }
                    else{
                        types = GraphOntGetterM.classOfEntityByID_yagoSimple(entity);
                    }*/
                    types = GraphOntGetterM.classOfEntityByID(entity);

                    for(int type : types){
                        for(int i = 0; i < conceptss_i.size(); i ++){
                            List<Integer> concepts = new ArrayList<>();
                            concepts.addAll(conceptss_i.get(i));
                            concepts.add(type);
                            nconceptss.add(concepts);
                        }
                    }

                    conceptss_i = nconceptss;
                }
            }

            for(List<Integer> concepts : conceptss_i){
                // System.out.println("concepts: " + concepts);
                MetaPath mp = new MetaPath(concepts, rp.getRelations());
                result.add(mp);
            }

        }

        return result;
    }

    private static Set<MetaPath> getMP(Map<RelationPath, Set<List<Integer>>> pathsMap){
        Set<MetaPath> result = new HashSet<>();

        for(RelationPath rp : pathsMap.keySet()){
            result.addAll(getMPByRP(rp, pathsMap.get(rp)));
        }

        return result;
    }

    /**
     *
     * @param a
     * @param b
     * @param len
     * @return all path instances linking a and b with length no longer than len, indexed by RelationPath
     */
    private static Map<RelationPath, Set<List<Integer>>> getPathsMap(int a, int b, int len){
        Map<RelationPath, Map<Integer, List<List<Integer>>>> mapa, mapb;
        mapa = getHalfRP(a, len/2);
        mapb = ((len%2)== 0)?getHalfRP(b, len/2):getHalfRP(b, len/2 + 1);

        //System.out.println("mapa: " + mapa);
        //System.out.println("mapb: " + mapb);


        Map<RelationPath, Set<List<Integer>>> result = new HashMap<>();
        for(RelationPath rpa : mapa.keySet()){
            for(RelationPath rpb : mapb.keySet()){
                Set<Integer> seta = mapa.get(rpa).keySet();
                Set<Integer> setb = mapb.get(rpb).keySet();

                Map<Integer, List<List<Integer>>> map0 = mapa.get(rpa);
                Map<Integer, List<List<Integer>>> map1 = mapb.get(rpb);

                if(seta.size() > setb.size()){ // 交换seta和setb，保证setb更大
                    Set<Integer> temp = setb;
                    setb = seta;
                    seta = temp;
                }

                for(int e : seta){
                    if(setb.contains(e)){
                        List<Integer> lista = rpa.getRelations();
                        List<Integer> listb = rpb.getReverse().getRelations();
                        List<Integer> nlist = new ArrayList<>();
                        nlist.addAll(lista);
                        nlist.addAll(listb);
                        //result.add(new RelationPath(nlist));

                        List<List<Integer>> pathsa = map0.get(e);
                        List<List<Integer>> pathsb = map1.get(e);

                        Set<List<Integer>> paths = new HashSet<>();
                        for(List<Integer> patha : pathsa){
                            for(List<Integer> pathb : pathsb){
                                List<Integer> npath = new ArrayList<>();
                                npath.addAll(patha);
                                boolean flag = true;
                                for(int i = pathb.size() - 2; i >= 0; i --){
                                    //npath.add(pathb.get(i));
                                    if(!npath.contains(pathb.get(i))){ // 这里还需要再去一次环路，因为双向bfs只保证一半没环路，没有保证拼起来也没有环路
                                        npath.add((pathb.get(i)));
                                    }
                                    else{
                                        flag = false;
                                        break;
                                    }
                                }
                                if(flag)
                                    paths.add(npath);
                            }
                        }

                        result.put(new RelationPath(nlist), paths);

                    }
                }
            }
        }

       return result;
    }

    private static Set<RelationPath> rpFind(int a, int b, int len){
        Map<RelationPath, Map<Integer, List<List<Integer>>>> mapa, mapb;
        //Map<RelationPath, Set<Integer>> mapa = new HashMap<>(), mapb = new HashMap<>();
        mapa = getHalfRP(a, len/2);
        mapb = ((len%2)== 0)?getHalfRP(b, len/2):getHalfRP(b, len/2 + 1);



        Set<RelationPath> result = new HashSet<>();
        for(RelationPath rpa : mapa.keySet()){
            for(RelationPath rpb : mapb.keySet()){
                Set<Integer> seta = mapa.get(rpa).keySet();
                Set<Integer> setb = mapb.get(rpb).keySet();

                if(seta.size() > setb.size()){ // 交换seta和setb，保证setb更大
                   Set<Integer> temp = setb;
                   setb = seta;
                   seta = temp;
                }

                for(int e : seta){
                    if(setb.contains(e)){
                        List<Integer> lista = rpa.getRelations();
                        List<Integer> listb = rpb.getReverse().getRelations();
                        List<Integer> nlist = new ArrayList<>();
                        nlist.addAll(lista);
                        nlist.addAll(listb);
                        result.add(new RelationPath(nlist));

                        break;
                    }
                }
            }
        }

        return result;
    }

    @Deprecated
    // 直接双向bfs搜meta-path对yago这种数据来说是不现实的
    private static Map<MetaPath, Set<Integer>> getHalfMP(int a, int len, boolean query){
        Map<MetaPath, Map<Integer, List<Set<Integer>>>> map = new HashMap<>();
        Map<Integer, List<Set<Integer>>> init = new HashMap<>();
        //List<List<Integer>> ilist = new ArrayList<>();
        List<Set<Integer>> ilist = new LinkedList<>();
        Set<Integer> iid = new HashSet<>();
        iid.add(a);
        ilist.add(iid);
        init.put(a, ilist);

        if(query){ // 如果是query的话，则只任意保留一个a的类(owl:Thing)，否则保留全部a的类
            List<Integer> concepts = new ArrayList<>();
            if(JdbcUtil.URL.contains("yago"))
                concepts.add(4832388);
            else if(JdbcUtil.URL.contains("dbpedia"))
                concepts.add(3481453);

            map.put(new MetaPath(concepts, new ArrayList<Integer>()), init);
        }
        else{
            Set<Integer> types = GraphOntGetterM.classOfEntityByID(a);
            for(int type : types){
                List<Integer> concepts = new ArrayList<>();
                concepts.add(type);

                map.put(new MetaPath(concepts, new ArrayList<Integer>()), init);
            }
        }

        Map<MetaPath, Set<Integer>> result = new HashMap<>();
        //op_count += 5;
      /*  for(int i = 0; i < len; i ++){
            Map<MetaPath, Map<Integer, List<Set<Integer>>>> map_i = new HashMap<>();
            //System.out.println("i: " + i);
            for(MetaPath mp : map.keySet()){
                Map<Integer, List<Set<Integer>>> nodesMap = map.get(mp);
                Set<Integer> relations = GraphModelM.getAllRelations(nodesMap.keySet());
                //op_count += 3*nodesMap.keySet().size();
                //System.out.println("relations: " + relations);
                for(int r : relations){
                    List<Integer> rs = new ArrayList<>();
                    rs.addAll(mp.getRelations());
                    rs.add(r);
                    //op_count += (rp.getRelations().size() + 4);
                    Map<Integer, List<Set<Integer>>> objsMap = new HashMap<>();
                    for(int node : nodesMap.keySet()){
                        List<Set<Integer>> idss = nodesMap.get(node);
                        for(Set<Integer> ids : idss){
                            List<Integer> wtf = GraphModelM.getObjects(node, r, ids);
                            if(wtf != null){
                                //op_count += wtf.size()*ids.size();
                                for(int w : wtf){
                                    if(objsMap.containsKey(w)){
                                        List<Set<Integer>> l = objsMap.get(w);
                                        Set<Integer> nids = new HashSet<>();
                                        //System.out.println(ids.size());
                                        nids.addAll(ids);
                                        nids.add(w);
                                        l.add(nids);
                                        objsMap.put(w, l);
                                    }
                                    else{
                                        List<Set<Integer>> l = new LinkedList<>();
                                        Set<Integer> nids = new HashSet<>();
                                        nids.addAll(ids);
                                        nids.add(w);
                                        l.add(nids);
                                        objsMap.put(w, l);
                                    }
                                }
                            }
                        }

                    }


                    map_i.put(path, objsMap);

                    result.put(path, objsMap.keySet());
                }
            }
            map = map_i;

        }*/

        return result;
    }

    /**
     *  返回的结果包括所有小于等于len的
     * @param a
     * @param len
     * @return
     */
    private static Map<RelationPath, Map<Integer, List<List<Integer>>>> getHalfRP(int a, int len){ // 从某一顶点BFS一半的长度以内所得到的结果(去环路)
        //System.out.println("a: " + a + "  len " + len);
        Map<RelationPath, Map<Integer, List<List<Integer>>>> map = new HashMap<>();

        Map<Integer, List<List<Integer>>> init = new HashMap<>();
        //List<List<Integer>> ilist = new ArrayList<>();
        List<List<Integer>> ilist = new LinkedList<>();
        List<Integer> iid = new ArrayList<>();
        iid.add(a);
        ilist.add(iid);
        init.put(a, ilist);
        map.put(new RelationPath(new ArrayList<Integer>()), init);
        Map<RelationPath, Map<Integer, List<List<Integer>>>> result = new HashMap<>();
        result.put(new RelationPath(new ArrayList<Integer>()), init);
        //op_count += 5;
        for(int i = 0; i < len; i ++){
            Map<RelationPath, Map<Integer, List<List<Integer>>>> map_i = new HashMap<>();
            //System.out.println("i: " + i);
            for(RelationPath rp : map.keySet()){
                Map<Integer, List<List<Integer>>> nodesMap = map.get(rp);
                Set<Integer> relations = RelationIndex.getAllRelations(nodesMap.keySet());
                //op_count += 3*nodesMap.keySet().size();
                //System.out.println("relations: " + relations);
                for(int r : relations){
                    List<Integer> rs = new ArrayList<>();
                    rs.addAll(rp.getRelations());
                    rs.add(r);
                    //op_count += (rp.getRelations().size() + 4);
                    RelationPath path = new RelationPath(rs);
                    Map<Integer, List<List<Integer>>> objsMap = new HashMap<>();
                    for(int node : nodesMap.keySet()){
                        List<List<Integer>> idss = nodesMap.get(node);
                        for(List<Integer> ids : idss){
                            Set<Integer> idsSet = new HashSet<>();
                            idsSet.addAll(ids);
                            List<Integer> wtf = RelationIndex.getObjects(node, r, idsSet);
                            if(wtf != null){
                                //op_count += wtf.size()*ids.size();
                                for(int w : wtf){
                                    if(objsMap.containsKey(w)){
                                        List<List<Integer>> l = objsMap.get(w);
                                        List<Integer> nids = new ArrayList<>();
                                        //System.out.println(ids.size());
                                        nids.addAll(ids);
                                        nids.add(w);
                                        l.add(nids);
                                        objsMap.put(w, l);
                                    }
                                    else{
                                        List<List<Integer>> l = new LinkedList<>();
                                        List<Integer> nids = new ArrayList<>();
                                        nids.addAll(ids);
                                        nids.add(w);
                                        l.add(nids);
                                        objsMap.put(w, l);
                                    }
                                }
                            }
                        }

                    }


                    map_i.put(path, objsMap);

                    result.put(path, objsMap);
                }
            }
            map = map_i;

        }

        return result;
    }

    public static void main(String[] args){
        RelationIndex.initializeMap();
        Ontology.Initialize();
        GraphOntGetterM.initializeMap();
        /*Map<RelationPath, Set<Integer>> map = getHalf(2291382, 2);
        for(RelationPath key : map.keySet()){
            System.out.println(key + " " + map.get(key).size());
        }

        System.out.println(map.keySet().size());*/

        BiBFSFinder bifinder = new BiBFSFinder();
        List<Integer> examples = new ArrayList<>();
        //examples.add(799040);
        //examples.add(899483);
        //examples.add(2125240);

        examples.add(1260783); //某个跟627504同类的人

        //examples.add(3462992);

        bifinder.setDiameter(3);
        //2291382/4103860/627504
        //Set<RelationPath> rps = rpFindAll(2291382, 2125240, 4);
        long start = System.currentTimeMillis();
        Set<RelationPath> rps = bifinder.findRelationPath(642012, examples);
        Map<RelationPath, Set<List<Integer>>> map = getPathsMap(642012, 1260783, 3);
        long end = System.currentTimeMillis();

        //System.out.println(map);
        int numOfpaths = 0;
        for(RelationPath key : map.keySet()){
            numOfpaths += map.get(key).size();
        }
        System.out.println(numOfpaths);
        //System.out.println(rps);
        System.out.println(rps.size());
        System.out.println("time : " + (end - start));

        start = System.currentTimeMillis();
        //Set<MetaPath> mps = getMP(map);
        Set<MetaPath> mps = bifinder.findMetaPath(642012, examples);
        end = System.currentTimeMillis();

        System.out.println(mps.size());
        System.out.println("time : " + (end - start));
    }

}
