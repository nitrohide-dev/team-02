package nl.tudelft.sem.template.submission.components.strategy;

import javassist.NotFoundException;
import nl.tudelft.sem.template.model.Statistics;
import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.submission.repositories.StatisticsRepository;

import java.util.Optional;

public class PcChairStrategy implements StatisticsStrategy {
    private final StatisticsRepository statisticsRepository;

    /**
     * TrackStrategy constructor.
     *
     * @param statisticsRepository statistics repository.
     */
    public PcChairStrategy(StatisticsRepository statisticsRepository) {
        this.statisticsRepository = statisticsRepository;
    }

    public boolean checkDeadline(long trackId) {
        return true;
    }

    public Submission getSubmission(long userId, Submission submission) {
        return submission;
    }

    /**
     * Returns statistics for a given track.
     *
     * @param id track id
     * @return statistics
     * @throws NotFoundException if statistics for a given track does not exist
     */
    public Statistics getStatistics(Long id) throws NotFoundException {
        Optional<Statistics> optional = statisticsRepository.findById(id);

        if (optional.isEmpty()) {
            throw new NotFoundException("Not Found - Statistics for a given track ID does not exist.");
        }

        return optional.get();
    }
}
