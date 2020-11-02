package Path;

import java.util.*;

public class GraphNode {
    int id;
    GraphNode parent = null;

    public GraphNode(int id, GraphNode parent){
        this.id = id;
        this.parent = parent;
    }

    public List<Integer> getIds(){
        List<Integer> result = new ArrayList<>();
        result.add(this.id);
        GraphNode p = this.parent;
        while(p != null){
            result.add(p.id);
            p = p.parent;
        }

        return  result;
    }
}
