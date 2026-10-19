package ca.mcgill.ecse428.huddle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** An open game request posted for a time slot. Mutable so later stories can join, confirm or cancel it. */
public class GameRequest {

    private final String id;
    private final User creator;
    private final TimeSlot timeSlot;
    private final int groupSize; // players already on the creator's side, 1 for a solo player
    private int spotsRemaining;
    private GameRequestStatus status;
    private List<User> participants;

    /** A newly posted request: status OPEN, participants = [creator]. Signature used by GameRequestService. */
    public GameRequest(String id, User creator, TimeSlot timeSlot, int groupSize, int spotsRemaining) {
        this(id, creator, timeSlot, groupSize, spotsRemaining, GameRequestStatus.OPEN,
                creator == null ? null : List.of(creator));
    }

    /** Any existing state; used by tests and step definitions to set up requests like R1–R6 in the features. */
    public GameRequest(String id, User creator, TimeSlot timeSlot, int groupSize, int spotsRemaining,
                       GameRequestStatus status, List<User> participants) {
        Objects.requireNonNull(id, "Game request id is required");
        String strippedId = id.strip();
        if (strippedId.isEmpty()) {
            throw new IllegalArgumentException("Game request id must not be blank");
        }
        Objects.requireNonNull(creator, "Creator is required");
        Objects.requireNonNull(timeSlot, "Time slot is required");
        Objects.requireNonNull(status, "Status is required");
        Objects.requireNonNull(participants, "Participants are required");
        if (groupSize < 1) {
            throw new IllegalArgumentException("Group size must be at least 1");
        }
        if (spotsRemaining < 0) {
            throw new IllegalArgumentException("Spots remaining must not be negative");
        }
        if (participants.isEmpty()) {
            throw new IllegalArgumentException("Participants must not be empty");
        }
        if (participants.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Participants must not contain null");
        }
        if (!participants.get(0).equals(creator)) {
            throw new IllegalArgumentException("The creator must be the first participant");
        }
        Set<User> seen = new HashSet<>();
        for (User participant : participants) {
            if (!seen.add(participant)) {
                throw new IllegalArgumentException("A user cannot participate twice");
            }
        }
        this.id = strippedId;
        this.creator = creator;
        this.timeSlot = timeSlot;
        this.groupSize = groupSize;
        this.spotsRemaining = spotsRemaining;
        this.status = status;
        this.participants = new ArrayList<>(participants);
    }

    public String getId() { return id; }
    public User getCreator() { return creator; }
    public TimeSlot getTimeSlot() { return timeSlot; }
    public int getGroupSize() { return groupSize; }
    public int getSpotsRemaining() { return spotsRemaining; }
    public GameRequestStatus getStatus() { return status; }
    public List<User> getParticipants() { return List.copyOf(participants); }

    public void setSpotsRemaining(int spotsRemaining) {
        if (spotsRemaining < 0) {
            throw new IllegalArgumentException("Spots remaining must not be negative");
        }
        this.spotsRemaining = spotsRemaining;
    }

    public void setStatus(GameRequestStatus status) {
        this.status = Objects.requireNonNull(status, "Status is required");
    }

    public void addParticipant(User user) {
        Objects.requireNonNull(user, "Participant is required");
        if (participants.contains(user)) {
            throw new IllegalArgumentException("A user cannot participate twice");
        }
        participants.add(user);
    }

    public int getParticipantCount() { return groupSize + participants.size() - 1; }
    public Sport getSport() { return timeSlot.sport(); }
    public boolean isOpen() { return status == GameRequestStatus.OPEN; }
    public boolean isFull() { return spotsRemaining == 0; }
    public boolean isCreatedBy(User user) { return creator.equals(user); }
    public boolean hasParticipant(User user) { return participants.contains(user); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameRequest other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }

    @Override
    public String toString() {
        return id + " by " + creator + ", " + getSport().getDisplayName() + " "
                + timeSlot.date() + " " + timeSlot.start() + ", "
                + spotsRemaining + " spot(s) left, " + status.getDisplayName();
    }
}
