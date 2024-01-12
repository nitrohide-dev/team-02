package nl.tudelft.sem.template.submission.components;

import javassist.NotFoundException;
import org.springframework.stereotype.Component;

import java.util.UUID;

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
    protected boolean checkNext(UUID submissionId, long userId) throws IllegalAccessException, NotFoundException {
        if (next == null) {
            return true;
        }
        return next.handle(submissionId, userId);
    }
}
