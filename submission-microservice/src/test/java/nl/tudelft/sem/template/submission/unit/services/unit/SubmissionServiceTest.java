package nl.tudelft.sem.template.submission.unit.services.unit;

import javassist.NotFoundException;
import nl.tudelft.sem.template.model.PaperType;
import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.model.Track;
import nl.tudelft.sem.template.submission.authentication.AuthManager;
import nl.tudelft.sem.template.submission.components.chain.DeadlinePassedException;
import nl.tudelft.sem.template.submission.components.chain.DuplicateSubmissionException;
import nl.tudelft.sem.template.submission.components.chain.SubmissionValidator;
import nl.tudelft.sem.template.submission.models.RequestType;
import nl.tudelft.sem.template.submission.repositories.SubmissionRepository;
import nl.tudelft.sem.template.submission.services.HttpRequestService;
import nl.tudelft.sem.template.submission.services.StatisticsService;
import nl.tudelft.sem.template.submission.services.SubmissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubmissionServiceTest {
    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private StatisticsService statisticsService;

    @Mock
    private HttpRequestService httpRequestService;

    @Mock
    private AuthManager authManager;

    @Autowired
    @InjectMocks
    private SubmissionService submissionService;

    @Autowired
    @InjectMocks
    private SubmissionValidator validator;

    private Submission submission;
    private Track mockTrack;

    @BeforeEach
    void setUp() {
        submission = new Submission();
        submission.setId(new Random().nextLong());
        submission.setCreated(LocalDateTime.now());
        submission.setUpdated(LocalDateTime.now());
        submission.setTitle("Paypuh titluh");
        submission.setAbstract("This paper goes hard, feel free to download");
        submission.setKeywords(new ArrayList<>(Arrays.asList("Keyword1", "Keyword2")));
        submission.setLink("http://example.com/papuh");
        submission.setAuthors(new ArrayList<>(Arrays.asList(1L, 2L)));
        submission.setTrackId(10L);
        submission.setType(PaperType.SHORT_PAPER);
        submission.setEventId(1L);

        mockTrack = new Track();
        mockTrack.setPaperType(submission.getType());
        mockTrack.setSubmitDeadline("2024-05-15T23:59:59");
    }

    @Test
    void testAddSubmission() throws Exception {
        when(httpRequestService.get("track/10", Track.class, RequestType.USER)).thenReturn(mockTrack);
        when(authManager.getEmail()).thenReturn("example@gmail.com");
        when(httpRequestService.getAttribute("user/byEmail/example@gmail.com", RequestType.USER, "id")).thenReturn("1");

        when(submissionRepository.save(any(Submission.class))).thenReturn(submission);
        ResponseEntity<String> response = submissionService.add(submission);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody().contains(submission.getId().toString()));
        verify(submissionRepository, times(1)).save(any(Submission.class));
    }

    @Test
    void testAddDuplicateSubmission() throws Exception {
        submission.setSubmittedBy(0L);
        when(httpRequestService.get("track/10", Track.class, RequestType.USER)).thenReturn(mockTrack);
        when(authManager.getEmail()).thenReturn("example@gmail.com");
        when(httpRequestService.getAttribute("user/byEmail/example@gmail.com", RequestType.USER, "id")).thenReturn("1");
        when(submissionRepository.findAllMatching(null, null, submission.getTitle(),
                null, null, submission.getEventId(), null))
                .thenReturn(Collections.singletonList(submission));

        Exception e = assertThrows(DuplicateSubmissionException.class,
                () -> {
                    submissionService.add(submission);
                });

        assertEquals("A submission with such a title already exists in this event!", e.getMessage());
    }

    @Test
    void testDeleteSubmissionNotFound() throws Exception {
        Long id = new Random().nextLong();
        when(submissionRepository.findById(id)).thenReturn(Optional.empty());

        Exception e = assertThrows(NotFoundException.class, () -> {
            submissionService.delete(id);
        });
        assertEquals("Submission with the given id was not found.", e.getMessage());
        verify(submissionRepository, never()).delete(any(Submission.class));
    }

    @Test
    void testDeleteSubmissionNoPermission() {
        when(authManager.getEmail()).thenReturn("example@gmail.com");
        when(httpRequestService.getAttribute("user/byEmail/example@gmail.com", RequestType.USER, "id")).thenReturn("10");
        Long id = new Random().nextLong();
        when(submissionRepository.findById(id)).thenReturn(Optional.of(submission));
        Exception e = assertThrows(IllegalAccessException.class, () -> {
            submissionService.delete(id);
        });
        assertEquals("You cannot delete a submission.", e.getMessage());
    }

    @Test
    void testDeleteSubmissionSuccess() throws Exception {
        Long id = new Random().nextLong();
        when(submissionRepository.findById(id)).thenReturn(Optional.of(submission));
        when(authManager.getEmail()).thenReturn("example@gmail.com");
        when(httpRequestService.getAttribute("user/byEmail/example@gmail.com", RequestType.USER, "id")).thenReturn("1");
        Track track = new Track();
        track.setSubmitDeadline("2024-02-06T23:59:59");
        when(httpRequestService.get("track/10", Track.class, RequestType.USER)).thenReturn(track);

        ResponseEntity<Void> response = submissionService.delete(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(submissionRepository, times(1)).delete(submission);
    }

    @Test
    void testUpdateSubmissionNotFound() throws Exception {
        Long id = new Random().nextLong();
        Submission updatedSubmission = new Submission();
        when(submissionRepository.findById(id)).thenReturn(Optional.empty());

        Exception e = assertThrows(NotFoundException.class, () -> {
            submissionService.update(id, updatedSubmission);
        });
        assertEquals("Submission with the given id was not found.", e.getMessage());
        verify(submissionRepository, never()).save(any(Submission.class));
    }

    @Test
    void testUpdateSubmissionSuccess() throws Exception {
        Long id = new Random().nextLong();

        when(submissionRepository.findById(id)).thenReturn(Optional.of(submission));
        when(authManager.getEmail()).thenReturn("example@gmail.com");
        when(httpRequestService.getAttribute("user/byEmail/example@gmail.com", RequestType.USER, "id")).thenReturn("1");
        Track track = new Track();
        track.setSubmitDeadline("2024-02-06T23:59:59");
        when(httpRequestService.get("track/10", Track.class, RequestType.USER)).thenReturn(track);

        Submission updatedSubmission = new Submission();
        ResponseEntity<Submission> response = submissionService.update(id, updatedSubmission);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(submissionRepository, times(1)).save(submission);
    }

    @Test
    void testCheckDuplicateSubmissions_NoDuplicates() throws DeadlinePassedException, IllegalAccessException {
        Submission duplicateSubmission = new Submission();
        duplicateSubmission.setTitle("Duplicate Title");
        duplicateSubmission.setEventId(1L);
        when(submissionRepository.findAllMatching(null, null, "Duplicate Title", null, null, 1L, null))
                .thenReturn(Collections.emptyList());

        boolean result = submissionService.checkDuplicateSubmissions(duplicateSubmission);

        assertTrue(result);
    }

    @Test
    void testCheckDuplicateSubmissions_WithDuplicates() throws DeadlinePassedException, IllegalAccessException {
        Submission duplicateSubmission = new Submission();
        duplicateSubmission.setTitle("Duplicate Title");
        Submission duplicateSubmission2 = new Submission();
        duplicateSubmission2.setTitle("Duplicate Title");
        when(submissionRepository.findAllMatching(null, null, "Duplicate Title", null, null, null, null))
                .thenReturn(List.of(duplicateSubmission2));

        boolean result = submissionService.checkDuplicateSubmissions(duplicateSubmission);

        assertFalse(result);
    }

    @Test
    void testGetSubmissions() {
        List<Submission> expectedSubmissions = Arrays.asList(new Submission(), new Submission());
        when(submissionRepository.findAllMatching(null,
                null, null, null, null, null, null)).thenReturn(expectedSubmissions);

        ResponseEntity<List<Submission>> response = submissionService.get(null,
                null, null, null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedSubmissions, response.getBody());
    }

    @Test
    void testGetWithAllParametersNull() {
        when(submissionRepository.findAllMatching(null, null,
                null, null, null, null, null))
                .thenReturn(Collections.singletonList(submission));

        ResponseEntity<List<Submission>> response = submissionService.get(null, null,
                null, null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void checkPaperTypeCorrect() {
        Track mockTrack = new Track();
        mockTrack.setPaperType(submission.getType());
        when(httpRequestService.get("track/10", Track.class, RequestType.USER)).thenReturn(mockTrack);
        String result = validator.checkPaperType(submission);
        assertEquals(result, null);
    }

    @Test
    void checkPaperTypeIncorrect() {
        Track mockTrack = new Track();
        mockTrack.setPaperType(PaperType.FULL_PAPER);
        when(httpRequestService.get("track/10", Track.class, RequestType.USER)).thenReturn(mockTrack);
        String result = validator.checkPaperType(submission);
        assertEquals(result, "You submitted a paper of incorrect type. The correct type is "
                + PaperType.FULL_PAPER.toString());
    }
}
