package org.kurron.imperative

import org.slf4j.helpers.MessageFormatter
import org.springframework.hateoas.Link
import org.springframework.hateoas.mediatype.problem.Problem
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.net.URI
import java.util.concurrent.ThreadLocalRandom

/**
 * The collection of all API feedback, used to assemble REST responses.
 */
enum class ApiFeedback: RestContext {
    GENERAL_FAILURE {
        override val code: Int get() = 100
        override val status: HttpStatus get() = HttpStatus.INTERNAL_SERVER_ERROR
        override val messageFormat: String get() = "A system failure has occurred: {}"
    },
    RANDOM_FAILURE {
        override val code: Int get() = 101
        override val status: HttpStatus get() = HttpStatus.I_AM_A_TEAPOT
        override val messageFormat: String get() = "Forced to fail: {}"
    }
}

/**
 * The collection of all logging feedback, used to assemble log and trace messages.
 */
enum class LoggingFeedback: LoggingContext {
    GENERAL_MESSAGE {
        override val code: Int get() = 200
        override val messageFormat: String get() = "A system failure has occurred: {} {} {} {}"
        override val logLevel: Level get() = Level.ERROR
    },
    TIMING_MESSAGE {
        override val code: Int get() = 201
        override val messageFormat: String get() = "Processing took {} milliseconds to complete."
        override val logLevel: Level get() = Level.DEBUG
    },
    GET_MESSAGE {
        override val code: Int get() = 202
        override val messageFormat: String get() = "GETing {}"
        override val logLevel: Level get() = Level.DEBUG
    },
    EVENT_RECEIVED_MESSAGE {
        override val code: Int get() = 203
        override val messageFormat: String get() = "Message received {}"
        override val logLevel: Level get() = Level.DEBUG
    },
    WIRETAP_MESSAGE {
        override val code: Int get() = 204
        override val messageFormat: String get() = "Wire tap {}"
        override val logLevel: Level get() = Level.DEBUG
    },
    PUBLISH_EVENT_MESSAGE {
        override val code: Int get() = 205
        override val messageFormat: String get() = "Publishing event {}"
        override val logLevel: Level get() = Level.DEBUG
    },
}

/**
 * Level to use when logging the failure.
 */
enum class Level { TRACE, DEBUG, INFO, WARN, ERROR }

/**
 * Retains information about a specific feedback scenario.
 */
interface FeedbackContext {

    /**
     * Unique error code for the scenario.
     */
    val code: Int

    /**
     * SLF4J compatible message format, e.g. "Too many invocations. {} within the last {} seconds."
     */
    val messageFormat: String
}

interface LoggingContext: FeedbackContext {
    /**
     * When logging the scenario, what level to use.
     */
    val logLevel: Level
}

interface RestContext: FeedbackContext {
    /**
     * What HTTP status code to use as a result of the scenario.
     */
    val status: HttpStatus
}

/**
 * Signals a failure scenario that was trapped by the application logic and should show up in the API response.
 */
open class InboundGatewayException(val context: RestContext, vararg messageArgument: Any): Exception(MessageFormatter.arrayFormat( context.messageFormat, messageArgument ).message )

/**
 * This class handles both application and system exceptions, translating them into the standard vnd.error format sent as the API response.
 */
@RestControllerAdvice
class GlobalApiExceptionHandler: ResponseEntityExceptionHandler() {

    @ExceptionHandler( Exception::class )
    fun fallbackHandler(failure: Exception): ResponseEntity<Problem> {
        return wrapDetails(failure.message!!, HttpStatus.INTERNAL_SERVER_ERROR, randomHexString(), assembleHelpLink( 0 ))
    }

    @ExceptionHandler( InboundGatewayException::class )
    fun applicationFailureHandler(failure: InboundGatewayException): ResponseEntity<Problem> {
        return wrapDetails(failure.message!!, failure.context.status, randomHexString(), assembleHelpLink( failure.context.code ))
    }

    private fun assembleHelpLink( code: Int ): Link {
        // Links to a document describing the error. This has the same definition as the help link relation in the HTML5 specification
        return Link.of("help", "https://help.example.com/failure-codes/$code")
    }

    private fun wrapDetails(message:String, status: HttpStatus, logReference: String, help: Link): ResponseEntity<Problem> {
        val logs = Link.of( "logs", "https://logs.example.com/trace-id/$logReference" )
        val trace = Link.of( "trace", "https://tracing.example.com/trace-id/$logReference" )
        // TODO: figure out how to send RFC-7807 information
        val details = Problem.create()
                             .withType(URI("https://example.com/probs/out-of-credit"))
                             .withTitle("You do not have enough credit.")
                             .withDetail( "Your current balance is 30, but that costs 50." )
                             .withInstance( URI("/account/12345/msgs/abc") )
                             .withStatus(status)
                             .withProperties( mapOf( "balance" to 30,
                                                     "accounts" to listOf("/account/12345","/account/67890" ),
                                                     "logs" to logs,
                                                     "trace" to trace,
                                                     "help" to help ) )
        val mediaType = MediaType.parseMediaType("application/vnd.error+json")
        return ResponseEntity.status(status).contentType(mediaType).body( details )
    }

    private fun randomHexString() = Integer.toHexString( ThreadLocalRandom.current().nextInt( Integer.MAX_VALUE ) ).toUpperCase()

}
