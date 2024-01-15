package nl.tudelft.sem.template.submission.components.chain;

import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.submission.components.strategy.SubmissionStrategy;
import nl.tudelft.sem.template.submission.services.HttpRequestService;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
public class DeadlineValidator extends SubmissionValidator {
    HttpRequestService httpRequestService;

    /**
     * AuthorizationValidator constructor.
     *
     * @param httpRequestService http request service
     */
    public DeadlineValidator(HttpRequestService httpRequestService) {
        this.httpRequestService = httpRequestService;
    }

    /**
     * Checks if the user is an author of the submission.
     *
     * @param submission that is being validated
     * @param userId     user id that will be returned
     * @return long which changes depending on what has happened
     */
    public SubmissionStrategy handle(SubmissionStrategy strategy,
                                     Long userId, Long trackId,
                                     Submission submission,
                                     HttpMethod requestType) throws DeadlinePassedException, IllegalAccessException {

        boolean beforeDeadline = strategy.checkDeadline(submission.getTrackId());
        if (requestType.equals(HttpMethod.PUT) && !beforeDeadline) {
            throw new DeadlinePassedException("You cannot modify submission after the deadline.");
        }

        return super.checkNext(strategy, userId, trackId, submission, requestType);
    }
}
