package nl.tudelft.sem.template.submission.components.chain;

import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.model.Track;
import nl.tudelft.sem.template.submission.models.RequestType;
import nl.tudelft.sem.template.submission.repositories.SubmissionRepository;
import nl.tudelft.sem.template.submission.services.HttpRequestService;
import nl.tudelft.sem.template.submission.services.SubmissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DuplicateValidator extends SubmissionValidator {

    SubmissionService submissionService;

    /**
     * AuthorizationValidator constructor.
     *
     * @param submissionService submission service.
     */
    public DuplicateValidator(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    /**
     * Checks if the user is an author of the submission.
     *
     * @param submission that is being validated
     * @param userId       user id that will be returned
     * @return long which changes depending on what has happened
     */
    public ResponseEntity<?> handle(Submission submission, Long userId) {

        List<Submission> submissions = submissionService.get(null, null, null,
                submission.getTitle(), null, null, submission.getEventId(),
                null, null).getBody();
        if(!submissions.isEmpty())
            return ResponseEntity.status(HttpStatus.CONFLICT).body("A submission with such a title already exists in this event!");

        return super.checkNext(submission, userId);
    }
}
