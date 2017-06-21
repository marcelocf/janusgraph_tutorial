
package marcelocf.janusgraph;

import org.apache.commons.configuration.*;
import org.apache.tinkerpop.gremlin.process.computer.*;
import org.apache.tinkerpop.gremlin.process.computer.util.AbstractVertexProgramBuilder;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.javatuples.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/**
 * This is a quite complex task, I don't fully understand myself. However there a couple of things to keep on:
 * <ul>
 *  <li>{@link #workerIterationStart} and {@link #workerIterationEnd} methods will handle connection to a read-write enabled graph</li>
 *  <li>{@link #execute} actually runs the computation for one specific vertex</li>
 *  <li>{@link Builder} is a class used to initialize this vertex program</li>
 */
class ComputeWeightVertexProgram implements VertexProgram<Tuple>{

  private static final Logger LOGGER = LoggerFactory.getLogger(ComputeWeightVertexProgram.class);
  private static final String RW_EXAMPLE_VERTEX_PROGRAM_CFG_PREFIX = "rw_example";

  private BaseConfiguration configuration;
  private JanusGraph graph;
  private GraphTraversalSource g;
  private long sevenDaysAgo;

  /**
   * Overall setup of our task
   * @param memory
   */
  @Override
  public void setup(Memory memory) {
    LOGGER.info("setup");
  }

  @Override
  public GraphComputer.ResultGraph getPreferredResultGraph() {
    return GraphComputer.ResultGraph.ORIGINAL;
  }

  @Override
  public GraphComputer.Persist getPreferredPersist() {
    return GraphComputer.Persist.NOTHING;
  }


  ////////////////////////
  // Worker Iteraction //
  //////////////////////

  @Override
  public void workerIterationStart(final Memory memory) {
    LOGGER.info("workerIterationStart");
    // TODO: add method in GraphEtl that allows us to simply copy from config to properties.
    graph = JanusGraphFactory.open(configuration);
    g = graph.traversal();
    sevenDaysAgo = (new Date()).getTime() - (1000 * 60 * 60 * 24 * 7);
  }

  @Override
  public void workerIterationEnd(final Memory memory) {
    graph.tx().commit();
    try {
      g.close();
      graph.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Execute this marvelous code, going from the Content to Users.
   *
   * Internally uses the RecommendationForNewUser class to build the recommendation as we want, pretty and
   * simple.
   *
   * @param vertex
   * @param messenger
   * @param memory
   */
  @Override
  public void execute(Vertex vertex, Messenger<Tuple> messenger, Memory memory) {
    try {
      GraphTraversal<Vertex, Edge> t = g.V(vertex.id()).outE(Schema.FOLLOWS);
      while(t.hasNext()) {
        updateWeight(t.next());
      }
    } catch (Exception e){
      e.printStackTrace();
      LOGGER.error("while processing " + vertex.id() + ": " + e.getClass().toString() + "(" + e.getMessage() + ")");
      return;
    }
  }


  private void updateWeight(Edge followsEdge) throws Exception {
    Vertex otherUser = followsEdge.inVertex();
    long since = followsEdge.value(Schema.CREATED_AT);
    if(since < sevenDaysAgo) {
      since = sevenDaysAgo;
    }

    HadoopQueryRunner runner = new HadoopQueryRunner(g, otherUser.value(Schema.USER_NAME));
    long commonFollowedUsers = runner.countCommonFollowedUsers(followsEdge.outVertex());
    long postsPerDaySince = runner.countPostsPerDaySince(since );
    long weight = (3 * commonFollowedUsers + postsPerDaySince) / 4;

    followsEdge.property(CreateWeightIndex.WEIGHT, weight);
  }


  /**
   * Run the task only once for each vertex.
   *
   * @param memory
   * @return
   */
  @Override
  public boolean terminate(Memory memory) {
    LOGGER.info("terminate");
    return true;
  }

  ////////////////
  // Messaging //
  //////////////

  /**
   * This program doesn't exchange messages, so all good ;)
   * @param memory
   * @return
   */
  @Override
  public Set<MessageScope> getMessageScopes(Memory memory) {
    LOGGER.info("getMessageScopes");
    return null;
  }

  @SuppressWarnings({"CloneDoesntDeclareCloneNotSupportedException", "CloneDoesntCallSuperClone"})
  public VertexProgram<Tuple> clone() {
    LOGGER.info("clone");
    try {
      return (ComputeWeightVertexProgram) super.clone();
    } catch (final CloneNotSupportedException e) {
      throw new IllegalStateException(e.getMessage(), e);
    }
  }

  /**
   * Survive states across task execution.
   *
   * @param graph
   * @param config
   */
  @Override
  public void loadState(final Graph graph, final Configuration config) {
    LOGGER.info("loadState");
    configuration = new BaseConfiguration();
    if (config != null) {
      ConfigurationUtils.copy(config, configuration);
    }
  }

  @Override
  public void storeState(final Configuration config) {
    LOGGER.info("storeState");
    VertexProgram.super.storeState(config);
    if (configuration != null) {
      ConfigurationUtils.copy(configuration, config);
    }
  }


  //////////////
  // Builder //
  ////////////

  public static Builder build() {
    return new Builder();
  }
  public static class Builder extends AbstractVertexProgramBuilder<Builder> {

    @SuppressWarnings("unchecked")
    @Override
    public ComputeWeightVertexProgram create(final Graph graph) {
      for (Iterator<String> it = configuration.getKeys(); it.hasNext(); ) {
        String key = it.next();
        System.out.println(key);
      }
      ComputeWeightVertexProgram program = (ComputeWeightVertexProgram) VertexProgram.createVertexProgram(graph, configuration);

      return program;
    }

    /**
     * Load a configuration from disk in the master node that can be used to connect to the graph from the clients.
     *
     * @param configFile
     * @return
     * @throws ConfigurationException
     */
    public Builder withRwGraphConfig(String configFile) throws ConfigurationException {
      PropertiesConfiguration rwConfig = new PropertiesConfiguration(configFile);
      ConfigurationUtils.append(rwConfig, configuration);
      return this;
    }
  }


}
