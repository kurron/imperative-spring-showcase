package org.kurron.imperative.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated
import java.time.Duration

/**
 * Showcasing nested configuration properties. These are the top-level properties.
 *
 * @property alpha string value that cannot be null.
 * @property bravo number value that must fall within a certain range.
 */
@ConstructorBinding
@ConfigurationProperties(prefix = "showcase", ignoreInvalidFields = false, ignoreUnknownFields = true)
@Validated
data class AlphaProperties(val alpha:String, val bravo:Int)

