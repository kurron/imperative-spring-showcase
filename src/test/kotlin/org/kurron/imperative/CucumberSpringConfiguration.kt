package org.kurron.imperative

import io.cucumber.java.Before
import io.cucumber.spring.CucumberContextConfiguration
import org.kurron.imperative.configuration.ApplicationConfiguration
import org.kurron.imperative.configuration.BravoProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.core.env.Environment
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Duration

//WARNING: including the cucumber-spring dependency requires all acceptance tests to be Spring-based. You cannot have a pure Cucumber test any longer.
@CucumberContextConfiguration
@SpringBootTest(classes = [ApplicationConfiguration::class], webEnvironment = SpringBootTest.WebEnvironment.NONE )
@Import(CucumberSpringConfiguration.AddsToProductionConfiguration::class)
@Testcontainers(disabledWithoutDocker = true)
class CucumberSpringConfiguration {

    @Suppress("unused")
    companion object {
        val timeout = Duration.ofSeconds(60)
        val mongodbImage = DockerImageName.parse("mongo:4.4.1")

        @Container
        val mongodb = MongoDBContainer(mongodbImage).withStartupTimeout(timeout)

        @DynamicPropertySource
        @JvmStatic
        fun runsBeforeSpringStartsUp(registry: DynamicPropertyRegistry) {
            registry.add("mongodb.url") { mongodb.replicaSetUrl }
        }
    }

    @Autowired
    lateinit var environment: Environment

    @TestConfiguration
    class AddsToProductionConfiguration(private val configuration: BravoProperties) {
        @Bean
        fun someDuration(): Duration = configuration.delta
    }
}