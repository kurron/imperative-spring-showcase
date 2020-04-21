package org.kurron.imperative

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.AmazonSNSClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.cloud.aws.messaging.core.NotificationMessagingTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.concurrent.ThreadLocalRandom

@SpringBootApplication
@EnableConfigurationProperties(ApplicationConfiguration::class)
class ShowcaseApplication {
	@Bean
	fun logAwareBeanPostProcessor() = FeedbackAwareBeanPostProcessor()

	@Bean
	fun restTemplate() = RestTemplateBuilder().build()!!

	// You would not sample EVERY data point in a production setting. There are more sophisticated samplers to select from.
	@Bean
	fun sampler() = brave.sampler.Sampler.ALWAYS_SAMPLE!!

	@Bean
	@Profile("cloud", "development")
	fun amazonSNS(configuration: ApplicationConfiguration, @Value("\${cloud.aws.region.static}") region:String ): AmazonSNS {
		return AmazonSNSClientBuilder.standard().withEndpointConfiguration( AwsClientBuilder.EndpointConfiguration( configuration.snsEndpoint, region ) ).build()
	}

	@Bean
	@Profile("cloud", "development")
	fun notificationMessagingTemplate(sns: AmazonSNS) = NotificationMessagingTemplate(sns)

	@Bean
	@Profile("cloud", "development")
	fun outboundMessagingGateway(template: NotificationMessagingTemplate, configuration: ApplicationConfiguration) = SimpleNotificationServiceGateway(template, configuration)

	@Bean
	@Profile("default")
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