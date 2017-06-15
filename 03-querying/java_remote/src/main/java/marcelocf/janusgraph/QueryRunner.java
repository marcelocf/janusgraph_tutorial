package marcelocf.janusgraph;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Map;

import static marcelocf.janusgraph.Schema.*;
import static org.apache.tinkerpop.gremlin.process.traversal.Order.decr;
import static org.apache.tinkerpop.gremlin.process.traversal.P.eq;
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

  public GraphTraversal<Vertex, Vertex> getTimeline(int limit){
    return getUser().
        aggregate("users").
        out(FOLLOWS).
        aggregate("users").
        cap("users").
        unfold().
        outE(POSTS).
        order().by(CREATED_AT, decr).
        limit(limit).
        inV();
  }

  public GraphTraversal<Vertex, Map<String, Object>> getTimeline2(int limit){
    return getUser().
        aggregate("users").
        out(FOLLOWS).
        aggregate("users").
        cap("users").
        unfold().
        as("userss").
        outE(POSTS).
        as("posts").
        order().by(CREATED_AT, decr).
        limit(limit).
        inV().
        as("statusUpdates").
        select("userss", "posts", "statusUpdates");
  }
}
