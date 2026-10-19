package ca.mcgill.ecse428.huddle;

public class InvalidGameRequestException extends RuntimeException {
    public static final String SLOT_NOT_AVAILABLE_MESSAGE = "Time slot is not available";
    public static final String NOT_ENOUGH_PLAYERS_MESSAGE = "At least 1 more player is required";
    public static final String OVERLAPPING_GAME_MESSAGE = "You already have a game at this time";

    public InvalidGameRequestException(String message) {
        super(message);
    }
}
