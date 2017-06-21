package marcelocf.janusgraph;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * Created by marcelo on 17/06/21.
 */
public class HadoopQueryRunner extends QueryRunner {

  public HadoopQueryRunner(GraphTraversalSource traversalSource, String userName) throws Exception {
    super(traversalSource, userName);
  }

  public long countCommonFollowedUsers(Vertex userA, Vertex userB) {
    return g.
        V(userB).as("userB"). // store userB in a side effect for later usage
        V(userA).                     // then starts traversing from userA
        out("follows").
        in("follows").
        where(P.within("userB")).
        count().
        next(); // we don't need to check for existence because this can be just null
  }

}
