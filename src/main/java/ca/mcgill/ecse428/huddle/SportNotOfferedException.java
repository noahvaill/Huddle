package ca.mcgill.ecse428.huddle;

public class SportNotOfferedException extends RuntimeException {
    public static final String MESSAGE = "Sport not offered";
    private final String requestedSport;

    public SportNotOfferedException(String requestedSport) {
        super(MESSAGE);
        this.requestedSport = requestedSport;
    }
    public String getRequestedSport() { return requestedSport; }
}
