package nl.tudelft.sem.template.submission.unit.models;

import nl.tudelft.sem.template.submission.Application;
import nl.tudelft.sem.template.submission.controllers.SubmissionController;
import nl.tudelft.sem.template.submission.models.RequestType;
import nl.tudelft.sem.template.submission.services.SubmissionService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
class RequestTypeTest {

    @MockBean
    private SubmissionService submissionService;
    @MockBean
    private SubmissionController submissionController;

    @Test
    void testGetValue() {
        Assertions.assertEquals("review", RequestType.REVIEW.getValue());
        assertEquals("user", RequestType.USER.getValue());
    }

    @Test
    void testToString() {
        assertEquals("review", RequestType.REVIEW.toString());
        assertEquals("user", RequestType.USER.toString());
    }
}
