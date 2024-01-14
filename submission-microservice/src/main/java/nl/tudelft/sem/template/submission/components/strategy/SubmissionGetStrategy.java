package nl.tudelft.sem.template.submission.components.strategy;

import nl.tudelft.sem.template.model.PaperType;
import nl.tudelft.sem.template.model.Submission;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public interface SubmissionGetStrategy {
    List<Submission> getSubmissions(Long userId, UUID id, Long submittedBy, List<Long> authors,
                                                   String title, List<String> keywords, Long trackId,
                                                   Long eventId, PaperType type);

    ResponseEntity<Submission> getSubmission(Long userId, UUID id);
}
