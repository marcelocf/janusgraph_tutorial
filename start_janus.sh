#!/bin/bash
#
# this will make sure janus is downloaded and running


JANUS_VERSION=0.1.0

function realpathMac() {
    [[ $1 = /* ]] && echo "$1" || echo "$PWD/${1#./}"
}

case "$OSTYPE" in
	darwin*) WORKDIR="$(realpathMac `dirname $0`)/work" ;;
    *) WORKDIR="$(realpath `dirname $0`)/work" ;;
esac

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
  case "$OSTYPE" in
  darwin*)  curl -LO https://github.com/JanusGraph/janusgraph/releases/download/v${JANUS_VERSION}/${PKGNAME} ;; 
  *)        wget -c https://github.com/JanusGraph/janusgraph/releases/download/v${JANUS_VERSION}/${PKGNAME} ;;
  esac
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
