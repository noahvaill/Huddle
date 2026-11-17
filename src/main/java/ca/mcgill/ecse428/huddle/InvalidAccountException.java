package ca.mcgill.ecse428.huddle;

/**
 * A create-account (ID001), log in (ID002) or reset password (ID011) request that breaks a rule, with the message
 * the feature file expects.
 */
public class InvalidAccountException extends RuntimeException {
    public static final String MCGILL_EMAIL_REQUIRED_MESSAGE = "A McGill email address is required";
    public static final String EMAIL_ALREADY_IN_USE_MESSAGE = "An account already exists for this email address";
    public static final String INVALID_CREDENTIALS_MESSAGE = "Invalid email address or password";
    public static final String INVALID_RESET_LINK_MESSAGE = "This password reset link is invalid or has expired";

    public InvalidAccountException(String message) {
        super(message);
    }
}
