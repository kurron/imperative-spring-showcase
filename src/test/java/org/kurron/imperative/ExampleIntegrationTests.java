package org.kurron.imperative;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers( disabledWithoutDocker = true )
class ExampleIntegrationTests {

    @Container
    static LocalStackContainer localstack = new LocalStackContainer().withServices(LocalStackContainer.Service.S3)
                                                                     .withStartupTimeout(Duration.ofSeconds(90));

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        System.out.println("registerDynamicProperties");
        System.out.flush();
        registry.add("cloud.aws.region.auto", () -> false);
        registry.add("cloud.aws.stack.auto", () -> false);
        registry.add("cloud.aws.credentials.instance-profile", () -> false);
        registry.add("cloud.aws.credentials.use-default-aws-credentials-chain", () -> false);
        registry.add("cloud.aws.region.static", () -> localstack.getRegion());
        registry.add("cloud.aws.credentials.access-key", () -> localstack.getAccessKey());
        registry.add("cloud.aws.credentials.secret-key", () -> localstack.getSecretKey());
    }

    @Test
    public void go() {
        String foo = localstack.getSecretKey();
        assert true == false;
    }
}
