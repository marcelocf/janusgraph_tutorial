package marcelocf.janusgraph;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

import static marcelocf.janusgraph.Schema.*;
import static org.apache.tinkerpop.gremlin.process.traversal.Order.decr;
import static org.apache.tinkerpop.gremlin.process.traversal.P.eq;
import static org.apache.tinkerpop.gremlin.process.traversal.P.without;

public class QueryRunner {


  private static final Logger LOGGER = LoggerFactory.getLogger(QueryRunner.class);

  public static final String userVertex = "userVertex";
  public static final String postsEdge = "postsEdge";
  public static final String statusUpdateVertex = "statusUpdateEdge";


  ///////////////////////
  // Instance Methods //
  /////////////////////

  protected final GraphTraversalSource g;
  protected final String userName;

  private long startedAt;

  public QueryRunner(GraphTraversalSource traversalSource, String userName) throws Exception {
    this.g = traversalSource;
    this.userName = userName;
  }

  public void close() throws Exception {
    g.close();
  }

  public GraphTraversal<Vertex, Vertex> getUser() {
    return getUser(userName);
  }

  public GraphTraversal<Vertex, Vertex> getUser(String userName) {
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

  public GraphTraversal<Vertex, Map<String, Map<String,Object>>> getTimeline2(int limit){
    return getUser().
        aggregate("users").
        out(FOLLOWS).
        aggregate("users").
        cap("users").
        unfold().
        as(userVertex).
        outE(POSTS).
        as(postsEdge).
        order().by(CREATED_AT, decr).
        limit(limit).
        inV().
        as(statusUpdateVertex).
        select(userVertex, postsEdge, statusUpdateVertex);
  }


  public void runQueries() {

    LOGGER.info("Getting user:");
    print(getUser());

    LOGGER.info("Getting status updates:");
    print(getStatusUpdate());

    LOGGER.info("Getting followed users");
    print(getFollowedUsers());

    LOGGER.info("Getting followers users");
    print(getFollowers());

    LOGGER.info("Getting followers of followed users");
    print(getFollowersOfFollowedUsers());

    LOGGER.info("Getting recommendations of users to follow");
    print(getFollowRecommendation());

    LOGGER.info("Printing timeline");
    print(getTimeline(100));

    LOGGER.info("Printing timeline");
    printTimeline(getTimeline2(100));

    System.exit(0);
  }

  /**
   * Just a simple print method for every property returned from a vertex traversal
   * @param traversal
   */
  private void print(GraphTraversal<Vertex, Vertex> traversal) {
    resetTimer();
    GraphTraversal<Vertex, Map<String, Object>> valueMap = traversal.valueMap(true);
    int count = 0;

    for (GraphTraversal<Vertex, Map<String, Object>> it = valueMap; it.hasNext(); ) {
      Map<String, Object> item = it.next();
      LOGGER.info(" {}: {} ", count++, item.toString());
    }
    LOGGER.info("Printed {} element(s) in {}ms", count, duration());
  }

  private void printTimeline(GraphTraversal<Vertex, Map<String, Map<String, Object>>> traversal) {
    int count = 0;
    resetTimer();
    while (traversal.hasNext()) {
      Map<String, Map<String, Object>> item = traversal.next();
      Vertex user = (Vertex) item.get(userVertex);
      Edge posts = (Edge) item.get(postsEdge);
      Vertex statusUpdate = (Vertex) item.get(statusUpdateVertex);
      LOGGER.info(
          " {}: @{} {}: {}",
          count++,
          user.value(USER_NAME),
          formatTimestamp(posts.value(CREATED_AT)),
          statusUpdate.value(CONTENT)
      );
    }

    LOGGER.info("Printed {} element(s) in {}ms", count, duration());
  }

  private String formatTimestamp(Long timestamp) {
    Date d = new Date(timestamp);
    return d.toString();
  }


  private void resetTimer(){
    startedAt = (new Date()).getTime();
  }

  private long duration(){
    return (new Date()).getTime() - startedAt;
  }
}
