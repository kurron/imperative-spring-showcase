package org.kurron.imperative.core

/**
 * Showcase how to accept commands from the Adapter and interact with the Ports.
 */
interface EchoProcessor {
    data class Command( val alpha: String)

    /**
     * Process the given command.
     * @param command instructions to execute.
     */
    fun execute(command:Command)
}