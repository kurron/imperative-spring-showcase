package org.kurron.imperative

import org.springframework.http.HttpStatus.CONFLICT
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
class Echo {

    @GetMapping("/hello", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun hello() = mapOf( "operation" to randomHexString(), "code" to randomHexString(), "should not be sent" to "" )

    @GetMapping("/sorted", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun sorted() = Stuff( randomHexString(), randomHexString(), randomHexString(), "" )

    @GetMapping("/failure", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun failure(): Stuff = if ( ThreadLocalRandom.current().nextBoolean() ) Stuff( randomHexString(), randomHexString(), randomHexString(), "" ) else throw ApplicationException( 1234, CONFLICT, "Simulated failure!")
}


class RandomFailure(message: String? = "Simulated failure") : Exception(message)