package nl.tudelft.sem.template.submission.services;

import nl.tudelft.sem.template.model.Track;
import nl.tudelft.sem.template.submission.models.RequestType;
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
        String url = "track/?eventId=" + eventId;

        return httpRequestService.getList(url, Track.class, RequestType.USER);
    }

    /**
     * A method used to get the track object from a trackId.
     *
     * @param trackId the id of the track that'll be used to get all the data from the object
     * @return Track object which has all the data that was stored in the database
     */
    private Track getTrackUsingTrackId(Long trackId) {
        String url = "track/" + trackId;

        return httpRequestService.get(url, Track.class, RequestType.USER);
    }

}

