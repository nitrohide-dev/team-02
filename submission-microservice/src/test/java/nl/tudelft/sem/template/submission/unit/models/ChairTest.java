package nl.tudelft.sem.template.submission.unit.models;

import nl.tudelft.sem.template.submission.models.Chair;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import nl.tudelft.sem.template.model.Role;

class ChairTest {

    @Test
    void testGetUserId() {
        Chair chair = new Chair(1L, 1L, 1L, Role.PC_CHAIR);
        assertEquals(1L, chair.getUserId());
    }

    @Test
    void testGetEventId() {
        Chair chair = new Chair(1L, 1L, 1L, Role.PC_CHAIR);
        assertEquals(1L, chair.getEventId());
    }

    @Test
    void testGetTrackId() {
        Chair chair = new Chair(1L, 1L, 1L, Role.PC_CHAIR);
        assertEquals(1L, chair.getTrackId());
    }

    @Test
    void testGetRole() {
        Chair chair = new Chair(1L, 1L, 1L, Role.GENERAL_CHAIR);
        assertEquals(Role.GENERAL_CHAIR, chair.getRole());
    }
    @Test
    void testEquals() {
        Chair chair1 = new Chair(1L, 1L, 1L, Role.GENERAL_CHAIR);
        Chair chair2 = new Chair(1L, 1L, 1L, Role.GENERAL_CHAIR);
        Chair chair3 = new Chair(2L, 2L, 2L, Role.GENERAL_CHAIR);

        assertEquals(chair1, chair2);
        assertNotEquals(chair1, chair3);
    }

    @Test
    void testHashCode() {
        Chair chair1 = new Chair(1L, 1L, 1L, Role.PC_CHAIR);
        Chair chair2 = new Chair(1L, 1L, 1L, Role.PC_CHAIR);
        Chair chair3 = new Chair(1L, 1L, 1L, Role.GENERAL_CHAIR);

        assertEquals(chair1.hashCode(), chair2.hashCode());
        assertNotEquals(chair1.hashCode(), chair3.hashCode());
    }

    @Test
    void testToString() {
        Chair chair = new Chair(1L, 1L, 1L, Role.PC_CHAIR);

        String expected = "Chair{userId=1, eventId=1, trackId=1, role=pc_chair}";
        assertEquals(expected, chair.toString());
    }
}