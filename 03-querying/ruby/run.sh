#!/bin/bash

gem install bundler && \
  bundle install && \
  bundler exec bin/run_queries.rb
