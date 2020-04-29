package org.kurron.imperative

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestOperations
import java.util.concurrent.ThreadLocalRandom

/**
 * Fake payload.
 */
data class Stuff( val zulu: String, val alpha: String, val hotel: String, val blank: String )

@RestController
@RequestMapping("/echo")
class Echo(private val template: RestOperations, private val gateway: OutboundMessagingGateway): AbstractLogAware() {

    @GetMapping("/hello", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun hello() = mapOf( "operation" to randomHexString(), "code" to randomHexString(), "should not be sent" to "" )

    @GetMapping("/sorted", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun sorted() = Stuff( randomHexString(), randomHexString(), randomHexString(), "" )

    @GetMapping("/failure", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun failure(): Stuff {
        val shouldFail = ThreadLocalRandom.current().nextBoolean()
        return if (shouldFail) {
            feedback.send(LoggingFeedback.GENERAL_MESSAGE, "Oops!")
//            feedback.send(LoggingFeedback.GENERAL_FAILURE, IllegalStateException("Show me the stack trace!"), "alpha", "bravo","charlie", "delta" )
            throw RandomizeFailure()
        }
        else {
            feedback.send( LoggingFeedback.TIMING_MESSAGE, ThreadLocalRandom.current().nextInt( Integer.MAX_VALUE ) )
            Stuff(randomHexString(), randomHexString(), randomHexString(), "")
        }
    }

    @GetMapping("/sleuth", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun sleuth(): ResponseEntity<String>  {
        val resources = listOf( "/posts", "/comments","/albums", "/photos", "/todos", "/users")
        val responses = resources.map { resource ->
            val url = "https://jsonplaceholder.typicode.com/$resource"
            feedback.send(LoggingFeedback.GET_MESSAGE, url )
            template.getForEntity(url, String::class.java )
        }
        return ResponseEntity.ok( "Called ${responses.size} resources" )
    }

    @GetMapping("/message", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun sendMessage(): ResponseEntity<String>  {
        feedback.send(LoggingFeedback.PUBLISH_EVENT_MESSAGE, "Hello" )
        gateway.sendCharacterPointsAllocatedEvent( CharacterPointsAllocatedEvent( "Hello" ) )
        return ResponseEntity.ok( "All good" )
    }
}

// just to show that failures can also be housed in individual failure classes
class RandomizeFailure: ApiException( ApiFeedback.RANDOM_FAILURE, "Hard coded." )