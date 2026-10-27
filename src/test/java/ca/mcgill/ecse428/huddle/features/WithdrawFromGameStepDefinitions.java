package ca.mcgill.ecse428.huddle.features;

import ca.mcgill.ecse428.huddle.GameRequestService;
import ca.mcgill.ecse428.huddle.InvalidGameRequestException;
import ca.mcgill.ecse428.huddle.User;
import io.cucumber.java.en.When;

/**
 * Step definitions for ID008 Withdraw from a Game (TID045).
 * The Background and Then steps are shared: the game requests table and the message checks are in
 * CommonStepDefinitions, and the participant and spots checks are in JoinOpenGameRequestStepDefinitions.
 */
public class WithdrawFromGameStepDefinitions {

    private final HuddleWorld world;

    public WithdrawFromGameStepDefinitions(HuddleWorld world) {
        this.world = world;
    }

    @When("{string} withdraws from game request {string}")
    public void withdraws_from_game_request(String userName, String requestId) {
        try {
            world.gameRequestService.withdrawFromRequest(new User(userName), requestId);
            world.message = GameRequestService.WITHDRAWN_MESSAGE;
        } catch (InvalidGameRequestException e) {
            world.errorMessage = e.getMessage();
        }
    }
}