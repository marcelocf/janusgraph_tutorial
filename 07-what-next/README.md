# What next?

Now that you have reached the current stage, I have a couple of recommendations on how to continue studying JanusGraph.


## Cluster Structure

For now we are running everything in the same node. It would be nice to split into different VMs. One for each component.

* cassandra
* elastic search
* gremlin server
* your own code
* spark master
* spark slave


Run this structure and keep checking where the load to see how each one of the examples affect performance in each one of
those components.


## More data

Next, try populate with more data. Use the 2nd section code as a base and create some users that follows/are followed by an
absurd ammount of users. See how this impact on performance.


## Users to follow recommendation

At last, try to write a query for recommendation users to be followed; remember to filter out users that are already being
followed.


## Better weight method

Try to understand the weight formula used in the hadoop section and come up with one of your own that has better results.



## Contribute!

Try to contribute to Janus Graph and this tutorial. You can reach the janus graph community at [gitter](https://gitter.im/janusgraph/janusgraph).
