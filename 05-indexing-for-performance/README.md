# Indexing for Performance

For now our queries are taking quite a long time to run. We want to provide a blasting fast system, so we need to make sure our queries
can be executed in a fraction of what they are right now.

The reason for this runtime is we centralize our indexes globally. Meaning the `weight` property of every `follows` edge is indexed in
the same elasticsearch index.

There is a better way for doing that. From [the official documentation](http://docs.janusgraph.org/latest/indexes.html):


> JanusGraph supports two different kinds of indexing to speed up query processing: graph indexes and vertex-centric indexes. Most graph queries start the traversal from a list of vertices or edges that are identified by their properties. Graph indexes make these global retrieval operations efficient on large graphs. Vertex-centric indexes speed up the actual traversal through the graph, in particular when traversing through vertices with many incident edges.


We want to use Vertex Centric indexes while traversing. To do this, we need to change the index type for the properties:

* `weight` in `follows` edges.
* `createdAt` in `posts` edges.

The way to do this is:

1. delete old index.
2. create new index.
3. tell Janus Graph to reindex everything.


## Delete old Index

Deleting an index is just a matter of running:

```java
JanusGraphIndex index = mgt.getGraphIndex(Schema.indexName(label, propertyKey));
mgt.updateIndex(index, SchemaAction.REMOVE_INDEX);
```

The index deletion is handled by the `updateIndex` method. Other actions can be used, plese check the
docs and the `SchemaAction` class.

## Create new Index

After this is done, you can create a new index. Previously we created mixed indexes. Now we want a vertex
centric index, and the creation process is fairly different:

```java
EdgeLabel edgeLabel = mgt.getEdgeLabel(label);
PropertyKey key = mgt.getPropertyKey(propertyKey);
mgt.buildEdgeIndex(edgeLabel, Schema.indexName(label, propertyKey), Direction.BOTH, Order.decr, key);
```

Even though it is called **vertex** centric index, it is an index for edges properties. Other important
settings are the direction (index only for incoming or outgoing vertices or both) and default order.

This means you will have localized in your vertex an index of elements based on this property. And it will
be already preordered as you wish to iterate; speed!

## Await Index

For good measure, after committing the management operation, let's wait for the indexes to be
ready to use:

```java
String indexName = Schema.indexName(label, propertyKey);
((ManagementSystem)mgt).awaitGraphIndexStatus(graph, indexName).call();
```

We need to cast it to a `ManagementSystem` instance.

This operation can take a long time, so patience my old friend. Patience. :)

## Reindex

This code is available there, but commented out. The reasoning for this is that reindexing is a time
consuming task that requires further configuration to run in optimal time.

Also, I haven't performed this operation myself in a real application, so I don't think I am able to
 teach properly how to do it.
 
With that said, reindexing should be as simple as running:

```java
MapReduceIndexManagement mr = new MapReduceIndexManagement(graph);
JanusGraphIndex index = mgt.getGraphIndex(Schema.indexName(label, propertyKey));
mr.updateIndex(index, SchemaAction.REINDEX).get();
```

This should run reindexing in parallel for better performance.

You could also do this outside of MapReduce.


### But how do I test?

Well, to make things easier and test how much you have learned, do the following:

1. stop gremlin-server/cassandra/elasticsearch
1. remove your db folder.
1. start cassandra and elasticsearch individualy.
1. run the schema creation (section 01) code.
1. create the index for weights (part of section 04).
1. run this code.
1. go back to section (02) to populate the DB.
1. on hadoop section, run the create-supernodes command.
1. run the compute-weight code from section (04).
1. now see the difference on performance on both timelines.
