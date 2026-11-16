package ca.mcgill.ecse428.huddle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RoleTest {

    @Test
    void displayNamesMatchTheFeatureFiles() {
        assertEquals("Student", Role.STUDENT.getDisplayName());
        assertEquals("Facility Administrator", Role.FACILITY_ADMINISTRATOR.getDisplayName());
    }

    @Test
    void fromNameIgnoresCaseAndSurroundingSpaces() {
        assertEquals(Role.STUDENT, Role.fromName("Student"));
        assertEquals(Role.FACILITY_ADMINISTRATOR, Role.fromName("  facility administrator "));
    }

    @Test
    void unknownOrMissingRoleIsRejected() {
        assertTrue(assertThrows(IllegalArgumentException.class, () -> Role.fromName("Coach"))
                .getMessage().contains("Coach"));
        assertThrows(IllegalArgumentException.class, () -> Role.fromName(null));
    }
}
