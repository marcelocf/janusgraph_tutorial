
package marcelocf.janusgraph;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.janusgraph.core.EdgeLabel;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.PropertyKey;
import org.janusgraph.core.schema.JanusGraphIndex;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Float;

/**
 * RecreateWeightIndex creation for our example graph db
 */
public class RecreateWeightIndex {

  ////////////////////////
  // Static Attributes //
  //////////////////////

  /**
   * It is usually good practice to output messages using a logging framework.
   * <br/>
   * Here we just use the same as JanusGraph.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(RecreateWeightIndex.class);


  public static final String WEIGHT = "weight";
  private final String followsWeightIndexName;


  /////////////////////
  // Static Methods //
  ///////////////////

  /**
   * The main code basically instantiate its own class and call individual methods.
   * @param argv
   */
  public static void main(String[] argv) {
    // conect the graph
    RecreateWeightIndex schema = new RecreateWeightIndex(Schema.CONFIG_FILE);

    schema.deleteOldIndexes();
    schema.createNewIndexes();
    schema.reindex();

    schema.close();
  }

  //////////////////////////
  // Instance Attributes //
  ////////////////////////

  /**
   * This <i>is</i> our graph database. Instead of connecting to a remote server, JanusGraph has a built-in implementation
   * of the graph and connects to the backend and indexing databases.
   * <br/>
   * Of course, you can connect to a <i>gremlin server</i> in your code, so you use JanusGraph in a server mode. But this
   * is not what we are doing here.
   */
  private final JanusGraph graph;


  /**
   * The schema management is done by an instance of @{@link JanusGraphManagement}. This class can do other interesting
   * stuff, such as kicking other nodes from the cluster. I recommend reading its javadocs.
   */
  private final JanusGraphManagement mgt;

  /**
   * Initialize the graph and the graph management interface.
   *
   * @param configFile
   */
  public RecreateWeightIndex(int configFile) {
    LOGGER.info("Connecting graph");
    graph = null;//JanusGraphFactory.open(configFile);
    LOGGER.info("Getting management");
    mgt = graph.openManagement();

    followsWeightIndexName = Schema.indexName(Schema.FOLLOWS, WEIGHT);

  }

  private void deleteOldIndexes() {
    JanusGraphIndex index = mgt.getGraphIndex(followsWeightIndexName);

  }

  /**
   * Create both <i>posts</i> and <i>follows</i> edges and related index.
   * <br/>
   *
   * Because the property and index for both follows and posts is the same we create them at the same point here.
   */
  private void createNewIndexes() {
    LOGGER.info("create weight index");
    EdgeLabel follows = mgt.getEdgeLabel(Schema.FOLLOWS);
    PropertyKey weight = mgt.makePropertyKey(WEIGHT).dataType(Float.class).make();

    mgt.buildIndex(Schema.indexName(Schema.FOLLOWS, WEIGHT), Edge.class).
        addKey(weight).
        indexOnly(follows).
        buildMixedIndex(Schema.BACKING_INDEX);
  }

  /**
   * Commit the current transaction and close the graph.
   */
  private void close(){
    // we need to commit the Management changes or else they are not applied.
    mgt.commit();
    graph.tx().commit();
    graph.close();
  }
}
