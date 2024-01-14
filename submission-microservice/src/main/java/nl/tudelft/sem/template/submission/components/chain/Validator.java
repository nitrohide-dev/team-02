package nl.tudelft.sem.template.submission.components.chain;

import nl.tudelft.sem.template.model.Submission;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface Validator {
    void setNext(Validator handler);

    ResponseEntity<?> handle(Submission submission, Long userId);
}
