package org.kurron.imperative

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.AmazonSNSClientBuilder
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.cloud.aws.messaging.config.QueueMessageHandlerFactory
import org.springframework.cloud.aws.messaging.core.NotificationMessagingTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.messaging.handler.annotation.support.PayloadMethodArgumentResolver
import org.springframework.stereotype.Component
import java.util.concurrent.ThreadLocalRandom

@SpringBootApplication
@EnableConfigurationProperties(ApplicationConfiguration::class)
class ShowcaseApplication {
	@Bean
	fun logAwareBeanPostProcessor() = FeedbackAwareBeanPostProcessor()

	@Bean
	fun restTemplate() = RestTemplateBuilder().build()!!

/*
	// You would not sample EVERY data point in a production setting. There are more sophisticated samplers to select from.
	@Bean
	fun sampler() = brave.sampler.Sampler.ALWAYS_SAMPLE!!
*/

	@Bean
	fun queueMessageHandlerFactory(): QueueMessageHandlerFactory {
		val factory = QueueMessageHandlerFactory()
		val converter = MappingJackson2MessageConverter()
		converter.isStrictContentTypeMatch = false
		factory.setArgumentResolvers( listOf( PayloadMethodArgumentResolver(converter) ) )
		return factory
	}

	@Bean
	fun amazonSQS(configuration: ApplicationConfiguration, @Value("\${cloud.aws.region.static}") region:String, provider: AWSCredentialsProvider ): AmazonSQSAsync {
		return AmazonSQSAsyncClientBuilder.standard()
				                          .withEndpointConfiguration( AwsClientBuilder.EndpointConfiguration( configuration.sqsEndpoint, region ) )
				                          .withCredentials( provider )
				                          .build()
	}

	@Bean
	fun amazonSNS(configuration: ApplicationConfiguration, @Value("\${cloud.aws.region.static}") region:String, provider: AWSCredentialsProvider ): AmazonSNS {
		return AmazonSNSClientBuilder.standard()
				                     .withEndpointConfiguration( AwsClientBuilder.EndpointConfiguration( configuration.snsEndpoint, region ) )
				                     .withCredentials( provider )
				                     .build()
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

fun main(args: Array<String>) {
	runApplication<ShowcaseApplication>(*args)
}

// shared functions -- too lazy to create a separate file
fun randomHexString() = Integer.toHexString( ThreadLocalRandom.current().nextInt( Integer.MAX_VALUE ) ).toUpperCase()

// small components

@Component( "warp drive" )
class CustomHealthIndicator: HealthIndicator {
	override fun health(): Health = Health.outOfService().withDetail( "dilithium crystals", "depleted" ).build()
}