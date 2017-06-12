#!/bin/bash

# this will compile, extract the product zip file and then run the comand


echo Building... && \
  ./gradlew distZip
  echo Extracting  && \
  pushd build/distributions && \
  unzip -q -o schema.zip && \
  echo Running && \
  pushd schema && \
  ./bin/schema && \
  popd && popd
