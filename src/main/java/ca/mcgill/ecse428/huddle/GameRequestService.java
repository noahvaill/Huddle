package ca.mcgill.ecse428.huddle;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class GameRequestService {

    public static final String POSTED_MESSAGE = "Game request posted";
    public static final String JOINED_MESSAGE = "You have joined the game";
    public static final String WITHDRAWN_MESSAGE = "You have withdrawn from the game";
    public static final String NO_OPEN_REQUESTS_MESSAGE = "No open game requests";
    public static final String CONFIRMED_MESSAGE_FORMAT = "Game confirmed on %s on %s at %s";
    public static final String CANCELLED_NOTIFICATION_FORMAT =
            "The %s game on %s at %s has been cancelled: time slot no longer available";   // sport display name, date, start
    public static final String CANCELLED_BY_CREATOR_NOTIFICATION_FORMAT =
            "The %s game on %s at %s has been cancelled by the creator";   // sport display name, date, start
    private final Map<Sport, Integer> maxPlayers = new EnumMap<>(Map.of(
            Sport.SQUASH, 2,
            Sport.TENNIS, 2,
            Sport.PICKLEBALL, 4,
            Sport.BADMINTON, 4,
            Sport.BASKETBALL, 10));
    private final List<GameRequest> requests = new ArrayList<>();
    private final List<Notification> notifications = new ArrayList<>();
    private Consumer<GameRequest> beforeSlotCheckHook = request -> { };

    /**
     * Test hook (TID034): runs during joinRequest, just before the time slot availability is checked.
     * Lets a test simulate something happening while a join is in progress, e.g. a facility admin
     * booking the slot. Does nothing by default; null resets it.
     */
    public void setBeforeSlotCheckHook(Consumer<GameRequest> hook) {
        beforeSlotCheckHook = hook == null ? request -> { } : hook;
    }

    public void setMaxPlayers(Sport sport, int max) {
        maxPlayers.put(sport, max);
    }

    public int getMaxPlayers(Sport sport) {
        return maxPlayers.get(sport);
    }

    public GameRequest postRequest(User creator, TimeSlot slot, int groupSize, int playersNeeded) {
        if (!slot.available()) {
            throw new InvalidGameRequestException(InvalidGameRequestException.SLOT_NOT_AVAILABLE_MESSAGE);
        }
        if (playersNeeded < 1) {
            throw new InvalidGameRequestException(InvalidGameRequestException.NOT_ENOUGH_PLAYERS_MESSAGE);
        }
        int max = getMaxPlayers(slot.sport());
        if (groupSize + playersNeeded > max) {
            int allowed = max - groupSize;
            throw new InvalidGameRequestException(slot.sport().getDisplayName() + " allows at most "
                    + allowed + (allowed == 1 ? " more player" : " more players"));
        }
        if (hasOverlappingGame(creator, slot)) {
            throw new InvalidGameRequestException(InvalidGameRequestException.OVERLAPPING_GAME_MESSAGE);
        }
        GameRequest request = new GameRequest(nextId(), creator, slot, groupSize, playersNeeded);
        requests.add(request);
        return request;
    }

    /** Adds an existing request as is (e.g. R1–R6 from a feature background), without the posting rules. */
    public void addRequest(GameRequest request) {
        Objects.requireNonNull(request, "Game request is required");
        if (findById(request.getId()).isPresent()) {
            throw new IllegalArgumentException("Game request id already exists: " + request.getId());
        }
        requests.add(request);
    }

    /**
     * Adds user to the request's participants and takes one spot. When that was the last spot (ID010),
     * the time slot is booked and the request becomes Confirmed. Returns the updated request.
     */
    public GameRequest joinRequest(User user, String requestId) {
        Objects.requireNonNull(user, "User is required");
        Objects.requireNonNull(requestId, "Game request id is required");
        GameRequest request = findById(requestId)
                .orElseThrow(() -> new InvalidGameRequestException(InvalidGameRequestException.REQUEST_NOT_FOUND_MESSAGE));
        if (request.isCreatedBy(user)) {
            throw new InvalidGameRequestException(InvalidGameRequestException.OWN_REQUEST_MESSAGE);
        }
        if (request.hasParticipant(user)) {
            throw new InvalidGameRequestException(InvalidGameRequestException.ALREADY_JOINED_MESSAGE);
        }
        if (request.isFull()) {
            throw new InvalidGameRequestException(InvalidGameRequestException.GAME_FULL_MESSAGE);
        }
        if (!request.isOpen()) {
            throw new InvalidGameRequestException(InvalidGameRequestException.NOT_OPEN_MESSAGE);
        }
        beforeSlotCheckHook.accept(request);
        if (!request.getTimeSlot().available()) {
            request.setStatus(GameRequestStatus.CANCELLED);
            TimeSlot slot = request.getTimeSlot();
            notifications.add(new Notification(request.getCreator(),
                    String.format(CANCELLED_NOTIFICATION_FORMAT, slot.sport().getDisplayName(), slot.date(), slot.start())));
            throw new InvalidGameRequestException(InvalidGameRequestException.SLOT_NO_LONGER_AVAILABLE_MESSAGE);
        }
        if (hasOverlappingGame(user, request.getTimeSlot())) {
            throw new InvalidGameRequestException(InvalidGameRequestException.OVERLAPPING_GAME_MESSAGE);
        }
        boolean takesLastSpot = request.getSpotsRemaining() == 1;
        if (takesLastSpot) {
            // Book first: if the slot cannot be booked, nothing about the request has changed yet
            request.getTimeSlot().book();
        }
        request.addParticipant(user);
        request.setSpotsRemaining(request.getSpotsRemaining() - 1);
        if (takesLastSpot) {
            request.setStatus(GameRequestStatus.CONFIRMED);
        }
        return request;
    }
    
    /**
     * Removes user from the request's participants and frees one spot (ID008). The creator cannot withdraw,
     * and only a participant can. When the game was Confirmed, it reopens and its time slot is released,
     * so the game can fill up and be confirmed again. Returns the updated request.
     */
    public GameRequest withdrawFromRequest(User user, String requestId) {
        Objects.requireNonNull(user, "User is required");
        Objects.requireNonNull(requestId, "Game request id is required");
        GameRequest request = findById(requestId)
                .orElseThrow(() -> new InvalidGameRequestException(InvalidGameRequestException.REQUEST_NOT_FOUND_MESSAGE));
        if (request.isCreatedBy(user)) {
            throw new InvalidGameRequestException(InvalidGameRequestException.CREATOR_CANNOT_WITHDRAW_MESSAGE);
        }
        if (!request.hasParticipant(user)) {
            throw new InvalidGameRequestException(InvalidGameRequestException.NOT_A_PARTICIPANT_MESSAGE);
        }
        request.removeParticipant(user);
        request.setSpotsRemaining(request.getSpotsRemaining() + 1);
        if (request.getStatus() == GameRequestStatus.CONFIRMED) {
            request.setStatus(GameRequestStatus.OPEN);
            // The slot was booked when the game was confirmed; free it, otherwise the next join would cancel the game
            if (!request.getTimeSlot().available()) {
                request.getTimeSlot().release();
            }
        }
        return request;
    }

    /**
     * Cancels a game request (ID009, TID051), retaining it and its participants for lookup.
     * Every joined participant is notified that the creator cancelled the game (TID052); the creator
     * is not. A confirmed game's booked time slot is released (TID053). Repeated cancellation returns
     * the request unchanged and sends nothing new. Creator-only validation is added by TID054.
     */
    public GameRequest cancelRequest(User user, String requestId) {
        Objects.requireNonNull(user, "User is required");
        Objects.requireNonNull(requestId, "Game request id is required");
        GameRequest request = findById(requestId)
                .orElseThrow(() -> new InvalidGameRequestException(InvalidGameRequestException.REQUEST_NOT_FOUND_MESSAGE));
        if (request.getStatus() != GameRequestStatus.CANCELLED) {
            if (request.getStatus() == GameRequestStatus.CONFIRMED
                    && !request.getTimeSlot().available()) {
                request.getTimeSlot().release();
            }
            request.setStatus(GameRequestStatus.CANCELLED);
            TimeSlot slot = request.getTimeSlot();
            for (User participant : request.getParticipants()) {
                if (!request.isCreatedBy(participant)) {
                    notifications.add(new Notification(participant, String.format(CANCELLED_BY_CREATOR_NOTIFICATION_FORMAT,
                            slot.sport().getDisplayName(), slot.date(), slot.start())));
                }
            }
        }
        return request;
    }

    /** The message issued when a join confirms the game, e.g. "Game confirmed on Squash Court 1 on 2026-10-15 at 18:00". */
    public static String confirmationMessage(GameRequest request) {
        TimeSlot slot = request.getTimeSlot();
        return String.format(CONFIRMED_MESSAGE_FORMAT, slot.court().name(), slot.date(), slot.start());
    }

    /** Messages sent to this user, oldest first. Empty list if none or if user is null. */
    public List<String> getNotificationsFor(User user) {
        if (user == null) {
            return List.of();
        }
        return notifications.stream()
                .filter(n -> user.equals(n.recipient()))
                .map(Notification::message)
                .toList();
    }

    private String nextId() {
        int n = requests.size() + 1;
        while (findById("R" + n).isPresent()) {
            n++;
        }
        return "R" + n;
    }

    // A user cannot be in two open or confirmed games whose time slots overlap on the same day
    private boolean hasOverlappingGame(User user, TimeSlot slot) {
        return requests.stream()
                .filter(r -> r.getStatus() == GameRequestStatus.OPEN || r.getStatus() == GameRequestStatus.CONFIRMED)
                .filter(r -> r.hasParticipant(user))
                .map(GameRequest::getTimeSlot)
                .anyMatch(other -> other.date().equals(slot.date())
                        && slot.start().isBefore(other.end())
                        && other.start().isBefore(slot.end()));
    }

    public List<GameRequest> getRequests() {
        return List.copyOf(requests);
    }

    public List<GameRequest> getOpenGameRequests(User currentUser, String sportName) {
        Sport sport = Sport.fromName(sportName);
        Objects.requireNonNull(currentUser, "User is required");
        return requests.stream()
                .filter(r -> r.getSport() == sport && r.isOpen() && !r.isCreatedBy(currentUser))
                .toList();
    }

    public Optional<GameRequest> findById(String id) {
        return requests.stream().filter(r -> r.getId().equals(id)).findFirst();
    }

    public Optional<GameRequest> findByTimeSlot(TimeSlot slot) {
        return requests.stream().filter(r -> r.getTimeSlot().equals(slot)).findFirst();
    }
}
