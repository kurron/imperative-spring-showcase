package org.kurron.imperative.configuration

import org.kurron.imperative.FeedbackAwareBeanPostProcessor
import org.kurron.imperative.core.EchoProcessor
import org.kurron.imperative.core.PassThruEchoProcessor
import org.kurron.imperative.gateway.outbound.MessagingGateway
import org.kurron.imperative.gateway.outbound.SimpleMessagingGateway
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestOperations

/**
 * Spring beans are defined here.
 */
@SpringBootConfiguration
@EnableConfigurationProperties(*[AlphaProperties::class,BravoProperties::class])
class ApplicationConfiguration(private val alpha: AlphaProperties) {
    @Bean
    fun logAwareBeanPostProcessor() = FeedbackAwareBeanPostProcessor()

    @Bean
    fun restTemplate(): RestOperations = RestTemplateBuilder().build()

    @Bean
    fun crystalHealth(): HealthIndicator = HealthIndicator { Health.outOfService().withDetail( "dilithium crystals", "depleted" ).build() }

    @Bean
    fun messageGateway(): MessagingGateway = SimpleMessagingGateway()

    @Bean
    fun echoProcessor(gateway: MessagingGateway): EchoProcessor = PassThruEchoProcessor(gateway)
}