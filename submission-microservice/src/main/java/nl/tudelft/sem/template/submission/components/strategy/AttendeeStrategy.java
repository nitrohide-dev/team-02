package nl.tudelft.sem.template.submission.components.strategy;

import nl.tudelft.sem.template.model.Submission;

public class AttendeeStrategy implements GeneralStrategy {
    public boolean checkDeadline(long trackId) {
        return true;
    }

    /**
     * Default get method for user that is not author/reviewer.
     *
     * @param submission submission to return
     * @return submission without created/updated fields
     */
    public Submission getSubmission(Submission submission) {
        submission.setCreated(null);
        submission.setUpdated(null);
        submission.setStatus(null);
        return submission;
    }
}
