package nl.tudelft.sem.template.submission.functional;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import nl.tudelft.sem.template.model.*;
import nl.tudelft.sem.template.submission.authentication.AuthManager;
import nl.tudelft.sem.template.submission.authentication.JwtTokenVerifier;
import nl.tudelft.sem.template.submission.controllers.StatsController;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = HttpRequestService.class)
@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {JwtTokenVerifier.class})

public class StatisticsTest {
    private static WireMockServer wireMockServerUser;
    private static WireMockServer wireMockServerReview;

    private SubmissionRepository submissionRepository;
    private SubmissionService submissionService;
    private StatisticsRepository statisticsRepository;
    private StatisticsService statisticsService;
    private StatsController statsController;

    @InjectMocks
    private HttpRequestService httpRequestService;
    @Mock
    private JwtTokenVerifier jwtTokenVerifier;
    private AuthManager authManager;

    @Captor
    private static ArgumentCaptor<Statistics> argumentCaptor;

    @BeforeAll
    static void startWireMock() {
        // Start user and review microservices on ports8085 and 8082
        wireMockServerUser = new WireMockServer(WireMockConfiguration.wireMockConfig().port(8085));
        wireMockServerReview = new WireMockServer(WireMockConfiguration.wireMockConfig().port(8082));

        wireMockServerUser.start();
        wireMockServerReview.start();
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServerUser.stop();
        wireMockServerReview.stop();
    }

    /**
     * Setup before each test. Submission service is initialized.
     */
    @BeforeEach
    public void setup() {
        // We only mock other microservices and repositories here
        submissionRepository = mock(SubmissionRepository.class);
        statisticsRepository = mock(StatisticsRepository.class);
        authManager = mock(AuthManager.class);

        // Initialize services without mocking them to test the entire system
        statisticsService = new StatisticsService(submissionRepository,
                statisticsRepository, httpRequestService, authManager);
        submissionService = new SubmissionService(submissionRepository,
                statisticsService, statisticsRepository,
                httpRequestService, authManager);
        statsController = new StatsController(statisticsService, statisticsRepository);

        wireMockServerUser.resetAll();
        wireMockServerReview.resetAll();


        lenient().when(authManager.getEmail()).thenReturn("example@gmail.com");
        wireMockServerUser.stubFor(
                WireMock.get("/user/byEmail/example@gmail.com")
                        .willReturn(aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody("""
                                        {
                                            "id": 1,
                                            "firstName": "name",
                                            "lastName": "lastName",
                                            "email": "example@gmail.com",
                                            "affiliation": "test",
                                            "personalWebsite":"",
                                            "preferredCommunication": "email"
                                        }
                                        """)
                        )
        );

        wireMockServerUser.stubFor(
                WireMock.get("/attendee/eventId=1&trackId=0&role=sub_reviewer")
                        .willReturn(aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody("""
                                        [
                                            {
                                                "id": "0",
                                                "eventId": "1",
                                                "trackId": "0",
                                                "role": "sub_reviewer",
                                                "userId": "0"
                                            },
                                            {
                                                "id": "1",
                                                "eventId": "1",
                                                "trackId": "0",
                                                "role": "pc_chair",
                                                "userId": "1"
                                            }
                                        ]
                                        """)
                        )
        );

        wireMockServerUser.stubFor(
                WireMock.get("/attendee/trackId=0")
                        .willReturn(aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody("""
                                        [
                                            {
                                                "id": "0",
                                                "eventId": "1",
                                                "trackId": "0",
                                                "role": "sub_reviewer",
                                                "userId": "0"
                                            },
                                            {
                                                "id": "1",
                                                "eventId": "1",
                                                "trackId": "0",
                                                "role": "pc_chair",
                                                "userId": "1"
                                            }
                                        ]
                                        """)
                        )
        );

        wireMockServerUser.stubFor(
                WireMock.get("/track/0")
                        .willReturn(aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody("""
                                        {
                                            "id": "0",
                                            "title": "track 0",
                                            "description": "description",
                                            "submit_deadline": "2024-05-03T23:59:59",
                                            "review_deadline": "2024-07-03T23:59:59",
                                            "paper_type":"full-paper",
                                            "event_id": "1"
                                        }
                                        """)
                        )
        );
    }

