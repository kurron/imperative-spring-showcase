package org.kurron.imperative

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers


@SpringBootTest
@Testcontainers
class ShowcaseApplicationTests {

	companion object Initializer  {
		@Container
		@JvmStatic
		val localstack = LocalStackContainer().withServices(LocalStackContainer.Service.S3)

		@DynamicPropertySource
		@JvmStatic
		fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
			println("registerDynamicProperties")
			registry.add("cloud.aws.region.auto") { "false" }
			registry.add("cloud.aws.stack.auto") { "false" }
			registry.add("cloud.aws.region.static", localstack::getRegion)
			registry.add("cloud.aws.credentials.instance-profile") { "false" }
			registry.add("cloud.aws.credentials.use-default-aws-credentials-chain") { "false" }
			registry.add("cloud.aws.credentials.access-key", localstack::getAccessKey)
			registry.add("cloud.aws.credentials.secret-key", localstack::getSecretKey)
		}
	}

	@Test
	fun contextLoads() {
		val endpoint = localstack.getEndpointConfiguration( LocalStackContainer.Service.S3 ).serviceEndpoint
		Assertions.assertTrue(2 == 3) { "Numbers " + 2 + " and " + 3 + " are not equal!" }
	}

}
