package nl.tudelft.sem.template.submission.components.chain;

import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.submission.components.strategy.GeneralStrategy;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
public class DeadlineValidator extends BaseValidator {

    /**
     * Checks if the user is an author of the submission.
     *
     * @param submission that is being validated
     * @param userId     user id that will be returned
     * @return long which changes depending on what has happened
     */
    public GeneralStrategy handle(GeneralStrategy strategy,
                                  Long userId, Long trackId,
                                  Submission submission,
                                  HttpMethod requestType) throws Exception {

        boolean beforeDeadline = strategy.checkDeadline(submission.getTrackId());
        if ((requestType.equals(HttpMethod.PUT) || requestType.equals(HttpMethod.POST)) && !beforeDeadline) {
            throw new DeadlinePassedException("You cannot modify submission after the deadline.");
        }

        return super.checkNext(strategy, userId, trackId, submission, requestType);
    }
}
