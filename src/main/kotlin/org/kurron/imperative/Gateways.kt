package org.kurron.imperative

interface OutboundMessagingGateway {
    fun sendCharacterPointsAllocatedEvent( event: CharacterPointsAllocatedEvent )
}

class NullNotificationServiceGateway: OutboundMessagingGateway {
    override fun sendCharacterPointsAllocatedEvent(event: CharacterPointsAllocatedEvent) {}
}