package org.kurron.imperative.features.abilities.testers

import io.cucumber.java8.En
import io.cucumber.java8.PendingException

class UseCumberOnlySteps: En {
    init {
        Given("Scot has a test scenario") { "pretend Scot has a scenario" }
        When("Scot executes the scenario via Cucumber only") { "pretend we executed some code" }
        Then("a Cucumber report is generated") { "pretend we validated that a report was generated" }
    }
}