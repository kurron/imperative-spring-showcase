package org.kurron.imperative

import org.springframework.hateoas.Link
import org.springframework.hateoas.mediatype.vnderrors.VndErrors
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

/**
 * Signals a failure scenario that was trapped by the application logic.
 */
class ApplicationException(val code: Int, val status: HttpStatus, message: String, cause: Throwable? ): Exception(message, cause) {
    constructor( code: Int, status: HttpStatus, message: String ): this( code, status, message, null )
}

/**
 * This class handles both application and system exceptions, translating them into the standard vnd.error format.
 */
@RestControllerAdvice
class GlobalExceptionHandler: ResponseEntityExceptionHandler() {

    @ExceptionHandler( Exception::class )
    fun fallbackHandler(failure: Exception): ResponseEntity<VndErrors> {
        logger.debug( "fallbackHandler called" )
        // Links to a resource that this error is related to. See RFC6903 for further details.
        val about = Link( "about", "https://help.example.com/general-failure" )
        return wrapDetails(failure.message!!, HttpStatus.INTERNAL_SERVER_ERROR, randomHexString(), about)
    }

    @ExceptionHandler( ApplicationException::class )
    fun applicationFailureHandler(failure: ApplicationException): ResponseEntity<VndErrors> {
        logger.debug( "randomFailureHandler called" )
        // Links to a document describing the error. This has the same definition as the help link relation in the HTML5 specification
        val help = Link( "help", "https://help.example.com/failure-codes/${failure.code}" )
        return wrapDetails(failure.message!!, failure.status, randomHexString(), help)
    }

    private fun wrapDetails(message:String, status: HttpStatus, logReference: String, help: Link): ResponseEntity<VndErrors> {
        val logs = Link( "logs", "https://logs.example.com/trace-id/$logReference" )
        val details = VndErrors(logReference, message, help, logs)
        val mediaType = MediaType.parseMediaType("application/vnd.error+json")
        return ResponseEntity.status(status).contentType(mediaType).body( details )
    }
}

