package org.kurron.imperative.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated
import java.time.Duration

/**
 * Showcasing nested configuration properties. These are the inner properties.
 *
 * @property charlie boolean value that cannot be null.
 * @property delta duration value that can be expressed using various units.
 */
@ConstructorBinding
@ConfigurationProperties(prefix = "showcase.inner", ignoreInvalidFields = false, ignoreUnknownFields = false)
@Validated
data class BravoProperties(val charlie:Boolean, val delta:Duration)
