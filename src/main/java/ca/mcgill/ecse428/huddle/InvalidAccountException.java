package ca.mcgill.ecse428.huddle;

/**
 * A create-account (ID001), log in (ID002), reset password (ID011), log out (ID012), edit profile (ID013) or set skill
 * level (ID014) request that breaks a rule, with the message the feature file expects.
 */
public class InvalidAccountException extends RuntimeException {
    public static final String MCGILL_EMAIL_REQUIRED_MESSAGE = "A McGill email address is required";
    public static final String EMAIL_ALREADY_IN_USE_MESSAGE = "An account already exists for this email address";
    public static final String INVALID_CREDENTIALS_MESSAGE = "Invalid email address or password";
    public static final String INVALID_RESET_LINK_MESSAGE = "This password reset link is invalid or has expired";
    public static final String NOT_LOGGED_IN_MESSAGE = "You are not logged in";
    public static final String CURRENT_PASSWORD_INCORRECT_MESSAGE = "Current password is incorrect";
    public static final String NAME_EMPTY_MESSAGE = "Name cannot be empty";
    public static final String SKILL_LEVEL_NOT_OFFERED_MESSAGE = "Skill level not offered";

    public InvalidAccountException(String message) {
        super(message);
    }
}
