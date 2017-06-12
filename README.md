# JanusGraph tutorial


This is a hands on guide for JanusGraph. It is organized in sections (each folder is an independent project with a section) and it is expected you follow each guide in order.


Every code here assumes you are running JanusGraph 0.1.0 locally. To accomplish this, run the commands:

```bash
$ wget https://github.com/JanusGraph/janusgraph/releases/download/v0.1.0/janusgraph-0.1.0-hadoop2.zip
$ unzip janusgraph-0.1.0-hadoop2.zip 
$ cd janusgraph-0.1.0-hadoop2/
$ ./bin/janusgraph.sh start
```


The last command should output:

```
Forking Cassandra...
Running `nodetool statusthrift`.. OK (returned exit status 0 and printed string "running").
Forking Elasticsearch...
Connecting to Elasticsearch (127.0.0.1:9300)... OK (connected to 127.0.0.1:9300).
Forking Gremlin-Server...
Connecting to Gremlin-Server (127.0.0.1:8182)..... OK (connected to 127.0.0.1:8182).
Run gremlin.sh to connect.
```


Meaning you have cassandra and elasticsearch listening on the loopback interface. This is important for the examples to work.


If you need to clean your data:

1. stop janus graph
1. `rm -rf db`
1. start janus graph



It is also recommended that you read:

* [GraphDB - diving into JanusGraph part 1](https://medium.com/finc-engineering/graph-db-diving-into-janusgraph-part-1f-199b807697d2) (3 min read)
* [GraphDB - diving into JanusGraph part 2](https://medium.com/finc-engineering/graph-db-diving-into-janusgraph-part-2-f4b9cbd967ac) (4 min read)


## Why

I wrote this guide after trying to find my way through this technology. I had to learn it because the traditional tools were not enough for the kind of data processing required in the task assigned to me.

JanusGraph has proven to be a solid and reliable solution to our project and I hope this guide is useful for you.

## Scope

On this tutorial we will build the backend database of a twitter clone. The sections are divided into:

1. basic schema
1. data loading
1. querying
1. indexing for performance
1. hadoop integration

By the end of this tutorial you should be able to design your own database backend using JanusGraph.

## Code

Every code depends on the main schema class. This is a design decision to reuse code and have more consistency in naming. Also, by doing so, we avoid usage of hard coded Strings as much as possible.

Also, every code loads the configuration from the [conf](conf) folder. It forces you to run the code from a specific folder in order to locate the file, but makes it a lot easier to maintain this code.

To ease your life, there is a simple shell script in each section called `run.sh`. This will build and evoke the example code for you.
