package org.kurron.imperative

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kurron.imperative.configuration.AlphaProperties
import org.kurron.imperative.configuration.ApplicationConfiguration
import org.kurron.imperative.configuration.BravoProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(classes = [ApplicationConfiguration::class], webEnvironment = SpringBootTest.WebEnvironment.NONE )
@Testcontainers(disabledWithoutDocker = true)
class ShowcaseApplicationTests {

    @Suppress("unused")
    companion object {
        @DynamicPropertySource
        @JvmStatic
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            registry.add("cloud.aws.region.auto") { false }
        }
    }

    @TestConfiguration
    class ShowcaseApplicationTestsConfiguration {
    }

    @Autowired
    lateinit var outer: AlphaProperties

    @Autowired
    lateinit var inner: BravoProperties

    val logger = LoggerFactory.getLogger( ShowcaseApplicationTests::class.java )

    @BeforeEach
    fun setup() {
        logger.debug( "setup" )
    }

    @AfterEach
    fun teardown() {
        logger.debug( "teardown" )
    }

    @Test
    fun applicationStarts() {
        println("outer: $outer")
        println("inner: $inner")
    }
}