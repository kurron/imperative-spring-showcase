package org.kurron.imperative

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(  prefix = "application")
data class ApplicationConfiguration(var snsEndpoint: String, var snsTopic: String, var sqsEndpoint: String, var elasticsearchEndpoint: String)

data class CharacterPointsAllocatedEvent(@JsonProperty("message") val message: String)