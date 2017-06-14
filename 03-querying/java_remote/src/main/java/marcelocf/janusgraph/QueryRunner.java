package marcelocf.janusgraph;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.empty.EmptyGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static marcelocf.janusgraph.Schema.USER_NAME;
import static org.apache.tinkerpop.gremlin.process.traversal.P.eq;
import static org.apache.tinkerpop.gremlin.process.traversal.P.not;
import static org.apache.tinkerpop.gremlin.process.traversal.P.without;

public class QueryRunner {

  ///////////////////////
  // Instance Methods //
  /////////////////////

  private final GraphTraversalSource g;
  private final String userName;

  public QueryRunner(GraphTraversalSource traversalSource, String userName) throws Exception {
    this.g = traversalSource;
    this.userName = userName;
  }

  public void close() throws Exception {
    g.close();
    // no need to close the graph because it is an empty graph allocated only to be able to connect to a remote
  }

  public GraphTraversal<Vertex, Vertex> getUser() {
    return g.V().hasLabel(Schema.USER).has(USER_NAME, eq(userName));
  }

  public GraphTraversal<Vertex, Vertex> getStatusUpdate() {
    return getUser().out(Schema.POSTS);
  }

  public GraphTraversal<Vertex, Vertex> getFollowedUsers() {
    return getUser().out(Schema.FOLLOWS);
  }

  public GraphTraversal<Vertex, Vertex> getFollowers() {
    return getUser().aggregate("ignore").in(Schema.FOLLOWS);
  }

  public GraphTraversal<Vertex, Vertex> getFollowersOfFollowedUsers() {
    return getFollowedUsers().in(Schema.FOLLOWS);
  }

  public GraphTraversal<Vertex, Vertex> getFollowRecommendation(){
    return getUser().
        aggregate("me").
        aggregate("ignore").
        out(Schema.FOLLOWS).
        aggregate("ignore").
        cap("me").
        unfold().
        in(Schema.FOLLOWS).
        where(without("ignore"));
  }
}
