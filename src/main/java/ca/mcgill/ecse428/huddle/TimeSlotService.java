package ca.mcgill.ecse428.huddle;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class TimeSlotService {

    public static final String NO_AVAILABLE_SLOTS_MESSAGE = "No available time slots";
    public static final String ONE_SLOT_PUBLISHED_MESSAGE = "1 time slot published";
    private final List<TimeSlot> timeSlots = new ArrayList<>();
    public void addTimeSlot(TimeSlot slot) {
        timeSlots.add(slot);
    }

    /**
     * Publishes one Available time slot for a court (ID003, TID016) and returns it. Only a facility administrator
     * can publish, and the slot cannot overlap another slot on the same court and date (slots that only touch,
     * e.g. 18:00-18:45 and 18:45-19:30, do not overlap). Nothing is added when a rule is broken.
     */
    public TimeSlot publishTimeSlot(Account publisher, Court court, LocalDate date, LocalTime start, LocalTime end) {
        requireFacilityAdministrator(publisher);
        TimeSlot slot = new TimeSlot(court, date, start, end, TimeSlotStatus.AVAILABLE);
        requireNoOverlap(slot);
        timeSlots.add(slot);
        return slot;
    }

    /**
     * Publishes adjacent Available slots covering a same-day range (ID003, TID017). The positive interval must
     * divide the range exactly. The returned list is immutable and ordered by start time. All slots are checked
     * before any are added, so invalid inputs or an overlap leave the schedule unchanged.
     */
    public List<TimeSlot> publishConsecutiveTimeSlots(Account publisher, Court court, LocalDate date,
                                                     LocalTime start, LocalTime end, int intervalMinutes) {
        requireFacilityAdministrator(publisher);
        // Reuse the single-slot rules for required details and end after start, including their error messages.
        TimeSlot range = new TimeSlot(court, date, start, end, TimeSlotStatus.AVAILABLE);
        if (intervalMinutes <= 0) {
            throw new IllegalArgumentException("Interval must be positive");
        }
        Duration interval = Duration.ofMinutes(intervalMinutes);
        Duration duration = Duration.between(range.start(), range.end());
        long slotCount = duration.dividedBy(interval);
        if (slotCount == 0 || !interval.multipliedBy(slotCount).equals(duration)) {
            throw new IllegalArgumentException("Time range must contain a whole number of intervals");
        }

        List<TimeSlot> slots = new ArrayList<>();
        LocalTime slotStart = start;
        for (long i = 0; i < slotCount; i++) {
            LocalTime slotEnd = slotStart.plusMinutes(intervalMinutes);
            TimeSlot slot = new TimeSlot(court, date, slotStart, slotEnd, TimeSlotStatus.AVAILABLE);
            requireNoOverlap(slot);
            slots.add(slot);
            slotStart = slotEnd;
        }
        List<TimeSlot> published = List.copyOf(slots);
        timeSlots.addAll(published);
        return published;
    }

    private static void requireFacilityAdministrator(Account publisher) {
        Objects.requireNonNull(publisher, "Publisher is required");
        if (publisher.getRole() != Role.FACILITY_ADMINISTRATOR) {
            throw new InvalidTimeSlotException(InvalidTimeSlotException.FACILITY_ADMINISTRATOR_ONLY_MESSAGE);
        }
    }

    private void requireNoOverlap(TimeSlot slot) {
        if (overlapsExistingSlot(slot)) {
            throw new InvalidTimeSlotException(InvalidTimeSlotException.OVERLAPPING_SLOT_MESSAGE);
        }
    }

    // Whatever its status, a slot on the same court and date blocks any slot whose times intersect it
    private boolean overlapsExistingSlot(TimeSlot slot) {
        return timeSlots.stream()
                .anyMatch(other -> other.court().equals(slot.court())
                        && other.date().equals(slot.date())
                        && slot.start().isBefore(other.end())
                        && other.start().isBefore(slot.end()));
    }

    /** All time slots, Available or Booked, in the order they were added. */
    public List<TimeSlot> getTimeSlots() {
        return List.copyOf(timeSlots);
    }

    public List<TimeSlot> getAvailableTimeSlots(String sportName, LocalDate date) {
        Sport sport = Sport.fromName(sportName);
        return timeSlots.stream()
                .filter(s -> s.available() && s.sport() == sport && s.date().equals(date))
                .sorted(Comparator.comparing(TimeSlot::start).thenComparing(s -> s.court().name()))
                .toList();
    }
}
