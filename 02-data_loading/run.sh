#!/bin/bash

# this will compile, extract the product zip file and then run the comand


echo Building... && \
  ./gradlew distZip && \
  echo Extracting  && \
  pushd build/distributions && \
  unzip -q -o data_loading && \
  echo Running && \
  pushd data_loading && \
  ./bin/data_loading && \
  popd && popd
