package ca.mcgill.ecse428.huddle.features;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.mcgill.ecse428.huddle.GameRequest;
import ca.mcgill.ecse428.huddle.GameRequestService;
import ca.mcgill.ecse428.huddle.GameRequestStatus;
import ca.mcgill.ecse428.huddle.InvalidGameRequestException;
import ca.mcgill.ecse428.huddle.Sport;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.Map;

public class PostOpenGameRequestStepDefinitions {

    private final HuddleWorld world;

    public PostOpenGameRequestStepDefinitions(HuddleWorld world) {
        this.world = world;
    }

    private void postRequest(String userName, String slotId, int groupSize, int playersNeeded) {
        try {
            world.gameRequestService.postRequest(userName, world.timeSlotsById.get(slotId), groupSize, playersNeeded);
            world.message = GameRequestService.POSTED_MESSAGE;
        } catch (InvalidGameRequestException e) {
            world.errorMessage = e.getMessage();
        }
    }

    @Given("the maximum number of players per sport is:")
    public void the_maximum_number_of_players_per_sport_is(DataTable dataTable) {
        for (Map<String, String> row : dataTable.asMaps()) {
            world.gameRequestService.setMaxPlayers(
                    Sport.fromName(row.get("sport")), Integer.parseInt(row.get("max players")));
        }
    }

    @Given("{string} has an open game request for time slot {string}")
    public void has_an_open_game_request_for_time_slot(String userName, String slotId) {
        world.gameRequestService.postRequest(userName, world.timeSlotsById.get(slotId), 1, 1);
    }

    @When("{string} posts an open game request for time slot {string} needing {int} more player(s)")
    public void posts_an_open_game_request_needing_more_players(String userName, String slotId, int playersNeeded) {
        postRequest(userName, slotId, 1, playersNeeded);
    }

    @When("{string} posts an open game request for time slot {string} for a group of {int} players needing {int} more players")
    public void posts_an_open_game_request_for_a_group(String userName, String slotId, int groupSize, int playersNeeded) {
        postRequest(userName, slotId, groupSize, playersNeeded);
    }

    @Then("an open game request exists for time slot {string} created by {string} with {int} spot(s) remaining")
    public void an_open_game_request_exists(String slotId, String creator, int spotsRemaining) {
        GameRequest request = world.gameRequestService.findByTimeSlot(world.timeSlotsById.get(slotId)).orElseThrow();
        assertEquals(creator, request.getCreator());
        assertEquals(spotsRemaining, request.getSpotsRemaining());
        assertEquals(GameRequestStatus.OPEN, request.getStatus());
    }

    @Then("no open game request exists for time slot {string}")
    public void no_open_game_request_exists(String slotId) {
        assertTrue(world.gameRequestService.findByTimeSlot(world.timeSlotsById.get(slotId)).isEmpty());
    }
}
