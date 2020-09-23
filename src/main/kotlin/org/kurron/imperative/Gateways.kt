package org.kurron.imperative

import org.kurron.imperative.LoggingFeedback.EVENT_RECEIVED_MESSAGE
import org.kurron.imperative.LoggingFeedback.WIRETAP_MESSAGE
import org.springframework.cloud.aws.messaging.core.NotificationMessagingTemplate
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener
import org.springframework.context.annotation.Profile
import org.springframework.messaging.handler.annotation.Headers
import org.springframework.stereotype.Component

interface OutboundMessagingGateway {
    fun sendCharacterPointsAllocatedEvent( event: CharacterPointsAllocatedEvent )
}

class SimpleNotificationServiceGateway(private val template: NotificationMessagingTemplate, private val configuration: ApplicationConfiguration): OutboundMessagingGateway {
    override fun sendCharacterPointsAllocatedEvent(event: CharacterPointsAllocatedEvent) {
        template.convertAndSend(configuration.snsTopic, event, mutableMapOf<String,Any>( "type" to "event", "label" to "entity.character-points-allocated" ) )
    }
}

class NullNotificationServiceGateway: OutboundMessagingGateway {
    override fun sendCharacterPointsAllocatedEvent(event: CharacterPointsAllocatedEvent) {}
}

@Component
@Profile("cloud", "development")
class SqsInboundMessageGateway: AbstractLogAware() {
    @SqsListener("alpha")
    fun alphaListener( event: CharacterPointsAllocatedEvent, @Headers headers: Map<String,String> ) {
        feedback.send(EVENT_RECEIVED_MESSAGE, event.message)
    }

    @SqsListener("bravo")
    fun bravoListener( event: CharacterPointsAllocatedEvent, @Headers headers: Map<String,String> ) {
        feedback.send(WIRETAP_MESSAGE, event.message)
    }

}