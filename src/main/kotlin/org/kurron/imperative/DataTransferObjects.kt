package org.kurron.imperative

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Profile

@Profile("cloud", "development")
@ConstructorBinding
@ConfigurationProperties(  prefix = "application")
data class ApplicationConfiguration(var snsEndpoint: String, var snsTopic: String)

data class CharacterPointsAllocatedEvent(@JsonProperty("message") val message: String)