package nl.tudelft.sem.template.submission.components.strategy;

import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.submission.components.chain.DeadlinePassedException;

public interface SubmissionStrategy {
    boolean checkDeadline(long trackId);

    Submission getSubmission(Submission submission);

    default void updateSubmission(Submission oldSubmission, Submission newSubmission) throws IllegalAccessException,
            DeadlinePassedException {
    }

    default void deleteSubmission(Submission submission) throws IllegalAccessException {
    }
}
