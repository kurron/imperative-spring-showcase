package org.kurron.imperative

import io.cucumber.junit.Cucumber
import io.cucumber.junit.CucumberOptions
import org.junit.runner.RunWith

@RunWith(Cucumber::class)
@CucumberOptions(features=["src/test/resources/features/abilities/testers/cucumber_only.feature"], tags="@pure-cucumber")
class UseCumberOnlyAcceptanceTests