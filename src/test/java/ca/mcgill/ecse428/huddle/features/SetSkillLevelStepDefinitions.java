package ca.mcgill.ecse428.huddle.features;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.mcgill.ecse428.huddle.Account;
import ca.mcgill.ecse428.huddle.AccountService;
import ca.mcgill.ecse428.huddle.SkillLevel;
import ca.mcgill.ecse428.huddle.Sport;
import ca.mcgill.ecse428.huddle.User;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Steps for the ID014 set skill level scenarios, including the error-flow Scenario Outline: Cucumber fills in the
 * Examples values before matching, so the same When step handles the error rows by recording the error message.
 */
public class SetSkillLevelStepDefinitions {
    private final HuddleWorld world;

    public SetSkillLevelStepDefinitions(HuddleWorld world) {
        this.world = world;
    }

    /** The offered levels are fixed by the SkillLevel enum, so this step only checks they match the feature file. */
    @Given("the following skill levels are offered: {string}, {string}, {string}")
    public void the_following_skill_levels_are_offered(String first, String second, String third) {
        assertEquals(List.of(first, second, third), SkillLevel.offeredLevelNames());
    }

    @Given("{string} has the following skill levels:")
    public void has_the_following_skill_levels(String userName, DataTable dataTable) {
        for (Map<String, String> row : dataTable.asMaps()) {
            world.accountService.setSkillLevel(new User(userName), row.get("sport"), row.get("level"));
        }
    }

    @When("{string} sets her/his/their skill level for {string} to {string}")
    public void sets_skill_level_for_to(String userName, String sportName, String levelName) {
        try {
            world.accountService.setSkillLevel(new User(userName), sportName, levelName);
            world.messages.add(AccountService.SKILL_LEVEL_SAVED_MESSAGE);
        } catch (RuntimeException e) {
            world.errorMessage = e.getMessage();
        }
    }

    @Then("{string} has skill level {string} for {string}")
    public void has_skill_level_for(String userName, String levelName, String sportName) {
        Account account = accountOf(userName);
        assertEquals(Optional.of(SkillLevel.fromName(levelName)), account.getSkillLevel(Sport.fromName(sportName)));
    }

    /**
     * Sport.fromName throws for a sport that is not offered (e.g. "Curling"), so the sport is looked up here instead.
     * An account can only hold levels for offered sports, so a sport that is not offered never has one.
     */
    @Then("{string} has no skill level for {string}")
    public void has_no_skill_level_for(String userName, String sportName) {
        Account account = accountOf(userName);
        Arrays.stream(Sport.values())
                .filter(s -> s.getDisplayName().equalsIgnoreCase(sportName.strip()))
                .findFirst()
                .ifPresent(sport -> assertTrue(account.getSkillLevel(sport).isEmpty(),
                        userName + " should have no skill level for " + sportName));
    }

    private Account accountOf(String userName) {
        return world.accountService.findByUser(new User(userName))
                .orElseThrow(() -> new AssertionError("No account for " + userName));
    }
}
