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

    /**
     * Check if the required fields are actually present.
     *
     * @param title title of paper
     * @param authors list of authors
     * @param bstract abstract
     * @param keywords list of keywords
     * @param link link
     * @return true if all good, false if something is missing
     */
    public boolean requiredFields(String title, List<Long> authors, String bstract, List<String> keywords, String link) {
        if (title == null || title.trim().isEmpty()) {
            return false;
        }
        if (authors == null || authors.isEmpty()) {
            return false;
        }
        if (bstract == null || bstract.trim().isEmpty()) {
            return false;
        }
        if (keywords == null || keywords.isEmpty()) {
            return false;
        }
        if (link == null || link.trim().isEmpty()) {
            return false;
        }
        return true;
    }
}

