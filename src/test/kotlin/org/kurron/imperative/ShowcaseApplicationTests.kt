package org.kurron.imperative

import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.model.CreateQueueRequest
import com.amazonaws.services.sqs.model.CreateQueueResult
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Duration


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
    lateinit var sqs: AmazonSQSAsync

    // shared references
    var alpha: CreateQueueResult? = null
    var bravo: CreateQueueResult? = null

    @BeforeEach
    fun setup() {
        println( "setup" )
        alpha = sqs.createQueue("alpha")
        Assertions.assertTrue( 200 == alpha!!.sdkHttpMetadata.httpStatusCode )
        bravo = sqs.createQueue("bravo")
        Assertions.assertTrue( 200 == bravo!!.sdkHttpMetadata.httpStatusCode )
    }

    @AfterEach
    fun teardown() {
        println( "teardown" )
        Assertions.assertTrue( 200 == sqs.deleteQueue(alpha!!.queueUrl).sdkHttpMetadata.httpStatusCode )
        Assertions.assertTrue( 200 == sqs.deleteQueue(bravo!!.queueUrl).sdkHttpMetadata.httpStatusCode )
    }

    @Test
    fun contextLoads() {
        val foo = localstack.getEndpointConfiguration(LocalStackContainer.Service.SNS).serviceEndpoint
        val bar = localstack.getEndpointConfiguration(LocalStackContainer.Service.SQS).serviceEndpoint
        Assertions.assertTrue(foo == bar ) { "$foo and $bar are not equal!" }
    }
}