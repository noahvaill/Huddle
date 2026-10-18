package ca.mcgill.ecse428.huddle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HuddleTest {

    @Test
    void nameIsHuddle() {
        assertEquals("Huddle", Huddle.name());
    }
}
