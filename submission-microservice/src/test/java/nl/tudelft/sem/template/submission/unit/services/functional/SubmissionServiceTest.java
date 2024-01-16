package nl.tudelft.sem.template.submission.unit.services.functional;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import nl.tudelft.sem.template.model.PaperType;
import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.model.SubmissionStatus;
import nl.tudelft.sem.template.submission.authentication.AuthManager;
import nl.tudelft.sem.template.submission.authentication.JwtTokenVerifier;
import nl.tudelft.sem.template.submission.components.chain.DeadlinePassedException;
import nl.tudelft.sem.template.submission.repositories.StatisticsRepository;
import nl.tudelft.sem.template.submission.repositories.SubmissionRepository;
import nl.tudelft.sem.template.submission.services.HttpRequestService;
import nl.tudelft.sem.template.submission.services.StatisticsService;
import nl.tudelft.sem.template.submission.services.SubmissionService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@SpringBootTest(classes = HttpRequestService.class)
@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {JwtTokenVerifier.class})
public class SubmissionServiceTest {
    private static WireMockServer wireMockServerAuth;
    private static WireMockServer wireMockServerUser;
    private static WireMockServer wireMockServerReview;

    private SubmissionRepository submissionRepository;
    private StatisticsRepository statisticsRepository;
    private StatisticsService statisticsService;
    @InjectMocks
    private HttpRequestService httpRequestService;
    @Mock
    private JwtTokenVerifier jwtTokenVerifier;
    private AuthManager authManager;
    private SubmissionService submissionService;

    @Captor
    private static ArgumentCaptor<Submission> submissionCaptor;

    private static Submission submission;

    @BeforeAll
    static void startWireMock() {
        wireMockServerAuth = new WireMockServer(WireMockConfiguration.wireMockConfig().port(8081));
        wireMockServerUser = new WireMockServer(WireMockConfiguration.wireMockConfig().port(8085));
        wireMockServerReview = new WireMockServer(WireMockConfiguration.wireMockConfig().port(8082));

        wireMockServerAuth.start();
        wireMockServerUser.start();
        wireMockServerReview.start();
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServerAuth.stop();
        wireMockServerUser.stop();
        wireMockServerReview.stop();
    }

    /**
     * Setup before each test. Submission service is initialized.
     */
    @BeforeEach
    public void setup() {
        submissionRepository = mock(SubmissionRepository.class);
        statisticsRepository = mock(StatisticsRepository.class);
        statisticsService = new StatisticsService(submissionRepository,
                statisticsRepository, httpRequestService, authManager);
        authManager = mock(AuthManager.class);
        submissionService = new SubmissionService(
                submissionRepository, statisticsService,
                statisticsRepository,
                httpRequestService, authManager
        );

        submission = new Submission();
        submission.setEventId(1L);
        submission.setTrackId(0L);
        submission.setTitle("title");
    }

    /**
     * Mrthod for a common mock servers setup.
     */
    public void commonAuthorSetup() {
        when(authManager.getEmail()).thenReturn("example@gmail.com");
        wireMockServerUser.stubFor(
                WireMock.get("/user/byEmail/example@gmail.com")
                        .willReturn(aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody("{\"id\":1,\"firstName\":\"name\",\"lastName\":\"lastName\","
                                        + "\"email\":\"example@gmail.com\",\"affiliation\":\"test\","
                                        + "\"personalWebsite\":\"\",\"preferredCommunication\":\"email\"}")
                        )
        );

        wireMockServerUser.stubFor(
                WireMock.get("/attendee/eventId=1&trackId=0&role=sub_reviewer")
                        .willReturn(aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody("[]")
                        )
        );

        wireMockServerUser.stubFor(
                WireMock.get("/attendee/0")
                        .willReturn(aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody("[]")
                        )
        );
    }

    void commonReviewerSetup() {
        when(authManager.getEmail()).thenReturn("example@gmail.com");
        wireMockServerUser.stubFor(
                WireMock.get("/user/byEmail/example@gmail.com")
                        .willReturn(aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody("{\"id\":1,\"firstName\":\"name\",\"lastName\":\"lastName\","
                                        + "\"email\":\"example@gmail.com\",\"affiliation\":\"test\","
                                        + "\"personalWebsite\":\"\",\"preferredCommunication\":\"email\"}")
                        )
        );

        wireMockServerUser.stubFor(
                WireMock.get("/attendee/eventId=1&trackId=0&role=sub_reviewer")
                        .willReturn(aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody("[{\"id\":\"0\",\"eventId\":\"1\",\"trackId\":\"0\","
                                        + "\"role\":\"sub_reviewer\",\"userId\":\"0\"}, "
                                        + "{\"id\":\"1\",\"eventId\":\"1\",\"trackId\":\"0\","
                                        + "\"role\":\"sub_reviewer\",\"userId\":\"1\"}]")
                        )
        );
    }

