package nl.tudelft.sem.template.submission.controllers;


import nl.tudelft.sem.template.api.StatsApi;
import nl.tudelft.sem.template.model.Statistics;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class StatsController implements StatsApi {

    @Override
    public ResponseEntity<Statistics> statsEventEventIdGet(Long eventId) {
        return StatsApi.super.statsEventEventIdGet(eventId);
    }

    @Override
    public ResponseEntity<Statistics> statsGet() {
        return StatsApi.super.statsGet();
    }

    @Override
    public ResponseEntity<Statistics> statsTrackTrackIdGet(Long trackId) {
        return StatsApi.super.statsTrackTrackIdGet(trackId);
    }
}
