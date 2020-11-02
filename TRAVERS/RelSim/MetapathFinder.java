package RelSim;

import Structures.*;

import java.util.*;

public class MetapathFinder {

    public Integer D = 0;
    public Set<Integer> HasWalked = new HashSet<>();

    public metapathModel newMetaPath(Integer bgnode)
    {
        List<Integer> conc = new ArrayList<Integer>(); conc.clear();
        List<Integer> rela = new ArrayList<Integer>(); rela.clear();
        conc.add(bgnode);
        return new metapathModel(conc, rela);
    }

    public metapathModel appendMetaPath(metapathModel origin, Integer node, Integer edge)
    {
        List<Integer> conc = (List<Integer>) ((ArrayList)origin.getConcepts()).clone();
        List<Integer> rela = (List<Integer>) ((ArrayList)origin.getRelations()).clone();
        conc.add(node); rela.add(edge);
        return new metapathModel(conc, rela);
    }

    public void Find_DFS(Integer bg, Integer len, Integer len_limit, metapathModel mm, Set<metapathModel> mp)
    {
        mp.add(mm);
        if(len >= len_limit) return;
        HashMap<Integer, List<Integer>> nb = graphModel.getNeighbours(bg);
        if(null == nb) return;
        HasWalked.add(bg);
        for(Map.Entry<Integer, List<Integer>> kv : nb.entrySet())
        {
            Integer edge = kv.getKey();
            for(Integer node : kv.getValue())
            {
                if(HasWalked.contains(node)) continue;
                Find_DFS(node, len + 1, len_limit, appendMetaPath(mm, node, edge), mp);
            }
        }
        HasWalked.remove(bg);
    }

    public void Find_BFS(Integer bg, Integer len, Set<metapathModel> mp)
    {
        Map<Integer, ArrayList<metapathModel> > now = new HashMap<Integer, ArrayList<metapathModel>>();
        Map<Integer, ArrayList<metapathModel> > last = new HashMap<Integer, ArrayList<metapathModel>>(); last.clear();
        ArrayList<metapathModel> justforbg = new ArrayList<metapathModel>(); justforbg.clear(); justforbg.add(newMetaPath(bg));
        last.put(bg, justforbg); mp.add(newMetaPath(bg));

        for(Integer steps = 1; steps <= len; ++steps)
        {
            now.clear();
            for(Map.Entry<Integer, ArrayList<metapathModel> > kv_last : last.entrySet())
            {
                Integer nd = kv_last.getKey();
                ArrayList<metapathModel> roads = kv_last.getValue();
                HashMap<Integer, List<Integer>> nb = graphModel.getNeighbours(nd);
                if(nb == null) continue;
                for(Map.Entry<Integer, List<Integer> > kv_nb : nb.entrySet())
                {
                    Integer edge = kv_nb.getKey();
                    List<Integer> outnodes = kv_nb.getValue();
                    for(Integer node : outnodes)
                    {
                        if(!now.containsKey(node))
                        {
                            ArrayList<metapathModel> ss = new ArrayList<metapathModel>();
                            ss.clear();
                            now.put(node, ss);
                        }
                        ArrayList<metapathModel> tmp = now.get(node);
                        for(metapathModel mpm : roads) if(!mpm.concepts.contains(node))tmp.add(appendMetaPath(mpm, node, edge));
                        now.put(node, tmp);
                    }
                }
            }
            last.clear(); last = (Map<Integer, ArrayList<metapathModel>>) ((HashMap<Integer, ArrayList<metapathModel>>) now).clone();
            for(ArrayList<metapathModel> mpm_now : now.values()) mp.addAll(mpm_now);
        }
    }

    public Boolean checkRepeat(List<Integer> lista, List<Integer> listb)
    {
        if(lista.size() <= 1) return false;
        for(Integer i=0; i + 1<lista.size(); ++i) if(listb.contains(lista.get(i))) return true;
        return false;
    }

    public Set<metapathModel> getPathUnion(Set<metapathModel> mpa, Set<metapathModel> mpb)
    {
        Set<metapathModel> result = new HashSet<metapathModel>(); result.clear();
        Map<Integer, HashSet<metapathModel> > rec = new HashMap<Integer, HashSet<metapathModel>>(); rec.clear();
        for(metapathModel mp_a : mpa)
        {
            List<Integer> conc_a = mp_a.getConcepts();
            Integer tail = conc_a.get(conc_a.size() - 1);
            if(!rec.containsKey(tail))
            {
                HashSet<metapathModel> tmp = new HashSet<metapathModel>();
                tmp.clear();
                rec.put(tail, tmp);
            }
            HashSet<metapathModel> ss = rec.get(tail);
            ss.add(mp_a);
            rec.put(tail, ss);
        }
        for (metapathModel mp_b : mpb)
        {
            List<Integer> conc_b = mp_b.getConcepts();
            if (!rec.containsKey(conc_b.get(conc_b.size() - 1))) continue;
            HashSet<metapathModel> ss = rec.get(conc_b.get(conc_b.size() - 1));
            for(metapathModel mp_a : ss)
            {
                List<Integer> conc_a = mp_a.getConcepts();
                if(checkRepeat(conc_a, conc_b) || checkRepeat(conc_b, conc_a)) continue;
                List<Integer> concepts = new ArrayList<>();
                List<Integer> relations = new ArrayList<>();
                List<Integer> rela_a = mp_a.getRelations();
                metapathModel mp_b_r = mp_b.getReverse();
                List<Integer> relb_r = mp_b_r.getRelations();
                List<Integer> conb_r = mp_b_r.getConcepts();
                relations.addAll(rela_a);
                relations.addAll(relb_r);
                for(int i = 0; i < conc_a.size() - 1; i ++){
                    concepts.add(conc_a.get(i));
                }
                concepts.addAll(conb_r);
                metapathModel mp = new metapathModel(concepts, relations);

                result.add(mp);
            }
        }
        return result;
    }

