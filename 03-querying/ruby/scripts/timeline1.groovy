g.V().hasLabel(userLabel).has(userNameProperty, userName).
        aggregate("users").
        out(followsLabel).
        aggregate("users").
        cap("users").
        unfold().
        outE(postsLabel).
        order().by(createdAtProperty, decr).
        limit(10).
        inV();
