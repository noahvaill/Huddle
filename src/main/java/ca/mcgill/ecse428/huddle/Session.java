package ca.mcgill.ecse428.huddle;

import java.util.Objects;

/** The active session started when an account logs in (ID002): who is logged in and with which role. */
public record Session(Account account) {

    public Session {
        Objects.requireNonNull(account, "Account is required");
    }

    public User user() { return account.getUser(); }
    public Role role() { return account.getRole(); }
}
