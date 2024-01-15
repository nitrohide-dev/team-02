package nl.tudelft.sem.template.submission.components.chain;

import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.submission.components.strategy.SubmissionStrategy;
import org.springframework.http.HttpMethod;
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
    protected SubmissionStrategy checkNext(SubmissionStrategy strategy, Long userId, Submission submission,
                                           HttpMethod requestType) throws DeadlinePassedException, IllegalAccessException {
        if (next == null) {
            return strategy;
        }
        return next.handle(strategy, userId, submission, requestType);
    }
}
