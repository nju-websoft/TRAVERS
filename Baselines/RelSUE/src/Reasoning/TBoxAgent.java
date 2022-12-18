package Reasoning;

import GraphData.*;
import JDBCUtils.JdbcUtil;
import Path.MetaPath;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.util.*;

public class TBoxAgent {
    static Connection conn = JdbcUtil.getConnection();
    static int numOfAxiom0 = 20;
    static Set<Set<MetaPath>> axioms0 = new HashSet<>();
    static int maxComponents = 5; // maximal number of conjunction components per axiom
    //static int maxDepth = 4; // redundant since the information is contained in the length of proportion
    //// the proportion of different maximal depth for axioms, from 1 - maxDepth. Moreover, the number of conjunction components
    // can be 2-maxComponents, with the same probability
    static double[] proportion = {0.25, 0.25, 0.25, 0.25}; // the length of this list should be the same as maxDepth
    static Random rand = new Random();

    public static MetaPath sampleMP(int len, int rootID){
        List<Integer> concepts = new ArrayList<>(); // 除了最后一个，其他都是owl:Thing
        List<Integer> roles = new ArrayList<>();
        if(len > 0){
            concepts.add(3481453);
            for(int i = 0, currentID = rootID; i < len; i ++){
                Map<Integer, List<Integer>> m0 = FilteredGraphModelM.map.get(currentID);
                List<Integer> edges = new ArrayList<>();
                edges.addAll(m0.keySet());
                int eID = rand.nextInt(edges.size());
                int edge = edges.get(eID);
                roles.add(edge);

                // ########################################## 有可能走不到预期的长度
                List<Integer> nbs = m0.get(edge);
                if(nbs.size() > 0){
                    int nID = rand.nextInt(nbs.size());
                    currentID = nbs.get(nID);

                    if((len - 1) == i){
                        List<Integer> types = GraphOntGetterM.basicClassOfEntityByID(currentID);
                        concepts.add(types.get(0));
                    }
                    else concepts.add(3481453);
                }
                else{
                    concepts.add(3481453);
                    break;
                }
            }
        }
        else{
            List<Integer> types = GraphOntGetterM.basicClassOfEntityByID(rootID);
            concepts.add(types.get(0));
        }

        MetaPath mp = new MetaPath(concepts, roles);
        return mp;
    }

    public static void getAxiom0(int maxDepth, int num){
        Set<Set<MetaPath>> result = new HashSet<>();
        while(result.size() != num){
            System.out.println(result.size());
            int width = rand.nextInt(maxComponents - 1) + 2;
            int randomID, root;
            randomID = rand.nextInt(FilteredGraphModelM.allEntities.size());
            root = FilteredGraphModelM.allEntities.get(randomID);
            while(FilteredGraphModelM.map.get(root).size() < width){
                randomID = rand.nextInt(FilteredGraphModelM.allEntities.size());
                root = FilteredGraphModelM.allEntities.get(randomID);
            }
            Set<MetaPath> axiom = new HashSet<>();
            axiom.add(sampleMP(maxDepth, root));
            while(axiom.size() < width){
                System.out.println("width: " + axiom.size());
                int nextDepth = rand.nextInt(maxDepth) + 1;
                axiom.add(sampleMP(nextDepth, root));
            }
            result.add(axiom);
        }
        System.out.println(result.size());

        axioms0.addAll(result);
    }

