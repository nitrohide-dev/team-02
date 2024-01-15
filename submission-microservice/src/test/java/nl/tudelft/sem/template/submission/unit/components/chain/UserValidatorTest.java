package nl.tudelft.sem.template.submission.unit.components.chain;

import nl.tudelft.sem.template.model.Role;
import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.submission.Application;
import nl.tudelft.sem.template.submission.components.chain.UserValidator;
import nl.tudelft.sem.template.submission.components.strategy.SubmissionAuthorStrategy;
import nl.tudelft.sem.template.submission.components.strategy.SubmissionReviewerStrategy;
import nl.tudelft.sem.template.submission.components.strategy.SubmissionStrategy;
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
import nl.tudelft.sem.template.submission.authentication.AuthManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import nl.tudelft.sem.template.submission.components.chain.DeadlinePassedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private SubmissionStrategy nextStrategy;

    @BeforeEach
    void setUp() {
        submission = new Submission();
        submission.setTrackId(14L);
        submission.setEventId(15L);
        submission.setAuthors(new ArrayList<>(Arrays.asList(1L, 2L)));
        userId = 123L;
        nextStrategy = mock(SubmissionStrategy.class);
        userValidator.setNext(null);


    }

    @Test
    void deleteAttemptTest() throws IllegalAccessException, DeadlinePassedException {
        HttpMethod requestType = HttpMethod.DELETE;
        String email = "author@example.com";
        when(authManager.getEmail()).thenReturn(email);
        when(httpRequestService.get("user/byEmail/" + email, Long.class, RequestType.USER)).thenReturn(userId);

        when(httpRequestService.getList("attendee/eventId=" + Long.class + "&trackId=" + Long.class
                        + "&role=sub_reviewer",
                Attendee.class, RequestType.USER)).thenReturn(new ArrayList<Attendee>());

        IllegalAccessException e =  assertThrows(IllegalAccessException.class,
            () -> userValidator.handle(nextStrategy, userId, submission, requestType));
        assertEquals(e.getMessage(), "You cannot delete a submission.");

    }

    @Test
    void modifyAttemptTestAttendee() throws IllegalAccessException, DeadlinePassedException {
        HttpMethod requestType = HttpMethod.PUT;
        String email = "author@example.com";
        when(authManager.getEmail()).thenReturn(email);
        when(httpRequestService.get("user/byEmail/" + email, Long.class, RequestType.USER)).thenReturn(userId);

        when(httpRequestService.getList("attendee/eventId=" + Long.class + "&trackId=" + Long.class
                        + "&role=sub_reviewer",
                Attendee.class, RequestType.USER)).thenReturn(new ArrayList<Attendee>());

        IllegalAccessException e =  assertThrows(IllegalAccessException.class,
                () -> userValidator.handle(nextStrategy, userId, submission, requestType));
        assertEquals(e.getMessage(), "You cannot modify a submission.");

    }

    @Test
    void subreviewerTest() throws IllegalAccessException, DeadlinePassedException {
        String email = "author@example.com";
        when(authManager.getEmail()).thenReturn(email);
        when(httpRequestService.get("user/byEmail/" + email, Long.class, RequestType.USER)).thenReturn(userId);
        Attendee a = new Attendee(userId, 1L, 2L, Role.SUB_REVIEWER);
        List<Attendee> attendeeList = new ArrayList<Attendee>();
        HttpMethod requestType = HttpMethod.PUT;
        attendeeList.add(a);
        when(httpRequestService.getList("attendee/eventId=" + submission.getEventId() + "&trackId=" + submission.getTrackId()
                        + "&role=sub_reviewer",
                Attendee.class, RequestType.USER)).thenReturn(attendeeList);
        SubmissionStrategy result = userValidator.handle(nextStrategy, null, submission, requestType);
        assertEquals(result.getClass(), SubmissionReviewerStrategy.class);
    }

    @Test
    void testAuthor() throws IllegalAccessException, DeadlinePassedException {
        submission.setAuthors(new ArrayList<>(Arrays.asList(userId)));
        String email = "author@example.com";
        when(authManager.getEmail()).thenReturn(email);
        when(httpRequestService.get("user/byEmail/" + email, Long.class, RequestType.USER)).thenReturn(userId);
        Attendee a = new Attendee(userId, 1L, 2L, Role.SUB_REVIEWER);
        List<Attendee> attendeeList = new ArrayList<Attendee>();
        attendeeList.add(a);
        when(httpRequestService.getList("attendee/eventId=" + submission.getEventId() + "&trackId=" + submission.getTrackId()
                        + "&role=sub_reviewer",
                Attendee.class, RequestType.USER)).thenReturn(attendeeList);
        SubmissionStrategy result = userValidator.handle(nextStrategy, null, submission, HttpMethod.PUT);
        assertEquals(result.getClass(), SubmissionAuthorStrategy.class);
    }

    @Test
    void modifyAttemptTestMultipleAttendees() throws IllegalAccessException, DeadlinePassedException {
        String email = "author@example.com";
        when(authManager.getEmail()).thenReturn(email);
        when(httpRequestService.get("user/byEmail/" + email, Long.class, RequestType.USER)).thenReturn(userId);

        Attendee a = new Attendee(99L, 1L, 2L, Role.SUB_REVIEWER);
        Attendee b = new Attendee(98L, 1L, 2L, Role.SUB_REVIEWER);

        List<Attendee> attendeeList = new ArrayList<Attendee>();
        attendeeList.add(a);
        attendeeList.add(b);
        when(httpRequestService.getList("attendee/eventId=" + submission.getEventId() + "&trackId=" + submission.getTrackId()
                        + "&role=sub_reviewer",
                Attendee.class, RequestType.USER)).thenReturn(attendeeList);

        IllegalAccessException e =  assertThrows(IllegalAccessException.class,
                () -> userValidator.handle(nextStrategy, userId, submission, HttpMethod.PUT));
        assertEquals(e.getMessage(), "You cannot modify a submission.");

    }


}

