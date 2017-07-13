package marcelocf.janusgraph;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Date;
import java.util.Map;

import static marcelocf.janusgraph.Schema.*;
import static org.apache.tinkerpop.gremlin.process.traversal.Order.decr;
import static org.apache.tinkerpop.gremlin.process.traversal.P.without;

/**
 * Created by marcelo on 17/06/21.
 */
public class HadoopQueryRunner extends QueryRunner {

  public HadoopQueryRunner(GraphTraversalSource traversalSource, String userName) throws Exception {
    super(traversalSource, userName);
  }

  public long countCommonFollowedUsers(String userB) {
    return countCommonFollowedUsers(getUser(userB).next());
  }

  public long countCommonFollowedUsers(Vertex otherUser) {
    return g.
        V(otherUser.id()).aggregate("otherUser"). // store otherUser in a side effect for later usage
        V(getUser().next().id()).                     // then starts traversing from userA
        out("follows").
        in("follows").
        where(P.within("otherUser")).
        count().
        next(); // we don't need to check for existence because this can be just null
  }


  public long countPostsPerDaySince(long since) {
    return countPostsPerDaySince(getUser().next(), since);
  }

  public long countPostsPerDaySince(String userName, long since) {
    return countPostsPerDaySince(getUser(userName).next(), since);
  }

  public long countPostsPerDaySince(Vertex user, long since) {
    long days = ( (new Date()).getTime() - since ) / (1000*60*60*24);
    if(days == 0) {
      days = 1;
    }
    return countPostsSince(user, since) / days;
  }

  public long countPostsSince(Vertex user, long since) {
    return g.
        V(user).
        outE(Schema.POSTS).
        has(Schema.CREATED_AT, P.gte(since)).
        count().
        next();
  }

  @Override
  public GraphTraversal<Vertex, Vertex> getFollowRecommendation(){
    return getUser().
        aggregate("me").
        aggregate("ignore").
        outE(Schema.FOLLOWS).
        order().by(CreateWeightIndex.WEIGHT, decr).
        outV().
        aggregate("ignore").
        cap("me").
        unfold().
        inE(Schema.FOLLOWS).
        order().by(CreateWeightIndex.WEIGHT, decr).
        inV().
        where(without("ignore"));
  }

  public GraphTraversal<Vertex, Map<String, Map<String,Object>>> getTimeline3(int limit){
    return getUser().
        aggregate("users").
        local(
            __.outE(FOLLOWS).
                has(CreateWeightIndex.WEIGHT).
                order().by(CreateWeightIndex.WEIGHT, decr).
                dedup().
                limit(50).
                inV()
        ).
        aggregate("users").
        cap("users").
        unfold().
        as(userVertex).
        outE(POSTS).
        dedup().
        as(postsEdge).
        order().by(CREATED_AT, decr).
        limit(limit).
        inV().
        as(statusUpdateVertex).
        select(userVertex, postsEdge, statusUpdateVertex);
  }
}
