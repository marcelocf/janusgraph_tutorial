# Schema

Graph Databases are usually known for being schema free. This makes sense because on a Graph you are interested on how data relate to each other instead of trying to fetch intervals of data.


But there are cases when mixing both is the ideal scenario; this holds true to most real life applications, to be honest.

JanusGraph supports building schema and has actually 3 modes of operation:

1. schema free
1. mixed schema free/schema
1. require schema


We will create schema for everything we need, but we will not change the JanusGraph configuration defaults. Whenever you need to do so, however, you can read the [official reference](http://docs.janusgraph.org/latest/config-ref.html).

## Data Structure


Our base data structure will be `user` who can `post` `message`s and `follow` other `user`s. The `user` has username and the `message` has content. `post` and `follow` edges have a `createdAt` timestamp.



