package org.kurron.imperative

import io.cucumber.java.Before
import io.cucumber.java8.En
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration

class UseCumberAndSpringSteps: En, CucumberSpringConfiguration() {
    @Autowired
    lateinit var duration: Duration

    @Before
    fun startContainers() = mongodb.start()

    // WARNING: steps are not scoped between classes and will be reused between scenarios. You will get an error if a duplicate step is detected.
    init {
        Given("Scot has a multi-component test scenario") { "pretend Scot has a scenario" }
        When("Scot executes the scenario via Cucumber, Spring and Testcontainers") {
            assert(duration != null)
            assert(null != environment.getRequiredProperty("mongodb.url"))
            println( "MongoDB link is ${mongodb.replicaSetUrl}" )
            println( "mongodb.url is ${environment.getRequiredProperty("mongodb.url")}" )
        }
    }
}