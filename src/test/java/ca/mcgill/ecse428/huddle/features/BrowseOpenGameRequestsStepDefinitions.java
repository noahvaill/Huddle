package ca.mcgill.ecse428.huddle.features;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.mcgill.ecse428.huddle.GameRequest;
import ca.mcgill.ecse428.huddle.GameRequestService;
import ca.mcgill.ecse428.huddle.SportNotOfferedException;
import ca.mcgill.ecse428.huddle.User;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import java.util.Map;

/** Step definitions for ID006 Browse Open Game Requests. */
public class BrowseOpenGameRequestsStepDefinitions {

    private final HuddleWorld world;
    private List<GameRequest> listedRequests = List.of();

    public BrowseOpenGameRequestsStepDefinitions(HuddleWorld world) {
        this.world = world;
    }

    @When("{string} requests the open game requests for {string}")
    public void requests_the_open_game_requests_for(String userName, String sport) {
        try {
            listedRequests = world.gameRequestService.getOpenGameRequests(new User(userName), sport);
            if (listedRequests.isEmpty()) {
                world.messages.add(GameRequestService.NO_OPEN_REQUESTS_MESSAGE);
            }
        } catch (SportNotOfferedException e) {
            listedRequests = List.of();
            world.errorMessage = e.getMessage();
        }
    }

    @Then("the following game requests are listed:")
    public void the_following_game_requests_are_listed(DataTable dataTable) {
        List<Map<String, String>> actual = listedRequests.stream()
                .map(r -> Map.of(
                        "request", r.getId(),
                        "creator", r.getCreator().name(),
                        "court", r.getTimeSlot().court().name(),
                        "date", r.getTimeSlot().date().toString(),
                        "start", r.getTimeSlot().start().toString(),
                        "end", r.getTimeSlot().end().toString(),
                        "spots remaining", String.valueOf(r.getSpotsRemaining())))
                .toList();
        assertEquals(dataTable.asMaps(), actual);
    }

    @Then("no game requests are listed")
    public void no_game_requests_are_listed() {
        assertTrue(listedRequests.isEmpty());
    }
}
