package org.kurron.imperative

import org.springframework.hateoas.Link
import org.springframework.hateoas.mediatype.vnderrors.VndErrors
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.I_AM_A_TEAPOT
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
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
    fun failure(): Stuff = if ( ThreadLocalRandom.current().nextBoolean() ) Stuff( randomHexString(), randomHexString(), randomHexString(), "" ) else throw RandomFailure()
}

@RestControllerAdvice
class GlobalExceptionHandler: ResponseEntityExceptionHandler() {

    @ExceptionHandler( Exception::class )
    fun fallbackHandler(failure: Exception): ResponseEntity<VndErrors> {
        // Links to a resource that this error is related to. See RFC6903 for further details.
        val about = Link( "about", "https://help.example.com/general-failure" )
        return wrapDetails(failure.message!!, INTERNAL_SERVER_ERROR, randomHexString(), about)
    }

    @ExceptionHandler( RandomFailure::class )
    fun randomFailureHandler(failure: Exception): ResponseEntity<VndErrors> {
        // Links to a resource that this error is related to. See RFC6903 for further details.
        val about = Link( "about", "https://help.example.com/random-failure" )
        return wrapDetails(failure.message!!, I_AM_A_TEAPOT, randomHexString(), about)
    }

    private fun wrapDetails(message:String, status: HttpStatus, logReference: String, about: Link ): ResponseEntity<VndErrors> {
        // Links to a document describing the error. This has the same definition as the help link relation in the HTML5 specification
        val help = Link( "help", "https://logs.example.com/$logReference" )
        val details = VndErrors(logReference, message, help, about )
        val mediaType = MediaType.parseMediaType("application/vnd.error+json")
        return ResponseEntity.status(status).contentType(mediaType).body( details )
    }
}

class RandomFailure(message: String? = "Simulated failure") : Exception(message)