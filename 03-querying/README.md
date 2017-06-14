# Query Graph DB

I provide 3 different implementations of the queries discussed here, them being:


* [java_remote]: runs the queries in a remote instance of JanusGraph.
* [java_builtin]: runs the queries in a built-in instance of JanusGraph (but still connects to cassandra and elasticsearch).
* [ruby]: simple ruby version, that relies on Groovy scripts.


# Queries


Here I simply list the queries used in the Java implementations and explain what they do.

Every query starts by the `g` variable. This is simply an instance of a graph traversal, which allow us to construct queries and
then execute them. More on that on [tinkerpop documentation](http://tinkerpop.apache.org/docs/current/reference/).


## Getting the User by userName

```java
return g.V().hasLabel(Schema.USER).has(USER_NAME, eq(userName));
```

This is the most basic query and will be reused over and over again. Also, variations of the steps used here are also reused a lot in the other queries.
So make sure you understand well that it does and read the [tinkerpop documentation](http://tinkerpop.apache.org/docs/current/reference/) if you still have doupts


First step is `V()`. This will tell our traversal we are looking for vertices. Then we have some filters:

* `hasLabel`: let the DB knows we are looking for a specific kind of vertex.
* `has(USER_NAME, eq(...))`: let the DB knows we are looking for a specific property filter.


The `eq` operator in the later filter lets the DB know we are looking for an identical value. And, because we are packing those 2 conditions next to each other in the same query scope, JanusGraph understands it can use the index we created before to optmize our queries.

If we were just using one of those filters it would still work, but would require an entire graph scan to locate results, which is super slow. We could also have a centralized index for user names where we index every kind of Vertex, but this is not as efficient as having individual indexes for each one of our types. Remember, you want to store billions of vertices in your DB.



The return type of this query (as for the others we will discuss in this section) is a `Traversal<Vertex, Vertex>`, meaninig you are going from vertex and expect to return a vertex.

This query can then be iterated using `hasNext()` and `next()` methods.




## Getting user status update

```java
:
```

## Getting followed users

## Getting followers

## Recommending users

## Building a timeline
