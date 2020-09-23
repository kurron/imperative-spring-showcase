import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.3.4.RELEASE"
	id("io.spring.dependency-management") version "1.0.10.RELEASE"
	id("com.gorylenko.gradle-git-properties") version "2.2.2"
	kotlin("jvm") version "1.3.72"
	kotlin("plugin.spring") version "1.3.72"
}

group = "org.kurron"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
	mavenCentral()
}

springBoot {
	buildInfo()
}

extra["springCloudVersion"] = "Hoxton.SR8"

val junitJupiterVersion = "5.4.2"
val testContainerVersion = "1.14.3"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-hateoas")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
//	implementation("org.springframework.cloud:spring-cloud-starter-zipkin")
	implementation("org.springframework.cloud:spring-cloud-starter")
	implementation("org.springframework.cloud:spring-cloud-aws-context")
	implementation("org.springframework.cloud:spring-cloud-aws-autoconfigure")
	implementation("org.springframework.cloud:spring-cloud-aws-messaging")
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
	}
/*
	implementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
	implementation("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
*/
	testImplementation("org.testcontainers:testcontainers:$testContainerVersion")
	testImplementation( "org.testcontainers:junit-jupiter:$testContainerVersion")
	testImplementation("org.testcontainers:localstack:$testContainerVersion")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "1.8"
	}
}
