package ca.mcgill.ecse428.huddle.features;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.mcgill.ecse428.huddle.Court;
import ca.mcgill.ecse428.huddle.GameRequest;
import ca.mcgill.ecse428.huddle.GameRequestStatus;
import ca.mcgill.ecse428.huddle.Sport;
import ca.mcgill.ecse428.huddle.TimeSlot;
import ca.mcgill.ecse428.huddle.User;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
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
                    new Court(row.get("court"), Sport.fromName(row.get("sport"))),
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

    /**
     * Adds existing game requests as they are, without the posting rules (e.g. R1-R6 in ID006 and ID007).
     * Columns: request, creator, spots remaining, status, and either "slot" (an id from the time slots table, as in
     * ID010) or sport, date, start, end.
     * Optional "court": when missing, the court is named after the sport (e.g. "Squash Court").
     * Optional "participants": either comma-separated names, creator first, or a number of players on the creator's
     * side (the group size, as in ID010); when missing, only the creator.
     */
    @Given("the following game requests exist:")
    public void the_following_game_requests_exist(DataTable dataTable) {
        for (Map<String, String> row : dataTable.asMaps()) {
            User creator = new User(row.get("creator"));
            GameRequestStatus status = GameRequestStatus.fromName(row.get("status"));
            TimeSlot slot;
            if (row.containsKey("slot")) {
                slot = world.timeSlotsById.get(row.get("slot"));
            } else {
                Sport sport = Sport.fromName(row.get("sport"));
                String courtName = row.containsKey("court") ? row.get("court") : sport.getDisplayName() + " Court";
                slot = new TimeSlot(
                        new Court(courtName, sport),
                        LocalDate.parse(row.get("date")),
                        LocalTime.parse(row.get("start")),
                        LocalTime.parse(row.get("end")),
                        status != GameRequestStatus.CONFIRMED); // a confirmed game has booked its slot
            }
            int groupSize = 1;
            List<User> participants = List.of(creator);
            String participantsCell = row.get("participants");
            if (participantsCell != null && participantsCell.strip().matches("\\d+")) {
                groupSize = Integer.parseInt(participantsCell.strip());
            } else if (participantsCell != null) {
                participants = Arrays.stream(participantsCell.split(",")).map(String::strip).map(User::new).toList();
            }
            world.gameRequestService.addRequest(new GameRequest(
                    row.get("request"),
                    creator,
                    slot,
                    groupSize,
                    Integer.parseInt(row.get("spots remaining")),
                    status,
                    participants));
        }
    }

    @Then("game request {string} has status {string}")
    public void game_request_has_status(String requestId, String expectedStatus) {
        GameRequest request = world.gameRequestService.findById(requestId)
                .orElseThrow(() -> new AssertionError("No game request with id " + requestId));
        assertEquals(GameRequestStatus.fromName(expectedStatus), request.getStatus());
    }

    @Then("the message {string} is issued")
    public void the_message_is_issued(String expected) {
        assertTrue(world.messages.contains(expected), "Expected \"" + expected + "\" but got " + world.messages);
    }

    @Then("the confirmation message {string} is issued")
    public void the_confirmation_message_is_issued(String expected) {
        assertTrue(world.messages.contains(expected), "Expected \"" + expected + "\" but got " + world.messages);
    }

    @Then("the error message {string} is issued")
    public void the_error_message_is_issued(String expected) {
        assertEquals(expected, world.errorMessage);
    }
}
