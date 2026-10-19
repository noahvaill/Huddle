package ca.mcgill.ecse428.huddle.features;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.mcgill.ecse428.huddle.SportNotOfferedException;
import ca.mcgill.ecse428.huddle.TimeSlot;
import ca.mcgill.ecse428.huddle.TimeSlotService;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ViewAvailableTimeSlotsStepDefinitions {

    private final HuddleWorld world;
    private List<TimeSlot> listedSlots = List.of();

    public ViewAvailableTimeSlotsStepDefinitions(HuddleWorld world) {
        this.world = world;
    }

    @When("{string} requests the available time slots for {string} on {string}")
    public void requests_the_available_time_slots_for_on(String userName, String sport, String date) {
        try {
            listedSlots = world.timeSlotService.getAvailableTimeSlots(sport, LocalDate.parse(date));
            if (listedSlots.isEmpty()) {
                world.message = TimeSlotService.NO_AVAILABLE_SLOTS_MESSAGE;
            }
        } catch (SportNotOfferedException e) {
            listedSlots = List.of();
            world.errorMessage = e.getMessage();
        }
    }

    @Then("the following time slots are listed:")
    public void the_following_time_slots_are_listed(DataTable dataTable) {
        List<Map<String, String>> actual = listedSlots.stream()
                .map(s -> Map.of(
                        "court", s.court(),
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
}
