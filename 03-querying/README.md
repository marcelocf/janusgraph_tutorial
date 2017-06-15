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



**NOTE:** from now own, whenever we use `getUser` it means the result of this query!



## Getting user status update

```java
return getUser().out(Schema.POSTS);
```

From the user traversal, look for output edges with label `label` and return
the connected vertices.

We previously made sure the receiving end of `posts` edges are of Status
Updates. If that was not the case we would still have to filter by label.

## Getting followed users

```java
return getUser().out(Schema.FOLLOWS);
```

Behaves exactly the status update query, but return other users and traverse
through the `follows`  edges.

## Getting followers

```java
return getUser().in(Schema.FOLLOWS);
```

Here we use the `in` step, which is the oposite of `out` used previously. It
traverses the graph to edges of label `follows` that arrive on present node,

## Getting Followers of users a specific user follows

```java
return getUser().out(Schema.FOLLOWS).in(Schema.FOLLOWS);
```

Now we combine both. Notice that this query can return duplicated users. If it
is desired to filter out duplicates, we could use the `dedup` step.

## Recommending users

We want to recommend users as long our user is not following them; and we don't want to recommend our specific user to him again.

```java
return getUser().
        aggregate("me").
        aggregate("ignore").
        out(Schema.FOLLOWS).
        aggregate("ignore").
        cap("me").
        unfold().
        in(Schema.FOLLOWS).
        where(without("ignore"));
```


The important bits are:

* `aggregate` step: append the results to an alias (called sideffect) and 
  doesn't change it when returning.
* `cap`: emmits one or more side effects with the given aliases into a map from
  alias to result.
* `unfold`: receiving a map like the one from `cap`, return the results in a
  linear form.

Lastly the step `where(without("ignore"))` filters out the user and people
already being followed.

## Building a timeline
