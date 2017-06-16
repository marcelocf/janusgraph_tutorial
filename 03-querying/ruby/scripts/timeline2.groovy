g.V().hasLabel(userLabel).has(userNameProperty, userName).
        aggregate("users").
        out(followsLabel).
        aggregate("users").
        cap("users").
        unfold().
        as('userVertex').
        outE(postsLabel).
        as('postEdge').
        order().by(createdAtProperty, decr).
        limit(10).
        inV().
        as('statusUpdateVertex').
        select('userVertex', 'postEdge', 'statusUpdateVertex')
