package ca.mcgill.ecse428.huddle.features;

import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.mcgill.ecse428.huddle.GameRequestService;
import ca.mcgill.ecse428.huddle.InvalidGameRequestException;
import ca.mcgill.ecse428.huddle.User;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/**
 * Step definitions for ID009 Cancel a Game Request (TID055).
 * The Background and the other Then steps are shared: see CommonStepDefinitions and
 * ConfirmGameAndBookCourtStepDefinitions (time slot status, notifications received).
 */
public class CancelGameRequestStepDefinitions {

    private final HuddleWorld world;

    public CancelGameRequestStepDefinitions(HuddleWorld world) {
        this.world = world;
    }

    @When("{string} cancels game request {string}")
    public void cancels_game_request(String userName, String requestId) {
        try {
            world.gameRequestService.cancelRequest(new User(userName), requestId);
            world.messages.add(GameRequestService.CANCELLED_MESSAGE);
        } catch (InvalidGameRequestException e) {
            world.errorMessage = e.getMessage();
        }
    }

    @Then("{string} receives no notification")
    public void receives_no_notification(String userName) {
        assertTrue(world.gameRequestService.getNotificationsFor(new User(userName)).isEmpty());
    }
}
