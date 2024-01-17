package nl.tudelft.sem.template.submission.unit.components.chain;

import nl.tudelft.sem.template.model.Role;
import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.submission.Application;
import nl.tudelft.sem.template.submission.authentication.AuthManager;
import nl.tudelft.sem.template.submission.components.chain.UserValidator;
import nl.tudelft.sem.template.submission.components.strategy.AttendeeStrategy;
import nl.tudelft.sem.template.submission.components.strategy.GeneralStrategy;
import nl.tudelft.sem.template.submission.components.strategy.SubmissionAuthorStrategy;
import nl.tudelft.sem.template.submission.components.strategy.SubmissionReviewerStrategy;
import nl.tudelft.sem.template.submission.controllers.SubmissionController;
import nl.tudelft.sem.template.submission.models.Attendee;
import nl.tudelft.sem.template.submission.models.RequestType;
import nl.tudelft.sem.template.submission.services.HttpRequestService;
import nl.tudelft.sem.template.submission.services.SubmissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserValidatorTest {
    @MockBean
    private SubmissionService submissionService;
    @MockBean
    private SubmissionController submissionController;

    @MockBean
    private HttpRequestService httpRequestService;
    @MockBean
    private AuthManager authManager;

    @Autowired
    @InjectMocks
    private UserValidator userValidator;

    private Submission submission;
    private Long userId;
    private GeneralStrategy nextStrategy;

    Long trackId;
    Long eventId;

    @BeforeEach
    void setUp() {
        submission = new Submission();
        trackId = 14L;
        eventId = 15L;
        submission.setTrackId(trackId);
        submission.setEventId(eventId);
        submission.setAuthors(new ArrayList<>(Arrays.asList(1L, 2L)));
        userId = 123L;
        nextStrategy = mock(GeneralStrategy.class);
        userValidator.setNext(null);
        String email = "author@example.com";
        when(httpRequestService.getAttribute("user/byEmail/"
                + email, RequestType.USER, "id")).thenReturn(String.valueOf(userId));
        when(authManager.getEmail()).thenReturn(email);
    }
    
    @Test
    void subreviewerTest() throws Exception {
        Attendee a = new Attendee(userId, userId, 2L, 3L, Role.SUB_REVIEWER);
        List<Attendee> attendeeList = new ArrayList<Attendee>();
        HttpMethod requestType = HttpMethod.PUT;
        attendeeList.add(a);
        when(httpRequestService.getList("attendee/eventId=" + eventId + "&trackId=" + trackId
                        + "&role=sub_reviewer",
                Attendee[].class, RequestType.USER)).thenReturn(attendeeList);

        GeneralStrategy result = userValidator.handle(nextStrategy,
                userId, null, submission, requestType);
        assertEquals(result.getClass(), SubmissionReviewerStrategy.class);
    }

    @Test
    void testAuthor() throws Exception {
        submission.setTrackId(null);
        submission.setAuthors(new ArrayList<>(Arrays.asList(userId)));
        String email = "author@example.com";
        when(authManager.getEmail()).thenReturn(email);
        when(httpRequestService.get("user/byEmail/" + email, Long.class, RequestType.USER)).thenReturn(userId);
        Attendee a = new Attendee(userId, 1L, 2L, 3L, Role.SUB_REVIEWER);
        List<Attendee> attendeeList = new ArrayList<Attendee>();
        attendeeList.add(a);
        when(httpRequestService.getList("attendee/eventId=" + eventId + "&trackId=" + trackId
                        + "&role=sub_reviewer",
                Attendee[].class, RequestType.USER)).thenReturn(attendeeList);
        GeneralStrategy result = userValidator.handle(nextStrategy,
                userId, submission.getTrackId(), submission, HttpMethod.PUT);
        assertEquals(result.getClass(), SubmissionAuthorStrategy.class);
    }

    @Test
    void checkPermissionsMultipleAttendees() throws Exception {
        Attendee a = new Attendee(99L, 1L, 2L, 3L, Role.SUB_REVIEWER);
        Attendee b = new Attendee(99L, 1L, 2L, 3L, Role.ATTENDEE);
        Attendee c = new Attendee(userId, 1L, 2L, 3L, Role.ATTENDEE);

        List<Attendee> attendeeList = new ArrayList<Attendee>();
        attendeeList.add(a);
        attendeeList.add(b);
        attendeeList.add(c);
        when(httpRequestService.getList("attendee/eventId=" + eventId + "&trackId=" + trackId
                        + "&role=sub_reviewer",
                Attendee[].class, RequestType.USER)).thenReturn(attendeeList);

        GeneralStrategy result = userValidator.handle(nextStrategy,
                userId, submission.getTrackId(), submission, HttpMethod.GET);
        assertEquals(result.getClass(), AttendeeStrategy.class);

    }


}

