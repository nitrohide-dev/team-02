package nl.tudelft.sem.template.submission.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.OffsetDateTime;

@Service
public class TrackService {

    private final HttpRequestService httpRequestService;

    /**
     * Track Service constructor.
     *
     * @param httpRequestService httpRequest handler
     */
    @Autowired
    public TrackService(HttpRequestService httpRequestService) {
        this.httpRequestService = httpRequestService;
    }

    /**
     * Checks the deadline of a track.
     *
     * @param trackId the id of the track that we should check the deadline of
     * @return boolean which is true if the track deadline has not been passed yet
     */
    public boolean checkDeadline(Long trackId) {
        String url = "http://ip_adress:8082/microservice/api/data";

        String trackTime = httpRequestService.get(url).body();

        return (OffsetDateTime.now().equals(OffsetDateTime.now()));
    }
}

