package nl.tudelft.sem.template.submission.components.chain;

import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.submission.components.strategy.GeneralStrategy;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
public abstract class BaseValidator implements Validator {
    private Validator next;

    public void setNext(Validator h) {
        this.next = h;
    }

    /**
     * Runs check on the next object in chain or ends traversing if we're in
     * last object in chain.
     */
    protected GeneralStrategy checkNext(GeneralStrategy strategy,
                                        Long userId, Long trackId,
                                        Submission submission,
                                        HttpMethod requestType) throws Exception {
        if (next == null) {
            return strategy;
        }
        return next.handle(strategy, userId, trackId, submission, requestType);
    }
}
