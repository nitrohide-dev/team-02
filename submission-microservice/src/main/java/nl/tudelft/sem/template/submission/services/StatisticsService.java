package nl.tudelft.sem.template.submission.services;

import javassist.NotFoundException;
import nl.tudelft.sem.template.model.KeywordsCounts;
import nl.tudelft.sem.template.model.Statistics;
import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.model.SubmissionStatus;
import nl.tudelft.sem.template.submission.authentication.AuthManager;
import nl.tudelft.sem.template.submission.components.chain.BaseValidator;
import nl.tudelft.sem.template.submission.components.chain.UserValidator;
import nl.tudelft.sem.template.submission.components.strategy.SubmissionStrategy;
import nl.tudelft.sem.template.submission.repositories.StatisticsRepository;
import nl.tudelft.sem.template.submission.repositories.SubmissionRepository;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class StatisticsService {
    private final SubmissionRepository submissionRepository;
    private final StatisticsRepository statisticsRepository;
    private final TrackService trackService;
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
                             TrackService trackService,
                             HttpRequestService requestService,
                             AuthManager authManager) {
        this.submissionRepository = submissionRepository;
        this.statisticsRepository = statisticsRepository;
        this.trackService = trackService;
        this.requestService = requestService;
        this.authManager = authManager;
    }

    private void setupChain() {
        handler = new UserValidator(submissionRepository, statisticsRepository,
                requestService, trackService, authManager);
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
        SubmissionStrategy strategy = handler.handle(null, null, trackId, null, HttpMethod.GET);

        return strategy.getStatistics(trackService.getTrackById(trackId));
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
     * Updates keywords statistics. Called after a submission is added/deleted/updated.
     *
     * @param statistics  statistics for a given track
     * @param keywords    keywords in submission
     * @param updateValue 1 if submission is added, -1 if submission is deleted
     */
    private void updateKeywordsCounts(Statistics statistics, List<String> keywords, long updateValue) {
        KeywordsCounts keywordsCounts = statistics.getKeywordsCounts();
        if (keywordsCounts == null) {
            keywordsCounts = new KeywordsCounts();
            keywordsCounts.setKeywords(List.of());
            keywordsCounts.setCounts(List.of());
        }
        if (keywords == null) {
            return;
        }
        Map<String, Long> counts = IntStream.range(0, keywordsCounts.getKeywords().size())
                .boxed()
                .collect(Collectors.toMap(keywordsCounts.getKeywords()::get, keywordsCounts.getCounts()::get));

        for (String keyword : keywords) {
            long count = counts.getOrDefault(keyword, 0L);
            counts.put(keyword, count + updateValue);
        }

        keywordsCounts.setKeywords(new ArrayList<>(counts.keySet()));
        keywordsCounts.setCounts(new ArrayList<>(counts.values()));
        statistics.setKeywordsCounts(keywordsCounts);
    }

    /**
     * Updates statistics after new submission was made.
     *
     * @param statistics current statistics record
     * @param submission new submission.
     */
    private void addSubmission(Statistics statistics, Submission submission) {
        changePaperCount(statistics, submission, 1L);

        long n = statistics.getTotalSubmissions();
        statistics.setAverageNumberOfAuthors((statistics.getAverageNumberOfAuthors()
                * n + submission.getAuthors().size()) / (n + 1));

        updateKeywordsCounts(statistics, submission.getKeywords(), 1L);
        statisticsRepository.save(statistics);
    }

    /**
     * Updates statistics after a submission was removed.
     *
     * @param statistics current statistics record
     * @param submission removed submission.
     */
    private void deleteSubmission(Statistics statistics, Submission submission) {
        changePaperCount(statistics, submission, -1L);

        long n = statistics.getTotalSubmissions();
        statistics.setAverageNumberOfAuthors((statistics.getAverageNumberOfAuthors()
                * n - submission.getAuthors().size()) / (n - 1));
        updateKeywordsCounts(statistics, submission.getKeywords(), -1L);
        statisticsRepository.delete(statistics);
    }

    /**
     * Updates papers count per track. Called after a submission is added/deleted/updated.
     *
     * @param statistics  statistics for a given track
     * @param submission  submission
     * @param updateValue 1 if submission is added, -1 if submission is deleted
     */
    private void changePaperCount(Statistics statistics, Submission submission, long updateValue) {
        SubmissionStatus status = submission.getStatus();
        switch (status) {
            case ACCEPTED -> {
                statistics.setAccepted(statistics.getAccepted() + updateValue);
            }
            case OPEN -> {
                statistics.setOpen(statistics.getOpen() + updateValue);
            }
            case REJECTED -> {
                statistics.setRejected(statistics.getRejected() + updateValue);
            }
            default -> {
                statistics.setUnderReview(statistics.getUnderReview() + updateValue);
            }
        }

        long n = statistics.getTotalSubmissions();
        statistics.setTotalSubmissions(n + updateValue);
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
        updateKeywordsCounts(statistics, oldSubmission.getKeywords(), -1L);
        updateKeywordsCounts(statistics, newSubmission.getKeywords(), 1L);
        statisticsRepository.save(statistics);
    }
}
