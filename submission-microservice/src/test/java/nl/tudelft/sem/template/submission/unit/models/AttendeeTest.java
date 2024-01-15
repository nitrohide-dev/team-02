package nl.tudelft.sem.template.submission.unit.models;

import nl.tudelft.sem.template.submission.Application;
import nl.tudelft.sem.template.submission.controllers.SubmissionController;
import nl.tudelft.sem.template.submission.models.Attendee;
import nl.tudelft.sem.template.submission.services.SubmissionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import nl.tudelft.sem.template.model.Role;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AttendeeTest {
    @MockBean
    private SubmissionService submissionService;
    @MockBean
    private SubmissionController submissionController;

    @Test
    void testEqualsSame() {
        Attendee attendee1 = new Attendee(1, 1, 1, Role.PC_CHAIR);

        assertTrue(attendee1.equals(attendee1));
    }

    @Test
    void testEqualsNull() {
        Attendee attendee1 = new Attendee(1, 1, 1, Role.PC_CHAIR);

        assertFalse(attendee1.equals(null));
    }

    @Test
    void testEqualsDifferentClass() {
        Attendee attendee1 = new Attendee(1, 1, 1, Role.PC_CHAIR);
        Object object = new Object();

        assertFalse(attendee1.equals(object));
    }

    @Test
    void testEqualsDifferentUserId() {
        Attendee attendee1 = new Attendee(1, 1, 1, Role.PC_CHAIR);
        Attendee attendee2 = new Attendee(2, 1, 1, Role.PC_CHAIR);

        assertFalse(attendee1.equals(attendee2));
    }

    @Test
    void testEqualsDifferentEventId() {
        Attendee attendee1 = new Attendee(1, 1, 1, Role.PC_CHAIR);
        Attendee attendee2 = new Attendee(1, 2, 1, Role.PC_CHAIR);

        assertFalse(attendee1.equals(attendee2));
    }

    @Test
    void testEqualsDifferentTrackId() {
        Attendee attendee1 = new Attendee(1, 1, 1, Role.PC_CHAIR);
        Attendee attendee2 = new Attendee(1, 1, 2, Role.PC_CHAIR);

        assertFalse(attendee1.equals(attendee2));
    }

    @Test
    void testEqualsDifferentRole() {
        Attendee attendee1 = new Attendee(1, 1, 1, Role.PC_CHAIR);
        Attendee attendee2 = new Attendee(1, 1, 1, Role.ATTENDEE);

        assertFalse(attendee1.equals(attendee2));
    }



    @Test
    void testEqualsTwoObjects() {
        Attendee attendee1 = new Attendee(1, 1, 1, Role.PC_CHAIR);
        Attendee attendee2 = new Attendee(1, 1, 1, Role.PC_CHAIR);

        assertTrue(attendee1.equals(attendee2));
    }

    @Test
    void testHashCodeSame() {
        Attendee attendee1 = new Attendee(1, 1, 1, Role.PC_CHAIR);
        Attendee attendee2 = new Attendee(1, 1, 1, Role.PC_CHAIR);

        assertEquals(attendee1.hashCode(), attendee2.hashCode());
    }

    @Test
    void testToString() {
        Attendee attendee = new Attendee(1, 1, 1, Role.PC_CHAIR);
        String expectedString = "Chair{userId=1, eventId=1, trackId=1, role=pc_chair}";

        assertEquals(expectedString, attendee.toString());
    }
}