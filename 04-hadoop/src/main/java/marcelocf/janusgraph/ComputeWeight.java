package marcelocf.janusgraph;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.tinkerpop.gremlin.process.computer.ComputerResult;
import org.apache.tinkerpop.gremlin.spark.structure.Spark;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.util.GraphFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.hasLabel;

/**
 * Use the @{@link ComputeWeightVertexProgram} to calculate weight for every user to user follow link.
 *
 * Created by marcelo on 17/06/20.
 */
public class ComputeWeight {
  private final static Logger LOGGER = LoggerFactory.getLogger(ComputeWeight.class);

  public final static String HADOOP_CONFIG_FILE = "conf/janusgraph-hadoop.properties";

  public static void main(String[] argv) throws InterruptedException, IOException {
    Graph hadoopGraph = null;

    try {
      LOGGER.info("Connect to the hadoop graph");
      hadoopGraph = GraphFactory.open(new PropertiesConfiguration(HADOOP_CONFIG_FILE));
      ComputeWeightVertexProgram.Builder builder = ComputeWeightVertexProgram.build().withRwGraphConfig(Schema.CONFIG_FILE);

      ComputerResult result = hadoopGraph.
          compute().
          program(
              builder.create(hadoopGraph)
          ).
          vertices(hasLabel(Schema.USER)).
          submit().get();
      result.close();
      hadoopGraph.close();
      Spark.close();

      hadoopGraph = null;
    } catch (Exception e) {
      e.printStackTrace();
      try {
        if (hadoopGraph != null) {
          hadoopGraph.close();
          Spark.close();
        }
      } catch (Exception e1) {
        System.err.println("Couldn't close graph or spark...");
      }
    }

    // we need to call this one or else the program will be waiting forever
    LOGGER.info("bye bye");
    System.exit(0);
  }
}
