package org.kurron.imperative.gateway.outbound

/**
 * Knows how to publish messages to a broker.
 */
interface MessagingGateway {
    data class Message(val alpha:String, val bravo:Boolean)

    fun send(message:Message)
}