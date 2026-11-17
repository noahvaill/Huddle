package ca.mcgill.ecse428.huddle;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The registry of Huddle accounts, one per email address (ID001).
 * Log in (ID002), publishing time slots (ID003), resetting a password (ID011) and logging out (ID012)
 * look accounts up here.
 */
public class AccountService {

    public static final String ACCOUNT_CREATED_MESSAGE = "Account created";
    private static final String STUDENT_EMAIL_DOMAIN = "@mail.mcgill.ca";
    private static final String STAFF_EMAIL_DOMAIN = "@mcgill.ca";

    private final Map<String, Account> accountsByEmail = new LinkedHashMap<>();
    private final Map<String, PasswordResetLink> resetLinksByToken = new HashMap<>();
    private Clock clock = Clock.systemDefaultZone();

    /**
     * Creates an account for a McGill student or staff member (ID001) and returns it.
     * Only @mail.mcgill.ca (student) and @mcgill.ca (staff) addresses are accepted, in any case and with
     * surrounding spaces, and an email address can have only one account. A student address gets the Student role
     * and a staff address the Staff role. Nothing is added when a rule is broken.
     */
    public Account createAccount(String name, String email, String password) {
        Role role = roleForMcGillEmail(email);
        if (findByEmail(email).isPresent()) {
            throw new InvalidAccountException(InvalidAccountException.EMAIL_ALREADY_IN_USE_MESSAGE);
        }
        Account account = new Account(new User(name), email, password, role);
        addAccount(account);
        return account;
    }

    /** Student or Staff for a McGill address with exactly one @ and something before it; fails for any other address. */
    private static Role roleForMcGillEmail(String email) {
        String normalized = email == null ? "" : email.strip().toLowerCase(Locale.ROOT);
        int at = normalized.indexOf('@');
        boolean oneAtWithNameBeforeIt = at > 0 && at == normalized.lastIndexOf('@');
        if (oneAtWithNameBeforeIt && normalized.endsWith(STUDENT_EMAIL_DOMAIN)) {
            return Role.STUDENT;
        }
        if (oneAtWithNameBeforeIt && normalized.endsWith(STAFF_EMAIL_DOMAIN)) {
            return Role.STAFF;
        }
        throw new InvalidAccountException(InvalidAccountException.MCGILL_EMAIL_REQUIRED_MESSAGE);
    }

    /**
     * Adds an existing account as it is, without createAccount's McGill rules, e.g. the accounts in a feature file's
     * Background. Fails if an account already exists for its email address, in any case.
     */
    public void addAccount(Account account) {
        Objects.requireNonNull(account, "Account is required");
        if (accountsByEmail.containsKey(account.getEmail())) {
            throw new IllegalArgumentException("An account already exists for " + account.getEmail());
        }
        accountsByEmail.put(account.getEmail(), account);
    }

    /** The account for this email address, in any case and with surrounding spaces; empty if there is none. */
    public Optional<Account> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(accountsByEmail.get(email.strip().toLowerCase(Locale.ROOT)));
    }

    /** The first account that belongs to this user, for steps that name a user (e.g. "Frank Ho" is logged in). */
    public Optional<Account> findByUser(User user) {
        return accountsByEmail.values().stream().filter(a -> a.getUser().equals(user)).findFirst();
    }

    /** All accounts, in the order they were added. */
    public List<Account> getAccounts() {
        return List.copyOf(accountsByEmail.values());
    }

    /** Adds a password reset link (ID011), e.g. the links in a feature file's Background. Tokens must be unique. */
    public void addResetLink(PasswordResetLink link) {
        Objects.requireNonNull(link, "Reset link is required");
        if (resetLinksByToken.putIfAbsent(link.getToken(), link) != null) {
            throw new IllegalArgumentException("A reset link already exists with this token");
        }
    }

    /** The reset link with this token; empty if there is none. */
    public Optional<PasswordResetLink> findResetLink(String token) {
        return Optional.ofNullable(token == null ? null : resetLinksByToken.get(token));
    }

    /** The current date and time, used to check whether a reset link has expired (ID011). */
    public LocalDateTime currentDateTime() {
        return LocalDateTime.now(clock);
    }

    /** Test hook: fixes the current date and time, so scenarios about expired links do not depend on the real clock. */
    public void setCurrentDateTime(LocalDateTime dateTime) {
        clock = Clock.fixed(dateTime.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
    }
}
