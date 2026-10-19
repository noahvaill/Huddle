package ca.mcgill.ecse428.huddle;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/** A period on a given date during which a court can be booked. */
public class TimeSlot {

    private final Court court;
    private final LocalDate date;
    private final LocalTime start;
    private final LocalTime end;
    private SlotStatus status;

    public TimeSlot(Court court, LocalDate date, LocalTime start, LocalTime end, SlotStatus status) {
        this.court = Objects.requireNonNull(court, "Court is required");
        this.date = Objects.requireNonNull(date, "Date is required");
        this.start = Objects.requireNonNull(start, "Start time is required");
        this.end = Objects.requireNonNull(end, "End time is required");
        this.status = Objects.requireNonNull(status, "Status is required");
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
    }

    /** Creates an available slot. */
    public TimeSlot(Court court, LocalDate date, LocalTime start, LocalTime end) {
        this(court, date, start, end, SlotStatus.AVAILABLE);
    }

    public Court getCourt() {
        return court;
    }

    public Sport getSport() {
        return court.sport();
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStart() {
        return start;
    }

    public LocalTime getEnd() {
        return end;
    }

    public SlotStatus getStatus() {
        return status;
    }

    public boolean isAvailable() {
        return status == SlotStatus.AVAILABLE;
    }

    /** Marks the slot as booked. Fails if it is already booked. */
    public void book() {
        if (!isAvailable()) {
            throw new IllegalStateException("Time slot is already booked");
        }
        status = SlotStatus.BOOKED;
    }

    /** True when both slots are on the same court and date and their periods intersect. */
    public boolean overlaps(TimeSlot other) {
        return court.equals(other.court)
                && date.equals(other.date)
                && start.isBefore(other.end)
                && other.start.isBefore(end);
    }

    @Override
    public String toString() {
        return court + " " + date + " " + start + "-" + end + " (" + status + ")";
    }
}
