package nl.tudelft.sem.template.submission.unit.models;

import nl.tudelft.sem.template.model.Role;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ChairTest {

    @MockBean
    private SubmissionService submissionService;
    @MockBean
    private SubmissionController submissionController;
    private Long id = 0L;

    @Test
    void testGetUserId() {
        Attendee chair = new Attendee(id, 1L, 1L, 1L, Role.PC_CHAIR);
        assertEquals(1L, chair.getUserId());
    }

    @Test
    void testGetEventId() {
        Attendee chair = new Attendee(id, 1L, 1L, 1L, Role.PC_CHAIR);
        assertEquals(1L, chair.getEventId());
    }

    @Test
    void testGetTrackId() {
        Attendee chair = new Attendee(id, 1L, 1L, 1L, Role.PC_CHAIR);
        assertEquals(1L, chair.getTrackId());
    }

    @Test
    void testGetRole() {
        Attendee chair = new Attendee(id, 1L, 1L, 1L, Role.GENERAL_CHAIR);
        assertEquals(Role.GENERAL_CHAIR, chair.getRole());
    }

    @Test
    void testEquals() {
        Attendee chair1 = new Attendee(id, 1L, 1L, 1L, Role.GENERAL_CHAIR);
        Attendee chair2 = new Attendee(id, 1L, 1L, 1L, Role.GENERAL_CHAIR);
        Attendee chair3 = new Attendee(id, 2L, 2L, 2L, Role.GENERAL_CHAIR);

        assertEquals(chair1, chair2);
        assertNotEquals(chair1, chair3);
    }

    @Test
    void testHashCode() {
        Attendee chair1 = new Attendee(id, 1L, 1L, 1L, Role.PC_CHAIR);
        Attendee chair2 = new Attendee(id, 1L, 1L, 1L, Role.PC_CHAIR);
        Attendee chair3 = new Attendee(id, 1L, 1L, 1L, Role.GENERAL_CHAIR);

        assertEquals(chair1.hashCode(), chair2.hashCode());
        assertNotEquals(chair1.hashCode(), chair3.hashCode());
    }

    @Test
    void testToString() {
        Attendee chair = new Attendee(id, 1L, 1L, 1L, Role.PC_CHAIR);

        String expected = "Chair{userId=1, eventId=1, trackId=1, role=pc_chair}";
        assertEquals(expected, chair.toString());
    }
}