    /**
     * Test adding two submissions to one track.
     */
    @Test
    void testSameTrackStatistics() throws Exception {
        // Create first submission for track 0
        Submission submission1 = new Submission();
        submission1.setEventId(1L);
        submission1.setTrackId(0L);
        submission1.setTitle("title");
        submission1.setKeywords(List.of("keyword1", "keyword2"));
        submission1.setAuthors(new ArrayList<>(List.of(2L)));
        submission1.setType(PaperType.FULL_PAPER);

        Submission submission2 = new Submission();
        submission2.setEventId(1L);
        submission2.setTrackId(0L);
        submission2.setTitle("title 2");
        submission2.setKeywords(List.of("keyword1", "keyword2", "keyword3"));
        submission2.setAuthors(new ArrayList<>(List.of(2L, 3L, 4L)));
        submission2.setType(PaperType.FULL_PAPER);

        // Create second submission for track 0
        Statistics expectedStats = new Statistics();
        expectedStats.setId(0L);
        expectedStats.setTotalSubmissions(1L);
        expectedStats.setOpen(1L);
        expectedStats.setAverageNumberOfAuthors(2L);
        KeywordsCounts keywordsCounts = new KeywordsCounts();
        keywordsCounts.setCounts(List.of(1L, 1L));
        keywordsCounts.setKeywords(List.of("keyword1", "keyword2"));
        expectedStats.setKeywordsCounts(keywordsCounts);

        // Verify that statistics was created and that it matches statistics for one submission
        submissionService.add(submission1);
        verify(statisticsRepository, times(1)).save(argumentCaptor.capture());
        Statistics stats1 = argumentCaptor.getValue();
        assertEquals(expectedStats, stats1);

        // Make repository return saved statistics for the same track id
        when(statisticsRepository.findById(0L)).thenReturn(Optional.of(stats1));

        // Verify that statistics was updated correctly
        submissionService.add(submission2);
        verify(statisticsRepository, times(2)).save(argumentCaptor.capture());

        expectedStats.setTotalSubmissions(2L);
        expectedStats.setOpen(2L);
        expectedStats.setAverageNumberOfAuthors(3L);
        expectedStats.getKeywordsCounts().setCounts(List.of(1L, 2L, 2L));
        expectedStats.getKeywordsCounts().setKeywords(List.of("keyword3", "keyword1", "keyword2"));

        Statistics stats2 = argumentCaptor.getValue();
        assertEquals(expectedStats, stats2);
    }

    /**
     * Test adding two submissions to different tracks, same event.
     */
    @Test
    void testDifferentTrackSameEventStatistics() throws Exception {
        wireMockServerUser.stubFor(
                WireMock.get("/track/1")
                        .willReturn(aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody("""
                                        {
                                            "id": "1",
                                            "title": "track 1",
                                            "description": "description",
                                            "submit_deadline": "2024-05-03T23:59:59",
                                            "review_deadline": "2024-07-03T23:59:59",
                                            "paper_type":"full-paper",
                                            "event_id": "1"
                                        }
                                        """)
                        )
        );

        wireMockServerUser.stubFor(
                WireMock.get("/track/eventId=1")
                        .willReturn(aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody("""
                                        [
                                            {
                                            "id": "0",
                                            "title": "track 0",
                                            "description": "description",
                                            "submit_deadline": "2024-05-03T23:59:59",
                                            "review_deadline": "2024-07-03T23:59:59",
                                            "paper_type":"full-paper",
                                            "event_id": "1"
                                            },
                                            {
                                            "id": "1",
                                            "title": "track 1",
                                            "description": "description",
                                            "submit_deadline": "2024-05-03T23:59:59",
                                            "review_deadline": "2024-07-03T23:59:59",
                                            "paper_type":"full-paper",
                                            "event_id": "1"
                                            }
                                        ]
                                        """)
                        )
        );

        wireMockServerUser.stubFor(
                WireMock.get("/attendee/eventId=1&trackId=1&role=sub_reviewer")
                        .willReturn(aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody("""
                                        [
                                        ]
                                        """)
                        )
        );

        wireMockServerUser.stubFor(
                WireMock.get("/attendee/trackId=1")
                        .willReturn(aResponse()
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody("""
                                        [
                                            {
                                                "id": "2",
                                                "eventId": "1",
                                                "trackId": "1",
                                                "role": "pc_chair",
                                                "userId": "0"
                                            },
                                            {
                                                "id": "3",
                                                "eventId": "1",
                                                "trackId": "1",
                                                "role": "general_chair",
                                                "userId": "1"
                                            }
                                        ]
                                        """)
                        )
        );

        // Create first submission
        Submission submission1 = new Submission();
        submission1.setEventId(1L);
        submission1.setTrackId(1L);
        submission1.setTitle("title");
        submission1.setKeywords(List.of("keyword1", "keyword2"));
        submission1.setAuthors(new ArrayList<>(List.of(2L)));
        submission1.setType(PaperType.FULL_PAPER);

        // Create second submission
        Submission submission2 = new Submission();
        submission2.setEventId(1L);
        submission2.setTrackId(0L);
        submission2.setTitle("title 2");
        submission2.setKeywords(List.of("keyword1", "keyword2", "keyword3"));
        submission2.setAuthors(new ArrayList<>(List.of(2L, 3L, 4L)));
        submission2.setType(PaperType.FULL_PAPER);

        Statistics expectedStats = new Statistics();
        expectedStats.setId(1L);
        expectedStats.setTotalSubmissions(1L);
        expectedStats.setOpen(1L);
        expectedStats.setAverageNumberOfAuthors(2L);
        KeywordsCounts keywordsCounts = new KeywordsCounts();
        keywordsCounts.setCounts(List.of(1L, 1L));
        keywordsCounts.setKeywords(List.of("keyword1", "keyword2"));
        expectedStats.setKeywordsCounts(keywordsCounts);

        // Two different statistics objects are crated, since tracks are different
        submissionService.add(submission1);
        verify(statisticsRepository, times(1)).save(argumentCaptor.capture());
        Statistics stats1 = argumentCaptor.getValue();
        assertEquals(expectedStats, stats1);

        when(statisticsRepository.findById(0L)).thenReturn(Optional.empty());
        submissionService.add(submission2);
        verify(statisticsRepository, times(2)).save(argumentCaptor.capture());

        expectedStats.setTotalSubmissions(1L);
        expectedStats.setId(0L);
        expectedStats.setOpen(1L);
        expectedStats.setAverageNumberOfAuthors(4L);
        expectedStats.getKeywordsCounts().setCounts(List.of(1L, 1L, 1L));
        expectedStats.getKeywordsCounts().setKeywords(List.of("keyword3", "keyword1", "keyword2"));

        Statistics stats2 = argumentCaptor.getValue();
        assertEquals(expectedStats, stats2);

        when(statisticsRepository.findAllById(List.of(0L, 1L))).thenReturn(List.of(stats1, stats2));

        Statistics eventStats = statsController.trackOrEventStatisticsGet(1L).getBody();
        eventStats.setId(expectedStats.getId());
        expectedStats.setOpen(2L);
        expectedStats.setTotalSubmissions(2L);
        keywordsCounts.setCounts(List.of(1L, 2L, 2L));
        keywordsCounts.setKeywords(List.of("keyword3", "keyword1", "keyword2"));
        expectedStats.setKeywordsCounts(keywordsCounts);
        expectedStats.setAverageNumberOfAuthors(3L);
        assertEquals(expectedStats, eventStats);
    }

