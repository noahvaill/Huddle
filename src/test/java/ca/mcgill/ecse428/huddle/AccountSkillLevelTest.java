package ca.mcgill.ecse428.huddle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the skill levels per sport kept on an account (ID014, TID048). They are kept on the Account, not
 * on the User record, since a User is only a name and is replaced when the name changes (ID013).
 */
class AccountSkillLevelTest {

    private Account alice;

    @BeforeEach
    void setUp() {
        alice = new Account(new User("Alice Chen"), "alice.chen@mail.mcgill.ca", "Racket!2026", Role.STUDENT);
    }

    @Test
    void newAccountHasNoSkillLevels() {
        assertTrue(alice.getSkillLevels().isEmpty());
        assertTrue(alice.getSkillLevel(Sport.SQUASH).isEmpty());
    }

    @Test
    void skillLevelIsKeptPerSport() {
        alice.setSkillLevel(Sport.BADMINTON, SkillLevel.BEGINNER);
        alice.setSkillLevel(Sport.SQUASH, SkillLevel.INTERMEDIATE);

        assertEquals(SkillLevel.BEGINNER, alice.getSkillLevel(Sport.BADMINTON).orElseThrow());
        assertEquals(SkillLevel.INTERMEDIATE, alice.getSkillLevel(Sport.SQUASH).orElseThrow());
        assertTrue(alice.getSkillLevel(Sport.TENNIS).isEmpty());
    }

    @Test
    void settingASportAgainReplacesItsLevel() {
        alice.setSkillLevel(Sport.BADMINTON, SkillLevel.BEGINNER);
        alice.setSkillLevel(Sport.BADMINTON, SkillLevel.ADVANCED);

        assertEquals(Map.of(Sport.BADMINTON, SkillLevel.ADVANCED), alice.getSkillLevels());
    }

    @Test
    void skillLevelsAreListedInSportOrderAndCannotBeChangedFromOutside() {
        alice.setSkillLevel(Sport.BASKETBALL, SkillLevel.ADVANCED);
        alice.setSkillLevel(Sport.SQUASH, SkillLevel.BEGINNER);

        Map<Sport, SkillLevel> levels = alice.getSkillLevels();

        assertEquals(List.of(Sport.SQUASH, Sport.BASKETBALL), List.copyOf(levels.keySet()));
        assertThrows(UnsupportedOperationException.class, () -> levels.put(Sport.TENNIS, SkillLevel.BEGINNER));
        assertTrue(alice.getSkillLevel(Sport.TENNIS).isEmpty());
    }

    @Test
    void skillLevelsAreKeptWhenTheNameChanges() {
        alice.setSkillLevel(Sport.SQUASH, SkillLevel.INTERMEDIATE);

        alice.changeName("Alice Chen-Roy");

        assertEquals(SkillLevel.INTERMEDIATE, alice.getSkillLevel(Sport.SQUASH).orElseThrow());
    }

    @Test
    void missingSportOrLevelIsRejectedAndNothingIsSet() {
        assertThrows(NullPointerException.class, () -> alice.setSkillLevel(null, SkillLevel.BEGINNER));
        assertThrows(NullPointerException.class, () -> alice.setSkillLevel(Sport.SQUASH, null));
        assertThrows(NullPointerException.class, () -> alice.getSkillLevel(null));

        assertTrue(alice.getSkillLevels().isEmpty());
    }
}
