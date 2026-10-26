package ca.mcgill.ecse428.huddle.features;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.mcgill.ecse428.huddle.GameRequest;
import ca.mcgill.ecse428.huddle.GameRequestService;
import ca.mcgill.ecse428.huddle.GameRequestStatus;
import ca.mcgill.ecse428.huddle.InvalidGameRequestException;
import ca.mcgill.ecse428.huddle.User;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/** Step definitions for ID007 Join an Open Game Request. */
public class JoinOpenGameRequestStepDefinitions {

    private final HuddleWorld world;

    public JoinOpenGameRequestStepDefinitions(HuddleWorld world) {
        this.world = world;
    }

    private GameRequest request(String requestId) {
        return world.gameRequestService.findById(requestId)
                .orElseThrow(() -> new AssertionError("No game request with id " + requestId));
    }

    /** ID010 (TID034): the slot gets booked by a facility admin after the join starts but before it completes. */
    @Given("a facility admin books the time slot of game request {string} while it is being joined")
    public void a_facility_admin_books_the_time_slot_while_it_is_being_joined(String requestId) {
        world.gameRequestService.setBeforeSlotCheckHook(request -> {
            if (request.getId().equals(requestId)) {
                request.getTimeSlot().book();
            }
        });
    }

    @When("{string} joins game request {string}")
    public void joins_game_request(String userName, String requestId) {
        try {
            GameRequest joined = world.gameRequestService.joinRequest(new User(userName), requestId);
            world.messages.add(GameRequestService.JOINED_MESSAGE);
            if (joined.getStatus() == GameRequestStatus.CONFIRMED) {
                world.messages.add(GameRequestService.confirmationMessage(joined));
            }
        } catch (InvalidGameRequestException e) {
            world.errorMessage = e.getMessage();
        }
    }

    @Then("{string} is a participant of game request {string}")
    public void is_a_participant_of_game_request(String userName, String requestId) {
        assertTrue(request(requestId).hasParticipant(new User(userName)),
                userName + " should be a participant of " + requestId);
    }

    @Then("{string} is not a participant of game request {string}")
    public void is_not_a_participant_of_game_request(String userName, String requestId) {
        assertFalse(request(requestId).hasParticipant(new User(userName)),
                userName + " should not be a participant of " + requestId);
    }

    @Then("game request {string} has {int} spot(s) remaining")
    public void game_request_has_spots_remaining(String requestId, int spotsRemaining) {
        assertEquals(spotsRemaining, request(requestId).getSpotsRemaining());
    }
}
