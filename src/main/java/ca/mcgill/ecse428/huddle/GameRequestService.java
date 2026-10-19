package ca.mcgill.ecse428.huddle;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GameRequestService {

    public static final String POSTED_MESSAGE = "Game request posted";
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

    public GameRequest postRequest(String creator, TimeSlot slot, int groupSize, int playersNeeded) {
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
        GameRequest request = new GameRequest("R" + (requests.size() + 1), creator, slot, groupSize, playersNeeded);
        requests.add(request);
        return request;
    }

    // A user cannot be in two open or confirmed games whose time slots overlap on the same day
    private boolean hasOverlappingGame(String user, TimeSlot slot) {
        return requests.stream()
                .filter(r -> r.getStatus() == GameRequestStatus.OPEN || r.getStatus() == GameRequestStatus.CONFIRMED)
                .filter(r -> r.getParticipants().contains(user))
                .map(GameRequest::getTimeSlot)
                .anyMatch(other -> other.date().equals(slot.date())
                        && slot.start().isBefore(other.end())
                        && other.start().isBefore(slot.end()));
    }

    public List<GameRequest> getRequests() {
        return List.copyOf(requests);
    }

    public Optional<GameRequest> findById(String id) {
        return requests.stream().filter(r -> r.getId().equals(id)).findFirst();
    }

    public Optional<GameRequest> findByTimeSlot(TimeSlot slot) {
        return requests.stream().filter(r -> r.getTimeSlot().equals(slot)).findFirst();
    }
}
