package ca.mcgill.ecse428.huddle;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TimeSlotService {

    public static final String NO_AVAILABLE_SLOTS_MESSAGE = "No available time slots";
    private final List<TimeSlot> timeSlots = new ArrayList<>();
    public void addTimeSlot(TimeSlot slot) {
        timeSlots.add(slot);
    }

    public List<TimeSlot> getAvailableTimeSlots(String sportName, LocalDate date) {
        Sport sport = Sport.fromName(sportName);
        return timeSlots.stream()
                .filter(s -> s.available() && s.sport() == sport && s.date().equals(date))
                .sorted(Comparator.comparing(TimeSlot::start).thenComparing(TimeSlot::court))
                .toList();
    }
}
