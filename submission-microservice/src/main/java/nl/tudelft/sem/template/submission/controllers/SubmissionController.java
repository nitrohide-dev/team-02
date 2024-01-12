package nl.tudelft.sem.template.submission.controllers;

import javassist.NotFoundException;
import nl.tudelft.sem.template.api.SubmissionApi;
import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.model.SubmissionStatus;
import nl.tudelft.sem.template.submission.repositories.SubmissionRepository;
import nl.tudelft.sem.template.submission.services.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
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
    public ResponseEntity<Submission> addSubmission(Submission submission) {
        return submissionService.add(submission);
    }


    /**
     * Delete submission with a given id.
     *
     * @param submissionId Submission id to delete (required)
     * @return response ok if submission is deleted, error otherwise
     */
    @Override
    public ResponseEntity<Void> deleteSubmission(UUID submissionId, Long userId) {
        try {
            return submissionService.delete(submissionId, userId);
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(401).build();
        } catch (NotFoundException e) {
            return ResponseEntity.status(404).build();
        }
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
     * @param track    Filter by track id (optional)
     * @param author   Filter by author id (optional)
     * @param keywords Filters by keywords (optional)
     * @param status   Filter by status (optional)
     * @param name     Filter by submission name (optional)
     * @return list of submissions. All submissions are returned if no criteria specified.
     */
    @Override
    public ResponseEntity<List<Submission>> submissionGet(Long event, Long track,
                                                          List<Long> author, List<String> keywords,
                                                          SubmissionStatus status, String name) {
        return ResponseEntity.of(Optional.of(submissionRepository.findAll()));
    }

    /**
     * Updated submission with a provided id.
     *
     * @param submissionId     (required)
     * @param updateSubmission updated submission
     * @return response with updated submission if success, error otherwise
     */
    @Override
    public ResponseEntity<Submission> submissionSubmissionIdUserIdPut(UUID submissionId,
                                                                      Long userId,
                                                                      Submission updateSubmission) {
        try {
            return submissionService.update(submissionId, userId, updateSubmission);
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(401).build();
        } catch (NotFoundException e) {
            return ResponseEntity.status(404).build();
        }
    }
}
