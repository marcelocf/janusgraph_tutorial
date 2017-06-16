g.V().hasLabel(userLabel).has(userNameProperty, userName).
        aggregate("me").
        aggregate("ignore").
        out(followsLabel).
        aggregate("ignore").
        cap("me").
        unfold().
        in(followsLabel).
        where(without("ignore"));
