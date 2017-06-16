String userVertex = 'userVertex';
String postsEdge = 'postsEdge';
String statusUpdateVertex = 'statusUpdateEdge';

String usersAggregate = 'users'

g.V().hasLabel(userLabel).has(userNameProperty, userName).
        aggregate(usersAggregate).
        out(followsLabel).
        aggregate(usersAggregate).
        cap(usersAggregate).
        unfold().
        as(userVertex).
        outE(postsLabel).
        as(postsEdge).
        order().by(createdAtProperty, decr).
        limit(10).
        inV().
        as(statusUpdateVertex).
        select(userVertex, postsEdge, statusUpdateVertex)
