package nl.tudelft.sem.template.submission.services;

import nl.tudelft.sem.template.model.PaperType;
import javassist.NotFoundException;
import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.model.SubmissionStatus;
import nl.tudelft.sem.template.submission.authentication.AuthManager;
import nl.tudelft.sem.template.submission.components.chain.*;
import nl.tudelft.sem.template.submission.repositories.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
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
    private final AuthManager authManager;

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
                             AuthManager authManager) {
        this.submissionRepository = submissionRepository;
        this.statisticsService = statisticsService;
        this.trackService = trackService;
        this.httpRequestService = httpRequestService;
        this.authManager = authManager;
    }

    /**
     * Saves new submission to database.
     *
     * @param submission new submission
     * @return response with created submission if success, otherwise error
     */
    public ResponseEntity<String> add(Submission submission) {
        if (!checkDuplicateSubmissions(submission)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    "A submission with such a title already exists in this event!");
        }
        /*
        Validator deadlineValidator = new DeadlineValidator(httpRequestService);
        deadlineValidator.setNext(new DuplicateValidator(this));

        Validator handler = new UserValidator(httpRequestService, authManager);
        handler.setNext(deadlineValidator);

        ResponseEntity<Submission> entity = ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        entity.getStatusCode();

        ResponseEntity<?> answer = handler.handle(submission, null);

        Long userId;

        if (answer.getStatusCode() == HttpStatus.OK) {
            userId = (Long) answer.getBody();
        } else {
            return (ResponseEntity<String>) answer;
        }

         */

        //if (!trackService.checkSubmissionDeadline(submission.getTrackId()))
        //    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        //}

        //        if (!trackService.requiredFields(submission.getTitle(),
        //                submission.getAuthors(),
        //                submission.getAbstract(),
        //                submission.getKeywords(),
        //                submission.getLink())) {
        //            return ResponseEntity.badRequest().build();
        //        }

        submission.setId(UUID.randomUUID());
        //submission.setSubmittedBy(userId);
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
        submissionRepository.save(submission);
        return ResponseEntity.status(HttpStatus.CREATED).body("""
                Submission added successfully!
                Here is the id for your new submission:\s""" + submission.getId());
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
        Optional<Submission> optional = submissionRepository.findById(submissionId);
        if (optional.isEmpty()) {
            ResponseEntity.badRequest().build();
        }

        Submission submission = optional.get();

        statisticsService.updateStatistics(submission, updatedSubmission);
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

        List<Submission> submissions = get(null, null, null,
                submission.getTitle(), null, null, submission.getEventId(),
                null, null).getBody();
        return submissions.isEmpty();
    }

    /**
     * Returns list of submissions matching search criteria.
     *
     * @param eventId    Filter by event id (optional)
     * @param trackId    Filter by track id (optional)
     * @param authors   Filter by author id (optional)
     * @param keywords Filters by keywords (optional)
     * @param status   Filter by status (optional)
     * @param title     Filter by submission name (optional)
     * @param type     Filter by submission type (optional)
     * @return list of submissions. All submissions are returned if no criteria specified.
     */
    public ResponseEntity<List<Submission>> get(UUID id, Long submittedBy, List<Long> authors,
                                                String title, List<String> keywords, Long trackId,
                                                Long eventId, PaperType type, SubmissionStatus status) {

        Specification<Submission> specification = Specification.where(null);

        if (id != null) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("id"), id));
        }

        if (submittedBy != null) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("submittedBy"), submittedBy));
        }

        if (authors != null && !authors.isEmpty()) {
            specification = specification.and((root, query, builder) ->
                    root.get("authors").in(authors));
        }

        if (title != null) {
            specification = specification.and((root, query, builder) ->
                    builder.like(builder.lower(root.get("title")), title.toLowerCase()));
        }

        if (keywords != null && !keywords.isEmpty()) {
            specification = specification.and((root, query, builder) ->
                    root.get("keywords").in(keywords));
        }

        if (trackId != null) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("trackId"), trackId));
        }

        if (eventId != null) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("eventId"), eventId));
        }

        if (type != null) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("type"), type));
        }

        if (status != null) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("status"), status));
        }

        return ResponseEntity.ok().body(submissionRepository.findAll(specification));
    }
}

