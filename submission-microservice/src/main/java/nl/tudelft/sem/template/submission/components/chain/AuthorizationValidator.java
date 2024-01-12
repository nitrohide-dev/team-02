package nl.tudelft.sem.template.submission.components.chain;

import javassist.NotFoundException;
import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.submission.repositories.SubmissionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class AuthorizationValidator extends SubmissionValidator {
    SubmissionRepository submissionRepository;

    /**
     * AuthorizationValidator constructor.
     *
     * @param submissionRepository submission repository.
     */
    public AuthorizationValidator(SubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }

    /**
     * Checks if the user is an author of the submission.
     *
     * @param submissionId submission id
     * @param userId       user id
     * @return true if user is an author
     * @throws IllegalAccessException if user has no permissions
     * @throws NotFoundException      if no submission with a given id exists
     */
    public boolean handle(UUID submissionId, long userId) throws IllegalAccessException, NotFoundException {
        Optional<Submission> optional = submissionRepository.findById(submissionId);
        if (optional.isEmpty()) {
            throw new NotFoundException("Not Found - Submission with the given id does not exist.");
        }

        List<Long> authors = optional.get().getAuthors();
        if (authors.contains(userId)) {
            return true;
        }
        throw new IllegalAccessException("No permission - User is not an author of the paper.");
    }
}
