package org.kurron.imperative

import io.cucumber.java8.En
import io.cucumber.java8.PendingException

class UseCumberOnlySteps: En {
    // WARNING: steps are not scoped between classes and will be reused between scenarios. You will get an error if a duplicate step is detected.
    init {
        Given("Scot has a test scenario") { "pretend Scot has a scenario" }
        When("Scot executes the scenario via Cucumber only") { "pretend we executed some code" }
        Then("a Cucumber report is generated") { "pretend we validated that a report was generated" }
    }
}