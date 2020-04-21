package org.kurron.imperative

import org.springframework.cloud.aws.messaging.core.NotificationMessagingTemplate

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