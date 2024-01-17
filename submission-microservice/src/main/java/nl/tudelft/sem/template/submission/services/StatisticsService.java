package nl.tudelft.sem.template.submission.services;

import javassist.NotFoundException;
import nl.tudelft.sem.template.model.*;
import nl.tudelft.sem.template.submission.authentication.AuthManager;
import nl.tudelft.sem.template.submission.components.chain.BaseValidator;
import nl.tudelft.sem.template.submission.components.chain.UserValidator;
import nl.tudelft.sem.template.submission.components.strategy.GeneralStrategy;
import nl.tudelft.sem.template.submission.models.RequestType;
import nl.tudelft.sem.template.submission.repositories.StatisticsRepository;
import nl.tudelft.sem.template.submission.repositories.SubmissionRepository;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class StatisticsService {
    private final SubmissionRepository submissionRepository;
    private final StatisticsRepository statisticsRepository;
    private final HttpRequestService requestService;
    private final AuthManager authManager;
    private BaseValidator handler;

    /**
     * StatisticsService constructor.
     *
     * @param statisticsRepository statistics repository
     * @param requestService       http request service
     */
    public StatisticsService(SubmissionRepository submissionRepository,
                             StatisticsRepository statisticsRepository,
                             HttpRequestService requestService,
                             AuthManager authManager) {
        this.submissionRepository = submissionRepository;
        this.statisticsRepository = statisticsRepository;
        this.requestService = requestService;
        this.authManager = authManager;
    }

    private void setupChain() {
        handler = new UserValidator(submissionRepository, statisticsRepository,
                requestService, authManager);
    }

    /**
     * Returns statistics for a given track (for a PC chair) and event (for general chair).
     *
     * @param trackId track id
     * @return statistics for track / event
     * @throws IllegalAccessException if user is not PC / general chair for the given track / event
     * @throws NotFoundException      if statistics for a given track / event was not collected yet
     */
    public Statistics getStatistics(long trackId) throws Exception {
        setupChain();
        GeneralStrategy strategy = handler.handle(null, null, trackId, null, HttpMethod.GET);

        return strategy.getStatistics(
                requestService.get("track/" + trackId,
                        Track.class,
                        RequestType.USER));
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
        StatisticsServiceUtils.changePaperCount(statistics, submission, 1L);

        long n = statistics.getTotalSubmissions();
        statistics.setAverageNumberOfAuthors((statistics.getAverageNumberOfAuthors()
                * (n - 1) + submission.getAuthors().size()) / n);

        StatisticsServiceUtils.updateKeywordsCounts(statistics, submission.getKeywords(), 1L);
        statisticsRepository.save(statistics);
    }

    /**
     * Updates statistics after a submission was removed.
     *
     * @param statistics current statistics record
     * @param submission removed submission.
     */
    private void deleteSubmission(Statistics statistics, Submission submission) {
        StatisticsServiceUtils.changePaperCount(statistics, submission, -1L);

        long n = statistics.getTotalSubmissions();
        statistics.setAverageNumberOfAuthors((statistics.getAverageNumberOfAuthors()
                * (n + 1) - submission.getAuthors().size()) / n);
        StatisticsServiceUtils.updateKeywordsCounts(statistics, submission.getKeywords(), -1L);
        statisticsRepository.save(statistics);
    }



    /**
     * Updates statistics after a submission was edited.
     *
     * @param statistics    statistics for a given track
     * @param oldSubmission old version of submission
     * @param newSubmission new version of submission
     */
    private void updateSubmission(Statistics statistics, Submission oldSubmission, Submission newSubmission) {
        long oldAuthorsNumber = oldSubmission.getAuthors().size();
        long newAuthorsNumber = newSubmission.getAuthors().size();

        long oldTotalAuthors = statistics.getAverageNumberOfAuthors() * statistics.getTotalSubmissions();
        long newTotalAuthors = oldTotalAuthors - oldAuthorsNumber + newAuthorsNumber;
        statistics.setAverageNumberOfAuthors(newTotalAuthors / statistics.getTotalSubmissions());
        StatisticsServiceUtils.updateKeywordsCounts(statistics, oldSubmission.getKeywords(), -1L);
        StatisticsServiceUtils.updateKeywordsCounts(statistics, newSubmission.getKeywords(), 1L);
        statisticsRepository.save(statistics);
    }
}
