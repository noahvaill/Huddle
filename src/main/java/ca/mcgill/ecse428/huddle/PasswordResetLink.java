package ca.mcgill.ecse428.huddle;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A link sent by email to reset an account's password (ID011).
 * It expires at a given date and time and can be used only once: after a new password is set it is Used.
 */
public final class PasswordResetLink {

    private final String token;
    private final Account account;
    private final LocalDateTime expiresAt;
    private boolean used;

    public PasswordResetLink(String token, Account account, LocalDateTime expiresAt) {
        Objects.requireNonNull(token, "Token is required");
        if (token.isBlank()) {
            throw new IllegalArgumentException("Token must not be blank");
        }
        this.token = token;
        this.account = Objects.requireNonNull(account, "Account is required");
        this.expiresAt = Objects.requireNonNull(expiresAt, "Expiry time is required");
    }

    public String getToken() { return token; }
    public Account getAccount() { return account; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public boolean isUsed() { return used; }

    /** A link is expired from its expiry time on. */
    public boolean isExpiredAt(LocalDateTime now) {
        return !now.isBefore(expiresAt);
    }

    /** Marks the link Used once a new password is set with it. Fails if it is already used. */
    public void markUsed() {
        if (used) {
            throw new IllegalStateException("Reset link has already been used");
        }
        used = true;
    }

    /** Never shows the token, since anyone holding it could reset the password. */
    @Override
    public String toString() {
        return "Reset link for " + account.getEmail() + " expiring " + expiresAt + (used ? " (Used)" : "");
    }
}
