package ca.mcgill.ecse428.huddle.features;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.mcgill.ecse428.huddle.Account;
import ca.mcgill.ecse428.huddle.Court;
import ca.mcgill.ecse428.huddle.InvalidTimeSlotException;
import ca.mcgill.ecse428.huddle.Sport;
import ca.mcgill.ecse428.huddle.TimeSlot;
import ca.mcgill.ecse428.huddle.TimeSlotService;
import ca.mcgill.ecse428.huddle.User;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Step definitions for ID003 Publish Court Time Slots (TID019). The accounts table, the login step, the existing
 * time slots table and the message checks are reused from CreateAccountStepDefinitions, CommonStepDefinitions and
 * LogInStepDefinitions.
 */
public class PublishCourtTimeSlotsStepDefinitions {

    private static final String SLOTS_PUBLISHED_MESSAGE_FORMAT = "%d time slots published";

    private final HuddleWorld world;

    public PublishCourtTimeSlotsStepDefinitions(HuddleWorld world) {
        this.world = world;
    }

    private Account publisher(String name) {
        return world.accountService.findByUser(new User(name)).orElseThrow();
    }

    private Court court(String courtName) {
        return world.facilityService.findCourt(courtName).orElseThrow();
    }

    @Given("the following courts exist at {string}:")
    public void the_following_courts_exist_at(String facility, DataTable dataTable) {
        for (Map<String, String> row : dataTable.asMaps()) {
            world.facilityService.addCourt(facility, row.get("court"), Sport.fromName(row.get("sport")));
        }
    }

    @When("{string} publishes a time slot for {string} on {string} from {string} to {string}")
    public void publishes_a_time_slot(String name, String courtName, String date, String start, String end) {
        try {
            world.timeSlotService.publishTimeSlot(publisher(name), court(courtName),
                    LocalDate.parse(date), LocalTime.parse(start), LocalTime.parse(end));
            world.messages.add(TimeSlotService.ONE_SLOT_PUBLISHED_MESSAGE);
        } catch (InvalidTimeSlotException e) {
            world.errorMessage = e.getMessage();
        }
    }

    @When("{string} publishes time slots for {string} on {string} every {int} minutes from {string} to {string}")
    public void publishes_time_slots(String name, String courtName, String date, int every, String start, String end) {
        try {
            List<TimeSlot> published = world.timeSlotService.publishConsecutiveTimeSlots(publisher(name),
                    court(courtName), LocalDate.parse(date), LocalTime.parse(start), LocalTime.parse(end), every);
            world.messages.add(published.size() == 1
                    ? TimeSlotService.ONE_SLOT_PUBLISHED_MESSAGE
                    : String.format(SLOTS_PUBLISHED_MESSAGE_FORMAT, published.size()));
        } catch (InvalidTimeSlotException e) {
            world.errorMessage = e.getMessage();
        }
    }

    @Then("an available time slot exists for {string} on {string} from {string} to {string}")
    public void an_available_time_slot_exists(String courtName, String date, String start, String end) {
        assertTrue(world.timeSlotService.getTimeSlots().stream().anyMatch(slot ->
                        slot.court().name().equals(courtName)
                        && slot.date().equals(LocalDate.parse(date))
                        && slot.start().equals(LocalTime.parse(start))
                        && slot.end().equals(LocalTime.parse(end))
                        && slot.available()),
                "Expected an available time slot for " + courtName + " on " + date + " from " + start + " to " + end);
    }

    @Then("no time slot exists for {string} on {string} from {string} to {string}")
    public void no_time_slot_exists(String courtName, String date, String start, String end) {
        assertTrue(world.timeSlotService.getTimeSlots().stream().noneMatch(slot ->
                        slot.court().name().equals(courtName)
                        && slot.date().equals(LocalDate.parse(date))
                        && slot.start().equals(LocalTime.parse(start))
                        && slot.end().equals(LocalTime.parse(end))),
                "Expected no time slot for " + courtName + " on " + date + " from " + start + " to " + end);
    }

    @Then("the following available time slots exist for {string} on {string}:")
    public void the_following_available_time_slots_exist(String courtName, String date, DataTable dataTable) {
        List<Map<String, String>> expected = dataTable.asMaps();
        List<TimeSlot> actual = world.timeSlotService.getTimeSlots().stream()
                .filter(slot -> slot.court().name().equals(courtName)
                        && slot.date().equals(LocalDate.parse(date))
                        && slot.available())
                .sorted(Comparator.comparing(TimeSlot::start))
                .toList();
        assertEquals(expected.size(), actual.size(),
                "Expected " + expected.size() + " available time slots but got " + actual);
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(LocalTime.parse(expected.get(i).get("start")), actual.get(i).start());
            assertEquals(LocalTime.parse(expected.get(i).get("end")), actual.get(i).end());
        }
    }
}
