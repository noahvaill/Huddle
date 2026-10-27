package ca.mcgill.ecse428.huddle.features;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.mcgill.ecse428.huddle.GameRequest;
import ca.mcgill.ecse428.huddle.TimeSlot;
import ca.mcgill.ecse428.huddle.TimeSlotStatus;
import ca.mcgill.ecse428.huddle.User;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import java.util.List;

/** Step definitions for ID010 Confirm Game and Book Court. */
public class ConfirmGameAndBookCourtStepDefinitions {

    private final HuddleWorld world;

    public ConfirmGameAndBookCourtStepDefinitions(HuddleWorld world) {
        this.world = world;
    }

    private TimeSlot timeSlot(String slotId) {
        TimeSlot slot = world.timeSlotsById.get(slotId);
        if (slot == null) {
            throw new AssertionError("No time slot with id " + slotId);
        }
        return slot;
    }

    /** Uses the TID034 test hook: the slot is booked once the join has started, just before availability is checked. */
    @Given("the time slot {string} is booked by a facility administrator before the join is completed")
    public void the_time_slot_is_booked_by_a_facility_administrator_before_the_join_is_completed(String slotId) {
        TimeSlot slot = timeSlot(slotId);
        world.gameRequestService.setBeforeSlotCheckHook(request -> {
            if (request.getTimeSlot().equals(slot)) {
                slot.book();
            }
        });
    }

    @Then("time slot {string} has status {string}")
    public void time_slot_has_status(String slotId, String expectedStatus) {
        assertEquals(TimeSlotStatus.fromName(expectedStatus), timeSlot(slotId).status());
    }

    @Then("game request {string} has {int} participants")
    public void game_request_has_participants(String requestId, int participantCount) {
        GameRequest request = world.gameRequestService.findById(requestId)
                .orElseThrow(() -> new AssertionError("No game request with id " + requestId));
        assertEquals(participantCount, request.getParticipantCount());
    }

    @Then("{string} receives the notification {string}")
    public void receives_the_notification(String userName, String expected) {
        List<String> notifications = world.gameRequestService.getNotificationsFor(new User(userName));
        assertTrue(notifications.contains(expected), userName + " should have received \"" + expected
                + "\" but received " + notifications);
    }
}
