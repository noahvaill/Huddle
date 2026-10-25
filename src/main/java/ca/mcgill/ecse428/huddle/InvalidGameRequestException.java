package ca.mcgill.ecse428.huddle;

public class InvalidGameRequestException extends RuntimeException {
    public static final String SLOT_NOT_AVAILABLE_MESSAGE = "Time slot is not available";
    public static final String NOT_ENOUGH_PLAYERS_MESSAGE = "At least 1 more player is required";
    public static final String OVERLAPPING_GAME_MESSAGE = "You already have a game at this time";
    public static final String REQUEST_NOT_FOUND_MESSAGE = "Game request not found";
    public static final String OWN_REQUEST_MESSAGE = "You cannot join your own game request";
    public static final String ALREADY_JOINED_MESSAGE = "You have already joined this game";
    public static final String GAME_FULL_MESSAGE = "This game is full";
    public static final String NOT_OPEN_MESSAGE = "This game is no longer open";
    public static final String SLOT_NO_LONGER_AVAILABLE_MESSAGE = "Time slot is no longer available";

    public InvalidGameRequestException(String message) {
        super(message);
    }
}
