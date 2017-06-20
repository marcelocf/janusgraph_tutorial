
package marcelocf.janusgraph;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.*;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.janusgraph.core.schema.Mapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Schema creation for our example graph db
 */
public class Schema {

  ////////////////////////
  // Static Attributes //
  //////////////////////

  /**
   * It is usually good practice to output messages using a logging framework.
   * <br/>
   * Here we just use the same as JanusGraph.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(Schema.class);

  /**
   * The configuration file path relative to the execute path of this code.
   * <br/>
   *
   * It is assumed you will run within the distributions folder.
   */
  public static final String CONFIG_FILE = "conf/janusgraph.properties";

  /**
   * The index backend is identified by a key in the configuration; in our example we called it
   * <pre>search</pre>.
   * <br/>
   * Saving it in a static variable so we can reuse.
   */
  public static final String BACKING_INDEX = "search";

  public static final String USER = "user";
  public static final String USER_NAME = "marcelocf.janusgraph.userName";

  public static final String STATUS_UPDATE = "statusUpdate";
  public static final String CONTENT = "marcelocf.janusgraph.content";

  public static final String CREATED_AT = "marcelocf.janusgraph.createdAt";

  public static final String POSTS = "posts";
  public static final String FOLLOWS = "follows";


  /////////////////////
  // Static Methods //
  ///////////////////

  /**
   * The main code basically instantiate its own class and call individual methods.
   * @param argv
   */
  public static void main(String[] argv) {
    // conect the graph
    Schema schema = new Schema(CONFIG_FILE);


    schema.createUserSchema();
    schema.createStatusUpdateSchema();
    schema.createEdgeSchema();

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
  public Schema(String configFile) {
    LOGGER.info("Connecting graph");
    graph = JanusGraphFactory.open(configFile);
    LOGGER.info("Getting management");
    mgt = graph.openManagement();
  }

  /**
   * Create the user schema - vertex label, property and index.
   */
  private void createUserSchema(){
    LOGGER.info("Create {} schema", USER);
    VertexLabel user = mgt.makeVertexLabel(USER).make();
    PropertyKey userName = mgt.makePropertyKey(USER_NAME).dataType(String.class).make();

    mgt.buildIndex(indexName(USER, USER_NAME), Vertex.class).
        addKey(userName, Mapping.STRING.asParameter()).
        indexOnly(user).
        buildMixedIndex(BACKING_INDEX);
  }

  /**
   * Create the statusUpdate schema - vertex label, property and full-text index.
   */
  private void createStatusUpdateSchema(){
    LOGGER.info("Create {} schema", STATUS_UPDATE);
    VertexLabel statusUpdate = mgt.makeVertexLabel(STATUS_UPDATE).make();
    PropertyKey content = mgt.makePropertyKey(CONTENT).dataType(String.class).make();

    mgt.buildIndex(indexName(STATUS_UPDATE, CONTENT), Vertex.class).
        addKey(content, Mapping.TEXTSTRING.asParameter()).
        indexOnly(statusUpdate).
        buildMixedIndex(BACKING_INDEX);
  }


  /**
   * Create both <i>posts</i> and <i>follows</i> edges and related index.
   * <br/>
   *
   * Because the property and index for both follows and posts is the same we create them at the same point here.
   */
  private void createEdgeSchema() {
    LOGGER.info("create edges schema");
    EdgeLabel posts = mgt.makeEdgeLabel(POSTS).make();
    EdgeLabel follows = mgt.makeEdgeLabel(FOLLOWS).make();
    PropertyKey createdAt = mgt.makePropertyKey(CREATED_AT).dataType(Long.class).make();

    mgt.buildIndex(indexName(POSTS, CREATED_AT), Edge.class).
        addKey(createdAt).
        indexOnly(posts).
        buildMixedIndex(BACKING_INDEX);

    mgt.buildIndex(indexName(FOLLOWS, CREATED_AT), Edge.class).
        addKey(createdAt).
        indexOnly(follows).
        buildMixedIndex(BACKING_INDEX);
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


  /**
   * We are using this to create predictable names for our indexes. You could name it however you want, but doing
   * like this will make it possible to reindex stuff in the future... if we want (we do want, btw)
   * @param label edge or vertex label
   * @param propertyKey property key
   * @return
   */
  public static String indexName(String label, String propertyKey) {
    return label + ":by:" + propertyKey;
  }
}
