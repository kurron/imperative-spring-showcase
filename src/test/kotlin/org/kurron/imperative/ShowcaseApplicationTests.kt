package org.kurron.imperative

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.AmazonSNSClientBuilder
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder
import com.amazonaws.services.sqs.model.CreateQueueResult
import com.fasterxml.jackson.annotation.JsonProperty
import org.elasticsearch.client.RestHighLevelClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.cloud.aws.messaging.config.QueueMessageHandlerFactory
import org.springframework.cloud.aws.messaging.core.NotificationMessagingTemplate
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.RestClients
import org.springframework.data.elasticsearch.annotations.Document as ElasticsearchDocument
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.messaging.handler.annotation.support.PayloadMethodArgumentResolver
import org.springframework.data.mongodb.core.mapping.Document as MongodbDocument
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.localstack.LocalStackContainer.Service.SNS
import org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS
import org.testcontainers.elasticsearch.ElasticsearchContainer
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

    // set the properties required to connect to the test containers
    @Suppress("unused")
    companion object Initializer {
        val indexCoordinates = IndexCoordinates.of( Integer.toHexString( ThreadLocalRandom.current().nextInt( Integer.MAX_VALUE ) ) )
        val timeout = Duration.ofSeconds(60)
        val localstackImage = DockerImageName.parse("localstack/localstack:0.11.2")
        val mongodbImage = DockerImageName.parse("mongo:4.4.1")
        val elasticsearchImage = DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:7.9.3")

        @Container
        @JvmStatic
        val mongodb = MongoDBContainer(mongodbImage).withStartupTimeout(timeout)

        @Container
        @JvmStatic
        val localstack = LocalStackContainer(localstackImage).withServices(SQS, SNS).withStartupTimeout(timeout)

        @Container
        @JvmStatic
        val elasticsearch = ElasticsearchContainer(elasticsearchImage).withStartupTimeout(timeout)

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
            registry.add("application.elasticsearch-endpoint" ) { elasticsearch.httpHostAddress }
        }
    }

    @TestConfiguration
    class ShowcaseApplicationTestsConfiguration {
        @Bean
        fun queueMessageHandlerFactory(): QueueMessageHandlerFactory {
            val factory = QueueMessageHandlerFactory()
            val converter = MappingJackson2MessageConverter()
            converter.isStrictContentTypeMatch = false
            factory.setArgumentResolvers( listOf( PayloadMethodArgumentResolver(converter) ) )
            return factory
        }

        @Bean
        fun amazonSQS(configuration: ApplicationConfiguration, @Value("\${cloud.aws.region.static}") region:String, provider: AWSCredentialsProvider): AmazonSQSAsync {
            return AmazonSQSAsyncClientBuilder.standard()
                    .withEndpointConfiguration( AwsClientBuilder.EndpointConfiguration( configuration.sqsEndpoint, region ) )
                    .withCredentials( provider )
                    .build()
        }

        @Bean
        fun messagingTemplate( sqs: AmazonSQSAsync ): QueueMessagingTemplate = QueueMessagingTemplate(sqs)

        @Bean
        fun amazonSNS(configuration: ApplicationConfiguration, @Value("\${cloud.aws.region.static}") region:String, provider: AWSCredentialsProvider): AmazonSNS {
            return AmazonSNSClientBuilder.standard()
                    .withEndpointConfiguration( AwsClientBuilder.EndpointConfiguration( configuration.snsEndpoint, region ) )
                    .withCredentials( provider )
                    .build()
        }

        @Bean
        fun elasticsearchClient( configuration: ApplicationConfiguration ): RestHighLevelClient {
            val restConfiguration = ClientConfiguration.builder().connectedTo( configuration.elasticsearchEndpoint ).build()
            return RestClients.create(restConfiguration).rest()
        }

        @Bean
        fun elasticsearch( client: RestHighLevelClient): ElasticsearchOperations {
            return ElasticsearchRestTemplate(client)
        }

        @Bean
        fun notificationMessagingTemplate(sns: AmazonSNS) = NotificationMessagingTemplate(sns)

        @Bean
        @Profile("!stubbed")
        fun outboundMessagingGateway(template: NotificationMessagingTemplate, configuration: ApplicationConfiguration) = SimpleNotificationServiceGateway(template, configuration)

        @Bean
        @Profile("stubbed")
        fun nullOutboundMessagingGateway() = NullNotificationServiceGateway()
    }

    @Autowired
    lateinit var lowLevelSQS: AmazonSQSAsync

    @Autowired
    lateinit var sqs: QueueMessagingTemplate

    @Autowired
    lateinit var mongodb: MongoOperations

    @Autowired
    lateinit var elasticsearch: ElasticsearchOperations

    // shared references
    var alpha: CreateQueueResult? = null
    var bravo: CreateQueueResult? = null

    val logger = LoggerFactory.getLogger( ShowcaseApplicationTests::class.java )

    @BeforeEach
    fun setup() {
        logger.debug( "setup" )
        alpha = lowLevelSQS.createQueue("alpha")
        assertTrue( 200 == alpha!!.sdkHttpMetadata.httpStatusCode )
        bravo = lowLevelSQS.createQueue("bravo")
        assertTrue( 200 == bravo!!.sdkHttpMetadata.httpStatusCode )
        assertTrue( elasticsearch.indexOps( indexCoordinates ).create() )
    }

    @AfterEach
    fun teardown() {
        logger.debug( "teardown" )
        assertTrue( 200 == lowLevelSQS.deleteQueue(alpha!!.queueUrl).sdkHttpMetadata.httpStatusCode )
        assertTrue( 200 == lowLevelSQS.deleteQueue(bravo!!.queueUrl).sdkHttpMetadata.httpStatusCode )
        assertTrue( elasticsearch.indexOps( indexCoordinates ).delete() )
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

    @MongodbDocument
    data class Person @JvmOverloads constructor (@Id var id: String = UUID.randomUUID().toString(), var name: String = "defaulted", var age: Int = 0 )

    @Test
    fun mongodbWorks() {
        val toSave = Person( name = randomHexValue(), age = randomInteger() )
        val written = mongodb.insert( toSave )
        val read = mongodb.findById(written.id, Person::class.java )
        assertEquals(written, read) { "Documents do not match!" }
    }

    @ElasticsearchDocument( indexName = "test", type = "article" )
    data class Article @JvmOverloads constructor (@Id var id: String = UUID.randomUUID().toString(), var title: String = "defaulted" )

    @Test
    fun elasticsearchWorks() {
        assertTrue(elasticsearch.indexOps( indexCoordinates ).exists()) { "Index does not exist!" }
    }
}