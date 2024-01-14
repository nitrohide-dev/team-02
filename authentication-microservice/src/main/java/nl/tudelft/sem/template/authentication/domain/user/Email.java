package nl.tudelft.sem.template.authentication.domain.user;

import lombok.EqualsAndHashCode;

/**
 * A DDD value object representing a NetID in our domain.
 */
@EqualsAndHashCode
public class Email {
    private final transient String emailValue;

    public Email(String email) {
        // validate NetID
        this.emailValue = email;
    }

    @Override
    public String toString() {
        return emailValue;
    }
}
