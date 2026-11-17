package ca.mcgill.ecse428.huddle;

/** A publish-time-slot request that breaks a rule of ID003, with the message the feature file expects. */
public class InvalidTimeSlotException extends RuntimeException {
    public static final String FACILITY_ADMINISTRATOR_ONLY_MESSAGE = "Only a facility administrator can publish time slots";
    public static final String OVERLAPPING_SLOT_MESSAGE = "Time slot overlaps an existing time slot";

    public InvalidTimeSlotException(String message) {
        super(message);
    }
}
