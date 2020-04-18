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
 * The collection of all application errors.  Used to assemble log messages and API responses.
 */
enum class ApplicationFailures: FailureContext {
    GENERAL_FAILURE {
        override val code: Int get() = 1
        override val status: HttpStatus get() = HttpStatus.INTERNAL_SERVER_ERROR
        override val messageFormat: String get() = "A system failure has occurred: {}"
    },
    RANDOM_FAILURE {
        override val code: Int get() = 2
        override val status: HttpStatus get() = HttpStatus.I_AM_A_TEAPOT
        override val messageFormat: String get() = "Forced to fail!"
    }
}

/**
 * Retains information about a specific failure occurrence.
 */
interface FailureContext{
    val code: Int
    val status: HttpStatus
    val messageFormat: String
}

/**
 * Signals a failure scenario that was trapped by the application logic.
 */
class ApplicationException(val context: FailureContext, vararg messageArgument: Any): Exception(MessageFormatter.arrayFormat( context.messageFormat, messageArgument ).message )

/**
 * This class handles both application and system exceptions, translating them into the standard vnd.error format.
 */
@RestControllerAdvice
class GlobalExceptionHandler: ResponseEntityExceptionHandler() {

    @ExceptionHandler( Exception::class )
    fun fallbackHandler(failure: Exception): ResponseEntity<VndErrors> {
        logger.debug( "fallbackHandler called" )
        return wrapDetails(failure.message!!, HttpStatus.INTERNAL_SERVER_ERROR, randomHexString(), assembleHelpLink( 0 ))
    }

    @ExceptionHandler( ApplicationException::class )
    fun applicationFailureHandler(failure: ApplicationException): ResponseEntity<VndErrors> {
        logger.debug( "randomFailureHandler called" )
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
