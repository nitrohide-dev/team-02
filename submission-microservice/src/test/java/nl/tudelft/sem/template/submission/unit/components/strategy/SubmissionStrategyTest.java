package nl.tudelft.sem.template.submission.unit.components.strategy;

import javassist.NotFoundException;
import nl.tudelft.sem.template.model.Statistics;
import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.model.SubmissionStatus;
import nl.tudelft.sem.template.model.Track;
import nl.tudelft.sem.template.submission.components.chain.DeadlinePassedException;
import nl.tudelft.sem.template.submission.components.strategy.SubmissionStrategy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SubmissionStrategyTest {

    @Test
    void testCheckDeadlineTrue() {
        SubmissionStrategy submissionStrategy = new SubmissionStrategy() {};

        boolean result = submissionStrategy.checkDeadline(777L);

        assertTrue(result);
    }

    @Test
    void testGetSubmission() {
        SubmissionStrategy submissionStrategy = new SubmissionStrategy() {};

        Submission submission = new Submission();
        submission.setCreated(null);
        submission.setUpdated(null);
        submission.setStatus(SubmissionStatus.OPEN);

        Submission result = submissionStrategy.getSubmission(submission);

        assertNull(result.getCreated());
        assertNull(result.getUpdated());
        assertNull(result.getStatus());
    }

    @Test
    void testUpdateSubmission() {
        SubmissionStrategy submissionStrategy = new SubmissionStrategy() {};

        Submission oldSubmission = new Submission();
        Submission newSubmission = new Submission();

        assertDoesNotThrow(() -> submissionStrategy.updateSubmission(oldSubmission, newSubmission));
    }

    @Test
    void testDeleteSubmission() {
        SubmissionStrategy submissionStrategy = new SubmissionStrategy() {};

        Submission submission = new Submission();

        assertDoesNotThrow(() -> submissionStrategy.deleteSubmission(submission));
    }

}