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

CREATE_SQS_QUEUE="aws --endpoint-url=http://localhost:4576 sqs create-queue --queue-name alpha"
echo "${CREATE_SQS_QUEUE}"
${CREATE_SQS_QUEUE} || error_exit "Unable to create SQS Queue"

BIND_QUEUE_TO_TOPIC="aws --endpoint-url=http://localhost:4575 sns subscribe --topic arn:aws:sns:us-east-1:000000000000:ronbo --protocol sqs --notification-endpoint arn:aws:sqs:us-east-1:000000000000:alpha"
echo "${BIND_QUEUE_TO_TOPIC}"
${BIND_QUEUE_TO_TOPIC} || error_exit "Unable to bind Queue to Topic"