    public static void outputAxiom0(){
        axioms0 = new HashSet<>();
        int maxdepth = 1;
        for(double p : proportion){
            int number = (int)(p*(double)numOfAxiom0);
            getAxiom0(maxdepth, number);
            maxdepth ++;
        }
        int i = 0;
        File file = new File("whatever.txt");
        try {
            FileWriter fw = new FileWriter(file);
            String content = "";
            for(Set<MetaPath> mps : axioms0){
                content += genAxiom0(mps, "Axiom0Concept" + i);
                content += "\n";
                i ++;
            }
            fw.write(content);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Axiom0 has the form whose left hand is a conjunction of several existential restrictions and right hand is bottom
     * Now each metapath in the set corresponds to a conjunction component, by ignoring the start class.
     * And for those whose length is greater than 1, the existential restriction can be described in a nested manner.
     * @param mps, set of different meta paths, each of which correspond to a conjunction component
     * @return The string of the Axiom in RDF/XML syntax, which can be directly copied into the owl file.
     */
    public static String genAxiom0(Set<MetaPath> mps, String className){
        String axiom = "<owl:Class rdf:about=\"http://dbpedia.org/ontology/" + className + "\">\n" +
                "<owl:equivalentClass>\n";
        if(mps.size() == 0){
           for(MetaPath mp : mps) {
               axiom += genNested(mp, mp.length());
               axiom += "\n";
           }
        }
        else{
            axiom += "<owl:Class>\n";
            axiom += "<owl:intersectionOf rdf:parseType=\"Collection\">\n";
            for(MetaPath mp : mps){
                axiom += genNested(mp, mp.length());
                System.out.println(genNested(mp, mp.length()));
                System.out.println("___________________________________");
                axiom += "\n";
            }
            axiom += "</owl:intersectionOf>\n";
            axiom += "</owl:Class>\n";
        }

        axiom += "</owl:equivalentClass>\n" +
                "<rdfs:subClassOf rdf:resource=\"http://www.w3.org/2002/07/owl#Nothing\"/>\n" +
                "</owl:Class>";

        return axiom;
    }

    private static String genNested(MetaPath mp, int left){
        int propertyID = mp.getRelations().get(mp.length() - left);
        String propertyDescription;
        String propertyName = LabelGetter.get(Math.abs(propertyID)).replace("wikidata.", "")
                .replace("<", "").replace(">","");
        if(propertyID > 0){
            propertyDescription = "<owl:onProperty rdf:resource=\"" + propertyName + "\"/>";
        }
        else{
            propertyDescription = "<owl:onProperty>\n" +
                    "<rdf:Description>\n" +
                    "<owl:inverseOf rdf:resource=\"" + propertyName + "\"/>\n" +
                    "</rdf:Description>\n" +
                    "</owl:onProperty>";
        }

        if(0 == left){
            try {
                throw new Exception("Error!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(1 == left){
            int classID = mp.getLastConcept();
            String className = LabelGetter.get(classID).replace("wikidata.", "")
                    .replace("<", "").replace(">", "");
            String result = "<owl:Restriction>\n" +
                    "" + propertyDescription + "\n" +
                    "<owl:someValuesFrom rdf:resource=\"" + className + "\"/>\n" +
                    "</owl:Restriction>";

            return result;
        }
        else{
            String result = "<owl:Restriction>\n" +
                    "" + propertyDescription + "\n" +
                    "<owl:someValuesFrom>\n" +
                    "" + genNested(mp, left - 1) + "\n" +
                    "</owl:someValuesFrom>\n" +
                    "</owl:Restriction>";

            return result;
        }
    }

    private static boolean isLegal(Set<MetaPath> axiom){
        boolean flag = true;

        for(Set<MetaPath> mps : axioms0){
            if(isSubPatternOf(mps, axiom)){ // 因为是从浅到深生成的，所以只需要检查单向
                flag = false;
                break;
            }
        }

        return flag;
    }

    private static boolean isSubPatternOf(Set<MetaPath> mps0, Set<MetaPath> mps1){
        for(MetaPath m0 : mps0){
            boolean flag = false;
            for(MetaPath m1: mps1){
                if(m0.length() == 0){
                    if(m1.length() == 0 && m0.getLastConcept() == m1.getLastConcept()){
                        flag = true;
                        break;
                    }
                    continue;
                }

                if(m0.length() <= m1.length()){
                    if(m1.getRelations().subList(0, m0.length()).equals(m0.getRelations())){
                        flag = true;
                        break;
                    }

                }
            }
            if(!flag) return false;
        }
        return true;
    }

    public static void main(String[] args){
       /* List<Integer> concepts = new ArrayList<>();
        concepts.add(3481452);
        concepts.add(3481453);
        concepts.add(3481454);
        concepts.add(3481455);
        List<Integer> roles = new ArrayList<>();
        roles.add(3480900);
        roles.add(-3480901);
        roles.add(3480902);
        MetaPath mp = new MetaPath(concepts, roles);
        //System.out.println(genNested(mp, mp.length()));
        List<Integer> concepts1 = new ArrayList<>();
        concepts1.add(3481456);
        concepts1.add(3481457);
        List<Integer> roles1= new ArrayList<>();
        roles1.add(3481000);
        MetaPath mp1 = new MetaPath(concepts1, roles1);
        Set<MetaPath> mps = new HashSet<>();
        //mps.add(mp);
        mps.add(mp1);

        System.out.println(genAxiom0(mps, "fuckU"));*/

       GraphModelM.initializeMap();
       FilteredGraphModelM.initializeMap(100);
       Ontology.Initialize();
       GraphOntGetterM.initializeMap();

       outputAxiom0();

    }

}
