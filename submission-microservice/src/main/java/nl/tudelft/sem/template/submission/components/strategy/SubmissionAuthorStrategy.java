package nl.tudelft.sem.template.submission.components.strategy;

import nl.tudelft.sem.template.model.Comment;
import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.model.SubmissionStatus;
import nl.tudelft.sem.template.model.Track;
import nl.tudelft.sem.template.submission.models.RequestType;
import nl.tudelft.sem.template.submission.repositories.SubmissionRepository;
import nl.tudelft.sem.template.submission.services.HttpRequestService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SubmissionAuthorStrategy implements GeneralStrategy {
    private final SubmissionRepository submissionRepository;
    private final HttpRequestService httpRequestService;
    Long userId;

    /**
     * TrackStrategy constructor.
     *
     * @param submissionRepository submission repository.
     */
    public SubmissionAuthorStrategy(SubmissionRepository submissionRepository,
                                    HttpRequestService httpRequestService,
                                    Long userId) {
        this.submissionRepository = submissionRepository;
        this.httpRequestService = httpRequestService;
        this.userId = userId;
    }

    @Override
    public boolean checkDeadline(long trackId) {
        String submissionDeadline = httpRequestService.get("track/" + trackId,
                Track.class,
                RequestType.USER).getSubmitDeadline();
        if (LocalDateTime.parse(submissionDeadline).isBefore(LocalDateTime.now())) {
            return false;
        }
        return true;
    }

    @Override
    public Submission getSubmission(Submission submission) {
        // need to change submission id to long !!!
        if (!submission.getStatus().equals(SubmissionStatus.OPEN)) {
            List<Comment> comments = httpRequestService.getList("comments/" + userId + "/papers/" + submission.getId(),
                    Comment[].class, RequestType.REVIEW);
            List<String> commentsContent = new ArrayList<>();
            for (Comment comment : comments) {
                commentsContent.add(comment.getDescription());
            }
            submission.setComments(commentsContent);
        }
        return submission;
    }

    @Override
    public void updateSubmission(Submission oldSubmission, Submission newSubmission) {
        oldSubmission.setTitle(newSubmission.getTitle());
        oldSubmission.setAbstract(newSubmission.getAbstract());
        oldSubmission.setAuthors(newSubmission.getAuthors());
        oldSubmission.setKeywords(newSubmission.getKeywords());
        oldSubmission.setType(newSubmission.getType());
        oldSubmission.setEventId(newSubmission.getEventId());
        oldSubmission.setTrackId(newSubmission.getTrackId());
        oldSubmission.setLink(newSubmission.getLink());
        oldSubmission.setUpdated(LocalDateTime.now());
        submissionRepository.save(oldSubmission);
    }

    @Override
    public void deleteSubmission(Submission submission) throws IllegalAccessException {
        submissionRepository.delete(submission);
    }
}
