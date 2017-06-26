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
    HadoopQueryRunner q = new HadoopQueryRunner(graph.traversal(), "testUser2");

    q.print(q.getTimeline(100));
    q.printTimeline(q.getTimeline2(100));
    q.printTimeline(q.getTimeline2(100));
    q.printTimeline(q.getTimeline2(100));
    q.printTimeline(q.getTimeline2(100));
    q.printTimeline(q.getTimeline2(100));
    for(int i = 0; i < 100; i++) {
      long t = (new Date()).getTime();
      long c = q.countCommonFollowedUsers("testUser"+i);
      long pc = q.countPostsPerDaySince("testUser"+i, (new Date()).getTime() - 1000*7*24*60*60);
      t = (new Date()).getTime() - t;
      System.out.println(c + " and " + pc + " in " + t + "ms");
    }

    q.printTimeline(q.getTimeline2(100));
    q.close();
    graph.close();
  }
}