    @Test
    public void testAddSubmission() throws Exception {
        commonAuthorSetup();

        wireMockServerUser.stubFor(
                WireMock.get("/track/0")
                        .willReturn(aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody("{\"id\":\"0\",\"event_id\":\"1\",\"submit_deadline\":\"2024-09-10T23:59:59\","
                                        + "\"paper_type\":\"full-paper\",\"title\":\"\",\"description\":\"\","
                                        + "\"review_deadline\":\"2024-09-10T23:59:59\"}")
                        )
        );

        submission.setType(PaperType.FULL_PAPER);
        submissionService.add(submission);
        verify(submissionRepository, times(1)).save(submissionCaptor.capture());
        Submission actual = submissionCaptor.getValue();
        submission.setId(actual.getId());
        submission.setAuthors(List.of(1L));
        assertEquals(submission, actual);
    }

    @Test
    void testAddSubmissionAfterDeadline() throws Exception {
        commonAuthorSetup();

        wireMockServerUser.stubFor(
                WireMock.get("/track/0")
                        .willReturn(aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody("{\"id\":\"0\",\"event_id\":\"1\",\"submit_deadline\":\"2023-09-10T23:59:59\","
                                        + "\"paper_type\":\"full-paper\",\"title\":\"\",\"description\":\"\","
                                        + "\"review_deadline\":\"2024-09-10T23:59:59\"}")
                        )
        );

        submission.setType(PaperType.FULL_PAPER);
        Exception e = assertThrows(DeadlinePassedException.class, () -> {
            submissionService.add(submission);
        });
        assertEquals("You cannot modify submission after the deadline.", e.getMessage());
    }

    @Test
    void testReviewAfterDeadline() {
        commonReviewerSetup();
        wireMockServerUser.stubFor(
                WireMock.get("/track/0")
                        .willReturn(aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody("{\"id\":\"0\",\"event_id\":\"1\",\"submit_deadline\":\"2022-09-10T23:59:59\","
                                        + "\"paper_type\":\"full-paper\",\"title\":\"\",\"description\":\"\","
                                        + "\"review_deadline\":\"2023-09-10T23:59:59\"}")
                        )
        );

        submission.setType(PaperType.FULL_PAPER);
        when(submissionRepository.findById(submission.getId())).thenReturn(Optional.of(submission));
        Submission updated = new Submission();
        updated.setStatus(SubmissionStatus.REJECTED);
        Exception e = assertThrows(DeadlinePassedException.class, () -> {
            submissionService.update(submission.getId(), updated);
        });
        assertEquals("You cannot modify submission after the deadline.", e.getMessage());
    }

    @Test
    void testReviewBeforeSubmissionDeadline() {
        commonReviewerSetup();
        wireMockServerUser.stubFor(
                WireMock.get("/track/0")
                        .willReturn(aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody("{\"id\":\"0\",\"event_id\":\"1\",\"submit_deadline\":\"2024-09-10T23:59:59\","
                                        + "\"paper_type\":\"full-paper\",\"title\":\"\",\"description\":\"\","
                                        + "\"review_deadline\":\"2024-08-10T23:59:59\"}")
                        )
        );

        submission.setType(PaperType.FULL_PAPER);

        when(submissionRepository.findById(submission.getId())).thenReturn(Optional.of(submission));
        Submission updated = new Submission();
        updated.setStatus(SubmissionStatus.REJECTED);

        Exception e = assertThrows(DeadlinePassedException.class, () -> {
            submissionService.update(submission.getId(), updated);
        });
        assertEquals("You cannot modify submission after the deadline.", e.getMessage());
    }

    //    @Test
    //    void testGetSubmissionAfterReviewDeadline() throws Exception {
    //        commonAuthorSetup();
    //        wireMockServerUser.stubFor(
    //                WireMock.get("/track/0")
    //                        .willReturn(aResponse()
    //                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
    //                                .withBody("{\"id\":\"0\",\"event_id\":\"1\",\"submit_deadline\":
    //                                \"2023-09-10T23:59:59\","
    //                                        + "\"paper_type\":\"full-paper\",\"title\":\"\",\"description\":\"\","
    //                                        + "\"review_deadline\":\"2023-08-10T23:59:59\"}")
    //                        )
    //        );
    //
    //        wireMockServerReview.stubFor(
    //                WireMock.get("/comments/1/papers/1")
    //                        .willReturn(aResponse()
    //                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
    //                                .withBody("{\"commentID\":\"0\",\"userID\":\"1\",\"reviewID\":\"2\","
    //                                        + "\"description\":\"description\",\"isConfidential\":\"true\"}")
    //                        )
    //        );
    //
    //        submission.setId(1L);
    //        submission.setStatus(SubmissionStatus.UNDERREVIEW);
    //        submission.setType(PaperType.FULL_PAPER);
    //
    //        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));
    //
    //        Submission result = submissionService.getById(1L).getBody();
    //        assertEquals(1, result.getComments().size());
    //    }

}


