package Test;

import JDBCUtils.JdbcUtil;
import RelSim.MetapathFinder;
import Structures.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class findPathBetweenNodes
{

    public void initializeKG()
    {
        typeModel.clean();
        graphModel.clean();
        OntologyTree.clean();
        try {
            Connection conn = JdbcUtil.getConnection();
            typeModel.loadTypes(conn);
            graphModel.loadGraph(conn);
            OntologyTree.loadOT(conn); OntologyTree.init();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Initialize KnowledgeGraph End!");
    }

    public void Do()
    {
        MetapathFinder MF = new MetapathFinder();
        while(true)
        {
            Scanner sc = new Scanner(System.in);
            Integer a = sc.nextInt();
            Integer b = sc.nextInt();
            Set<metapathModel> ret = MF.returnPath(a, b, 3);
            for(metapathModel p : ret) System.out.println(p.toString());
        }
    }

    public void main(String[] args)
    {
        GlobalVariances.SetPara_Database_name("dbpedia");
        initializeKG();
        Do();
    }

}
