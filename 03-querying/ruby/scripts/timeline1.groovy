String usersAggregate = 'users'

g.V().hasLabel(userLabel).has(userNameProperty, userName).
        aggregate(usersAggregate).
        out(followsLabel).
        aggregate(usersAggregate).
        cap(usersAggregate).
        unfold().
        outE(postsLabel).
        order().by(createdAtProperty, decr).
        limit(10).
        inV();
