package ca.mcgill.ecse428.huddle;

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

    private final Map<String, Account> accountsByEmail = new LinkedHashMap<>();

    /**
     * Adds an existing account as it is, e.g. the accounts in a feature file's Background.
     * Fails if an account already exists for its email address, in any case.
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
}
