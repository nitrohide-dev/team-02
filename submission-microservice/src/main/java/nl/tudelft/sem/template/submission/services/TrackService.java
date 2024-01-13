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
        Track track = httpRequestService.get("track/" + trackId, Track.class, RequestType.USER);

        return (LocalDateTime.parse(track.getSubmitDeadline()).isAfter(LocalDateTime.now()));
    }

}

