#!/bin/bash
#
# this will make sure janus is downloaded and running


JANUS_VERSION=0.1.0
WORKDIR="$(realpath `dirname $0`)/work"

JDIR=janusgraph-${JANUS_VERSION}-hadoop2
PKGNAME=${JDIR}.zip

JPATH="${WORKDIR}/${JDIR}"

function janus_exists(){
  test -d "${JPATH}"
}

function download_janus(){
  echo Downloading janus
  mkdir -p $WORKDIR
  cd "$WORKDIR"
  wget -c https://github.com/JanusGraph/janusgraph/releases/download/v${JANUS_VERSION}/${PKGNAME}
  echo Extracting
  unzip -o -q $PKGNAME
}


janus_exists || download_janus

echo starting janus
# make sure it is not running first
cd "$JPATH" && ./bin/janusgraph.sh stop
cd "$JPATH" && ./bin/janusgraph.sh start



cat << EOF


=================================================
|| JanusGraph should be running now!           ||
||                                             ||
|| Go ahead and try the code in this tutorial! ||
||                                             ||
|| I hope you have fun learning.               ||
||                                             ||
||                          Marcelo C. Freitas ||
=================================================
EOF
