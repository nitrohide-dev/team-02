package nl.tudelft.sem.template.submission.services;

import nl.tudelft.sem.template.model.KeywordsCounts;
import nl.tudelft.sem.template.model.Statistics;
import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.model.SubmissionStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StatisticsServiceUtils {

    private StatisticsServiceUtils() {
    }

    /**
     * Updates keywords statistics. Called after a submission is added/deleted/updated.
     *
     * @param statistics  statistics for a given track
     * @param keywords    keywords in submission
     * @param updateValue 1 if submission is added, -1 if submission is deleted
     */
    public static void updateKeywordsCounts(Statistics statistics, List<String> keywords, long updateValue) {
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
     * Updates papers count per track. Called after a submission is added/deleted/updated.
     *
     * @param statistics  statistics for a given track
     * @param submission  submission
     * @param updateValue 1 if submission is added, -1 if submission is deleted
     */
    public static void changePaperCount(Statistics statistics, Submission submission, long updateValue) {
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
}