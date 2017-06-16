package marcelocf.janusgraph;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.util.empty.EmptyGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    queryRunner.runQueries();

    queryRunner.close();
    // we don't need to close our graph because it is an empty graph.
  }
}
