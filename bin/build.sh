#!/usr/bin/env bash

PROGNAME=$(basename $0)
error_exit()
{
	  echo "${PROGNAME}: ${1:-"Unknown Error"}" 1>&2
	  exit 1
}

JDK_IMAGE="azul/zulu-openjdk:11"
echo "Pulling down current JDK Docker image"
PULL="docker pull ${JDK_IMAGE}"
echo "${PULL}"
${PULL} || error_exit "Unable to download JDK Docker image"

USER_ID=$(id -u $(whoami))
GROUP_ID=$(id -g $(whoami))
HOME_DIR=$(cut -d: -f6 < <(getent passwd ${USER_ID}))

#DOCKER_GROUP_ID=$(cut -d: -f3 < <(getent group docker))
#               --group-add ${DOCKER_GROUP_ID} \

CMD="docker run --hostname build-process \
                --env HOME=${HOME_DIR} \
                --interactive \
                --name build-process \
                --rm \
                --tty \
                --user=${USER_ID}:${GROUP_ID} \
                --volume $(pwd):$(pwd) \
                --volume ${HOME_DIR}:${HOME_DIR} \
                --volume /etc/passwd:/etc/passwd \
                --volume /etc/group:/etc/group \
                --workdir $(pwd) \
                ${JDK_IMAGE} \
		./gradlew --console=auto \
		          --gradle-user-home=${HOME_DIR} \
		          --warn \
              --no-daemon \
              --profile \
              --stacktrace \
              clean build"
echo "${CMD}"
${CMD} || error_exit "Build has failed"