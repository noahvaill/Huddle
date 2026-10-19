package ca.mcgill.ecse428.huddle;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class GameRequestService {

    public static final String POSTED_MESSAGE = "Game request posted";
    public static final String JOINED_MESSAGE = "You have joined the game";
    public static final String NO_OPEN_REQUESTS_MESSAGE = "No open game requests";
    private final Map<Sport, Integer> maxPlayers = new EnumMap<>(Map.of(
            Sport.SQUASH, 2,
            Sport.TENNIS, 2,
            Sport.PICKLEBALL, 4,
            Sport.BADMINTON, 4,
            Sport.BASKETBALL, 10));
    private final List<GameRequest> requests = new ArrayList<>();

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

    /** Adds user to the request's participants and takes one spot. Returns the updated request. */
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
        if (hasOverlappingGame(user, request.getTimeSlot())) {
            throw new InvalidGameRequestException(InvalidGameRequestException.OVERLAPPING_GAME_MESSAGE);
        }
        request.addParticipant(user);
        request.setSpotsRemaining(request.getSpotsRemaining() - 1);
        return request;
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
