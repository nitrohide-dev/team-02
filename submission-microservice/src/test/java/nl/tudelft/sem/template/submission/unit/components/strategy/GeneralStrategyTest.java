package nl.tudelft.sem.template.submission.unit.components.strategy;

import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.model.SubmissionStatus;
import nl.tudelft.sem.template.submission.components.strategy.GeneralStrategy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GeneralStrategyTest {

    @Test
    void testCheckDeadlineTrue() {
        GeneralStrategy generalStrategy = new GeneralStrategy() {};

        boolean result = generalStrategy.checkDeadline(777L);

        assertTrue(result);
    }

    @Test
    void testGetSubmission() {
        GeneralStrategy generalStrategy = new GeneralStrategy() {};

        Submission submission = new Submission();
        submission.setCreated(null);
        submission.setUpdated(null);
        submission.setStatus(SubmissionStatus.OPEN);

        Submission result = generalStrategy.getSubmission(submission);

        assertNull(result.getCreated());
        assertNull(result.getUpdated());
        assertNull(result.getStatus());
    }

    @Test
    void testUpdateSubmission() {
        GeneralStrategy generalStrategy = new GeneralStrategy() {};

        Submission oldSubmission = new Submission();
        Submission newSubmission = new Submission();

        assertDoesNotThrow(() -> generalStrategy.updateSubmission(oldSubmission, newSubmission));
    }

    @Test
    void testDeleteSubmission() {
        GeneralStrategy generalStrategy = new GeneralStrategy() {};

        Submission submission = new Submission();

        assertDoesNotThrow(() -> generalStrategy.deleteSubmission(submission));
    }

}