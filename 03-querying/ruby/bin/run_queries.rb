#!/usr/bin/env ruby
# this is a super simple graph db query execution example.


require 'gremlin_client'
require 'connection_pool'
require 'pp'


conn = GremlinClient::Connection.new


parameter = {
  userNameProperty: 'marcelocf.janusgraph.userName',
  userName: 'testUser0'
}

pp conn.send_query(
  'g.V().hasLabel("user").has(userNameProperty, userName)',
  parameter
)


# you can also use connection pools by

GremlinClient::Connection.pool =
  ConnectionPool.new(size: 15, timeout: 10) do
    GremlinClient::Connection.new(
      gremlin_script_path: 'scripts'
    );
  end
