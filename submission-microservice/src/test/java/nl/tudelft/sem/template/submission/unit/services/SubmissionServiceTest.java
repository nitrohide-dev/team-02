package nl.tudelft.sem.template.submission.unit.services;


import javassist.NotFoundException;
import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.submission.components.chain.SubmissionValidator;
import nl.tudelft.sem.template.submission.repositories.SubmissionRepository;
import nl.tudelft.sem.template.submission.services.StatisticsService;
import nl.tudelft.sem.template.submission.services.SubmissionService;
import nl.tudelft.sem.template.submission.services.TrackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
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
    private SubmissionValidator submissionValidator;

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


    }

    @Test
    void testAddSubmission() {
        when(submissionRepository.save(any(Submission.class))).thenReturn(submission);
        ResponseEntity<String> response = submissionService.add(submission);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody().contains(submission.getId().toString()));
        verify(submissionRepository, times(1)).save(any(Submission.class));
    }

    @Test
    void testAddDuplicateSubmission() {
        when(submissionRepository.findAll(any(Specification.class)))
                .thenReturn(Collections.singletonList(new Submission()));

        ResponseEntity<String> response = submissionService.add(submission);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void testDeleteSubmissionNotFound() throws NotFoundException, IllegalAccessException {
        UUID id = UUID.randomUUID();
        when(submissionRepository.findById(id)).thenReturn(Optional.empty());

        ResponseEntity<Void> response = submissionService.delete(id, 1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(submissionRepository, never()).delete(any(Submission.class));
    }

    @Test
    void testDeleteSubmissionSuccess() throws Exception {
        UUID id = submission.getId();
        when(submissionRepository.findById(id)).thenReturn(Optional.of(submission));

        ResponseEntity<Void> response = submissionService.delete(id, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(submissionRepository, times(1)).delete(submission);
    }

    @Test
    void testUpdateSubmissionNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        Submission updatedSubmission = new Submission();
        when(submissionRepository.findById(id)).thenReturn(Optional.empty());

        ResponseEntity<Submission> response = submissionService.update(id, 1L, updatedSubmission);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(submissionRepository, never()).save(any(Submission.class));
    }

    @Test
    void testUpdateSubmissionSuccess() throws Exception {
        UUID id = submission.getId();
        Submission updatedSubmission = new Submission();
        when(submissionRepository.findById(id)).thenReturn(Optional.of(submission));

        ResponseEntity<Submission> response = submissionService.update(id, 1L, updatedSubmission);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(submissionRepository, times(1)).save(submission);
    }

    @Test
    void testCheckDuplicateSubmissions_NoDuplicates() {
        Submission duplicateSubmission = new Submission();
        duplicateSubmission.setTitle("Duplicate Title");
        duplicateSubmission.setEventId(1L);
        when(submissionRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        boolean result = submissionService.checkDuplicateSubmissions(duplicateSubmission);

        assertTrue(result);
    }

    @Test
    void testCheckDuplicateSubmissions_WithDuplicates() {
        Submission duplicateSubmission = new Submission();
        duplicateSubmission.setTitle("Duplicate Title");
        Submission duplicateSubmission2 = new Submission();
        duplicateSubmission2.setTitle("Duplicate Title");
        when(submissionRepository.findAll(any(Specification.class))).thenReturn(List.of(duplicateSubmission2));

        boolean result = submissionService.checkDuplicateSubmissions(duplicateSubmission);

        assertFalse(result);
    }

    @Test
    void testGetSubmissions() {
        List<Submission> expectedSubmissions = Arrays.asList(new Submission(), new Submission());
        when(submissionRepository.findAll(any(Specification.class))).thenReturn(expectedSubmissions);

        ResponseEntity<List<Submission>> response = submissionService.get(null, null,
                null, null, null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedSubmissions, response.getBody());
    }

    @Test
    void testGetSubmissionsWithCriteria() {
        Long eventId = 1L;
        String title = "Test Paper";
        List<Submission> expectedSubmissions = Collections.singletonList(new Submission());
        when(submissionRepository.findAll(any(Specification.class))).thenReturn(expectedSubmissions);
        ResponseEntity<List<Submission>> response = submissionService.get(null, null,
                null, title, null, null, eventId, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedSubmissions, response.getBody());
    }

    @Test
    void testGetWithAllParametersNull() {
        when(submissionRepository.findAll(any(Specification.class)))
                .thenReturn(Collections.singletonList(submission));

        ResponseEntity<List<Submission>> response = submissionService.get(null, null, null,
                null, null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void testGetWithSpecificEventId() {
        //Only eventId
        Long eventId = 123L;
        when(submissionRepository.findAll(any(Specification.class)))
                .thenReturn(Collections.singletonList(submission));

        ResponseEntity<List<Submission>> response = submissionService.get(null, null, null,
                null, null, null, eventId, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void testGetWithTrackId() {
        Long trackId = 1L;
        when(submissionRepository.findAll(any(Specification.class)))
                .thenReturn(Collections.singletonList(submission));
        ResponseEntity<List<Submission>> response = submissionService.get(null,
                null, null, null, null, trackId, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void testGetWithKeywords() {
        List<String> keywords = Arrays.asList("Keyword1");
        when(submissionRepository.findAll(any(Specification.class)))
                .thenReturn(Collections.singletonList(submission));
        ResponseEntity<List<Submission>> response = submissionService.get(null, null,
                null, null, keywords, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void testGetWithId() {
        UUID id = UUID.randomUUID();
        when(submissionRepository.findAll(any(Specification.class)))
                .thenReturn(Collections.singletonList(submission));
        ResponseEntity<List<Submission>> response = submissionService.get(id, null,
                null, null, null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void testGetWithSubmittedBy() {
        Long submittedBy = 1L;
        when(submissionRepository.findAll(any(Specification.class)))
                .thenReturn(Collections.singletonList(submission));
        ResponseEntity<List<Submission>> response = submissionService.get(null, submittedBy,
                null, null, null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void testGetWithAuthors() {
        List<Long> authors = Arrays.asList(1L, 2L);
        when(submissionRepository.findAll(any(Specification.class)))
                .thenReturn(Collections.singletonList(submission));
        ResponseEntity<List<Submission>> response = submissionService.get(null, null,
                authors, null, null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
    }

}
