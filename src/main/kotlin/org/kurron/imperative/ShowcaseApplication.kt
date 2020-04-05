package org.kurron.imperative

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component
import java.util.concurrent.ThreadLocalRandom

@SpringBootApplication
class ShowcaseApplication

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