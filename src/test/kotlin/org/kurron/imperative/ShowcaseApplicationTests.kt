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
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Duration

@SpringBootTest(classes = [ApplicationConfiguration::class], webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = ["showcase.inner.delta=24h"] )
@Import(ShowcaseApplicationTests.ShowcaseApplicationTestsConfiguration::class)
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
    class ShowcaseApplicationTestsConfiguration(private val configuration: BravoProperties) {
        @Bean
        fun someDuration(): Duration = configuration.delta
    }

    @Autowired
    lateinit var outer: AlphaProperties

    @Autowired
    lateinit var inner: BravoProperties

    @Autowired
    lateinit var duration: Duration

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
        fun firstTest() {}

        @Test
        fun secondTest() {}
    }

    @Nested
    inner class GroupBravo {
        @Test
        fun firstTest() {}

        @Test
        fun secondTest() {}
    }

    @Test
    fun applicationStarts() {
        println("outer: $outer")
        println("inner: $inner")
        Assertions.assertNotNull(duration, "Durations wasn't injected!")
        println("duration: $duration")
        Assertions.assertEquals(Duration.ofHours(24), duration, "Property was not overridden by the test!")
    }

    @Suppress("unused")
    private fun dataProvider() = listOf( TestData("foo", "foo"), TestData("bar","bar"))
    data class TestData(val input: String?, val expected: String?)

    @ParameterizedTest
    @MethodSource("dataProvider")
    fun parameterizedTest(data:TestData) {
        Assertions.assertEquals(data.input, data.expected, "Values do not match!")
    }
}