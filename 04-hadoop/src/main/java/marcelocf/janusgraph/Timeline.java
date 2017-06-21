package marcelocf.janusgraph;

import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;

import java.util.Date;

/**
 * Prints out the timeline to stdout
 * <br/>
 * Created by marcelo on 17/06/21.
 */
public class Timeline {

  public static void main(String argv[]) throws Exception {
    JanusGraph graph = JanusGraphFactory.open(Schema.CONFIG_FILE);
    HadoopQueryRunner q = new HadoopQueryRunner(graph.traversal(), "testUser0");
    q.close();

    for(int i = 0; i < 100; i++) {
      long t = (new Date()).getTime();
      long c = q.countCommonFollowedUsers("testUser"+i);
      t = (new Date()).getTime() - t;
      System.out.println(c + " in " + t + "ms");
    }
    graph.close();
  }
}
