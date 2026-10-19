package ca.mcgill.ecse428.huddle;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/** A period on a given date during which a court can be booked. */
public record TimeSlot(Court court, LocalDate date, LocalTime start, LocalTime end, boolean available) {

    public TimeSlot {
        Objects.requireNonNull(court, "Court is required");
        Objects.requireNonNull(date, "Date is required");
        Objects.requireNonNull(start, "Start time is required");
        Objects.requireNonNull(end, "End time is required");
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
    }

    public Sport sport() {
        return court.sport();
    }
}
