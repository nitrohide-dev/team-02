package nl.tudelft.sem.template.submission.components.strategy;

import nl.tudelft.sem.template.model.PaperType;
import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.submission.repositories.SubmissionRepository;
import nl.tudelft.sem.template.submission.services.SubmissionService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SubmissionAuthorStrategy implements SubmissionGetStrategy {
    private final SubmissionRepository submissionRepository;

    /**
     * TrackStrategy constructor.
     *
     * @param submissionRepository submission repository.
     */
    public SubmissionAuthorStrategy(SubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
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
    public List<Submission> getSubmissions(Long userId, UUID id, Long submittedBy, List<Long> authors,
                                        String title, List<String> keywords, Long trackId,
                                        Long eventId, PaperType type) {

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


        List<Submission> submissions = submissionRepository.findAll(specification);

        for (Submission submission : submissions) {
            if (!submission.getAuthors().contains(userId)) {
                submission.setStatus(null);
                submission.setCreated(null);
                submission.setUpdated(null);
            }
        }


        return submissions;
    }

    /**
     * Method for getting a single submission by its submission id.
     *
     * @param userId the id of the user trying to get the submission
     * @param id the id of the submission that we are getting
     * @return submission that we found using the id
     */
    @Override
    public ResponseEntity<Submission> getSubmission(Long userId, UUID id) {
        Optional<Submission> submission = submissionRepository.findById(id);
        if (submission.isPresent() && !submission.get().getId().equals(id)) {
            submission.get().setUpdated(null);
            submission.get().setCreated(null);
            submission.get().setStatus(null);
        }
        return ResponseEntity.of(submission);
    }
}
