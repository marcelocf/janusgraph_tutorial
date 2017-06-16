package marcelocf.janusgraph;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuiltinQueries {

  ///////////////////
  // Static Block //
  /////////////////

  private static final Logger LOGGER = LoggerFactory.getLogger(BuiltinQueries.class);


  /**
   * Run every example query, outputting results via @LOGGER
   *
   * @param argv
   * @throws Exception
   */
  public static void main(String[] argv) throws Exception {
    JanusGraph graph = JanusGraphFactory.open(Schema.CONFIG_FILE);
    GraphTraversalSource graphTraversalSource = graph.traversal();

    QueryRunner queryRunner = new QueryRunner(graphTraversalSource, "testUser0");

    LOGGER.info("Initialized the builtin query executor");

    queryRunner.runQueries();

    queryRunner.close();

    graph.close();

    System.exit(0);
  }

}
