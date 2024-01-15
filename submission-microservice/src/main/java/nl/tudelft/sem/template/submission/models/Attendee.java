package nl.tudelft.sem.template.submission.models;

import nl.tudelft.sem.template.model.Role;

import java.util.Objects;

public class Attendee {
    private final long userId;
    private final long eventId;
    private final long trackId;
    private final Role role;

    /**
     * Chair constructor.
     *
     * @param userId  user id
     * @param eventId event id
     * @param trackId track id
     * @param role    role of a user
     */
    public Attendee(long userId, long eventId, long trackId, Role role) {
        this.userId = userId;
        this.eventId = eventId;
        this.trackId = trackId;
        this.role = role;
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
        return userId == chair.userId && eventId == chair.eventId && trackId == chair.trackId && role == chair.role;
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
