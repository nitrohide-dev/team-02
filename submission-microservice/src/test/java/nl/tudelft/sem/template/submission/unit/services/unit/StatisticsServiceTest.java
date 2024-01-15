package nl.tudelft.sem.template.submission.unit.services.unit;

import javassist.NotFoundException;
import nl.tudelft.sem.template.model.*;
import nl.tudelft.sem.template.submission.Application;
import nl.tudelft.sem.template.submission.authentication.AuthManager;
import nl.tudelft.sem.template.submission.controllers.SubmissionController;
import nl.tudelft.sem.template.submission.models.Attendee;
import nl.tudelft.sem.template.submission.models.RequestType;
import nl.tudelft.sem.template.submission.repositories.StatisticsRepository;
import nl.tudelft.sem.template.submission.services.HttpRequestService;
import nl.tudelft.sem.template.submission.services.StatisticsService;
import nl.tudelft.sem.template.submission.services.SubmissionService;
import nl.tudelft.sem.template.submission.services.TrackService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class StatisticsServiceTest {
    @MockBean
    private SubmissionService submissionService;
    @MockBean
    private SubmissionController submissionController;

    @MockBean
    private StatisticsRepository repository;

    @MockBean
    private HttpRequestService requestService;

    @MockBean
    private TrackService trackService;

    @MockBean
    private AuthManager authManager;

    @Autowired
    @InjectMocks
    private StatisticsService service;

    private static Statistics trackStats1;
    private static Statistics trackStats2;

    private static Attendee generalChair;
    private static Attendee pcChair1;
    private static Attendee pcChair2;

    private static Track track1;
    private static Track track2;
    private static Track track3;

    private static Submission submission;
    private static long id = 0L;

    @BeforeAll
    static void generalSetup() {
        generalChair = new Attendee(id, 0L, 0L, 0L, Role.GENERAL_CHAIR);
        pcChair1 = new Attendee(id, 1L, 0L, 0L, Role.PC_CHAIR);
        pcChair2 = new Attendee(id, 2L, 0L, 1L, Role.PC_CHAIR);

        track1 = new Track();
        track1.setId(0L);
        track1.setEventId(0L);
        track2 = new Track();
        track2.setId(1L);
        track2.setEventId(0L);
        track3 = new Track();
        track3.setId(2L);
        track3.setEventId(1L);

        submission = new Submission();
        submission.setId(new Random().nextLong());
        submission.setTrackId(0L);
        submission.setEventId(0L);
        submission.setAuthors(List.of(0L, 1L));
        submission.setStatus(SubmissionStatus.ACCEPTED);
        submission.setKeywords(List.of("keyword1", "keyword2"));
    }

    private void statsSetup() {
        trackStats1 = new Statistics();
        trackStats1.setId(0L);
        trackStats1.setTotalSubmissions(10L);
        trackStats1.setAccepted(3L);
        trackStats1.setRejected(7L);
        trackStats1.setAverageNumberOfAuthors(2L);

        KeywordsCounts keywordsCounts1 = new KeywordsCounts();
        keywordsCounts1.setKeywords(List.of("keyword1", "keyword2"));
        keywordsCounts1.setCounts(List.of(10L, 5L));
        trackStats1.setKeywordsCounts(keywordsCounts1);

        trackStats2 = new Statistics();
        trackStats2.setId(1L);
        trackStats2.setTotalSubmissions(20L);
        trackStats2.setAccepted(12L);
        trackStats2.setRejected(8L);
        trackStats2.setAverageNumberOfAuthors(4L);

        KeywordsCounts keywordsCounts2 = new KeywordsCounts();
        keywordsCounts2.setKeywords(List.of("keyword1", "keyword2", "keyword3"));
        keywordsCounts2.setCounts(List.of(5L, 5L, 10L));
        trackStats2.setKeywordsCounts(keywordsCounts2);
    }

    private void setupTrackService() {
        when(trackService.getTrackById(0L)).thenReturn(track1);
        when(trackService.getTrackById(1L)).thenReturn(track2);
        when(trackService.getTrackById(2L)).thenReturn(track3);
    }

    private void setupRequestService() {
        when(requestService.getList("track/" + "eventId=0", Track[].class, RequestType.USER))
                .thenReturn(List.of(track1, track2));
        when(requestService.getList("track/" + "eventId=1", Track[].class, RequestType.USER))
                .thenReturn(List.of());

        when(requestService.getList("attendee/0", Attendee[].class, RequestType.USER))
                .thenReturn(List.of(generalChair, pcChair1));
        when(requestService.getList("attendee/1", Attendee[].class, RequestType.USER))
                .thenReturn(List.of(generalChair, pcChair2));
        when(requestService.getList("attendee/2", Attendee[].class, RequestType.USER))
                .thenReturn(List.of(generalChair));
    }

    @BeforeEach
    void setup() {
        statsSetup();
        setupTrackService();
        setupRequestService();

        when(authManager.getEmail()).thenReturn("example@gmail.com");

        when(repository.findById(0L)).thenReturn(Optional.ofNullable(trackStats1));
        when(repository.findById(1L)).thenReturn(Optional.ofNullable(trackStats2));
        when(repository.findAllById(List.of(0L, 1L)))
                .thenReturn(List.of(trackStats1, trackStats2));
    }

    @Test
    void testGetStatisticsPcChair() throws Exception {
        when(requestService.getAttribute("user/byEmail/example@gmail.com", RequestType.USER, "id")).thenReturn("1");
        assertEquals(trackStats1, service.getStatistics(0L));

        when(requestService.getAttribute("user/byEmail/example@gmail.com", RequestType.USER, "id")).thenReturn("2");
        assertEquals(trackStats2, service.getStatistics(1L));
    }

    @Test
    void testGetStatisticsGeneralChair() throws Exception {
        when(requestService.getAttribute("user/byEmail/example@gmail.com", RequestType.USER, "id")).thenReturn("0");
        Statistics eventStats = service.getStatistics(0L);
        assertEquals(30L, eventStats.getTotalSubmissions());
        assertEquals(15L, eventStats.getAccepted());
        assertEquals(15L, eventStats.getRejected());
        assertEquals(100L / 30L, eventStats.getAverageNumberOfAuthors());
    }

    @Test
    void testNotFoundException() {
        when(requestService.getAttribute("user/byEmail/example@gmail.com", RequestType.USER, "id")).thenReturn("0");
        Exception e = assertThrows(NotFoundException.class, () -> {
            service.getStatistics(2L);
        });
        assertEquals("Not Found - Statistics for a given event ID does not exist.",
                e.getMessage());
    }

    @Test
    void testIllegalAccessException() {
        when(requestService.getAttribute("user/byEmail/example@gmail.com", RequestType.USER, "id")).thenReturn("1");
        Exception e = assertThrows(IllegalAccessException.class, () -> {
            service.getStatistics(1L);
        });
        assertEquals("User has not enough permissions to get statistics.",
                e.getMessage());
    }

    @Test
    void testAddSubmission() {
        service.updateStatistics(null, submission);
        Statistics saved = new Statistics();
        KeywordsCounts keywordsCounts = new KeywordsCounts();
        keywordsCounts.setKeywords(List.of("keyword1", "keyword2"));
        keywordsCounts.setCounts(List.of(11L, 6L));

        saved.setId(0L);
        saved.setTotalSubmissions(11L);
        saved.setAccepted(4L);
        saved.setRejected(7L);
        saved.setAverageNumberOfAuthors(2L);
        saved.setKeywordsCounts(keywordsCounts);
        verify(repository, times(1)).save(saved);
    }

    @Test
    void testDeleteSubmission() {
        service.updateStatistics(submission, null);
        Statistics saved = new Statistics();
        KeywordsCounts keywordsCounts = new KeywordsCounts();
        keywordsCounts.setKeywords(List.of("keyword1", "keyword2"));
        keywordsCounts.setCounts(List.of(9L, 4L));

        saved.setId(0L);
        saved.setTotalSubmissions(9L);
        saved.setAccepted(2L);
        saved.setRejected(7L);
        saved.setAverageNumberOfAuthors(2L);
        saved.setKeywordsCounts(keywordsCounts);
        verify(repository, times(1)).delete(saved);
    }

    @Test
    void testUpdateSubmission() {
        Submission updated = new Submission();
        updated.setId(submission.getId());
        updated.setTrackId(0L);
        updated.setEventId(0L);
        updated.setAuthors(List.of(0L, 1L, 2L, 3L));
        updated.setKeywords(List.of("keyword1", "keyword2"));
        updated.setStatus(SubmissionStatus.ACCEPTED);

        service.updateStatistics(submission, updated);
        Statistics saved = new Statistics();
        saved.setId(0L);
        saved.setTotalSubmissions(10L);
        saved.setAccepted(3L);
        saved.setRejected(7L);
        saved.setAverageNumberOfAuthors(2L);
        KeywordsCounts keywordsCounts = new KeywordsCounts();
        keywordsCounts.setKeywords(List.of("keyword1", "keyword2"));
        keywordsCounts.setCounts(List.of(10L, 5L));
        saved.setKeywordsCounts(keywordsCounts);
        verify(repository, times(1)).save(saved);
    }
}

