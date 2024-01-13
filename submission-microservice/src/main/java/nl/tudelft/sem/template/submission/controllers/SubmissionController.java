package nl.tudelft.sem.template.submission.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import nl.tudelft.sem.template.api.SubmissionApi;
import nl.tudelft.sem.template.model.PaperType;
import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.model.SubmissionStatus;
import nl.tudelft.sem.template.submission.repositories.SubmissionRepository;
import nl.tudelft.sem.template.submission.services.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;


@RestController
public class SubmissionController implements SubmissionApi {
    private final SubmissionService submissionService;
    private final SubmissionRepository submissionRepository;

    /**
     * Submission controller constructor.
     *
     * @param submissionService    submission service
     * @param submissionRepository submission repository
     */
    @Autowired
    public SubmissionController(SubmissionService submissionService,
                                SubmissionRepository submissionRepository) {
        this.submissionService = submissionService;
        this.submissionRepository = submissionRepository;
    }

    /**
     * New submission.
     *
     * @param submission (required)
     * @return response with created submission if success, otherwise error
     */
    @Override
    public ResponseEntity<String> addSubmission(Submission submission) {
        return submissionService.add(submission);
    }

    /**
     * Delete submission with a given id.
     *
     * @param submissionId Submission id to delete (required)
     * @return response ok if submission is deleted, error otherwise
     */
    @Override
    public ResponseEntity<Void> deleteSubmission(UUID submissionId) {
        return submissionService.delete(submissionId);
    }

    /**
     * Returns submission with a provided id.
     *
     * @param submissionId ID of submission to return (required)
     * @return submission if it is found for a given id, error otherwise
     */
    @Override
    public ResponseEntity<Submission> getSubmissionById(UUID submissionId) {
        return ResponseEntity.of(submissionRepository.findById(submissionId));
    }

    /**
     * Returns list of submissions matching search criteria.
     *
     * @param id        Filter by submission id (optional)
     * @param submittedBy Filter by person who submitted (optional)
     * @param authors   Filter by author id (optional)
     * @param title     Filter by submission name (optional)
     * @param keywords Filters by keywords (optional)
     * @param trackId    Filter by track id (optional)
     * @param eventId    Filter by event id (optional)
     * @param type     Filter by submission type (optional)
     * @param status   Filter by status (optional)
     * @return list of submissions. All submissions are returned if no criteria specified.
     */
    @Override
    public ResponseEntity<List<Submission>> submissionGet(UUID id, Long submittedBy, List<Long> authors,
                                                          String title, List<String> keywords, Long trackId,
                                                          Long eventId, PaperType type, SubmissionStatus status) {

        return submissionService.get(id, submittedBy, authors, title,
                keywords, trackId, eventId, type, status);
    }

    /**
     * Updated submission with a provided id.
     *
     * @param submissionId     (required)
     * @param updateSubmission updated submission
     * @return response with updated submission if success, error otherwise
     */
    @Override
    public ResponseEntity<Submission> submissionSubmissionIdPut(UUID submissionId,
                                                                Submission updateSubmission) {
        return submissionService.update(submissionId, updateSubmission);
    }
}
