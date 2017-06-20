# Hadoop

In the previous section we ran some queries. They are quite good and all, but
the one for timeline can be quite slow at times.

Consider users that follow an absurd number of people. Talking here about
hundred of thousands of people. In this case the timeline would take a long
time to process.

This is what we call a supernode problem, and there a couple of ways we can
handle it.

We can both identify the supernodes and give them some sort of special
treatment or we can simply use an efficient way to identify the most relevant
information we need to retrieve in any given step of our query.

Either way, we do need to go through most of our graph and potentially run
quite complex formulation (well, at least complex for billions of users).

A way to accomplish this with acceptable performance is by means of hadoop.

##  GremlinHadoop

JanusGraph is tighly integrated with the Tinkerpop stak. This stak provides
GremlinHadoop, which is what actually handles the hadoop side of JanusGraph.

The way it works is by

1. divide the graph into partitions (the details on this is beyound my
   understanding)
2. filter the data in those partitions.
3. for each filtered partition run your own code, where:
  * worker is initialized
  * for each contained vertex in this partition run an specific method
  * worker is finalized.


This is a super basic overview of what this stack is capable of, but serves as
a simple way to get started. You can effectively do complex map&reduce tasks
with this and gremlin comes with a couple of quite complex examples.

To learn it in detail, please reffer to the [official
docs](http://tinkerpop.apache.org/docs/current/reference/#graphcomputer) as
usual.

## Our code

On our previous timeline code, we had the following query:

```java
    return getUser().
        aggregate("users").
        out(FOLLOWS).
        aggregate("users").
        cap("users").
        unfold().
        as(userVertex).
        outE(POSTS).
        as(postsEdge).
        order().by(CREATED_AT, decr).
        limit(limit).
        inV().
        as(statusUpdateVertex).
        select(userVertex, postsEdge, statusUpdateVertex);
```

If the user follows way too many people we will load our memory with undesired
content. Therefore we need to rank the most relevant users to appear in this
timeline.

What `most relevant` means is a complex subject on its own and I don't
recommend using the following formulation in a real life application. There are
companies specialized on this and you are probably better off using machine
learning, trying to improve the problem of engagement.

With that said:

* **user:** to whom we want to provide recommendation
* **followedUser:** user we are trying to score for **user**.

Considere all numbers as double precision floats.

```java
dayThreshold = max(oneWeekAgo, startedFollowingAt)

postsCount = countPostsSince(dayThreshold)
postsPerDay = postsCount/daysFrom(dayThreshold)

followedCounts = countCommonFollowed(user, followedUsers)

relevance = (postsPerDay + followedCounts) / 2
```

The final reelevance formulation considers both how active the user has been
recently and how many commong people both users follows.


Adding such weight to the `follows` edge, we can change the query to:


```java
    return getUser().
        aggregate("users").
        local(
          outE(FOLLOWS).
          has(WEIGHT, gt(0)).
          order().by(WEIGHT, decr).
          limit(200)
        ).
        aggregate("users").
        cap("users").
        unfold().
        as(userVertex).
        outE(POSTS).
        as(postsEdge).
        order().by(CREATED_AT, decr).
        limit(limit).
        inV().
        as(statusUpdateVertex).
        select(userVertex, postsEdge, statusUpdateVertex);
```

This way we limit the number of users we retrieve for the timeline, even if our
recommendation is for our biggest supernode.


## Running

If you got this far in this tutorial I am assuming you are familiar on how to
run the code by hand now. If not, go back and study the `run.sh` file for each
example.

Here we have 3 main programs:

* **create-index:** will create the *weight* index.
* **compute-weight:** run spark with the built-in spark
* **timeline:** print the timeline for a specific user.

Make sure to run the commands in the presented order.
