package org.kurron.imperative

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.util.concurrent.ThreadLocalRandom

@SpringBootApplication
class ShowcaseApplication {
	@Bean
	fun logAwareBeanPostProcessor() = FeedbackAwareBeanPostProcessor()

	@Bean
	fun restTemplate() = RestTemplateBuilder().build()!!

	// You would not sample EVERY data point in a production setting. There are more sophisticated samplers to select from.
	@Bean
	fun sampler() = brave.sampler.Sampler.ALWAYS_SAMPLE
}

fun main(args: Array<String>) {
	runApplication<ShowcaseApplication>(*args)
}

// shared functions -- too lazy to create a separate file
fun randomHexString() = Integer.toHexString( ThreadLocalRandom.current().nextInt( Integer.MAX_VALUE ) ).toUpperCase()

// small components

@Component( "warp drive" )
class CustomHealthIndicator: HealthIndicator {
	override fun health(): Health = Health.outOfService().withDetail( "dilithium crystals", "depleted" ).build()
}