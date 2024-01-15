package nl.tudelft.sem.template.submission.components.strategy;

import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.model.Track;
import nl.tudelft.sem.template.submission.components.chain.DeadlinePassedException;
import nl.tudelft.sem.template.submission.repositories.SubmissionRepository;
import nl.tudelft.sem.template.submission.services.TrackService;

import java.time.LocalDateTime;

public class SubmissionReviewerStrategy implements GeneralStrategy {
    private final SubmissionRepository submissionRepository;
    private final TrackService trackService;

    /**
     * SubmissionNotAuthorStrategy constructor.
     *
     * @param submissionRepository submission repository.
     */
    public SubmissionReviewerStrategy(SubmissionRepository submissionRepository,
                                      TrackService trackService) {
        this.submissionRepository = submissionRepository;
        this.trackService = trackService;
    }

    @Override
    public boolean checkDeadline(long trackId) {
        Track track = trackService.getTrackById(trackId);
        String reviewDeadline = track.getReviewDeadline();
        String submissionDeadline = track.getSubmitDeadline();

        if (LocalDateTime.parse(reviewDeadline).isBefore(LocalDateTime.now())) {
            return false;
        }

        if (LocalDateTime.parse(submissionDeadline).isAfter(LocalDateTime.now())) {
            return false;
        }

        return true;
    }

    @Override
    public Submission getSubmission(Submission submission) {
        submission.setCreated(null);
        submission.setUpdated(null);
        return submission;
    }

    @Override
    public void updateSubmission(Submission oldSubmission, Submission newSubmission) throws DeadlinePassedException {
        oldSubmission.setStatus(newSubmission.getStatus());
        submissionRepository.save(oldSubmission);
    }
}
