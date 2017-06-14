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

  ///////////////////
  // Static Block //
  /////////////////

  public static final String CONFIG_FILE = "conf/janusgraph-remote.properties";
  private static final Logger LOGGER = LoggerFactory.getLogger(RemoteQueries.class);


  /**
   * Run every example query, outputting results via @LOGGER
   *
   * @param argv
   * @throws Exception
   */
  public static void main(String[] argv) throws Exception {
    Graph graph = EmptyGraph.instance();
    GraphTraversalSource graphTraversalSource = graph.traversal().withRemote(CONFIG_FILE);

    QueryRunner queryRunner = new QueryRunner(graphTraversalSource, "testUser0");

    LOGGER.info("Initialized the remote query executor");


    LOGGER.info("Getting user:");
    print(queryRunner.getUser());

    LOGGER.info("Getting status updates:");
    print(queryRunner.getStatusUpdate());

    LOGGER.info("Getting followed users");
    print(queryRunner.getFollowedUsers());

    LOGGER.info("Getting followers users");
    print(queryRunner.getFollowers());

    LOGGER.info("Getting followers of followed users");
    print(queryRunner.getFollowersOfFollowedUsers());

    LOGGER.info("Getting recommendations of users to follow");
    print(queryRunner.getFollowRecommendation());

    queryRunner.close();
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
}
