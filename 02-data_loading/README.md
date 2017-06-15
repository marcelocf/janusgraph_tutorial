# Data Loading

We need some fake data to test our queries. In order to do that, we generate
random data using  [java-faker lib](https://github.com/DiUS/java-faker).

We will add 1000 users, each with 1000 statusUpdate and following 50 users.

There is no real consistency check for either the user is following the same
user twice or even himself. There are relativelly big chances of that
happening, but it wouldn't invalidate our example.

## Adding Vertices

```java
  private Vertex addUser(String userName){
    Vertex user = graph.addVertex(Schema.USER);
    user.property(Schema.USER_NAME, userName);
    return user;
  }
```

Simply call the `addVertex` method in the graph instance passing the label you
want this vertex to have. You can set properties in remaining attributes or
calling the `property` method in the created vertex.


## Adding Edges

Say you want to create an edge connecting `a` and `b` as such:

```
a ===> b
```

The following syntax can be applied:

```java
a.addEdge(edgeLabel, b);
```

In our case, we have:

```java
  private Vertex addStatusUpdatew(Vertex user, String statusUpdateContent) {
    Vertex statusUpdate = graph.addVertex(Schema.STATUS_UPDATE);
    statusUpdate.property(Schema.CONTENT, statusUpdateContent);
    user.addEdge(Schema.POSTS, statusUpdate, Schema.CREATED_AT, getTimestamp());
    return statusUpdate;
  }
```

The 3rd line of the method contains the edge creation and setting of created at
property.


## Performance Considerations

This is a really simple code showing how to populate the database. It is not
optimized for bulk loading.

There are other things you need to consider in order to populate a graph with
billions of vertices.

It is out of the scope of this document to explain how to efficiently handle
bulk loading. Instead, the official JanusGraph
[documentation](http://docs.janusgraph.org/latest/) contains a
[section](http://docs.janusgraph.org/latest/bulk-loading.html) about this very
problem.
