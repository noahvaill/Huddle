package ca.mcgill.ecse428.huddle;

import java.util.Objects;

/** A message sent to a user, e.g. when their game is cancelled. */
public record Notification(User recipient, String message) {
    public Notification {
        Objects.requireNonNull(recipient, "Recipient is required");
        Objects.requireNonNull(message, "Message is required");
        if (message.isBlank()) {
            throw new IllegalArgumentException("Message must not be blank");
        }
    }
}
