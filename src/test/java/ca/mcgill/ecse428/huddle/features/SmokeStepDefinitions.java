package ca.mcgill.ecse428.huddle.features;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.mcgill.ecse428.huddle.Huddle;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class SmokeStepDefinitions {

    private String name;

    @When("the application name is requested")
    public void theApplicationNameIsRequested() {
        name = Huddle.name();
    }

    @Then("the name is {string}")
    public void theNameIs(String expected) {
        assertEquals(expected, name);
    }
}
