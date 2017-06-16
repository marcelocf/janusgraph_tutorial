# Ruby Queries

There are currently 2 ways for running queries in ruby. They both rely on
Groovy scripts - one uses strings containing the queries and other depends on
actual files.

On top of that, even though you can run queries using a single connection it is
recommended the use of connection pools in real life applications with
concurrent users.

This code presents:

* using 1 connection and Strings as parameters.
* using a connection pool and using query files.

This environment is built using:

1. [Ruby Versiuon Manager (RVM)](https://rvm.io/), even though you can make it
   work with something else.
1. [Bundler](http://bundler.io/) to manage dependencies.
1. [connection\_pool](https://rubygems.org/gems/connection_pool) gem for
   handling the ... yeah, you got it: connection pool.
1. [gremlin\_client](https://rubygems.org/gems/gremlin_client) gem to actually
   connect to our server and run queries.

For brevity the usual `run.sh` script was added. Have a read in it to
understand what is going on, but basically all you gotta do is:

1. install ruby
2. install dependencies
3. execute `./bin/run_queries.rb`.


