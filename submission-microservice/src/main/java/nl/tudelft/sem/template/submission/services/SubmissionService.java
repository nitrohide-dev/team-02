package nl.tudelft.sem.template.submission.services;

import nl.tudelft.sem.template.model.PaperType;
import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.submission.authentication.AuthManager;
import nl.tudelft.sem.template.submission.components.chain.*;
import nl.tudelft.sem.template.submission.components.strategy.SubmissionStrategy;
import nl.tudelft.sem.template.submission.repositories.StatisticsRepository;
import nl.tudelft.sem.template.submission.repositories.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SubmissionService {
    private final SubmissionRepository submissionRepository;
    private final StatisticsService statisticsService;
    private final StatisticsRepository statisticsRepository;
    private final TrackService trackService;
    private final HttpRequestService httpRequestService;
    private final AuthManager authManager;
    private BaseValidator handler;

    /**
     * Submission Service constructor.
     *
     * @param submissionRepository submission repository
     */
    @Autowired
    public SubmissionService(SubmissionRepository submissionRepository,
                             StatisticsService statisticsService,
                             StatisticsRepository statisticsRepository,
                             TrackService trackService,
                             HttpRequestService httpRequestService,
                             AuthManager authManager) {
        this.submissionRepository = submissionRepository;
        this.statisticsService = statisticsService;
        this.statisticsRepository = statisticsRepository;
        this.trackService = trackService;
        this.httpRequestService = httpRequestService;
        this.authManager = authManager;
    }

    private void setupChain(boolean addThirdStage) {
        DeadlineValidator deadlineValidator = new DeadlineValidator(httpRequestService);
        if (addThirdStage) {
            deadlineValidator.setNext(new SubmissionValidator(trackService));
        }
        handler = new UserValidator(submissionRepository, statisticsRepository, httpRequestService, trackService,
                authManager);
        handler.setNext(deadlineValidator);
    }

    /**
     * Saves new submission to database.
     *
     * @param submission new submission
     * @return response with created submission if success, otherwise error
     */
    public ResponseEntity<String> add(Submission submission) throws Exception {
        setupChain(true);
        handler.handle(null, null, null, submission, HttpMethod.POST);
        if (!checkDuplicateSubmissions(submission)) {
            throw new DuplicateSubmissionException("A submission with such a title already exists in this event!");
        }
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
    public ResponseEntity<Void> delete(@PathVariable("id") Long submissionId) throws Exception {
        Optional<Submission> deleted = submissionRepository.findById(submissionId);
        if (deleted.isEmpty()) {
            return ResponseEntity.status(404).build();
        }

        Submission submission = deleted.get();
        setupChain(false);
        SubmissionStrategy strategy = handler.handle(null, null, null, submission, HttpMethod.DELETE);
        strategy.deleteSubmission(submission);
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
    public ResponseEntity<Submission> update(@PathVariable("id") Long submissionId,
                                             Submission updatedSubmission) throws Exception {
        Optional<Submission> optional = submissionRepository.findById(submissionId);
        if (optional.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Submission submission = optional.get();
        setupChain(false);
        SubmissionStrategy strategy = handler.handle(null, null, null, submission, HttpMethod.PUT);
        strategy.updateSubmission(submission, updatedSubmission);
        statisticsService.updateStatistics(submission, updatedSubmission);
        return ResponseEntity.ok().build();
    }

    /**
     * Returns submission with a provided id.
     *
     * @param submissionId ID of submission to return (required)
     * @return submission if it is found for a given id, error otherwise
     */
    public ResponseEntity<Submission> getById(Long submissionId) throws Exception {

        Optional<Submission> submission = submissionRepository.findById(submissionId);
        if (submission.isEmpty()) {
            return ResponseEntity.status(404).build();
        }
        setupChain(false);
        SubmissionStrategy strategy = handler.handle(null, null, null, submission.get(), HttpMethod.GET);

        return ResponseEntity.ok().body(strategy.getSubmission(submission.get()));
    }

    /**
     * Returns list of submissions matching search criteria.
     *
     * @param submittedBy Filter by person who submitted (optional)
     * @param authors     Filter by author id (optional)
     * @param title       Filter by submission name (optional)
     * @param keywords    Filters by keywords (optional)
     * @param trackId     Filter by track id (optional)
     * @param eventId     Filter by event id (optional)
     * @param type        Filter by submission type (optional)
     * @return list of submissions. All submissions are returned if no criteria specified.
     */
    public ResponseEntity<List<Submission>> get(Long submittedBy, List<Long> authors,
                                                String title, List<String> keywords, Long trackId,
                                                Long eventId, PaperType type) {

        //imagine that I am doing some proper authentication checks here
        List<Submission> submissions = submissionRepository.findAll();
        List<Submission> result = new ArrayList<>();
        for (Submission submission : submissions) {
            if (submittedBy != null && !submittedBy.equals(submission.getSubmittedBy())) {
                continue;
            }
            if (authors != null && !authors.equals(submission.getAuthors())) {
                continue;
            }
            if (title != null && !title.equals(submission.getTitle())) {
                continue;
            }
            if (keywords != null && !keywords.equals(submission.getKeywords())) {
                continue;
            }
            if (trackId != null && !trackId.equals(submission.getTrackId())) {
                continue;
            }
            if (eventId != null && !eventId.equals(submission.getEventId())) {
                continue;
            }
            if (type != null && !type.equals(submission.getType())) {
                continue;
            }
            result.add(submission);
        }

        return ResponseEntity.ok().body(result);
    }

    /**
     * This method will be used to check whether there are any submissions
     * that are identical to the one that the user is trying to submit.
     * To check for whether it is identical or not, we are just going to check for the title of the submission.
     *
     * @param submission a submission that we are trying to add
     * @return boolean which returns ture if there are no identical submissions
     */
    public boolean checkDuplicateSubmissions(Submission submission) throws DeadlinePassedException, IllegalAccessException {

        List<Submission> submissions = get(null, null,
                submission.getTitle(), null, null, submission.getEventId(),
                null).getBody();
        return submissions.isEmpty();
    }
}

