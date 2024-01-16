package nl.tudelft.sem.template.submission.models;

import lombok.Getter;
import lombok.Setter;
import nl.tudelft.sem.template.model.Role;

import java.util.Objects;

@Getter
@Setter

public class Attendee {
    private final long id;
    private final long userId;
    private final long eventId;
    private final long trackId;
    private final Role role;

    /**
     * Default constructor.
     */
    public Attendee() {
        this.id = 0;
        this.userId = 0;
        this.eventId = 0;
        this.trackId = 0;
        this.role = Role.ATTENDEE;
    }

    /**
     * Chair constructor.
     *
     * @param id      id
     * @param userId  user id
     * @param eventId event id
     * @param trackId track id
     * @param role    role of a user
     */
    public Attendee(long id, long userId, long eventId, long trackId, Role role) {
        this.id = id;
        this.userId = userId;
        this.eventId = eventId;
        this.trackId = trackId;
        this.role = role;
    }

    /**
     * Returns id.
     *
     * @return id.
     */
    public long getId() {
        return id;
    }

    /**
     * Returns user id.
     *
     * @return user id.
     */
    public long getUserId() {
        return userId;
    }

    /**
     * Returns event id.
     *
     * @return event id.
     */
    public long getEventId() {
        return eventId;
    }

    /**
     * Retudns track id.
     *
     * @return track id.
     */
    public long getTrackId() {
        return trackId;
    }

    /**
     * Returns role of a chair.
     *
     * @return role.
     */
    public Role getRole() {
        return role;
    }

    /**
     * Compares two Chair objects.
     *
     * @param o other chair.
     * @return true if they are equal, false otherwise.
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Attendee chair = (Attendee) o;
        return id == chair.id && userId
                == chair.userId && eventId == chair.eventId && trackId == chair.trackId && role == chair.role;
    }

    /**
     * Returns a hashcode of a chair.
     *
     * @return hashcode.
     */
    public int hashCode() {
        return Objects.hash(userId, eventId, trackId, role);
    }

    /**
     * Returns a string representation of a chair.
     *
     * @return string representing a chair.
     */
    public String toString() {
        return "Chair{"
                + "userId="
                + userId
                + ", eventId="
                + eventId
                + ", trackId="
                + trackId
                + ", role="
                + role
                + '}';
    }
}
