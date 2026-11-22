package ca.mcgill.ecse428.huddle.features;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.mcgill.ecse428.huddle.Account;
import ca.mcgill.ecse428.huddle.AccountService;
import ca.mcgill.ecse428.huddle.InvalidAccountException;
import ca.mcgill.ecse428.huddle.PasswordResetLink;
import ca.mcgill.ecse428.huddle.Session;
import ca.mcgill.ecse428.huddle.User;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Step definitions for ID011 Reset Password (TID028), including the scenario outline for expired or used links.
 * The accounts table is in CreateAccountStepDefinitions (ID001), and the current date and time and the message checks
 * are in CommonStepDefinitions.
 */
public class ResetPasswordStepDefinitions {

    private final HuddleWorld world;
    // The links issued by "a password reset is requested", i.e. the ones that would be emailed
    private final List<PasswordResetLink> sentLinks = new ArrayList<>();

    public ResetPasswordStepDefinitions(HuddleWorld world) {
        this.world = world;
    }

    private Account account(String email) {
        return world.accountService.findByEmail(email)
                .orElseThrow(() -> new AssertionError("No account for " + email));
    }

    /** Columns: link (its token), email, expires ("2026-11-09 18:00"), status (Unused or Used). */
    @Given("the following password reset links exist:")
    public void the_following_password_reset_links_exist(DataTable dataTable) {
        for (Map<String, String> row : dataTable.asMaps()) {
            PasswordResetLink link = new PasswordResetLink(
                    row.get("link"),
                    account(row.get("email")),
                    LocalDateTime.parse(row.get("expires").strip().replace(' ', 'T')));
            if (row.get("status").equals("Used")) {
                link.markUsed();
            }
            world.accountService.addResetLink(link);
        }
    }

    @When("a password reset is requested for email {string}")
    public void a_password_reset_is_requested_for_email(String email) {
        world.accountService.requestResetLink(email).ifPresent(sentLinks::add);
        world.messages.add(AccountService.RESET_LINK_SENT_MESSAGE);
    }

    /** The name only says who is at the keyboard: the link decides which account gets the new password. */
    @When("{string} resets her/their password using link {string} with new password {string}")
    public void resets_password_using_link_with_new_password(String userName, String link, String newPassword) {
        try {
            world.accountService.resetPassword(link, newPassword);
            world.messages.add(AccountService.PASSWORD_RESET_MESSAGE);
        } catch (InvalidAccountException e) {
            world.errorMessage = e.getMessage();
        }
    }

    @Then("a password reset link is sent to {string}")
    public void a_password_reset_link_is_sent_to(String email) {
        Account account = account(email);
        assertTrue(sentLinks.stream().anyMatch(l -> l.getAccount() == account && !l.isUsed()),
                "Expected an unused reset link sent to " + email);
    }

    @Then("no password reset link is sent to {string}")
    public void no_password_reset_link_is_sent_to(String email) {
        String normalized = Account.normalizeEmail(email);
        assertTrue(sentLinks.stream().noneMatch(l -> l.getAccount().getEmail().equals(normalized)),
                "Expected no reset link sent to " + email);
    }

    @Then("{string} can log in with email {string} and password {string}")
    public void can_log_in_with_email_and_password(String userName, String email, String password) {
        Session session = world.accountService.logIn(email, password);
        assertEquals(new User(userName), session.user());
    }

    @Then("{string} cannot log in with email {string} and password {string}")
    public void cannot_log_in_with_email_and_password(String userName, String email, String password) {
        InvalidAccountException e = assertThrows(InvalidAccountException.class,
                () -> world.accountService.logIn(email, password));
        assertEquals(InvalidAccountException.INVALID_CREDENTIALS_MESSAGE, e.getMessage());
    }

    @Then("password reset link {string} has status {string}")
    public void password_reset_link_has_status(String link, String status) {
        PasswordResetLink resetLink = world.accountService.findResetLink(link)
                .orElseThrow(() -> new AssertionError("No reset link " + link));
        assertEquals(status, resetLink.isUsed() ? "Used" : "Unused");
    }
}
