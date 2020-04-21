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

SET_RAW_DELIVERY_ON_SUBSCRIPTION="aws --endpoint-url=http://localhost:4575 sns set-subscription-attributes --subscription-arn arn:aws:sns:us-east-1:000000000000:ronbo:31bab3ee-de00-4eaa-b7d0-247361688580 --attribute-name RawMessageDelivery --attribute-value true"
echo "${SET_RAW_DELIVERY_ON_SUBSCRIPTION}"
${SET_RAW_DELIVERY_ON_SUBSCRIPTION} || error_exit "Unable to set Raw Delivery"

# escaping of JSON is too painful so I can't echo the command
aws --endpoint-url=http://localhost:4575 sns set-subscription-attributes --subscription-arn arn:aws:sns:us-east-1:000000000000:ronbo:31bab3ee-de00-4eaa-b7d0-247361688580 --attribute-name FilterPolicy --attribute-value "{\"type\":[\"event\"],\"label\":[\"entity.character-points-allocated\"]}" || error_exit "Unable to set Filter Policy"
