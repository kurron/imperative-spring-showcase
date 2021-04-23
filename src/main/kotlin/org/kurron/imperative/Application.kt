package org.kurron.imperative

import org.kurron.imperative.configuration.ApplicationConfiguration
import org.springframework.boot.runApplication

/**
 * Application driver.
 * @param args application arguments
 */
fun main(args: Array<String>) {
	runApplication<ApplicationConfiguration>(*args)
}