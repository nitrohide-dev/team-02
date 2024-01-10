package nl.tudelft.sem.template.submission.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.tudelft.sem.template.model.Track;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
    public boolean checkSubmissionDeadline(Long trackId) {
        ObjectMapper objectMapper = new ObjectMapper();
        String url = "http://ip_adress:8082/track/" + trackId;

        String receivedJson = httpRequestService.get(url).body();
        Track track;
        try {
            track = objectMapper.readValue(receivedJson, Track.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return (LocalDateTime.parse(track.getSubmitDeadline()).isAfter(LocalDateTime.now()));
    }

}

