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

public class RemoteQueries {

  public static final String CONFIG_FILE = "conf/janusgraph-remote.properties";
  private static final Logger LOGGER = LoggerFactory.getLogger(RemoteQueries.class);
  private final Graph graph;
  private final GraphTraversalSource g;
  private final String userName;

  /**
   * Run every example query, outputting results via @LOGGER
   *
   * @param argv
   * @throws Exception
   */
  public static void main(String[] argv) throws Exception {
    RemoteQueries remoteQueries = new RemoteQueries("testUser0");

    LOGGER.info("Initialized the remote query executor");


    LOGGER.info("Getting user:");
    print(remoteQueries.getUser());

    LOGGER.info("Getting followed users");
    print(remoteQueries.getFollowedUsers());

    LOGGER.info("Getting followers users");
    print(remoteQueries.getFollowers());

    LOGGER.info("Getting followers of followed users");
    print(remoteQueries.getFollowersOfFollowedUsers());

    remoteQueries.close();
    System.exit(0);
  }

  /**
   * Just a simple print method for every property returned from a vertex traversal
   * @param traversal
   */
  private static void print(GraphTraversal<Vertex, Vertex> traversal) {
    GraphTraversal<Vertex, Map<String, Object>> valueMap = traversal.valueMap(true);
    int count = 0;

    for (GraphTraversal<Vertex, Map<String, Object>> it = valueMap; it.hasNext(); ) {
      Map<String, Object> item = it.next();
      LOGGER.info(" {}: {} ", count++, item.toString());
    }
    LOGGER.info("Printed {} element(s)", count);
  }


  // Instance Methods //
  ///////

  public RemoteQueries(String userName) throws Exception {
    this.userName = userName;
    graph = EmptyGraph.instance();
    g = graph.traversal().withRemote(CONFIG_FILE);
  }

  public void close() throws Exception {
    g.close();
    // no need to close the graph because it is an empty graph allocated only to be able to connect to a remote
  }

  public GraphTraversal<Vertex, Vertex> getUser() {
    return g.V().hasLabel(Schema.USER).has(USER_NAME, eq(userName));
  }

  public GraphTraversal<Vertex, Vertex> getFollowedUsers() {
    return getUser().out(Schema.FOLLOWS);
  }

  public GraphTraversal<Vertex, Vertex> getFollowers() {
    return getUser().in(Schema.FOLLOWS);
  }

  public GraphTraversal<Vertex, Vertex> getFollowersOfFollowedUsers() {
    return getFollowedUsers().in(Schema.FOLLOWS);
  }

  public GraphTraversal<Vertex, Vertex> getFollowRecommendation(){
    return getFollowersOfFollowedUsers();
  }
}
