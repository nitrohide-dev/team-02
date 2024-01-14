package nl.tudelft.sem.template.submission.unit.models;

import nl.tudelft.sem.template.submission.Application;
import nl.tudelft.sem.template.submission.controllers.SubmissionController;
import nl.tudelft.sem.template.submission.models.Chair;
import nl.tudelft.sem.template.submission.services.SubmissionService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import nl.tudelft.sem.template.model.Role;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ChairTest {

    @MockBean
    private SubmissionService submissionService;
    @MockBean
    private SubmissionController submissionController;

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