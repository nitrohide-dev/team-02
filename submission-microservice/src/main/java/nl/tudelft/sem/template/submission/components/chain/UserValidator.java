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
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserValidator extends BaseValidator {
    SubmissionRepository submissionRepository;
    StatisticsRepository statisticsRepository;
    AuthManager authManager;

    /**
     * AuthorizationValidator constructor.
     */
    public UserValidator(SubmissionRepository submissionRepository,
                         StatisticsRepository statisticsRepository,
                         HttpRequestService httpRequestService,
                         AuthManager authManager) {
        this.submissionRepository = submissionRepository;
        this.statisticsRepository = statisticsRepository;
        this.httpRequestService = httpRequestService;
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
                    Attendee[].class, RequestType.USER);

            long[] userIds = reviewers.stream().mapToLong(Attendee::getUserId).toArray();
            for (int i = 0; i < userIds.length; i++) {
                if (userId == userIds[i]) {
                    return Role.SUB_REVIEWER;
                }
            }
        } else {

            List<Attendee> chairsList = httpRequestService.getList("attendee/trackId="
                            + trackId, Attendee[].class,
                    RequestType.USER);
            for (Attendee c : chairsList) {
                if (c.getUserId() == userId && c.getRole().equals(Role.GENERAL_CHAIR)) {
                    return Role.GENERAL_CHAIR;
                }
                if (c.getUserId() == userId && c.getRole().equals(Role.PC_CHAIR)) {
                    return Role.PC_CHAIR;
                }
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
    public GeneralStrategy handle(GeneralStrategy strategy,
                                  Long userId, Long trackId,
                                  Submission submission,
                                  HttpMethod requestType) throws Exception {

        String email = authManager.getEmail();
        userId = Long.parseLong(httpRequestService.getAttribute("user/byEmail/" + email, RequestType.USER, "id"));
        Role role = checkPermissions(userId, trackId, submission);
        if (requestType.equals(HttpMethod.POST)) {
            role = Role.AUTHOR;
        }

        strategy = getStrategy(userId, role);

        return super.checkNext(strategy, userId, trackId, submission, requestType);
    }

    private GeneralStrategy getStrategy(Long userId, Role role) {
        GeneralStrategy strategy;
        switch (role) {
            case AUTHOR -> {
                strategy = new SubmissionAuthorStrategy(submissionRepository, httpRequestService, userId);
            }
            case SUB_REVIEWER -> {
                strategy = new SubmissionReviewerStrategy(submissionRepository, httpRequestService);
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
        return strategy;
    }
}
