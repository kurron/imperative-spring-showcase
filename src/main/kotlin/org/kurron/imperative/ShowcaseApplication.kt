package org.kurron.imperative

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.util.concurrent.ThreadLocalRandom

@SpringBootApplication
class ShowcaseApplication

fun main(args: Array<String>) {
	runApplication<ShowcaseApplication>(*args)
}

// shared functions -- too lazy to create a separate file
fun randomHexString() = Integer.toHexString( ThreadLocalRandom.current().nextInt( Integer.MAX_VALUE ) ).toUpperCase()