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

echo stopping janus
cd "$JPATH" && ./bin/janusgraph.sh stop
