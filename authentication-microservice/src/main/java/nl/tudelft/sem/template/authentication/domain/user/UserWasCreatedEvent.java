package nl.tudelft.sem.template.authentication.domain.user;

/**
 * A DDD domain event that indicated a user was created.
 */
public class UserWasCreatedEvent {
    private final Email email;

    public UserWasCreatedEvent(Email email) {
        this.email = email;
    }

    public Email getEmail() {
        return this.email;
    }
}
