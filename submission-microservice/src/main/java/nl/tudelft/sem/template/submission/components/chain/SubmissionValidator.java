package nl.tudelft.sem.template.submission.components.chain;

import nl.tudelft.sem.template.model.Submission;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public abstract class SubmissionValidator implements Validator {
    private Validator next;

    public void setNext(Validator h) {
        this.next = h;
    }

    /**
     * Runs check on the next object in chain or ends traversing if we're in
     * last object in chain.
     */
    protected ResponseEntity<?> checkNext(Submission submission, Long userId) {
        if (next == null) {
            return ResponseEntity.status(HttpStatus.OK).body(userId);
        }
        return next.handle(submission, userId);
    }
}
