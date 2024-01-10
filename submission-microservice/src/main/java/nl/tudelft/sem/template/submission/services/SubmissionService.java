package nl.tudelft.sem.template.submission.services;

import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.submission.repositories.SubmissionRepository;
import nl.tudelft.sem.template.submission.services.TrackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import javax.sound.midi.Track;
import javax.validation.constraints.Null;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static java.util.UUID.randomUUID;

@Service
public class SubmissionService {
    private final SubmissionRepository submissionRepository;

    private final TrackService trackService;

    /**
     * Submission Service constructor.
     *
     * @param submissionRepository submission repository
     */
    @Autowired
    public SubmissionService(SubmissionRepository submissionRepository, TrackService trackService) {
        this.submissionRepository = submissionRepository;
        this.trackService = trackService;
    }

    /**
     * Saves new submission to database.
     *
     * @param submission new submission
     * @return response with created submission if success, otherwise error
     */
    public ResponseEntity<Submission> add(Submission submission) {
        if (!trackService.checkSubmissionDeadline(submission.getTrackId())) {
            return ResponseEntity.badRequest().build();
        }
        submission.setId(randomUUID());
        submission.setCreated(LocalDateTime.now());
        submission.setUpdated(submission.getCreated());
        return ResponseEntity.ok(submissionRepository.save(submission));
    }

    /**
     * Deletes submission with provided id from database.
     *
     * @param submissionId id of submission to delete
     * @return response ok if submission is deleted, error otherwise
     */
    public ResponseEntity<Void> delete(@PathVariable("id") UUID submissionId) {
        Optional<Submission> deleted = submissionRepository.findById(submissionId);
        if (deleted.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        submissionRepository.delete(deleted.get());
        return ResponseEntity.ok().build();
    }

    /**
     * Updates submission with a provided id in database.
     *
     * @param submissionId      id of submission to update
     * @param updatedSubmission updated subission
     * @return response with updated submission if success, error otherwise
     */
    public ResponseEntity<Submission> update(@PathVariable("id") UUID submissionId,
                                             Submission updatedSubmission) {
        return submissionRepository.findById(submissionId).map(submission -> {
            submission.setStatus(updatedSubmission.getStatus());
            submission.setTitle(updatedSubmission.getTitle());
            submission.setAbstract(updatedSubmission.getAbstract());
            submission.setAuthors(updatedSubmission.getAuthors());
            submission.setKeywords(updatedSubmission.getKeywords());
            submission.setType(updatedSubmission.getType());
            submission.setTrackId(updatedSubmission.getTrackId());
            submission.setLink(updatedSubmission.getLink());
            submission.setUpdated(LocalDateTime.now());

            return ResponseEntity.ok(submissionRepository.save(submission));

        }).orElseGet(() -> ResponseEntity.badRequest().build());

    }
}

