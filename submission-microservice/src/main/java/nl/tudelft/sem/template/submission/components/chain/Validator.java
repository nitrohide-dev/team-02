package nl.tudelft.sem.template.submission.components.chain;

import javassist.NotFoundException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface Validator {
    void setNext(Validator handler);

    boolean handle(UUID submissionId, long userId) throws IllegalAccessException, NotFoundException;
}
