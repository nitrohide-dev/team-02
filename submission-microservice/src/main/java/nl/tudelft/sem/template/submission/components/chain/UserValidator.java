package nl.tudelft.sem.template.submission.components.chain;

import nl.tudelft.sem.template.model.Role;
import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.submission.authentication.AuthManager;
import nl.tudelft.sem.template.submission.components.strategy.*;
import nl.tudelft.sem.template.submission.models.Attendee;
import nl.tudelft.sem.template.submission.models.RequestType;
import nl.tudelft.sem.template.submission.repositories.StatisticsRepository;
import nl.tudelft.sem.template.submission.repositories.SubmissionRepository;
import nl.tudelft.sem.template.submission.services.HttpRequestService;
import nl.tudelft.sem.template.submission.services.TrackService;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserValidator extends SubmissionValidator {
    SubmissionRepository submissionRepository;
    StatisticsRepository statisticsRepository;
    HttpRequestService httpRequestService;
    TrackService trackService;
    AuthManager authManager;

    /**
     * AuthorizationValidator constructor.
     *
     * @param httpRequestService http request service
     */
    public UserValidator(SubmissionRepository submissionRepository,
                         StatisticsRepository statisticsRepository,
                         HttpRequestService httpRequestService,
                         TrackService trackService,
                         AuthManager authManager) {
        this.submissionRepository = submissionRepository;
        this.statisticsRepository = statisticsRepository;
        this.httpRequestService = httpRequestService;
        this.trackService = trackService;
        this.authManager = authManager;
    }

    private Role checkPermissions(long userId, Long trackId, Submission submission) {
        if (trackId == null) {
            long eventId = submission.getEventId();
            trackId = submission.getTrackId();

            if (submission.getAuthors().contains(userId)) {
                return Role.AUTHOR;
            }
            List<Attendee> reviewers = httpRequestService.getList("attendee/eventId=" + eventId + "&trackId=" + trackId
                            + "&role=sub_reviewer",
                    Attendee.class, RequestType.USER);
            long[] userIds = reviewers.stream().mapToLong(Attendee::getUserId).toArray();
            for (int i = 0; i < userIds.length; i++) {
                if (userId == userIds[i]) {
                    return Role.SUB_REVIEWER;
                }
            }
        }

        List<Attendee> chairsList = httpRequestService.getList("attendee/" + trackId, Attendee.class, RequestType.USER);
        for (Attendee c : chairsList) {
            if (c.getUserId() == userId && c.getRole().equals(Role.GENERAL_CHAIR)) {
                return Role.GENERAL_CHAIR;
            }
            if (c.getUserId() == userId && c.getRole().equals(Role.PC_CHAIR)) {
                return Role.PC_CHAIR;
            }
        }

        return Role.ATTENDEE;
    }

    /**
     * Checks if the user is an author of the submission.
     *
     * @param strategy that is being validated
     * @param userId   user id that will be returned
     * @return long which changes depending on what has happened
     */
    public SubmissionStrategy handle(SubmissionStrategy strategy,
                                     Long userId, Long trackId,
                                     Submission submission,
                                     HttpMethod requestType) throws IllegalAccessException, DeadlinePassedException {

        String email = authManager.getEmail();

        userId = httpRequestService.get("user/byEmail/" + email, Long.class, RequestType.USER);
        Role role = checkPermissions(userId, trackId, submission);

        if (requestType.equals(HttpMethod.DELETE) && !role.equals(Role.AUTHOR)) {
            throw new IllegalAccessException("You cannot delete a submission.");
        }
        if (requestType.equals(HttpMethod.PUT) && !(role.equals(Role.AUTHOR) || role.equals(Role.SUB_REVIEWER))) {
            throw new IllegalAccessException("You cannot modify a submission.");
        }
        if (requestType.equals(HttpMethod.GET) && submission == null
                && !(role.equals(Role.PC_CHAIR) || role.equals(Role.GENERAL_CHAIR))) {
            throw new IllegalAccessException("User has not enough permissions to get statistics.");
        }

        switch (role) {
            case AUTHOR -> {
                strategy = new SubmissionAuthorStrategy(submissionRepository, httpRequestService, trackService, userId);
            }
            case SUB_REVIEWER -> {
                strategy = new SubmissionReviewerStrategy(submissionRepository, trackService);
            }
            case PC_CHAIR -> {
                strategy = new PcChairStrategy(statisticsRepository);
            }
            case GENERAL_CHAIR -> {
                strategy = new GeneralChairStrategy(statisticsRepository,
                        httpRequestService);
            }
            default -> {
                strategy = new AttendeeStrategy();
            }
        }

        return super.checkNext(strategy, userId, trackId, submission, requestType);
    }
}
