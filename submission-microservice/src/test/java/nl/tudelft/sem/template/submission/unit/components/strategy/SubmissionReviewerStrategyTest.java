package nl.tudelft.sem.template.submission.unit.components.strategy;

import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.model.SubmissionStatus;
import nl.tudelft.sem.template.model.Track;
import nl.tudelft.sem.template.submission.Application;
import nl.tudelft.sem.template.submission.components.chain.DeadlinePassedException;
import nl.tudelft.sem.template.submission.components.strategy.SubmissionReviewerStrategy;
import nl.tudelft.sem.template.submission.controllers.SubmissionController;
import nl.tudelft.sem.template.submission.repositories.SubmissionRepository;
import nl.tudelft.sem.template.submission.services.SubmissionService;
import nl.tudelft.sem.template.submission.services.TrackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class SubmissionReviewerStrategyTest {
    @MockBean
    private SubmissionService submissionService;
    @MockBean
    private SubmissionController submissionController;
    @MockBean
    private SubmissionRepository submissionRepository;
    @MockBean
    private TrackService trackService;

    @Autowired
    @InjectMocks
    private SubmissionReviewerStrategy submissionReviewerStrategy;

    @Test
    void testCheckDeadline() {
        Track track = new Track();
        track.setReviewDeadline("1968-01-01T00:00:00");
        track.setSubmitDeadline("1968-01-01T00:00:00");

        when(trackService.getTrackById(anyLong())).thenReturn(track);

        boolean result = submissionReviewerStrategy.checkDeadline(123L);
        assertFalse(result);
    }

    @Test
    void testCheckDeadlineSecondCase() {
        Track track = new Track();
        track.setReviewDeadline("2026-01-01T00:00:00");
        track.setSubmitDeadline("2026-01-01T00:00:00");

        when(trackService.getTrackById(anyLong())).thenReturn(track);

        boolean result = submissionReviewerStrategy.checkDeadline(198L);
        assertFalse(result);
    }

    @Test
    void testCheckDeadlineEqual() {
        Track track = new Track();
        track.setReviewDeadline("2026-01-01T00:00:00");
        track.setSubmitDeadline("1968-01-01T00:00:00");

        when(trackService.getTrackById(anyLong())).thenReturn(track);

        boolean result = submissionReviewerStrategy.checkDeadline(422L);
        assertTrue(result);
    }


    @Test
    void testGetSubmission() {
        Submission submission = new Submission();
        submission.setCreated(LocalDateTime.now());
        submission.setUpdated(LocalDateTime.now());

        Submission result = submissionReviewerStrategy.getSubmission(submission);

        assertNull(result.getCreated());
        assertNull(result.getUpdated());
    }


    @Test
    void testUpdateSubmission() throws DeadlinePassedException {
        Submission oldSubmission = new Submission();
        Submission newSubmission = new Submission();
        newSubmission.setStatus(SubmissionStatus.ACCEPTED);

        submissionReviewerStrategy.updateSubmission(oldSubmission, newSubmission);

        assertEquals(SubmissionStatus.ACCEPTED, oldSubmission.getStatus());
        verify(submissionRepository, times(1)).save(oldSubmission);
    }

}