package ca.mcgill.ecse428.huddle;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * A period on a given date during which a court can be booked.
 * Court, date and times never change; the status goes from Available to Booked when a game is confirmed (ID010),
 * and back to Available when that game reopens because a participant withdrew (ID008).
 */
public final class TimeSlot {

    private final Court court;
    private final LocalDate date;
    private final LocalTime start;
    private final LocalTime end;
    private TimeSlotStatus status;

    public TimeSlot(Court court, LocalDate date, LocalTime start, LocalTime end, boolean available) {
        this(court, date, start, end, available ? TimeSlotStatus.AVAILABLE : TimeSlotStatus.BOOKED);
    }

    public TimeSlot(Court court, LocalDate date, LocalTime start, LocalTime end, TimeSlotStatus status) {
        this.court = Objects.requireNonNull(court, "Court is required");
        this.date = Objects.requireNonNull(date, "Date is required");
        this.start = Objects.requireNonNull(start, "Start time is required");
        this.end = Objects.requireNonNull(end, "End time is required");
        this.status = Objects.requireNonNull(status, "Status is required");
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
    }

    public Court court() { return court; }
    public LocalDate date() { return date; }
    public LocalTime start() { return start; }
    public LocalTime end() { return end; }
    public TimeSlotStatus status() { return status; }
    public boolean available() { return status == TimeSlotStatus.AVAILABLE; }
    public Sport sport() { return court.sport(); }

    /** Books the slot for a confirmed game. Fails if it is already booked. */
    public void book() {
        if (status == TimeSlotStatus.BOOKED) {
            throw new IllegalStateException("Time slot is already booked");
        }
        status = TimeSlotStatus.BOOKED;
    }

    /** Releases the slot when its confirmed game reopens (ID008). Fails if it is not booked. */
    public void release() {
        if (status != TimeSlotStatus.BOOKED) {
            throw new IllegalStateException("Time slot is not booked");
        }
        status = TimeSlotStatus.AVAILABLE;
    }

    /** Two slots are the same slot when they have the same court, date and times, whatever their status. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeSlot other)) return false;
        return court.equals(other.court) && date.equals(other.date)
                && start.equals(other.start) && end.equals(other.end);
    }

    @Override
    public int hashCode() { return Objects.hash(court, date, start, end); }

    @Override
    public String toString() {
        return court + " " + date + " " + start + "-" + end + " (" + status.getDisplayName() + ")";
    }
}