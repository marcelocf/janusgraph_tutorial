#!/bin/bash
#
# this will make sure janus is downloaded and running


JANUS_VERSION=0.1.0
WORKDIR="$(realpath `dirname $0`)/work"

JDIR=janusgraph-${JANUS_VERSION}-hadoop2
PKGNAME=${JDIR}.zip

JPATH="${WORKDIR}/${JDIR}"

echo stopping janus
cd "$JPATH" && ./bin/janusgraph.sh stop
