package nl.tudelft.sem.template.submission.components.chain;

import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.submission.components.strategy.GeneralStrategy;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
public interface Validator {
    void setNext(Validator handler);

    GeneralStrategy handle(GeneralStrategy strategy,
                              Long userId, Long trackId,
                              Submission submission,
                              HttpMethod requestType)
            throws Exception;
}
