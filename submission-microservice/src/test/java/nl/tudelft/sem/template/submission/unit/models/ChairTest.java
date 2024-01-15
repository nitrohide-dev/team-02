package nl.tudelft.sem.template.submission.unit.models;

import nl.tudelft.sem.template.model.Role;
import nl.tudelft.sem.template.submission.Application;
import nl.tudelft.sem.template.submission.controllers.SubmissionController;
import nl.tudelft.sem.template.submission.models.Attendee;
import nl.tudelft.sem.template.submission.services.SubmissionService;
import org.junit.jupiter.api.BeforeAll;
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

    static Attendee pChair;
    static Attendee pChair2;
    static Attendee gChair;
    static Attendee gChair2;
    static Attendee gChair3;

    @BeforeAll
    static void setUp() {
        pChair = new Attendee(1L, 1L, 1L, Role.PC_CHAIR);
        pChair2 = new Attendee(1L, 1L, 1L, Role.PC_CHAIR);
        gChair = new Attendee(1L, 1L, 1L, Role.GENERAL_CHAIR);
        gChair2 = new Attendee(1L, 1L, 1L, Role.GENERAL_CHAIR);
        gChair3 = new Attendee(2L, 2L, 2L, Role.GENERAL_CHAIR);

    }

    @Test
    void testGetUserId() {
        assertEquals(1L, pChair.getUserId());
    }

    @Test
    void testGetEventId() {
        assertEquals(1L, pChair.getEventId());
    }

    @Test
    void testGetTrackId() {
        assertEquals(1L, pChair.getTrackId());
    }

    @Test
    void testGetRole() {
        assertEquals(Role.GENERAL_CHAIR, gChair.getRole());
    }

    @Test
    void testEquals() {

        assertEquals(gChair, gChair2);
        assertNotEquals(gChair, gChair3);
    }

    @Test
    void testHashCode() {

        assertEquals(pChair.hashCode(), pChair2.hashCode());
        assertNotEquals(pChair.hashCode(), gChair.hashCode());
    }

    @Test
    void testToString() {

        String expected = "Chair{userId=1, eventId=1, trackId=1, role=pc_chair}";
        assertEquals(expected, pChair.toString());
    }
}
