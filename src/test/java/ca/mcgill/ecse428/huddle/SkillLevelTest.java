package ca.mcgill.ecse428.huddle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for the offered skill levels (ID014, TID048). */
class SkillLevelTest {

    @Test
    void offeredLevelsMatchTheFeatureFileInOrder() {
        assertEquals(List.of("Beginner", "Intermediate", "Advanced"), SkillLevel.offeredLevelNames());
    }

    @Test
    void fromNameIgnoresCaseAndSurroundingSpaces() {
        assertEquals(SkillLevel.BEGINNER, SkillLevel.fromName("Beginner"));
        assertEquals(SkillLevel.INTERMEDIATE, SkillLevel.fromName("  intermediate "));
        assertEquals(SkillLevel.ADVANCED, SkillLevel.fromName("ADVANCED"));
    }

    @Test
    void unknownOrMissingLevelIsRejected() {
        assertTrue(assertThrows(IllegalArgumentException.class, () -> SkillLevel.fromName("Expert"))
                .getMessage().contains("Expert"));
        assertThrows(IllegalArgumentException.class, () -> SkillLevel.fromName(null));
    }
}
