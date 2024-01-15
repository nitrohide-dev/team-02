package nl.tudelft.sem.template.submission.unit.components.chain;

import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.submission.Application;
import nl.tudelft.sem.template.submission.components.chain.DeadlineValidator;
import nl.tudelft.sem.template.submission.components.strategy.SubmissionStrategy;
import nl.tudelft.sem.template.submission.controllers.SubmissionController;
import nl.tudelft.sem.template.submission.services.SubmissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import nl.tudelft.sem.template.submission.components.chain.DeadlinePassedException;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class DeadlineValidatorTest {

    @MockBean
    private SubmissionService submissionService;
    @MockBean
    private SubmissionController submissionController;

    @Autowired
    @InjectMocks
    private DeadlineValidator deadlineValidator;

    private Submission submission;

    @BeforeEach
    void setUp() {
        submission = new Submission();
        submission.setTrackId(14L);
    }

    @Test
    void handle_ValidSubmissionBeforeDeadline_ReturnsNextStrategy() throws Exception {

        SubmissionStrategy nextStrategy = mock(SubmissionStrategy.class);
        HttpMethod requestType = HttpMethod.PUT;
        when(nextStrategy.checkDeadline(14L)).thenReturn(true);
        SubmissionStrategy result = deadlineValidator.handle(nextStrategy,
                123L, submission.getTrackId(), submission, requestType);

        //assert
        assertEquals(nextStrategy, result);
        verify(nextStrategy).checkDeadline(submission.getTrackId());
        verifyNoMoreInteractions(nextStrategy);
    }

    @Test
    void handle_ModifySubmissionAfterDeadline_ThrowsDeadlinePassedException() throws DeadlinePassedException {
        SubmissionStrategy nextStrategy = mock(SubmissionStrategy.class);
        HttpMethod requestType = HttpMethod.PUT;
        when(nextStrategy.checkDeadline(anyLong())).thenReturn(false);

        // assert
        assertThrows(DeadlinePassedException.class, () ->
                deadlineValidator.handle(nextStrategy, 123L, submission.getTrackId(), submission, requestType));

        verify(nextStrategy).checkDeadline(submission.getTrackId());
        verifyNoMoreInteractions(nextStrategy);
    }
}
