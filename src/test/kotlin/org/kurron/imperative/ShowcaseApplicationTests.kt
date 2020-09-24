package org.kurron.imperative

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
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
        val localstack = LocalStackContainer().withServices(LocalStackContainer.Service.S3, LocalStackContainer.Service.SNS)
                                              .withStartupTimeout(Duration.ofSeconds(90))

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

    @Test
    fun contextLoads() {
        val foo = localstack.getEndpointConfiguration(LocalStackContainer.Service.SNS).serviceEndpoint
        val bar = localstack.getEndpointConfiguration(LocalStackContainer.Service.SQS).serviceEndpoint
        Assertions.assertTrue(foo == bar ) { "Numbers " + 2 + " and " + 3 + " are not equal!" }
    }
}