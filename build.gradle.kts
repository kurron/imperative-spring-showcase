import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("org.springframework.boot") version "2.5.3"
  id("io.spring.dependency-management") version "1.0.11.RELEASE"
  id("org.asciidoctor.convert") version "1.5.8"
  kotlin("jvm") version "1.5.21"
  kotlin("plugin.spring") version "1.5.21"
}

group = "org.kurron"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

val cucumberRuntime by configurations.creating {
	extendsFrom(configurations["testImplementation"])
}

repositories {
	mavenCentral()
}

springBoot {
	buildInfo()
}

extra["snippetsDir"] = file("build/generated-snippets")
extra["springCloudVersion"] = "2020.0.3"
extra["testcontainersVersion"] = "1.16.0"
extra["cucumberVersion"] = "6.10.4"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-hateoas")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-json")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
//	implementation("com.fasterxml.jackson.module:jackson-modules-java8:2.12.3")
//	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8")
//	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
	testImplementation("org.springframework.boot:spring-boot-starter-data-mongodb")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:mongodb")
	testImplementation("org.testcontainers:localstack")
	testImplementation( "io.cucumber:cucumber-java8:${property("cucumberVersion")}")
	testImplementation( "io.cucumber:cucumber-java:${property("cucumberVersion")}")
	testImplementation( "io.cucumber:cucumber-junit:${property("cucumberVersion")}")
	testImplementation( "io.cucumber:cucumber-spring:${property("cucumberVersion")}")
}

dependencyManagement {
  imports {
    mavenBom("org.testcontainers:testcontainers-bom:${property("testcontainersVersion")}")
    mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
  }
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs = listOf("-Xjsr305=strict")
    jvmTarget = "11"
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}

// https://github.com/eaneto/gradle-kotlin-dsl-cucumber-configuration
task("cucumber") {
	dependsOn("assemble", "compileTestJava")
	doLast {
		javaexec {
			mainClass.set("io.cucumber.core.cli.Main")
			classpath = cucumberRuntime + sourceSets.main.get().output + sourceSets.test.get().output
			// Change glue for your project package where the step definitions are.
			// And where the feature files are.
			args = listOf("--plugin", "pretty", "--glue", "org.kurron.imperative", "src/test/resources")
		}
	}
}

/*
tasks.test {
  outputs.dir(snippetsDir)
}

tasks.asciidoctor {
  inputs.dir(snippetsDir)
  dependsOn(test)
}
*/
