package ca.mcgill.ecse428.huddle;

import java.util.ArrayList;
import java.util.List;

/** An open game request posted for a time slot. Mutable so later stories can join, confirm or cancel it. */
public class GameRequest {

    private final String id;
    private final String creator;
    private final TimeSlot timeSlot;
    private final int groupSize; // players already on the creator's side, 1 for a solo player
    private int spotsRemaining;
    private GameRequestStatus status;
    private final List<String> participants = new ArrayList<>();

    public GameRequest(String id, String creator, TimeSlot timeSlot, int groupSize, int spotsRemaining) {
        this.id = id;
        this.creator = creator;
        this.timeSlot = timeSlot;
        this.groupSize = groupSize;
        this.spotsRemaining = spotsRemaining;
        this.status = GameRequestStatus.OPEN;
        participants.add(creator);
    }

    public String getId() { return id; }
    public String getCreator() { return creator; }
    public TimeSlot getTimeSlot() { return timeSlot; }
    public int getGroupSize() { return groupSize; }
    public int getSpotsRemaining() { return spotsRemaining; }
    public GameRequestStatus getStatus() { return status; }
    public List<String> getParticipants() { return List.copyOf(participants); }

    public void setSpotsRemaining(int spotsRemaining) { this.spotsRemaining = spotsRemaining; }
    public void setStatus(GameRequestStatus status) { this.status = status; }
    public void addParticipant(String participant) { participants.add(participant); }
}
