package org.kurron.imperative

import org.slf4j.helpers.MessageFormatter
import org.springframework.hateoas.Link
import org.springframework.hateoas.mediatype.vnderrors.VndErrors
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

/**
 * The collection of all application feedback.  Used to assemble log messages and API responses.
 */
enum class ApplicationFeedback: FeedbackContext {
    GENERAL_FAILURE {
        override val code: Int get() = 1
        override val status: HttpStatus get() = HttpStatus.INTERNAL_SERVER_ERROR
        override val messageFormat: String get() = "A system failure has occurred: {}"
        override val logLevel: Level get() = Level.ERROR
    },
    RANDOM_FAILURE {
        override val code: Int get() = 2
        override val status: HttpStatus get() = HttpStatus.I_AM_A_TEAPOT
        override val messageFormat: String get() = "Forced to fail: {}"
        override val logLevel: Level get() = Level.WARN
    },
    TIMING_MESSAGE {
        override val code: Int get() = 3
        override val status: HttpStatus get() = HttpStatus.OK
        override val messageFormat: String get() = "Processing took {} milliseconds to complete."
        override val logLevel: Level get() = Level.DEBUG
    }
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
     * What HTTP status code to use as a result of the scenario.
     */
    val status: HttpStatus

    /**
     * SLF4J compatible message format, e.g. "Too many invocations. {} within the last {} seconds."
     */
    val messageFormat: String

    /**
     * When logging the scenario, what level to use.
     */
    val logLevel: Level
}

/**
 * Signals a failure scenario that was trapped by the application logic.
 */
open class ApplicationException(val context: FeedbackContext, vararg messageArgument: Any): Exception(MessageFormatter.arrayFormat( context.messageFormat, messageArgument ).message )

/**
 * This class handles both application and system exceptions, translating them into the standard vnd.error format.
 */
@RestControllerAdvice
class GlobalExceptionHandler: ResponseEntityExceptionHandler() {

    @ExceptionHandler( Exception::class )
    fun fallbackHandler(failure: Exception): ResponseEntity<VndErrors> {
        return wrapDetails(failure.message!!, HttpStatus.INTERNAL_SERVER_ERROR, randomHexString(), assembleHelpLink( 0 ))
    }

    @ExceptionHandler( ApplicationException::class )
    fun applicationFailureHandler(failure: ApplicationException): ResponseEntity<VndErrors> {
        return wrapDetails(failure.message!!, failure.context.status, randomHexString(), assembleHelpLink( failure.context.code ))
    }

    private fun assembleHelpLink( code: Int ): Link {
        // Links to a document describing the error. This has the same definition as the help link relation in the HTML5 specification
        return Link("help", "https://help.example.com/failure-codes/$code")
    }

    private fun wrapDetails(message:String, status: HttpStatus, logReference: String, help: Link): ResponseEntity<VndErrors> {
        val logs = Link( "logs", "https://logs.example.com/trace-id/$logReference" )
        val trace = Link( "trace", "https://tracing.example.com/trace-id/$logReference" )
        val details = VndErrors(logReference, message, help, logs, trace)
        val mediaType = MediaType.parseMediaType("application/vnd.error+json")
        return ResponseEntity.status(status).contentType(mediaType).body( details )
    }
}
