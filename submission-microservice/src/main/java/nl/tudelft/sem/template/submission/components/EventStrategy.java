package nl.tudelft.sem.template.submission.components;

import javassist.NotFoundException;
import nl.tudelft.sem.template.model.Statistics;
import nl.tudelft.sem.template.model.Track;
import nl.tudelft.sem.template.submission.models.RequestType;
import nl.tudelft.sem.template.submission.repositories.StatisticsRepository;
import nl.tudelft.sem.template.submission.services.HttpRequestService;

import java.util.Arrays;
import java.util.List;

public class EventStrategy implements StatisticsStrategy {
    private final StatisticsRepository statisticsRepository;
    private final HttpRequestService httpRequestService;

    /**
     * EventStrategy constructor.
     *
     * @param statisticsRepository statistics repository
     * @param httpRequestService   http request service
     */
    public EventStrategy(StatisticsRepository statisticsRepository,
                         HttpRequestService httpRequestService) {
        this.statisticsRepository = statisticsRepository;
        this.httpRequestService = httpRequestService;
    }

    /**
     * Returns all tracks for a given event.
     *
     * @param eventId id of an event
     * @return array of tracks.
     */
    private List<Track> getTracks(long eventId) {
        return httpRequestService.getList(
                "track/" + "eventId=" + eventId,
                Track.class,
                RequestType.USER
        );
    }

    /**
     * Returns statistics for a given event.
     *
     * @param id event id
     * @return statistics
     * @throws NotFoundException if statistics for this event does not exist
     */
    public Statistics getStatistics(Long id) throws NotFoundException {
        List<Track> tracks = getTracks(id);

        if (tracks.size() == 0) {
            throw new NotFoundException("Not Found - Statistics for a given event ID does not exist.");
        }

        // long[] tracksIds = Arrays.stream(tracks).mapToLong(Track::getId).toArray();
        Long[] tracksIds = new Long[tracks.size()];
        for (int i = 0; i < tracksIds.length; i++) {
            tracksIds[i] = tracks.get(i).getId();
        }
        List<Statistics> trackStatistics = statisticsRepository.findAllById(Arrays.asList(tracksIds));

        Statistics statistics = new Statistics();
        for (Statistics stats : trackStatistics) {
            statistics.setAccepted(statistics.getAccepted() + stats.getAccepted());
            statistics.setAccepted(statistics.getOpen() + stats.getOpen());
            statistics.setAccepted(statistics.getRejected() + stats.getRejected());
            statistics.setAccepted(statistics.getUnderReview() + stats.getUnderReview());
        }

        return statistics;
    }
}
