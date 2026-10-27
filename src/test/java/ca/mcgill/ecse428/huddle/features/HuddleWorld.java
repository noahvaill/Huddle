package ca.mcgill.ecse428.huddle.features;

import ca.mcgill.ecse428.huddle.GameRequestService;
import ca.mcgill.ecse428.huddle.TimeSlot;
import ca.mcgill.ecse428.huddle.TimeSlotService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * State shared by all step definition classes during one scenario.
 * Picocontainer creates a new instance per scenario and injects it through the step class constructors.
 */
public class HuddleWorld {

    public TimeSlotService timeSlotService = new TimeSlotService();
    public GameRequestService gameRequestService = new GameRequestService();
    public Map<String, TimeSlot> timeSlotsById = new HashMap<>();
    public String loggedInUser;
    public List<String> messages = new ArrayList<>(); // a join that fills the last spot issues two messages (ID010)
    public String errorMessage;
}
