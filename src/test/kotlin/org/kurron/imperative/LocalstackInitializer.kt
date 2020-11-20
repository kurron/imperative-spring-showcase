package org.kurron.imperative

import org.slf4j.LoggerFactory
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.localstack.LocalStackContainer

class LocalstackInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

    val logger = LoggerFactory.getLogger( LocalstackInitializer::class.java )

    override fun initialize(context: ConfigurableApplicationContext) {
        logger.debug( "initialize")
        TestPropertyValues.of( "cloud_aws_credentials_accessKey=${localstack.accessKey}",
                "cloud_aws_credentials_secretKey=${localstack.secretKey}",
                "cloud_aws_region_static=${localstack.region}")
                .applyTo(context.environment)
    }

    companion object {
        val localstack = LocalStackContainer().withServices(LocalStackContainer.Service.S3)
    }
}