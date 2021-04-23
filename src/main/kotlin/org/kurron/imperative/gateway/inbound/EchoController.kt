package org.kurron.imperative.gateway.inbound

import org.kurron.imperative.*
import org.kurron.imperative.core.EchoProcessor
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.ThreadLocalRandom

/**
 * Sample controller used to showcase various operations.
 */
@RestController
@RequestMapping("/echo")
class EchoController(private val processor: EchoProcessor): AbstractLogAware() {
    @GetMapping("/hello", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun hello() = mapOf( "operation" to randomHexString(), "code" to randomHexString(), "should not be sent" to "" )

    @GetMapping("/sorted", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun sorted() = Stuff( randomHexString(), randomHexString(), randomHexString(), "" )

    @GetMapping("/failure", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun failure(): Stuff {
        val shouldFail = randomBoolean()
        return if (shouldFail) {
            feedback.send(LoggingFeedback.GENERAL_MESSAGE, "Oops!")
            throw RandomizeFailure()
        }
        else {
            feedback.send( LoggingFeedback.TIMING_MESSAGE, randomInteger() )
            Stuff(randomHexString(), randomHexString(), randomHexString(), "")
        }
    }

    @GetMapping("/message", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun sendMessage(): ResponseEntity<String> {
        feedback.send(LoggingFeedback.PUBLISH_EVENT_MESSAGE, "Hello" )
        val command = EchoProcessor.Command(randomHexString())
        processor.execute(command)
        return ResponseEntity.accepted().body("job accepted")
    }

    private fun randomBoolean() = ThreadLocalRandom.current().nextBoolean()
    private fun randomInteger() = ThreadLocalRandom.current().nextInt( Integer.MAX_VALUE )
    private fun randomHexString() = Integer.toHexString( ThreadLocalRandom.current().nextInt( Integer.MAX_VALUE ) ).toUpperCase()

    // fake payload
    data class Stuff( val zulu: String, val alpha: String, val hotel: String, val blank: String )

    // just to show that failures can also be housed in individual failure classes
    class RandomizeFailure: RestGatewayException( ApiFeedback.RANDOM_FAILURE, "Hard coded." )
}