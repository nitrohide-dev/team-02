package nl.tudelft.sem.template.submission.services;

import javassist.NotFoundException;
import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.model.SubmissionStatus;
import nl.tudelft.sem.template.submission.components.AuthorizationValidator;
import nl.tudelft.sem.template.submission.components.SubmissionValidator;
import nl.tudelft.sem.template.submission.models.RequestType;
import nl.tudelft.sem.template.submission.repositories.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SubmissionService {
    private final SubmissionRepository submissionRepository;
    private final StatisticsService statisticsService;
    private final TrackService trackService;
    private final HttpRequestService httpRequestService;
    private final SubmissionValidator submissionValidator;

    /**
     * Submission Service constructor.
     *
     * @param submissionRepository submission repository
     */
    @Autowired
    public SubmissionService(SubmissionRepository submissionRepository,
                             StatisticsService statisticsService,
                             TrackService trackService,
                             HttpRequestService httpRequestService,
                             SubmissionValidator submissionValidator) {
        this.submissionRepository = submissionRepository;
        this.statisticsService = statisticsService;
        this.trackService = trackService;
        this.httpRequestService = httpRequestService;
        this.submissionValidator = submissionValidator;
    }

    private void runChain(UUID submissionId, long userId) throws IllegalAccessException, NotFoundException {
        submissionValidator.setNext(new AuthorizationValidator(submissionRepository));
        // submissionValidator.setNext(new DeadlineValidator(submissionRepository));
        submissionValidator.handle(submissionId, userId);
    }

    /**
     * Saves new submission to database.
     *
     * @param submission new submission
     * @return response with created submission if success, otherwise error
     */
    public ResponseEntity<Submission> add(Submission submission) {
        //if (checkDuplicateSubmissions(submission)) {
        //if (!trackService.checkSubmissionDeadline(submission.getTrackId()))
        //return ResponseEntity.badRequest().build();
        //}
        //        if (!trackService.requiredFields(submission.getTitle(),
        //                submission.getAuthors(),
        //                submission.getAbstract(),
        //                submission.getKeywords(),
        //                submission.getLink())) {
        //            return ResponseEntity.badRequest().build();
        //        }
        submission.setId(UUID.randomUUID());
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
        statisticsService.updateStatistics(null, submission);
        return ResponseEntity.ok(submissionRepository.save(submission));
    }

    /**
     * Deletes submission with provided id from database.
     *
     * @param submissionId id of submission to delete
     * @return response ok if submission is deleted, error otherwise
     */
    public ResponseEntity<Void> delete(@PathVariable("id") UUID submissionId,
                                       @PathVariable("userId") long userId) throws NotFoundException,
            IllegalAccessException {
        runChain(submissionId, userId);
        Optional<Submission> deleted = submissionRepository.findById(submissionId);
        if (deleted.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Submission submission = deleted.get();
        submissionRepository.delete(submission);
        statisticsService.updateStatistics(submission, null);
        return ResponseEntity.ok().build();
    }

    /**
     * Updates submission with a provided id in database.
     *
     * @param submissionId      id of submission to update
     * @param updatedSubmission updated submission
     * @return response with updated submission if success, error otherwise
     */
    public ResponseEntity<Submission> update(@PathVariable("id") UUID submissionId,
                                             @PathVariable("userId") long userId,
                                             Submission updatedSubmission) throws NotFoundException,
            IllegalAccessException {
        runChain(submissionId, userId);
        Optional<Submission> optional = submissionRepository.findById(submissionId);
        if (optional.isEmpty()) {
            ResponseEntity.badRequest().build();
        }

        Submission submission = optional.get();

        statisticsService.updateStatistics(submission, updatedSubmission);
        submission.setStatus(updatedSubmission.getStatus());
        submission.setTitle(updatedSubmission.getTitle());
        submission.setAbstract(updatedSubmission.getAbstract());
        submission.setAuthors(updatedSubmission.getAuthors());
        submission.setKeywords(updatedSubmission.getKeywords());
        submission.setType(updatedSubmission.getType());
        submission.setEventId(updatedSubmission.getEventId());
        submission.setTrackId(updatedSubmission.getTrackId());
        submission.setLink(updatedSubmission.getLink());
        submission.setUpdated(LocalDateTime.now());

        return ResponseEntity.ok(submissionRepository.save(submission));
    }

    /**
     * This method will be used to check whether there are any submissions
     * that are identical to the one that the user is trying to submit.
     * To check for whether it is identical or not, we are just going to check for the title of the submission.
     *
     * @param submission a submission that we are trying to add
     * @return boolean which returns ture if there are no identical submissions
     */
    public boolean checkDuplicateSubmissions(Submission submission) {
        String url = "submission?title=" + submission.getTitle()
                + "?eventId=" + submission.getEventId();

        List<Submission> submissions = httpRequestService.getList(url, Submission.class, RequestType.USER);
        return submissions.isEmpty();
    }
}

