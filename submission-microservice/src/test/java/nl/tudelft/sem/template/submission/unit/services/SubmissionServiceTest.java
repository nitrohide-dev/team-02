package nl.tudelft.sem.template.submission.unit.services;


import javassist.NotFoundException;
import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.model.Track;
import nl.tudelft.sem.template.submission.authentication.AuthManager;
import nl.tudelft.sem.template.submission.components.chain.DeadlinePassedException;
import nl.tudelft.sem.template.submission.models.RequestType;
import nl.tudelft.sem.template.submission.repositories.SubmissionRepository;
import nl.tudelft.sem.template.submission.services.HttpRequestService;
import nl.tudelft.sem.template.submission.services.StatisticsService;
import nl.tudelft.sem.template.submission.services.SubmissionService;
import nl.tudelft.sem.template.submission.services.TrackService;
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
    private TrackService trackService;

    @Mock
    private HttpRequestService httpRequestService;

    @Mock
    private AuthManager authManager;

    @Autowired
    @InjectMocks
    private SubmissionService submissionService;

    private Submission submission;

    @BeforeEach
    void setUp() {
        submission = new Submission();
        submission.setId(UUID.randomUUID());
        submission.setCreated(LocalDateTime.now());
        submission.setUpdated(LocalDateTime.now());
        submission.setTitle("Paypuh titluh");
        submission.setAbstract("This paper goes hard, feel free to download");
        submission.setKeywords(new ArrayList<>(Arrays.asList("Keyword1", "Keyword2")));
        submission.setLink("http://example.com/papuh");
        submission.setAuthors(new ArrayList<>(Arrays.asList(1L, 2L)));
        submission.setTrackId(10L);
        submission.setEventId(1L);
        Track track = new Track();
    }

    @Test
    void testAddSubmission() throws DeadlinePassedException, IllegalAccessException {
        when(submissionRepository.save(any(Submission.class))).thenReturn(submission);

        ResponseEntity<String> response = submissionService.add(submission);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody().contains(submission.getId().toString()));
        verify(submissionRepository, times(1)).save(any(Submission.class));
    }

    @Test
    void testAddDuplicateSubmission() throws DeadlinePassedException, IllegalAccessException {
        when(submissionRepository.findAll())
                .thenReturn(Collections.singletonList(submission));

        ResponseEntity<String> response = submissionService.add(submission);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void testDeleteSubmissionNotFound() throws NotFoundException, IllegalAccessException, DeadlinePassedException {
        UUID id = UUID.randomUUID();
        when(submissionRepository.findById(id)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = submissionService.delete(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(submissionRepository, never()).delete(any(Submission.class));
    }

    @Test
    void testDeleteSubmissionSuccess() throws Exception {
        UUID id = submission.getId();
        when(submissionRepository.findById(id)).thenReturn(Optional.of(submission));
        when(authManager.getEmail()).thenReturn("example@gmail.com");
        when(httpRequestService.get("user/byEmail/example@gmail.com", Long.class, RequestType.USER)).thenReturn(1L);
        Track track = new Track();
        track.setSubmitDeadline("2024-02-06T23:59:59");
        when(trackService.getTrackById(10L)).thenReturn(track);

        ResponseEntity<Void> response = submissionService.delete(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(submissionRepository, times(1)).delete(submission);
    }

    @Test
    void testUpdateSubmissionNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        Submission updatedSubmission = new Submission();
        when(submissionRepository.findById(id)).thenReturn(Optional.empty());

        ResponseEntity<Submission> response = submissionService.update(id, updatedSubmission);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(submissionRepository, never()).save(any(Submission.class));
    }

    @Test
    void testUpdateSubmissionSuccess() throws Exception {
        UUID id = submission.getId();

        when(submissionRepository.findById(id)).thenReturn(Optional.of(submission));
        when(authManager.getEmail()).thenReturn("example@gmail.com");
        when(httpRequestService.get("user/byEmail/example@gmail.com", Long.class, RequestType.USER)).thenReturn(1L);
        Track track = new Track();
        track.setSubmitDeadline("2024-02-06T23:59:59");
        when(trackService.getTrackById(10L)).thenReturn(track);

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
        when(submissionRepository.findAll()).thenReturn(Collections.emptyList());

        boolean result = submissionService.checkDuplicateSubmissions(duplicateSubmission);

        assertTrue(result);
    }

    @Test
    void testCheckDuplicateSubmissions_WithDuplicates() throws DeadlinePassedException, IllegalAccessException {
        Submission duplicateSubmission = new Submission();
        duplicateSubmission.setTitle("Duplicate Title");
        Submission duplicateSubmission2 = new Submission();
        duplicateSubmission2.setTitle("Duplicate Title");
        when(submissionRepository.findAll()).thenReturn(List.of(duplicateSubmission2));

        boolean result = submissionService.checkDuplicateSubmissions(duplicateSubmission);

        assertFalse(result);
    }

    @Test
    void testGetSubmissions() throws DeadlinePassedException, IllegalAccessException {
        List<Submission> expectedSubmissions = Arrays.asList(new Submission(), new Submission());
        when(submissionRepository.findAll()).thenReturn(expectedSubmissions);

        ResponseEntity<List<Submission>> response = submissionService.get(null,
                null, null, null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedSubmissions, response.getBody());
    }

    @Test
    void testGetWithAllParametersNull() throws DeadlinePassedException, IllegalAccessException {
        when(submissionRepository.findAll())
                .thenReturn(Collections.singletonList(submission));

        ResponseEntity<List<Submission>> response = submissionService.get(null, null,
                null, null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void testGetWithSpecificEventId() throws DeadlinePassedException, IllegalAccessException {
        //Only eventId
        Long eventId = 1L;
        when(submissionRepository.findAll())
                .thenReturn(Collections.singletonList(submission));

        ResponseEntity<List<Submission>> response = submissionService.get(null, null,
                null, null, null, eventId, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void testGetWithTrackId() throws DeadlinePassedException, IllegalAccessException {
        Long trackId = 10L;
        when(submissionRepository.findAll())
                .thenReturn(Collections.singletonList(submission));
        ResponseEntity<List<Submission>> response = submissionService.get(
                null, null, null, null, trackId, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void testGetWithKeywords() throws DeadlinePassedException, IllegalAccessException {
        List<String> keywords = Arrays.asList("Keyword1", "Keyword2");
        when(submissionRepository.findAll())
                .thenReturn(Collections.singletonList(submission));
        ResponseEntity<List<Submission>> response = submissionService.get(null,
                null, null, keywords, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void testGetWithSubmittedBy() throws DeadlinePassedException, IllegalAccessException {
        Long submittedBy = 1L;
        submission.setSubmittedBy(1L);
        when(submissionRepository.findAll())
                .thenReturn(Collections.singletonList(submission));
        ResponseEntity<List<Submission>> response = submissionService.get(submittedBy,
                null, null, null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void testGetWithAuthors() throws DeadlinePassedException, IllegalAccessException {
        List<Long> authors = Arrays.asList(1L, 2L);
        when(submissionRepository.findAll())
                .thenReturn(Collections.singletonList(submission));
        ResponseEntity<List<Submission>> response = submissionService.get(null,
                authors, null, null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
    }

}
