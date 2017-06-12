# Schema

Graph Databases are usually known for being schema free. This makes sense because on a Graph you are interested on how data relate to each other instead of trying to fetch intervals of data.


But there are cases when mixing both is the ideal scenario; this holds true to most real life applications, to be honest.

JanusGraph supports building schema and has actually 3 modes of operation:

1. schema free
1. mixed schema free/schema
1. require schema


We will create schema for everything we need, but we will not change the JanusGraph configuration defaults. Whenever you need to do so, however, you can read the [official reference](http://docs.janusgraph.org/latest/config-ref.html).

## Data Structure


Our base data structure will be `user` who `posts` `statusUpdate`s and `follows` other `user`s. The `user` has username and the `message` has content. `post` and `follow` edges have a `createdAt` timestamp.

The propertyKeys are using a namespace schema, but the labels are not for brevity.


## Code breakdown



### Connection

Firs we instantiate our Graph connection by issuing:

```java
    graph = JanusGraphFactory.open(configFile);
    mgt = graph.openManagement();
```

The `graph` variable holds the connection to the graph. It manages not only the connection state, but can also be used to commit transactions.
`mgt` is a variable holding the graph management implementation - this is exclusive to JanusGraph. This is the object we use to create our schema;


### User Schema

Then we create our user schema:

```java
  private void createUserSchema(){
    VertexLabel user = mgt.makeVertexLabel(USER).make();
    PropertyKey userName = mgt.makePropertyKey(USER_NAME).dataType(String.class).make();

    mgt.buildIndex(indexName(USER, USER_NAME), Vertex.class).
        addKey(userName).
        indexOnly(user).
        buildMixedIndex(BACKING_INDEX);
  }
```

The first 2 lines of the method we are declaring the kind of information we will store. In the PropertyKey we also specify the data type. Not every class in Java is supported, however. For instance, to store timestamp there is no `Date` type support. We need to use `Long` instead. Strings are fully supported.

Then we create a mixed index, which basically means the index will use the indexing backend (in our main example it is elasticsearch). Nothing big is set here, only that this string is searchable using elasticsearch and then we can do things like ordering.

### Status Update Schema

The next vertex type is quite similar to user:

```java
  private void createStatusUpdateSchema(){
    VertexLabel statusUpdate = mgt.makeVertexLabel(STATUS_UPDATE).make();
    PropertyKey content = mgt.makePropertyKey(CONTENT).dataType(String.class).make();

    mgt.buildIndex(indexName(STATUS_UPDATE, CONTENT), Vertex.class).
        addKey(content, Mapping.TEXT.asParameter()).
        indexOnly(statusUpdate).
        buildMixedIndex(BACKING_INDEX);
  }
```

One big difference, however, is that the `addKey` method also receives a mapping parameter of `TEXT`. This will tell our indexing backend to support full-text search. Useful for things like tracking hashtags and the sorts.


### Edges Schema

Lastly the edges `follows` and `posts` must be created:

```java
  private void createEdgeSchema() {
    EdgeLabel posts = mgt.makeEdgeLabel(POSTS).make();
    EdgeLabel follows = mgt.makeEdgeLabel(FOLLOWS).make();
    PropertyKey createdAt = mgt.makePropertyKey(CREATED_AT).dataType(Long.class).make();

    mgt.buildIndex(indexName(POSTS, CREATED_AT), Edge.class).
        addKey(createdAt).
        indexOnly(posts).
        buildMixedIndex(BACKING_INDEX);

    mgt.buildIndex(indexName(FOLLOWS, CREATED_AT), Edge.class).
        addKey(createdAt).
        indexOnly(follows).
        buildMixedIndex(BACKING_INDEX);
  }
```

Notice how we are using the `Long` type for our property key. Timestamps must be stored as a long number and the meaning of what a timestamp means is application specific.

Here we will be using the `Timestamp.getTime()` from Java as timestamp. In your app you can user whatever and maybe Integer might even be more appropriate for you.

Notice also that we created 2 different indexes - one for `posts` and other for `follows`. We do this for performance, at cost of complexity in your schema.
