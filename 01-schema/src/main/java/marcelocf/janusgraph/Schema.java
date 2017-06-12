
package marcelocf.janusgraph;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.PropertyKey;
import org.janusgraph.core.VertexLabel;
import org.janusgraph.core.schema.JanusGraphManagement;
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
    Schema schema = new Schema("conf/janusgraph.properties");


    schema.createUserSchema();
    // build content schema
    // build post edge
    // build follows edge

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
    graph = JanusGraphFactory.open(configFile);
    mgt = graph.openManagement();
  }

  /**
   * Create the user schema - vertex label and properties and index.
   */
  private void createUserSchema(){
    VertexLabel user = mgt.makeVertexLabel(USER).make();
    PropertyKey userName = mgt.makePropertyKey(USER_NAME).make();

    mgt.buildIndex(indexName(USER, USER_NAME), Vertex.class).addKey(userName).indexOnly(user).buildMixedIndex(BACKING_INDEX);
  }

  /**
   * Commit the current transaction and close the graph.
   */
  private void close(){
    graph.tx().commit();
    graph.close();
  }


  private String indexName(String label, String propertyKey) {
    return label + ":by:" + propertyKey;
  }
}
