package nl.tudelft.sem.template.submission.components.chain;

import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.model.Track;
import nl.tudelft.sem.template.submission.models.RequestType;
import nl.tudelft.sem.template.submission.repositories.SubmissionRepository;
import nl.tudelft.sem.template.submission.services.HttpRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

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
     * @param userId       user id that will be returned
     * @return long which changes depending on what has happened
     */
    public ResponseEntity<?> handle(Submission submission, Long userId) {


        Track track = httpRequestService.get("track/" + submission.getTrackId(), Track.class, RequestType.USER);

        if(LocalDateTime.parse(track.getSubmitDeadline()).isAfter(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("The deadline for submitting submissions to this track has passed.");
        }

        return super.checkNext(submission, userId);
    }
}
