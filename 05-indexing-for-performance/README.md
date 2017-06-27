# Indexing for Performance

For now our queries are taking quite a long time to run. We want to provide a blasting fast system, so we need to make sure our queries
can be executed in a fraction of what they are right now.

The reason for this runtime is we centralize our indexes globally. Meaning the `weight` property of every `follows` edge is indexed in
the same elasticsearch index.

There is a better way for doing that. From [the official documentation](http://docs.janusgraph.org/latest/indexes.html):


> JanusGraph supports two different kinds of indexing to speed up query processing: graph indexes and vertex-centric indexes. Most graph queries start the traversal from a list of vertices or edges that are identified by their properties. Graph indexes make these global retrieval operations efficient on large graphs. Vertex-centric indexes speed up the actual traversal through the graph, in particular when traversing through vertices with many incident edges.
