package nl.tudelft.sem.template.submission.components.strategy;

import javassist.NotFoundException;
import nl.tudelft.sem.template.model.Statistics;
import nl.tudelft.sem.template.model.Track;
import nl.tudelft.sem.template.submission.repositories.StatisticsRepository;

import java.util.Optional;

public class PcChairStrategy implements SubmissionStrategy {
    private final StatisticsRepository statisticsRepository;

    /**
     * TrackStrategy constructor.
     *
     * @param statisticsRepository statistics repository.
     */
    public PcChairStrategy(StatisticsRepository statisticsRepository) {
        this.statisticsRepository = statisticsRepository;
    }

    /**
     * Returns statistics for a given track.
     *
     * @param track track
     * @return statistics
     * @throws NotFoundException if statistics for a given track does not exist
     */
    public Statistics getStatistics(Track track) throws NotFoundException {
        long id = track.getId();
        Optional<Statistics> optional = statisticsRepository.findById(id);

        if (optional.isEmpty()) {
            throw new NotFoundException("Not Found - Statistics for a given track ID does not exist.");
        }

        return optional.get();
    }
}