    public Set<metapathModel> getMetaPathFromOnePath(metapathModel path)
    {
        List<Integer> concepts = path.getConcepts();
        List<Integer> relation = path.getRelations();

        Set<metapathModel> result = new HashSet<metapathModel>(); result.clear();
        List< ArrayList<Integer> > now = new ArrayList< ArrayList<Integer> >(); now.clear();
        List< ArrayList<Integer> > last = new ArrayList< ArrayList<Integer> >(); last.clear();
        ArrayList<Integer> justforbg = new ArrayList<Integer>(); justforbg.clear(); justforbg.add(0);
        last.add(justforbg);

        for(Integer node : concepts)
        {
            now.clear();
            Set<Integer> types = typeModel.getTypes(node);
            //System.out.println("Types of " + node.toString() + " : " + types.toString());
            for (ArrayList<Integer> pt : last)
                for (Integer typ : types)
                {
                    ArrayList<Integer> tmppath = (ArrayList<Integer>) ((ArrayList<Integer>) pt).clone();
                    tmppath.add(typ);
                    now.add(tmppath);
                }
            last.clear(); last = (List<ArrayList<Integer>>) ((ArrayList<ArrayList<Integer>>) now).clone();
        }

        for(ArrayList<Integer> pt : now)
        {
            List<Integer> conc = new ArrayList<Integer>(); conc.clear();
            List<Integer> rela = new ArrayList<Integer>(); rela.clear();
            for(Integer id=1; id < pt.size(); ++id) conc.add(pt.get(id));
            for(Integer id=0; id < relation.size(); ++id) rela.add(relation.get(id));
            result.add(new metapathModel(conc, rela));
        }

        return result;
    }

    public Set<metapathModel> Path2MetaPath(Set<metapathModel> path)
    {
        Set<metapathModel> result = new HashSet<metapathModel>(); result.clear();
        for(metapathModel pt : path) result.addAll(getMetaPathFromOnePath(pt));
        return result;
    }

    public Set<metapathModel> returnPath(Integer a, Integer b, Integer len)
    {
        D = len;

        Set<metapathModel> metapatha = new HashSet<metapathModel>();
        Set<metapathModel> metapathb = new HashSet<metapathModel>();
        metapatha.clear(); Find_DFS(a, 0, D / 2, newMetaPath(a), metapatha);
        //System.out.println("Size of path_A : " + metapatha.size());
        metapathb.clear(); Find_DFS(b, 0, D - D / 2,newMetaPath(b), metapathb);
        //System.out.println("Size of path_B : " + metapathb.size());

        Set<metapathModel> union = getPathUnion(metapatha, metapathb);

        return union;
    }

    public void findPath(Integer a, Integer b, Integer len, Map<List<Integer>, Set<metapathModel>> MetaPath_Cluster)
    {
        D = len;

        Set<metapathModel> metapatha = new HashSet<metapathModel>();
        Set<metapathModel> metapathb = new HashSet<metapathModel>();
        metapatha.clear(); Find_DFS(a, 0, D / 2, newMetaPath(a), metapatha);
        //Find_BFS(a, D / 2, metapatha);
        //System.out.println("Size of path_A : " + metapatha.size());
        metapathb.clear(); Find_DFS(b, 0, D - D / 2, newMetaPath(b), metapathb);
        //Find_BFS(b, D - D / 2, metapathb);
        //System.out.println("Size of path_B : " + metapathb.size());

        Set<metapathModel> union = getPathUnion(metapatha, metapathb);
        for(metapathModel mp : union)
        {
            List<Integer> now_rel = mp.getRelations();
            if( !MetaPath_Cluster.containsKey(now_rel) )
            {
                Set<metapathModel> tmp = new HashSet<>(); tmp.clear();
                MetaPath_Cluster.put(now_rel, tmp);
            }
            Set<metapathModel> now = MetaPath_Cluster.get(now_rel);
            now.add(mp); MetaPath_Cluster.put(now_rel, now);
        }
        //System.out.println("Size of path_Union : " + union.size());
        //Set<metapathModel> result = Path2MetaPath(union);
        //System.out.println("Size of MetaPath : " + result.size());
        //List<metapathModel> ret = new ArrayList<metapathModel>(); ret.clear();
        //ret.addAll(result);

    }

    public metapathModel combine(Set<metapathModel> mpset)
    {
        metapathModel result = new metapathModel();
        Integer len = 0;
        for(metapathModel mp : mpset) len = Math.max(len, mp.relations.size());
        for(Integer i = 0; i <= len; ++ i)
        {
            boolean f = true;
            if(i .equals(len) ) f = false;
            Set<Integer> nodeSet = new HashSet<Integer>(); nodeSet.clear();
            for(metapathModel mp : mpset)
            {
                if(f) { result.relations.add(mp.relations.get(i)); f = false; }
                nodeSet.add(mp.concepts.get(i));
            }
            result.concepts.add(OntologyTree.getNodeSetLCA(nodeSet));
        }
        return result;
    }

    public void main(String[] args)
    {
        //List<metapathModel> ans = findPath(1, 2, 2);
    }

}
