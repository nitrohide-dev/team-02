package nl.tudelft.sem.template.submission.components.strategy;

import javassist.NotFoundException;
import nl.tudelft.sem.template.model.PaperType;
import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.model.SubmissionStatus;
import nl.tudelft.sem.template.submission.controllers.SubmissionController;
import nl.tudelft.sem.template.submission.repositories.SubmissionRepository;
import nl.tudelft.sem.template.submission.services.SubmissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SubmissionNotAuthorStrategy implements SubmissionGetStrategy {
    private final SubmissionService submissionService;

    /**
     * SubmissionNotAuthorStrategy constructor.
     *
     * @param submissionService submission service.
     */
    public SubmissionNotAuthorStrategy(SubmissionService submissionService) {
        this.submissionService = submissionService;
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
     * @return list of submissions. All submissions are returned if no criteria specified.
     */
    public List<Submission> getSubmission(UUID id, Long submittedBy, List<Long> authors,
                                                          String title, List<String> keywords, Long trackId,
                                                          Long eventId, PaperType type) {

        List<Submission> submissions = submissionService.get(id, submittedBy, authors, title,
                keywords, trackId, eventId, type).getBody();

        for (Submission submission : submissions) {
            submission.setStatus(null);
            submission.setCreated(null);
            submission.setUpdated(null);
        }


        return submissions;
    }
}
