package nl.tudelft.sem.template.submission.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.model.Track;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
        Track track = getTrackUsingTrackId(trackId);

        return (LocalDateTime.parse(track.getSubmitDeadline()).isAfter(LocalDateTime.now()));
    }

    /**
     * A method that is used to get the eventId from a track using its trackId.
     *
     * @param trackId the id of the track that we want to get the eventId from
     * @return long int representing the needed eventId
     */
    public Long getEventIdFromTrack(Long trackId) {
        return getTrackUsingTrackId(trackId).getEventId();
    }

    /**
     * This method is used to get all the tracks that are related to an event using the
     * event's id.
     *
     * @param eventId the id of the event that'll be used to find all the necessary tracks
     * @return list of tracks that are associated with the eventId that was provided
     */
    public List<Track> getAllTracksByEventId(Long eventId) {
        ObjectMapper objectMapper = new ObjectMapper();
        String url = "http://ip_adress:8084/track/?eventId=" + eventId;

        String receivedJson = httpRequestService.get(url).body();
        List<Track> track;
        try {
            track = objectMapper.readValue(receivedJson, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return track;
    }

    /**
     * A method used to get the track object from a trackId.
     *
     * @param trackId the id of the track that'll be used to get all the data from the object
     * @return Track object which has all the data that was stored in the database
     */
    private Track getTrackUsingTrackId(Long trackId) {
        ObjectMapper objectMapper = new ObjectMapper();
        String url = "http://ip_adress:8084/track/" + trackId;

        String receivedJson = httpRequestService.get(url).body();
        Track track;
        try {
            track = objectMapper.readValue(receivedJson, Track.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return track;
    }

}

