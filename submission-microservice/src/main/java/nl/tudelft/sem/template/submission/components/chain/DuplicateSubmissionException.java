package nl.tudelft.sem.template.submission.components.chain;

public class DuplicateSubmissionException extends Exception {
    public DuplicateSubmissionException(String errorMessage) {
        super(errorMessage);
    }
}
