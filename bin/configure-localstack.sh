#!/usr/bin/env bash

PROGNAME=$(basename $0)
error_exit()
{
	  echo "${PROGNAME}: ${1:-"Unknown Error"}" 1>&2
	  exit 1
}

echo "Creating AWS resources in localstack"
CREATE_SNS_TOPIC="aws --endpoint-url=http://localhost:4575 sns create-topic --name ronbo"
echo "${CREATE_SNS_TOPIC}"
${CREATE_SNS_TOPIC} || error_exit "Unable to create SNS Topic"
