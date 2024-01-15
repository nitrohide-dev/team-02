package nl.tudelft.sem.template.submission.components.chain;

public class DeadlinePassedException extends Exception {
    public DeadlinePassedException(String errorMessage) {
        super(errorMessage);
    }
}
