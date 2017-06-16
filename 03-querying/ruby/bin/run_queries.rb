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

#{results.pretty_inspect.strip}
---------------------------------------------------------------------

EOF

end

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


##########################
# Using Connection Pools #
##########################

GremlinClient::Connection.pool =
  ConnectionPool.new(size: 15, timeout: 10) do
    GremlinClient::Connection.new(
      gremlin_script_path: 'scripts'
    );
  end

def run_pooled_query(description, filename)
  GremlinClient::Connection.pool.with do |pooled_conn|
    print(
      description,
      pooled_conn.send_file(filename, $bindings)
    )
  end
end
  

run_pooled_query(
  'Fetch Recommendation of Users to follow',
  'follow_recommendation.groovy'
)
