#!/usr/bin/env ruby
# this is a super simple graph db query execution example.

require 'gremlin_client'
require 'connection_pool'
require 'pp'



# $bindings are set as variables you can use within your query
$bindings = {
  userLabel:         'user',
  statusUpdateLabel: 'statusUpdate',
  followsLabel:      'follows',
  postsLabel:        'posts',
  contentProperty:   'marcelocf.janusgraph.content',
  createdAtProperty: 'marcelocf.janusgraph.createdAt',
  userNameProperty:  'marcelocf.janusgraph.userName',
  userName:          'testUser0'
}


########################
# Using One Connection #
########################

puts 'Connecting to Gremlin Server'
$conn = GremlinClient::Connection.new


def print(description, results)
  puts <<EOF
---------------------------------------------------------------------
#{description}

#{results.pretty_inspect}
---------------------------------------------------------------------
EOF
end
print(1,2)

def run_query(description, query)
  print(
    description,
    $conn.send_query(
      query,
      $bindings
    )
  )
end

run_query(
  'Query the user',
  'g.V().hasLabel("user").has(userNameProperty, userName)'
)


# you can also use connection pools by

GremlinClient::Connection.pool =
  ConnectionPool.new(size: 15, timeout: 10) do
    GremlinClient::Connection.new(
      gremlin_script_path: 'scripts'
    );
  end

GremlinClient::Connection.pool.with do |pooled_conn|
  pp pooled_conn.send_file('follow_recommendation.groovy', $bindings)
end
