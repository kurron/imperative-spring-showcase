#!/usr/bin/env bash

PROGNAME=$(basename $0)
error_exit()
{
	  echo "${PROGNAME}: ${1:-"Unknown Error"}" 1>&2
	  exit 1
}

echo "Running application from Docker image. Type ctrl-c to stop."
RUN="docker run --hostname imperative \
                --interactive \
                --name imperative \
                --network host \
                --rm \
                --tty \
                kurron/showcase-imperative:latest"
echo "${RUN}"
${RUN} || error_exit "Unable to run the application"
