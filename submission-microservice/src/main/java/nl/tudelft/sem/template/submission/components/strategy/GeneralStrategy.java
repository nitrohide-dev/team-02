package nl.tudelft.sem.template.submission.components.strategy;

import javassist.NotFoundException;
import nl.tudelft.sem.template.model.Statistics;
import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.model.Track;
import nl.tudelft.sem.template.submission.components.chain.DeadlinePassedException;

public interface GeneralStrategy {
    /**
     * Checks deadline for a role.
     *
     * @param trackId track id
     * @return true if deadline has passed, false otherwise
     */
    default boolean checkDeadline(long trackId) {
        return true;
    }

    /**
     * Returns submission for a given role.
     * Depending on the role, not all information might be displayed.
     *
     * @param submission submission to be returned
     * @return submission
     */
    default Submission getSubmission(Submission submission) {
        submission.setCreated(null);
        submission.setUpdated(null);
        submission.setStatus(null);
        return submission;
    }

    /**
     * Update submission for a given role.
     *
     * @param oldSubmission old submission
     * @param newSubmission new submission
     * @throws IllegalAccessException  if user has no permission to update submission
     * @throws DeadlinePassedException if deadline has passed
     */
    default void updateSubmission(Submission oldSubmission, Submission newSubmission) throws IllegalAccessException,
            DeadlinePassedException {
        throw new IllegalAccessException("You cannot modify a submission.");
    }

    /**
     * Deletes submission if user is an author.
     *
     * @param submission submission to be deleted
     * @throws IllegalAccessException if user is not an author
     */
    default void deleteSubmission(Submission submission) throws IllegalAccessException {
        throw new IllegalAccessException("You cannot delete a submission.");
    }

    /**
     * Returns statistics if user is a chair.
     * Type of statistics is dependent on type of chair.
     *
     * @param track track id
     * @return statistics
     * @throws NotFoundException if no statistics was found for a track
     */
    default Statistics getStatistics(Track track) throws NotFoundException, IllegalAccessException {
        throw new IllegalAccessException("User has not enough permissions to get statistics.");
    }
}
