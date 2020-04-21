package org.kurron.imperative

import org.springframework.cloud.aws.messaging.core.NotificationMessagingTemplate
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener
import org.springframework.messaging.handler.annotation.Headers
import org.springframework.stereotype.Component

interface OutboundMessagingGateway {
    fun sendCharacterPointsAllocatedEvent( event: CharacterPointsAllocatedEvent )
}

class SimpleNotificationServiceGateway(private val template: NotificationMessagingTemplate, private val configuration: ApplicationConfiguration): OutboundMessagingGateway {
    override fun sendCharacterPointsAllocatedEvent(event: CharacterPointsAllocatedEvent) {
        template.convertAndSend(configuration.snsTopic, event )
    }
}

class NullNotificationServiceGateway: OutboundMessagingGateway {
    override fun sendCharacterPointsAllocatedEvent(event: CharacterPointsAllocatedEvent) {}
}

@Component
class SqsInboundMessageGateway {

    @SqsListener("alpha")
    fun queueListener( event: CharacterPointsAllocatedEvent, @Headers headers: Map<String,String> ) {
        val i = 1
    }
}