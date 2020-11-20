package org.kurron.imperative

import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.model.CreateQueueResult
import com.fasterxml.jackson.annotation.JsonProperty
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.localstack.LocalStackContainer.Service.SNS
import org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom


@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("containerized")
class ShowcaseApplicationTests {

    // set the properies required to connect to the test containers
    @Suppress("unused")
    companion object Initializer  {
        val timeout = Duration.ofSeconds(60)
        val localstackImage = DockerImageName.parse("localstack/localstack:0.11.2")
        val mongodbImage = DockerImageName.parse("mongo:4.4.1")

        @Container
        @JvmStatic
        val mongodb = MongoDBContainer(mongodbImage).withStartupTimeout(timeout)

        @Container
        @JvmStatic
        val localstack = LocalStackContainer(localstackImage).withServices(SQS, SNS).withStartupTimeout(timeout)

        @DynamicPropertySource
        @JvmStatic
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            registry.add("cloud.aws.region.auto") { false }
            registry.add("cloud.aws.stack.auto") { false }
            registry.add("cloud.aws.credentials.instance-profile") { false }
            registry.add("cloud.aws.credentials.use-default-aws-credentials-chain") { false }
            registry.add("cloud.aws.region.static") { localstack.region }
            registry.add("cloud.aws.credentials.access-key") { localstack.accessKey }
            registry.add("cloud.aws.credentials.secret-key") { localstack.secretKey }
            registry.add("application.sns-endpoint" ) { localstack.getEndpointConfiguration(SNS).serviceEndpoint }
            registry.add("application.sqs-endpoint" ) { localstack.getEndpointConfiguration(SQS).serviceEndpoint }
            registry.add("spring.data.mongodb.uri" ) { mongodb.replicaSetUrl }
        }

    }

    @Autowired
    lateinit var lowLevelSQS: AmazonSQSAsync

    @Autowired
    lateinit var sqs: QueueMessagingTemplate

    @Autowired
    lateinit var mongodb: MongoOperations

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
    fun randomInteger(): Int = ThreadLocalRandom.current().nextInt( Integer.MAX_VALUE )

    data class SimpleDTO(@JsonProperty("code") var code: String, @JsonProperty("timestamp") var timestamp: Instant)

    @Test
    fun messagingWorks() {
        val sent = SimpleDTO( randomHexValue(), Instant.now() )
        sqs.convertAndSend( "alpha", sent)
        val received = sqs.receiveAndConvert( "alpha", SimpleDTO::class.java )
        assertEquals(sent, received) { "Messages do not match!" }
    }

    @Document
    data class Person @JvmOverloads constructor (@Id var id: String = UUID.randomUUID().toString(),
                                                 var name: String = "defaulted",
                                                 var age: Int = 0 )

    @Test
    fun mongodbWorks() {
        val toSave = Person( name = randomHexValue(), age = randomInteger() )
        val written = mongodb.insert( toSave )
        val read = mongodb.findById(written.id, Person::class.java )
        assertEquals(written, read) { "Documents do not match!" }
    }

}