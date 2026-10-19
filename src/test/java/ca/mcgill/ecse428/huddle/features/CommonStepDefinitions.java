package ca.mcgill.ecse428.huddle.features;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.mcgill.ecse428.huddle.Sport;
import ca.mcgill.ecse428.huddle.TimeSlot;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

/** Steps shared by several stories. */
public class CommonStepDefinitions {

    private final HuddleWorld world;

    public CommonStepDefinitions(HuddleWorld world) {
        this.world = world;
    }

    @Given("{string} is logged in to Huddle")
    public void is_logged_in_to_huddle(String userName) {
        world.loggedInUser = userName;
    }

    @Given("the following time slots exist:")
    public void the_following_time_slots_exist(DataTable dataTable) {
        for (Map<String, String> row : dataTable.asMaps()) {
            TimeSlot slot = new TimeSlot(
                    row.get("court"),
                    Sport.fromName(row.get("sport")),
                    LocalDate.parse(row.get("date")),
                    LocalTime.parse(row.get("start")),
                    LocalTime.parse(row.get("end")),
                    row.get("status").equals("Available"));
            world.timeSlotService.addTimeSlot(slot);
            // Optional "slot" column lets later steps refer to a time slot by its id
            if (row.containsKey("slot")) {
                world.timeSlotsById.put(row.get("slot"), slot);
            }
        }
    }

    @Then("the message {string} is issued")
    public void the_message_is_issued(String expected) {
        assertEquals(expected, world.message);
    }

    @Then("the confirmation message {string} is issued")
    public void the_confirmation_message_is_issued(String expected) {
        assertEquals(expected, world.message);
    }

    @Then("the error message {string} is issued")
    public void the_error_message_is_issued(String expected) {
        assertEquals(expected, world.errorMessage);
    }
}
