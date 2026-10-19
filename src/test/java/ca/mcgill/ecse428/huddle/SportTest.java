package ca.mcgill.ecse428.huddle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class SportTest {

    @Test
    void everyOfferedSportIsFoundByItsName() {
        for (Sport s : Sport.values()) {
            assertEquals(s, Sport.fromName(s.getDisplayName()));
        }
    }

    @Test
    void nameMatchIgnoresCaseAndSpaces() {
        assertEquals(Sport.TENNIS, Sport.fromName("  tennis "));
        assertEquals(Sport.PICKLEBALL, Sport.fromName("PICKLEBALL"));
    }

    @Test
    void unknownSportThrowsSportNotOffered() {
        SportNotOfferedException e =
                assertThrows(SportNotOfferedException.class, () -> Sport.fromName("Basketball"));
        assertEquals("Sport not offered", e.getMessage());
        assertEquals("Basketball", e.getRequestedSport());
    }

    @Test
    void nullOrBlankThrowsSportNotOffered() {
        assertThrows(SportNotOfferedException.class, () -> Sport.fromName(null));
        assertThrows(SportNotOfferedException.class, () -> Sport.fromName("   "));
    }

    @Test
    void offeredSportNamesListsAllSports() {
        assertEquals(List.of("Squash", "Tennis", "Pickleball"), Sport.offeredSportNames());
    }
}
