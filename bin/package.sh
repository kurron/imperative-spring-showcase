#!/usr/bin/env bash

PROGNAME=$(basename $0)
error_exit()
{
	  echo "${PROGNAME}: ${1:-"Unknown Error"}" 1>&2
	  exit 1
}

# use time as the tag
UNIXTIME=$(date +%s)

echo "Packaging application as a Docker image"
BUILD="docker build --rm \
                    --build-arg APPJAR=build/libs/imperative-0.0.1-SNAPSHOT.jar \
                    --tag kurron/showcase-imperative:${UNIXTIME} \
                    --tag kurron/showcase-imperative:latest \
                    ."
echo "${BUILD}"
${BUILD} || error_exit "Unable to package application"
