package nl.tudelft.sem.template.submission.components.chain;

import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.model.Track;
import nl.tudelft.sem.template.submission.authentication.AuthManager;
import nl.tudelft.sem.template.submission.models.RequestType;
import nl.tudelft.sem.template.submission.services.HttpRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserValidator extends SubmissionValidator {
    HttpRequestService httpRequestService;

    AuthManager authManager;

    /**
     * AuthorizationValidator constructor.
     *
     * @param httpRequestService http request service
     */
    public UserValidator(HttpRequestService httpRequestService, AuthManager authManager) {
        this.httpRequestService = httpRequestService;
        this.authManager = authManager;
    }

    /**
     * Checks if the user is an author of the submission.
     *
     * @param submission that is being validated
     * @param userId       user id that will be returned
     * @return long which changes depending on what has happened
     */
    public ResponseEntity<?> handle(Submission submission, Long userId) {

        String email = authManager.getEmail();

        userId = httpRequestService.get("user/byEmail/" + email, Long.class, RequestType.USER);

        return super.checkNext(submission, userId);
    }
}
