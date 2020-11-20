package org.kurron.imperative

import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.model.CreateQueueResult
import com.fasterxml.jackson.annotation.JsonProperty
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Duration
import java.time.Instant
import java.util.Date
import java.util.concurrent.ThreadLocalRandom


@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("containerized")
class ShowcaseApplicationTests {

    companion object Initializer  {
        @Container
        @JvmStatic
        val localstack = LocalStackContainer().withServices(LocalStackContainer.Service.SQS, LocalStackContainer.Service.SNS)
                                              .withStartupTimeout(Duration.ofSeconds(60))

        @DynamicPropertySource
        @JvmStatic
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            println("registerDynamicProperties")
            registry.add("cloud.aws.region.auto") { false }
            registry.add("cloud.aws.stack.auto") { false }
            registry.add("cloud.aws.credentials.instance-profile") { false }
            registry.add("cloud.aws.credentials.use-default-aws-credentials-chain") { false }
            registry.add("cloud.aws.region.static") { localstack.region }
            registry.add("cloud.aws.credentials.access-key") { localstack.accessKey }
            registry.add("cloud.aws.credentials.secret-key") { localstack.secretKey }
            registry.add("application.sns-endpoint" ) { localstack.getEndpointConfiguration(LocalStackContainer.Service.SNS).serviceEndpoint }
            registry.add("application.sqs-endpoint" ) { localstack.getEndpointConfiguration(LocalStackContainer.Service.SQS).serviceEndpoint }
        }

    }

    @Autowired
    lateinit var lowLevelSQS: AmazonSQSAsync

    @Autowired
    lateinit var sqs: QueueMessagingTemplate

    // shared references
    var alpha: CreateQueueResult? = null
    var bravo: CreateQueueResult? = null

    val logger = LoggerFactory.getLogger( ShowcaseApplicationTests::class.java )

    @BeforeEach
    fun setup() {
        logger.debug( "setup" )
        alpha = lowLevelSQS.createQueue("alpha")
        Assertions.assertTrue( 200 == alpha!!.sdkHttpMetadata.httpStatusCode )
        bravo = lowLevelSQS.createQueue("bravo")
        Assertions.assertTrue( 200 == bravo!!.sdkHttpMetadata.httpStatusCode )
    }

    @AfterEach
    fun teardown() {
        logger.debug( "teardown" )
        Assertions.assertTrue( 200 == lowLevelSQS.deleteQueue(alpha!!.queueUrl).sdkHttpMetadata.httpStatusCode )
        Assertions.assertTrue( 200 == lowLevelSQS.deleteQueue(bravo!!.queueUrl).sdkHttpMetadata.httpStatusCode )
    }

    fun randomHexValue(): String = Integer.toHexString( ThreadLocalRandom.current().nextInt( Integer.MAX_VALUE ) ).toUpperCase()

    data class SimpleDTO(@JsonProperty("code") var code: String, @JsonProperty("timestamp") var timestamp: Instant)

    @Test
    fun messagingWorks() {
        val sent = SimpleDTO( randomHexValue(), Instant.now() )
        sqs.convertAndSend( "alpha", sent)
        val received = sqs.receiveAndConvert( "alpha", SimpleDTO::class.java )
        Assertions.assertEquals(sent, received) { "Messages do not match!" }
    }
}