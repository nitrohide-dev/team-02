package nl.tudelft.sem.template.submission.services;

import javassist.NotFoundException;
import nl.tudelft.sem.template.model.*;
import nl.tudelft.sem.template.submission.components.strategy.EventStrategy;
import nl.tudelft.sem.template.submission.components.strategy.StatisticsStrategy;
import nl.tudelft.sem.template.submission.components.strategy.TrackStrategy;
import nl.tudelft.sem.template.submission.models.Chair;
import nl.tudelft.sem.template.submission.models.RequestType;
import nl.tudelft.sem.template.submission.repositories.StatisticsRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StatisticsService {
    private final StatisticsRepository statisticsRepository;
    private final HttpRequestService requestService;

    /**
     * StatisticsService constructor.
     *
     * @param statisticsRepository statistics repository
     * @param requestService       http request service
     */
    public StatisticsService(StatisticsRepository statisticsRepository,
                             HttpRequestService requestService) {
        this.statisticsRepository = statisticsRepository;
        this.requestService = requestService;
    }

    /**
     * Checks if user has a specific role on the specified track.
     *
     * @param trackId id of a track
     * @param userId  user id
     * @param role    role of a user
     * @return true if user has this role, false otherwise.
     */
    private boolean checkPermissions(Long trackId, Long userId, Role role) {
        List<Chair> chairsList = requestService.getList("attendee/" + trackId, Chair.class, RequestType.USER);
        for (Chair c : chairsList) {
            if (c.getUserId() == userId && c.getRole().equals(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns statistics for a given track (for a PC chair) and event (for general chair).
     *
     * @param userId  user id
     * @param trackId track id
     * @return statistics for track / event
     * @throws IllegalAccessException if user is not PC / general chair for the given track / event
     * @throws NotFoundException      if statistics for a given track / event was not collected yet
     */
    public Statistics getStatistics(long userId, long trackId) throws IllegalAccessException, NotFoundException {
        StatisticsStrategy strategy;
        long id;

        if (checkPermissions(trackId, userId, Role.GENERAL_CHAIR)) {
            strategy = new EventStrategy(statisticsRepository, requestService);
            Track track = requestService.get(
                    "track/" + trackId,
                    Track.class,
                    RequestType.USER
            );
            id = track.getEventId();
        } else {
            if (checkPermissions(trackId, userId, Role.PC_CHAIR)) {
                strategy = new TrackStrategy(statisticsRepository);
                id = trackId;
            } else {
                throw new IllegalAccessException("User has not enough permissions to get statistics.");
            }
        }

        return strategy.getStatistics(id);
    }

    /**
     * Updates statistics for a given track after a new submission was added.
     *
     * @param oldSubmission old submission
     * @param newSubmission new submission
     */
    public void updateStatistics(Submission oldSubmission, Submission newSubmission) {

        Statistics trackStats;

        if (oldSubmission == null) {
            long trackId = newSubmission.getTrackId();
            Optional<Statistics> optional = statisticsRepository.findById(trackId);
            if (optional.isEmpty()) {
                trackStats = new Statistics();
                trackStats.setId(newSubmission.getTrackId());
                trackStats.setTotalSubmissions(0L);
            } else {
                trackStats = optional.get();
            }

            addSubmission(trackStats, newSubmission);
        } else {
            long oldTrackId = oldSubmission.getTrackId();
            Optional<Statistics> optional = statisticsRepository.findById(oldTrackId);
            trackStats = optional.get();

            if (newSubmission == null) {
                deleteSubmission(trackStats, oldSubmission);
            } else {
                updateSubmission(trackStats, oldSubmission, newSubmission);
            }
        }
    }

    /**
     * Updates statistics after new submission was made.
     *
     * @param statistics current statistics record
     * @param submission new submission.
     */
    private void addSubmission(Statistics statistics, Submission submission) {
        SubmissionStatus status = submission.getStatus();
        if (status.equals(SubmissionStatus.ACCEPTED)) {
            statistics.setAccepted(statistics.getAccepted() + 1);
        }
        if (status.equals(SubmissionStatus.OPEN)) {
            statistics.setOpen(statistics.getOpen() + 1);
        }
        if (status.equals(SubmissionStatus.REJECTED)) {
            statistics.setRejected(statistics.getRejected() + 1);
        }
        if (status.equals(SubmissionStatus.UNDERREVIEW)) {
            statistics.setUnderReview(statistics.getUnderReview() + 1);
        }

        long n = statistics.getTotalSubmissions();
        statistics.setTotalSubmissions(statistics.getTotalSubmissions() + 1);
        statistics.setAverageNumberOfAuthors((statistics.getAverageNumberOfAuthors()
                * n + submission.getAuthors().size()) / (n + 1));
        statisticsRepository.save(statistics);
    }

    /**
     * Updates statistics after a submission was removed.
     *
     * @param statistics current statistics record
     * @param submission removed submission.
     */
    private void deleteSubmission(Statistics statistics, Submission submission) {
        SubmissionStatus status = submission.getStatus();
        if (status.equals(SubmissionStatus.ACCEPTED)) {
            statistics.setAccepted(statistics.getAccepted() - 1);
        }
        if (status.equals(SubmissionStatus.OPEN)) {
            statistics.setOpen(statistics.getOpen() - 1);
        }
        if (status.equals(SubmissionStatus.REJECTED)) {
            statistics.setRejected(statistics.getRejected() - 1);
        }
        if (status.equals(SubmissionStatus.UNDERREVIEW)) {
            statistics.setUnderReview(statistics.getUnderReview() - 1);
        }

        long n = statistics.getTotalSubmissions();
        statistics.setTotalSubmissions(n - 1);
        statistics.setAverageNumberOfAuthors((statistics.getAverageNumberOfAuthors()
                * n - submission.getAuthors().size()) / (n - 1));
        statisticsRepository.delete(statistics);
    }

    private void updateSubmission(Statistics statistics, Submission oldSubmission, Submission newSubmission) {
        long oldAuthorsNumber = oldSubmission.getAuthors().size();
        long newAuthorsNumber = newSubmission.getAuthors().size();

        long oldTotalAuthors = statistics.getAverageNumberOfAuthors() * statistics.getTotalSubmissions();
        long newTotalAuthors = oldTotalAuthors - oldAuthorsNumber + newAuthorsNumber;
        statistics.setAverageNumberOfAuthors(newTotalAuthors / statistics.getTotalSubmissions());
        statisticsRepository.save(statistics);
    }
}