    /**
     * Test deleting a submission from a track.
     */
    @Test
    void testDeletingSubmissionSameTrack() throws Exception {
        // Initialize submission to be deleted
        Submission submission1 = new Submission();
        submission1.setId(0L);
        submission1.setEventId(1L);
        submission1.setTrackId(0L);
        submission1.setTitle("title");
        submission1.setKeywords(List.of("keyword1", "keyword2", "keyword3"));
        submission1.setAuthors(new ArrayList<>(List.of(1L, 2L, 3L, 4L)));
        submission1.setType(PaperType.FULL_PAPER);
        submission1.setStatus(SubmissionStatus.ACCEPTED);

        // Initialize collected statistics for a track 0
        Statistics collectedStats = new Statistics();
        collectedStats.setId(0L);
        collectedStats.setTotalSubmissions(10L);
        collectedStats.setAccepted(7L);
        collectedStats.setRejected(3L);
        collectedStats.setAverageNumberOfAuthors(3L);
        KeywordsCounts keywordsCounts = new KeywordsCounts();
        keywordsCounts.setCounts(List.of(10L, 12L, 18L, 20L));
        keywordsCounts.setKeywords(List.of("keyword1", "keyword2", "keyword3", "keyword4"));
        collectedStats.setKeywordsCounts(keywordsCounts);

        // Make repositories return submission and statistics
        when(submissionRepository.findById(0L)).thenReturn(Optional.of(submission1));
        when(statisticsRepository.findById(0L)).thenReturn(Optional.of(collectedStats));

        // Verify that statistics updated correctly when deleting submission
        submissionService.delete(0L);
        verify(statisticsRepository, times(1)).save(argumentCaptor.capture());

        collectedStats.setAccepted(6L);
        collectedStats.setTotalSubmissions(9L);
        collectedStats.setAverageNumberOfAuthors(2L);
        keywordsCounts.setCounts(List.of(17L, 20L, 9L, 11L));
        keywordsCounts.setKeywords(List.of("keyword3", "keyword4", "keyword1", "keyword2"));
        collectedStats.setKeywordsCounts(keywordsCounts);

        Statistics updated = argumentCaptor.getValue();
        assertEquals(collectedStats, updated);
    }

}
