
package marcelocf.janusgraph;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.tinkerpop.gremlin.process.computer.*;
import org.apache.tinkerpop.gremlin.process.computer.util.AbstractVertexProgramBuilder;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.javatuples.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Set;

class ComputeWeightVertexProgram implements VertexProgram<Tuple>{

  private static final Logger LOGGER = LoggerFactory.getLogger(ComputeWeightVertexProgram.class);
  private static final String RW_EXAMPLE_VERTEX_PROGRAM_CFG_PREFIX = "rw_example";

  private BaseConfiguration configuration;
  private JanusGraph graph;
  private GraphTraversalSource g;

  //////////////////////
  // Main Task Setup //
  ////////////////////

  /**
   * Keeps the original graph.
   * @return
   */

  @Override
  public void setup(Memory memory) {
    LOGGER.info("setup");
  }

  @Override
  public GraphComputer.ResultGraph getPreferredResultGraph() {
    return GraphComputer.ResultGraph.ORIGINAL;
  }

  /**
   * Let the framework know we won't change the queried graph.
   * @return
   */
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
    Vertex changeV = g.V(vertex.id()).next();
    if(changeV == null) {
      // situation where the vertex has been deleted after it has been loaded by gremlin hadoop
      LOGGER.warn("Skipping null vertex");
      return;
    }

    try {
      // TODO: implement the cool stuff here
    } catch (Exception e){
      e.printStackTrace();
      LOGGER.error("while processing " + vertex.id() + ": " + e.getClass().toString() + "(" + e.getMessage() + ")");
      return;
    }
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
      // TODO: fix how the config is loaded - should be loaded from a 2nd file instead of subset of cfg.
      ConfigurationUtils.append(graph.configuration().subset(RW_EXAMPLE_VERTEX_PROGRAM_CFG_PREFIX), configuration);
      for (Iterator<String> it = configuration.getKeys(); it.hasNext(); ) {
        String key = it.next();
        System.out.println(key);
      }
      ComputeWeightVertexProgram program = (ComputeWeightVertexProgram) VertexProgram.createVertexProgram(graph, configuration);

      return program;
    }
  }


}
