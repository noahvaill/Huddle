package ca.mcgill.ecse428.huddle.features;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.mcgill.ecse428.huddle.Court;
import ca.mcgill.ecse428.huddle.Sport;
import ca.mcgill.ecse428.huddle.SportNotOfferedException;
import ca.mcgill.ecse428.huddle.TimeSlot;
import ca.mcgill.ecse428.huddle.TimeSlotService;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public class ViewAvailableTimeSlotsStepDefinitions {

    private final TimeSlotService service = new TimeSlotService();
    private String loggedInUser;
    private List<TimeSlot> listedSlots = List.of();
    private String message;
    private String errorMessage;

    @Given("{string} is logged in to Huddle")
    public void is_logged_in_to_huddle(String userName) {
        loggedInUser = userName;
    }

    @Given("the following time slots exist:")
    public void the_following_time_slots_exist(DataTable dataTable) {
        for (Map<String, String> row : dataTable.asMaps()) {
            service.addTimeSlot(new TimeSlot(
                    new Court(row.get("court"), Sport.fromName(row.get("sport"))),
                    LocalDate.parse(row.get("date")),
                    LocalTime.parse(row.get("start")),
                    LocalTime.parse(row.get("end")),
                    row.get("status").equals("Available")));
        }    
    }

    @When("{string} requests the available time slots for {string} on {string}")
    public void requests_the_available_time_slots_for_on(String userName, String sport, String date) {
        try {
            listedSlots = service.getAvailableTimeSlots(sport, LocalDate.parse(date));
            if (listedSlots.isEmpty()) {
                message = TimeSlotService.NO_AVAILABLE_SLOTS_MESSAGE;
            }
        } catch (SportNotOfferedException e) {
            listedSlots = List.of();
            errorMessage = e.getMessage();
        }
    }

    @Then("the following time slots are listed:")
    public void the_following_time_slots_are_listed(DataTable dataTable) {
        List<Map<String, String>> actual = listedSlots.stream()
                .map(s -> Map.of(
                        "court", s.court().name(),
                        "date", s.date().toString(),
                        "start", s.start().toString(),
                        "end", s.end().toString()))
                .toList();
        assertEquals(dataTable.asMaps(), actual);
    }

    @Then("no time slots are listed")
    public void no_time_slots_are_listed() {
        assertTrue(listedSlots.isEmpty());
    }

    @Then("the message {string} is issued")
    public void the_message_is_issued(String expected) {
        assertEquals(expected, message);
    }

    @Then("the error message {string} is issued")
    public void the_error_message_is_issued(String expected) {
        assertEquals(expected, errorMessage);
    }
}