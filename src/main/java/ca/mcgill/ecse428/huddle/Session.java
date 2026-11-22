package ca.mcgill.ecse428.huddle;

import java.util.Objects;

/**
 * The active session started when an account logs in on a device (ID002): who is logged in and with which role.
 * It ends when they log out on that device (ID012).
 */
public record Session(Account account) {

    public Session {
        Objects.requireNonNull(account, "Account is required");
    }

    public User user() { return account.getUser(); }
    public Role role() { return account.getRole(); }
}
