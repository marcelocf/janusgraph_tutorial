
package marcelocf.janusgraph;

import com.github.javafaker.Faker;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;

/**
 * LoadData creation for our example graph db
 */
public class CreateSupernodes {

  ////////////////////////
  // Static Attributes //
  //////////////////////

  /**
   * It is usually good practice to output messages using a logging framework.
   * <br/>
   * Here we just use the same as JanusGraph.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateSupernodes.class);



  /////////////////////
  // Static Methods //
  ///////////////////

  /**
   * The main code basically instantiate its own class and call individual methods.
   * @param argv
   */
  public static void main(String[] argv) throws Exception {
    CreateSupernodes loader = new CreateSupernodes(Schema.CONFIG_FILE);

    Vertex users[] = loader.getUsers(0, 99);
    Vertex followedUsers[] = loader.getUsers(100, 9999);
    loader.commit();
    for(Vertex user: users) {
      LOGGER.info("User {} comments:", user.value(Schema.USER_NAME).toString());
      for(Vertex update: loader.generateStatusUpdates(user, 10000)) {
        LOGGER.info("     -> {}", update.value(Schema.CONTENT).toString());
      }
      loader.commit();

      LOGGER.info("User {} follows:", user.value(Schema.USER_NAME).toString());
      for(Vertex followedUser: loader.generateFollows(user, followedUsers, 500)) {
        LOGGER.info("     -> {}", followedUser.value(Schema.USER_NAME).toString());
      }
      loader.commit();
    }

    loader.close();
  }

  //////////////////////////
  // Instance Attributes //
  ////////////////////////

  private final JanusGraph graph;

  private final Faker faker;
  private final Date oneYearAgo;
  private final QueryRunner queryRunner;


  /**
   * Initialize the graph connection.
   *
   * @param configFile
   */
  public CreateSupernodes(String configFile) throws Exception {
    graph = JanusGraphFactory.open(configFile);
    faker = new Faker();
    queryRunner = new QueryRunner(graph.traversal(), "test");

    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.YEAR, -1);
    oneYearAgo = cal.getTime();
  }


  private void commit(){
    graph.tx().commit();
  }

  /**
   * Commit the current transaction and close the graph.
   */
  private void close(){
    commit();
    graph.close();
  }


  private Vertex[] getUsers(int from, int to){
    Vertex[] users = new Vertex[to - from + 1];

    for(int i=from; i <= to; i++){
      users[i-1] = queryRunner.getUser("testUser" + i).next();
    }

    return users;
  }

  private Vertex[] generateStatusUpdates(Vertex user, int count){
    Vertex[] updates = new Vertex[count];
    for(int i=0; i < count; i++) {
      updates[i] = addStatusUpdate(user, getContent());
    }
    return updates;
  }


  private Vertex addStatusUpdate(Vertex user, String statusUpdateContent) {
    Vertex statusUpdate = graph.addVertex(Schema.STATUS_UPDATE);
    statusUpdate.property(Schema.CONTENT, statusUpdateContent);
    user.addEdge(Schema.POSTS, statusUpdate, Schema.CREATED_AT, getTimestamp());
    return statusUpdate;
  }

  private Vertex[] generateFollows(Vertex forUser, Vertex[] users, int count){
    Vertex[] followedUsers = new Vertex[count];

    for(int i = 0; i < count; i++) {
      followedUsers[i] = users[faker.number().numberBetween(0, users.length - 1)];
      Edge follows = forUser.addEdge(Schema.FOLLOWS, followedUsers[i], Schema.CREATED_AT, getTimestamp());
    }
    return followedUsers;
  }

  /**
   * Return a timestamp between 1 month ago and now.
   * @return
   */
  private Long getTimestamp(){
    return faker.date().between(oneYearAgo, new Date()).getTime();
  }

  private String getContent(){
    return faker.chuckNorris().fact();
  }

}
