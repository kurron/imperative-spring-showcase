package org.kurron.imperative

import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.kurron.imperative.configuration.AlphaProperties
import org.kurron.imperative.configuration.ApplicationConfiguration
import org.kurron.imperative.configuration.BravoProperties
import org.slf4j.LoggerFactory
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

@SpringBootTest(classes = [ApplicationConfiguration::class], webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = ["showcase.inner.delta=24h"] )
@Import(ShowcaseApplicationTests.AddsToProductionConfiguration::class)
@Testcontainers(disabledWithoutDocker = true)
class ShowcaseApplicationTests {

    @Suppress("unused")
    companion object {
        val timeout = Duration.ofSeconds(60)
        val mongodbImage = DockerImageName.parse("mongo:4.4.1")

        @Container
        val mongodb = MongoDBContainer(mongodbImage).withStartupTimeout(timeout)

        @DynamicPropertySource
        @JvmStatic
        fun runsBeforeSpringStartsUp(registry: DynamicPropertyRegistry) {
            registry.add("showcase.outer.bravo") { 1995 }
            registry.add("mongodb.url") { mongodb.replicaSetUrl }
        }
    }


    @TestConfiguration
    class AddsToProductionConfiguration(private val configuration: BravoProperties) {
        @Bean
        fun someDuration(): Duration = configuration.delta
    }

    @Autowired
    lateinit var outer: AlphaProperties

    @Autowired
    lateinit var inner: BravoProperties

    @Autowired
    lateinit var duration: Duration

    @Autowired
    lateinit var environment: Environment

    val logger = LoggerFactory.getLogger(ShowcaseApplicationTests::class.java)

    @BeforeEach
    fun setup() {
        logger.debug( "setup" )
    }

    @AfterEach
    fun teardown() {
        logger.debug( "teardown" )
    }

    @Nested
    inner class GroupAlpha {
        @Test
        fun `given one then foo`() {}

        @Test
        fun `given two then bar`() {}
    }

    @Nested
    inner class GroupBravo {
        @Test
        fun `given one then foo`() {}

        @Test
        fun `given two then bar`() {}
    }

    @Test
    fun `verify that the test environment starts properly`() {
        println("outer: $outer")
        println("inner: $inner")
        Assertions.assertNotNull(duration, "Durations wasn't injected!")
        println("duration: $duration")
        Assertions.assertEquals(Duration.ofHours(24), duration, "Property was not overridden by the test!")
        Assertions.assertNotNull(environment, "Environment wasn't injected!")
        Assertions.assertEquals("1995", environment.getProperty("showcase.outer.bravo"), "Property was not overridden by the DynamicPropertySource!")
        Assertions.assertNotNull(environment.getProperty("mongodb.url"), "MongoDB URL wasn't captured!")
        println("MongoDB URL: ${environment.getProperty("mongodb.url")}")
    }

    @Suppress("unused")
    private fun dataProvider() = listOf( TestData("foo", "foo"), TestData("bar","bar"))
    data class TestData(val input: String?, val expected: String?)

    @ParameterizedTest
    @MethodSource("dataProvider")
    fun `run the same test against various data`(data:TestData) {
        Assertions.assertEquals(data.input, data.expected, "Values do not match!")
    }
}