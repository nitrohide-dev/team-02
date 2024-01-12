package nl.tudelft.sem.template.submission.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.model.SubmissionStatus;
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

    private final TrackService trackService;

    private final HttpRequestService httpRequestService;

    /**
     * Submission Service constructor.
     *
     * @param submissionRepository submission repository
     */
    @Autowired
    public SubmissionService(SubmissionRepository submissionRepository, TrackService trackService,
                             HttpRequestService httpRequestService) {
        this.submissionRepository = submissionRepository;
        this.trackService = trackService;
        this.httpRequestService = httpRequestService;
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
            submission.setEventId(updatedSubmission.getEventId());
            submission.setTrackId(updatedSubmission.getTrackId());
            submission.setLink(updatedSubmission.getLink());
            submission.setUpdated(LocalDateTime.now());

            return ResponseEntity.ok(submissionRepository.save(submission));

        }).orElseGet(() -> ResponseEntity.badRequest().build());

    }

    /**
     * This method will be used to check whether there are any submissions
     * that are identical to the one that the user is trying to submit.
     * To check for whether it is ideantical or not, we are just going to check for the title of the submission.
     *
     * @param submission a submission that we are trying to add
     * @return boolean which returns ture if there are no identical submissions
     */
    public boolean checkDuplicateSubmissions(Submission submission) {
        ObjectMapper objectMapper = new ObjectMapper();
        String url = "http://ip_adress:8084/submission?title=" + submission.getTitle()
                + "?eventId=" + submission.getEventId();

        String receivedJson = httpRequestService.get(url).body();
        List<Submission> submissions;
        try {
            submissions = objectMapper.readValue(receivedJson, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return submissions.isEmpty();
    }
}

