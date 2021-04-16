package org.kurron.imperative.core

import org.kurron.imperative.gateway.outbound.MessagingGateway
import java.util.concurrent.ThreadLocalRandom

class PassThruEchoProcessor(private val gateway: MessagingGateway): EchoProcessor {

    override fun execute(command: EchoProcessor.Command) {
        val message = MessagingGateway.Message(command.alpha,randomBoolean())
        gateway.send(message)
    }

    private fun randomBoolean() = ThreadLocalRandom.current().nextBoolean()
}