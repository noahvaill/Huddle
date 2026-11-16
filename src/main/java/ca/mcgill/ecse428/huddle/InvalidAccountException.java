package ca.mcgill.ecse428.huddle;

/** A create-account request that breaks a rule of ID001, with the message the feature file expects. */
public class InvalidAccountException extends RuntimeException {
    public static final String MCGILL_EMAIL_REQUIRED_MESSAGE = "A McGill email address is required";
    public static final String EMAIL_ALREADY_IN_USE_MESSAGE = "An account already exists for this email address";

    public InvalidAccountException(String message) {
        super(message);
    }
}
