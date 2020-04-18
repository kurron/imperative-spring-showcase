package org.kurron.imperative

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.ThreadLocalRandom

/**
 * Fake payload.
 */
data class Stuff( val zulu: String, val alpha: String, val hotel: String, val blank: String )

@RestController
@RequestMapping("/echo")
class Echo: AbstractLogAware() {

    @GetMapping("/hello", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun hello() = mapOf( "operation" to randomHexString(), "code" to randomHexString(), "should not be sent" to "" )

    @GetMapping("/sorted", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun sorted() = Stuff( randomHexString(), randomHexString(), randomHexString(), "" )

    @GetMapping("/failure", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun failure(): Stuff {
        val shouldFail = ThreadLocalRandom.current().nextBoolean()
        return if (shouldFail) {
            feedback.send(ApplicationFeedback.GENERAL_FAILURE, "Oops!")
            throw RandomizeFailure()
        }
        else {
            feedback.send( ApplicationFeedback.TIMING_MESSAGE, ThreadLocalRandom.current().nextInt( Integer.MAX_VALUE ) )
            Stuff(randomHexString(), randomHexString(), randomHexString(), "")
        }
    }
}

// just to show that application failures can also be housed in individual failure classes
class RandomizeFailure: ApplicationException( ApplicationFeedback.RANDOM_FAILURE, "Hard coded." )