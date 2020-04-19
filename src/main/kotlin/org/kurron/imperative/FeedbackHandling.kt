package org.kurron.imperative

import org.kurron.imperative.Level.DEBUG
import org.kurron.imperative.Level.ERROR
import org.kurron.imperative.Level.INFO
import org.kurron.imperative.Level.TRACE
import org.kurron.imperative.Level.WARN
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.slf4j.helpers.MessageFormatter
import org.springframework.beans.factory.config.BeanPostProcessor
import java.lang.IllegalArgumentException


interface FeedbackProvider {
    fun send(context: LoggingContext, vararg messageArgument: Any )
    fun send(context: LoggingContext, failure: Throwable )
    fun send(context: LoggingContext, failure: Throwable, vararg messageArgument: Any )
}

interface FeedbackAware {
    var feedback: FeedbackProvider
}

/**
 * This object will inject a feedback provider into any beans that indicate they want it.
 */
class FeedbackAwareBeanPostProcessor: BeanPostProcessor {
    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
        return if ( bean is FeedbackAware ) {
            bean.feedback = LoggingProvider( LoggerFactory.getLogger( bean.javaClass ) )
            bean
        }
        else {
            bean
        }
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        return bean
    }
}

/**
 * This feedback provider uses SLF4J logging as its feedback mechanism.
 */
private class LoggingProvider(val logger: Logger): FeedbackProvider {
    override fun send(context: LoggingContext, vararg messageArgument: Any) {
        try{
            populateMDC( context )
            when( context.logLevel ) {
                TRACE -> logger.trace( context.messageFormat, *messageArgument )
                DEBUG -> logger.debug( context.messageFormat, *messageArgument )
                INFO -> logger.info( context.messageFormat, *messageArgument )
                WARN -> logger.warn( context.messageFormat, *messageArgument )
                ERROR -> logger.error( context.messageFormat, *messageArgument )
            }
        }
        finally {
            // only clear message code -- other values are fixed for the duration of this process
            clearMessageCode()
        }
    }

    override fun send(context: LoggingContext, failure: Throwable) {
        try{
            populateMDC( context )
            when( context.logLevel ) {
                TRACE, DEBUG, INFO -> throw IllegalArgumentException( "${context.logLevel.name} cannot be associated with a failure!" )
                WARN -> logger.warn( context.messageFormat, failure )
                ERROR -> logger.error( context.messageFormat, failure )
            }
        }
        finally {
            // only clear message code -- other values are fixed for the duration of this process
            clearMessageCode()
        }
    }

    override fun send(context: LoggingContext, failure: Throwable, vararg messageArgument: Any) {
        try{
            populateMDC( context )
            val message = MessageFormatter.arrayFormat( context.messageFormat, messageArgument ).message
            when( context.logLevel ) {
                TRACE, DEBUG, INFO -> throw IllegalArgumentException( "${context.logLevel.name} cannot be associated with a failure!" )
                WARN -> logger.warn( message, failure )
                ERROR -> logger.error( message, failure )
            }
        }
        finally {
            // only clear message code -- other values are fixed for the duration of this process
            clearMessageCode()
        }
    }

    private fun populateMDC(context: FeedbackContext ) {
        MDC.put( "message-code", context.code.toString() )
    }

    private fun clearMessageCode() {
        MDC.remove( "message-code" )
    }
}

/**
 * Null Object Pattern: a no-op implementation of the log provider interface. Typically used only in testing
 * environments where real providers are not desired or a fallback is needed.
 */
class NullFeedbackProvider: FeedbackProvider {
    override fun send(context: LoggingContext, vararg messageArgument: Any) {}
    override fun send(context: LoggingContext, failure: Throwable) {}
    override fun send(context: LoggingContext, failure: Throwable, vararg messageArgument: Any) {}
}

/**
 * Convenience base class for objects that want logging injected.
 */
abstract class AbstractLogAware: FeedbackAware {
    override var feedback: FeedbackProvider = NullFeedbackProvider()
}