package Structures;

import java.util.ArrayList;
import java.util.List;

public class metapathModel {

    public List<Integer> concepts = new ArrayList<Integer>();
    public List<Integer> relations = new ArrayList<Integer>();

    public metapathModel(){
        concepts = new ArrayList<>();
        relations = new ArrayList<>();
    }

    public metapathModel(List<Integer> concepts){
        if(concepts.size() == 1){
            this.concepts = concepts;
            relations = new ArrayList<>();
        }
        else{
            try {
                throw new Exception("cannot initialize metapath in this way!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public metapathModel(List<Integer> concepts, List<Integer> relations){
        if(concepts.size() == (relations.size() + 1)){
            this.relations = relations;
            this.concepts = concepts;
        }
        else
            try {
                throw new Exception("concepts and relations do not match!");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    }

    public List<Integer> getConcepts() { return concepts; }
    public void setConcepts(List<Integer> concepts) { this.concepts = concepts; }
    public List<Integer> getRelations() { return relations; }
    public void setRelations(List<Integer> relations) { this.relations = relations; }

    public int hashCode(){
        int code = 0;
        for(int i : relations){
            if(i < 0){
                i *= -1;
                code -= i;
            }
            code += i;
        }
        for(int i : concepts)
            code += i;
        return code;
    }
    public boolean equals(Object hp){
        if(((metapathModel)hp).getRelations().equals(this.relations) && ((metapathModel)hp).getConcepts().equals(this.concepts))
            return true;
        else
            return false;
    }
    public String toString(){
        List<Integer> items = new ArrayList<Integer>();
        for(int i = 0; i < relations.size(); i ++){
            items.add(concepts.get(i));
            items.add(relations.get(i));
        }
        items.add(concepts.get(concepts.size() - 1));

        return items.toString();
    }
    public List<Integer> toArray(){
        List<Integer> items = new ArrayList<Integer>(); items.clear();
        for(int i = 0; i < relations.size(); i ++){
            items.add(concepts.get(i));
            items.add(relations.get(i));
        }
        items.add(concepts.get(concepts.size() - 1));

        return items;
    }
    public boolean isLegal(){
        return concepts.size() == (relations.size() + 1);
    }
    public metapathModel getReverse(){
        List<Integer> rs = new ArrayList<Integer>();
        int sizer = this.relations.size();
        for(int i = 0; i < sizer; i ++){
            rs.add(this.relations.get(sizer - 1 - i)*-1);
        }
        List<Integer> cs = new ArrayList<Integer>();
        int sizec = this.concepts.size();
        for(int i = 0; i < sizec; i ++){
            cs.add(this.concepts.get(sizec - 1 - i));
        }

        return new metapathModel(cs, rs);
    }
    public metapathModel getHalf(){
        List<Integer> rs = new ArrayList<Integer>();
        int sizer = this.relations.size();
        for(int i = 0; i < sizer/2; i ++)
            rs.add(this.relations.get(i));
        List<Integer> cs = new ArrayList<Integer>();
        int sizec = this.concepts.size();
        for(int i = 0; i < (sizec+1)/2; i ++)
            cs.add(this.concepts.get(i));

        return new metapathModel(cs, rs);
    }
    public metapathModel get(int i){
        List<Integer> rs = new ArrayList<Integer>();
        rs.add(this.relations.get(i));
        List<Integer> cs = new ArrayList<Integer>();
        cs.add(this.concepts.get(i));
        cs.add(this.concepts.get(i + 1));

        return new metapathModel(cs, rs);
    }
    /**
     *
     * @return the size of relations
     */
    public int length(){
        return this.relations.size();
    }

    //如果两个meta-path relation完全相同，且concept只有开头不同，那么就视为两个相同的mp
    public boolean isSame(metapathModel mp){
        if(this.relations.equals(mp.relations)){
            for(int i = 1; i <= this.length(); i ++){
                if(!this.concepts.get(i).equals(mp.getConcepts().get(i)))
                    return false;
            }
            return  true;
        }
        else
            return false;
    }

    public int getLastRelation(){
        if(relations.size() > 0)
            return relations.get(relations.size() - 1);
        else
            return -1;
    }

    public metapathModel clone()
    {
        metapathModel ret = new metapathModel();
        ret.concepts = (List<Integer>) ((ArrayList<Integer>)this.concepts).clone();
        ret.relations = (List<Integer>) ((ArrayList<Integer>)this.relations).clone();
        return ret;
    }

    public Boolean Cross_Head_Head(metapathModel ano)
    {
        for(Integer i = 0; i < this.concepts.size(); ++i)
            for(Integer j = 0; j < ano.concepts.size(); ++j)
                if(this.concepts.get(i).equals(ano.concepts.get(j)))
                {
                    if(i .equals(0) && j .equals(0)) continue;
                    return false;
                }
        return true;
    }

    public Boolean Cross_Head_Tail(metapathModel ano)
    {
        for(Integer i = 0; i < this.concepts.size(); ++i)
            for(Integer j = 0; j < ano.concepts.size(); ++j)
                if(this.concepts.get(i).equals(ano.concepts.get(j)))
                {
                    if(i .equals(0) && j .equals( ano.concepts.size() - 1)) continue;
                    return false;
                }
        return true;
    }
    public Boolean Cross_Tail_Head(metapathModel ano)
    {
        for(Integer i = 0; i < this.concepts.size(); ++i)
            for(Integer j = 0; j < ano.concepts.size(); ++j)
                if(this.concepts.get(i).equals(ano.concepts.get(j)))
                {
                    if(i .equals( this.concepts.size() - 1) && j .equals(0)) continue;
                    return false;
                }
        return true;
    }
    public Boolean Cross_Tail_Tail(metapathModel ano)
    {
        for(Integer i = 0; i < this.concepts.size(); ++i)
            for(Integer j = 0; j < ano.concepts.size(); ++j)
                if(this.concepts.get(i).equals(ano.concepts.get(j)))
                {
                    if(i .equals( this.concepts.size() - 1) && j .equals( ano.concepts.size() - 1)) continue;
                    return false;
                }
        return true;
    }

    public Integer getBeginNode()
    {
        return this.concepts.get(0);
    }

    public Integer getEndNode()
    {
        return this.concepts.get(this.concepts.size() - 1);
    }

    public metapathModel Concat(metapathModel nex)
    {
        List<Integer> rela = (List<Integer>) ((ArrayList<Integer>)this.relations).clone();
        rela.addAll(nex.getRelations());
        List<Integer> conc = (List<Integer>) ((ArrayList<Integer>)this.concepts).clone();
        for(Integer i = 1; i < nex.concepts.size(); ++ i) conc.add(nex.concepts.get(i));
        return new metapathModel(conc, rela);
    }

}
