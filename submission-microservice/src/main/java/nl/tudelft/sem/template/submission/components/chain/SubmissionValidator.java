package nl.tudelft.sem.template.submission.components.chain;


import nl.tudelft.sem.template.model.PaperType;
import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.model.SubmissionStatus;
import nl.tudelft.sem.template.model.Track;
import nl.tudelft.sem.template.submission.components.strategy.SubmissionStrategy;
import nl.tudelft.sem.template.submission.services.TrackService;
import org.springframework.http.HttpMethod;

import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SubmissionValidator extends BaseValidator {
    private final TrackService trackService;

    public SubmissionValidator(TrackService trackService) {
        this.trackService = trackService;
    }

    /**
     * Verifies that submission is valid.
     *
     * @param strategy    strategy
     * @param userId      user id
     * @param trackId     track id
     * @param submission  submission to be evaluated
     * @param requestType type of request
     * @return strategy used
     */
    public SubmissionStrategy handle(SubmissionStrategy strategy, Long userId, Long trackId,
                                     Submission submission, HttpMethod requestType) {

        String paperType = checkPaperType(submission);
        if (paperType != null || submission.getTrackId() == null) {
            throw new InvalidParameterException();
        }

        if (submission.getEventId() == null) {
            submission.setEventId(trackService.getTrackById(submission.getTrackId()).getEventId());
        }
        submission.setId(new Random().nextLong());
        submission.setSubmittedBy(userId);
        submission.setCreated(LocalDateTime.now());
        submission.setUpdated(submission.getCreated());
        submission.setStatus(SubmissionStatus.OPEN);
        List<Long> authors;
        if (submission.getAuthors() == null) {
            authors = new ArrayList<>();
        } else {
            authors = submission.getAuthors();
        }
        authors.add(submission.getSubmittedBy());
        submission.setAuthors(authors);
        return strategy;
    }

    /**
     * Checks whether the paper type submitted is accepted at the stated track.
     *
     * @param submission paper submission
     * @return returns null if there's no conflict, otherwise returns message specifying required type
     */
    public String checkPaperType(Submission submission) {
        Track track = trackService.getTrackById(submission.getTrackId());
        PaperType paperType = track.getPaperType();
        String incorrectType = "You submitted a paper of incorrect type. The correct type is " + paperType.toString();
        if (paperType.equals(submission.getType())) {
            return null;
        } else {
            return incorrectType;
        }
    }
}
