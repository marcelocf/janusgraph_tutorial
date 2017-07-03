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

## Create new Index

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
2. remove your db folder.
3. start cassandra and elasticsearch individualy.
4. run the schema creation (section 01) code.
5. create the index for weights (part of section 04).
6. run this code.
7. go back to section (02) to populate the DB.
8. run the compute-weight code from section (04).
9. test the timeline again to see performance difference.