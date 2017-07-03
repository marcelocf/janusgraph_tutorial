
package marcelocf.janusgraph;

import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.janusgraph.core.*;
import org.janusgraph.core.schema.JanusGraphIndex;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.janusgraph.core.schema.RelationTypeIndex;
import org.janusgraph.core.schema.SchemaAction;
import org.janusgraph.diskstorage.BackendException;
import org.janusgraph.hadoop.MapReduceIndexManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Float;

import java.util.concurrent.ExecutionException;

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

  /////////////////////
  // Static Methods //
  ///////////////////

  /**
   * The main code basically instantiate its own class and call individual methods.
   * @param argv
   */
  public static void main(String[] argv) throws BackendException, ExecutionException, InterruptedException {
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
  public RecreateWeightIndex(String configFile) {
    LOGGER.info("Connecting graph");
    graph = JanusGraphFactory.open(configFile);
    LOGGER.info("Getting management");
    mgt = graph.openManagement();
  }

  private void deleteOldIndexes() {
    deleteIndex(Schema.FOLLOWS, Schema.CREATED_AT);
    deleteIndex(Schema.FOLLOWS, WEIGHT);
    deleteIndex(Schema.POSTS, Schema.CREATED_AT);
  }

  private void deleteIndex(String label, String propertyKey) {
    LOGGER.info("Deleting index for edge {} and property {}", label, propertyKey);
    JanusGraphIndex index = mgt.getGraphIndex(Schema.indexName(label, propertyKey));
    mgt.updateIndex(index, SchemaAction.REMOVE_INDEX);
    mgt.commit();
  }

  /**
   * Create both <i>posts</i> and <i>follows</i> edges and related index.
   * <br/>
   *
   * Because the property and index for both follows and posts is the same we create them at the same point here.
   */
  private void createNewIndexes() {
    createVertexCentricIndex(Schema.FOLLOWS, Schema.CREATED_AT);
    createVertexCentricIndex(Schema.FOLLOWS, WEIGHT);
    createVertexCentricIndex(Schema.POSTS, Schema.CREATED_AT);
  }

  private void createVertexCentricIndex(String label, String propertyKey) {
    LOGGER.info("Creating vertex centric index for edge {} and property {}", label, propertyKey);
    EdgeLabel edgeLabel = mgt.getEdgeLabel(label);
    PropertyKey key = mgt.getPropertyKey(propertyKey);
    mgt.buildEdgeIndex(edgeLabel, Schema.indexName(label, propertyKey), Direction.BOTH, Order.decr, key);
    mgt.commit();
  }

  private void reindex() throws BackendException, ExecutionException, InterruptedException {
    reindexFor(Schema.FOLLOWS, Schema.CREATED_AT);
    reindexFor(Schema.FOLLOWS, WEIGHT);
    reindexFor(Schema.POSTS, Schema.CREATED_AT);
  }


  private void reindexFor(String label, String propertyKey) throws BackendException, ExecutionException, InterruptedException {
    LOGGER.info("Reindexing index for edge {} and property {} using map reduce", label, propertyKey);

    MapReduceIndexManagement mr = new MapReduceIndexManagement(graph);
    RelationType relationType = mgt.getRelationType(label);
    RelationTypeIndex relationIndex = mgt.getRelationIndex(relationType, propertyKey);
    mr.updateIndex(relationIndex, SchemaAction.REINDEX).get();
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
