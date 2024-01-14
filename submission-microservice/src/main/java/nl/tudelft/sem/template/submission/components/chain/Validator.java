package nl.tudelft.sem.template.submission.components.chain;

import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.submission.components.strategy.SubmissionStrategy;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
public interface Validator {
    void setNext(Validator handler);

    SubmissionStrategy handle(SubmissionStrategy strategy, Long userId, Submission submission, HttpMethod requestType)
            throws DeadlinePassedException, IllegalAccessException;
}
