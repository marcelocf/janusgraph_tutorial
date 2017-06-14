#!/bin/bash

# this will compile, extract the product zip file and then run the comand


echo Building... && \
  ./gradlew distZip && \
  echo Extracting  && \
  pushd build/distributions && \
  unzip -q -o java_remote && \
  echo Running && \
  pushd java_remote && \
  ./bin/java_remote && \
  popd && popd
