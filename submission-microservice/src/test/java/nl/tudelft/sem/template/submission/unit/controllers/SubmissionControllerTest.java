package nl.tudelft.sem.template.submission.unit.controllers;

import javassist.NotFoundException;
import nl.tudelft.sem.template.model.PaperType;
import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.submission.components.chain.DeadlinePassedException;
import nl.tudelft.sem.template.submission.controllers.SubmissionController;
import nl.tudelft.sem.template.submission.repositories.SubmissionRepository;
import nl.tudelft.sem.template.submission.services.SubmissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SubmissionControllerTest {
    @Mock
    private SubmissionService submissionService;

    @Mock
    private SubmissionRepository submissionRepository;

    @MockBean
    Files files;

    @InjectMocks
    private SubmissionController submissionController;

    private Submission submission;
    private String submissionData;
    private MockMultipartFile mockFile;


    @BeforeEach
    void setUp() {
        submissionData = new String("{\n"
                +
                "    \"title\": \"Sample Paper Title\",\n"
                +
                "    \"authors\": [1, 2],\n"
                +
                "    \"abstract\": \"This is a sample abstract of the paper.\",\n"
                +
                "    \"keywords\": [\"keyword1\", \"keyword2\"],\n"
                +
                "    \"link\": \"https://github.com/sample-repo\",\n"
                +
                "    \"trackId\": 1\n"
                +
                "}");
        mockFile = new MockMultipartFile(
                "meow",
                "meowmeow.txt",
                "text/plain",
                "contentttttt".getBytes()
        );
    }

    @Test
    void testAddSubmission() throws Exception {
        when(submissionService.add(any(Submission.class))).thenReturn(ResponseEntity.ok("Submission Added"));
        ResponseEntity<String> response = submissionController.addSubmission(submissionData, mockFile);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Submission Added", response.getBody());
    }

    @Test
    void testDeleteSubmission() throws Exception {
        Long submissionId = 123L;
        when(submissionService.delete(submissionId)).thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Void> response = submissionController.deleteSubmission(submissionId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testSaveFile() throws InvocationTargetException, IllegalAccessException, IOException, NoSuchMethodException {
        //private method:((
        Method saveFileMethod = SubmissionController.class.getDeclaredMethod("saveFile", MultipartFile.class);
        saveFileMethod.setAccessible(true);
        String filePath = (String) saveFileMethod.invoke(submissionController, mockFile);
        assertNotNull(filePath);
        assertTrue(filePath.contains("meowmeow.txt"));
        Path path = Paths.get(filePath);
        assertTrue(Files.exists(path));
        Files.deleteIfExists(path);
    }

    @Test
    void testDeleteSubmissionNotFoundException() throws Exception {
        Long submissionId = 123L;
        when(submissionService.delete(submissionId)).thenThrow(new NotFoundException("Not found"));
        ResponseEntity<Void> response = submissionController.deleteSubmission(submissionId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testUpdateSubmission_NotFoundException() throws Exception {
        Long submissionId = 123L;
        Submission updatedSubmission = new Submission();
        when(submissionService.update(submissionId, updatedSubmission))
                .thenThrow(new NotFoundException("Not Found"));

        ResponseEntity<Submission> response = submissionController.submissionSubmissionIdPut(submissionId,
                updatedSubmission);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testDeleteSubmissionNoPermission() throws Exception {
        Long submissionId = 123L;
        when(submissionService.delete(submissionId)).thenThrow(IllegalAccessException.class);
        ResponseEntity<Void> response = submissionController.deleteSubmission(submissionId);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }


    //    How to make a file corrupt/unreadable?
    //
    //    @Test
    //    void testSaveFileException() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
    //        mockFile = new MockMultipartFile(
    //                "/",
    //                "/",
    //                "",
    //                "/".getBytes()
    //        );
    //        Method saveFileMethod = SubmissionController.class.getDeclaredMethod("saveFile", MultipartFile.class);
    //        saveFileMethod.setAccessible(true);
    //
    //        assertThrows(IOException.class, () -> {
    //            try {
    //                saveFileMethod.invoke(submissionController, mockFile);
    //            } catch (InvocationTargetException e) {
    //                throw e.getTargetException();
    //            }
    //        });
    //    }

    @Test
    void testAddSubmissionBadResponse() {
        mockFile = new MockMultipartFile(
                "/",
                "/",
                "",
                "/".getBytes()
        );
        ResponseEntity<String> response = submissionController.addSubmission(submissionData, mockFile);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testDeleteSubmissionDeadlinePassedException()
            throws Exception {
        Long submissionId = 123L;
        when(submissionService.delete(submissionId)).thenThrow(new DeadlinePassedException("Deadline has passed."));
        ResponseEntity<Void> response = submissionController.deleteSubmission(submissionId);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testGetSubmissionByIdReturnsSubmission() throws Exception {
        Long submissionId = 123L;
        ResponseEntity<Submission> expectedResponse = ResponseEntity.ok(new Submission());

        when(submissionService.getById(submissionId)).thenReturn(expectedResponse);

        ResponseEntity<Submission> result = submissionController.getSubmissionById(submissionId);

        assertEquals(expectedResponse, result);
        verify(submissionService).getById(submissionId);
    }

    @Test
    void testGetSubmissionByIdBadRequest() throws Exception {
        Long submissionId = 123L;

        when(submissionService.getById(submissionId)).thenThrow(IllegalArgumentException.class);

        ResponseEntity<Submission> result = submissionController.getSubmissionById(submissionId);

        assertEquals(400, result.getStatusCodeValue());
    }

    @Test
    void testGetSubmissionByIdUnauthorized() throws Exception {
        Long submissionId = 123L;

        when(submissionService.getById(submissionId)).thenThrow(IllegalAccessException.class);

        ResponseEntity<Submission> result = submissionController.getSubmissionById(submissionId);

        assertEquals(401, result.getStatusCodeValue());
    }

    @Test
    void testUpdateSubmissionIllegalAccessException()
            throws Exception {
        Long submissionId = 123L;
        Submission updatedSubmission = new Submission();
        when(submissionService.update(submissionId, updatedSubmission))
                .thenThrow(new IllegalAccessException("This is illegal."));

        ResponseEntity<Submission> response = submissionController.submissionSubmissionIdPut(submissionId,
                updatedSubmission);

        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    void testUpdateSubmissionDeadlinePassedException()
            throws Exception {
        Long submissionId = 123L;
        Submission updatedSubmission = new Submission();
        when(submissionService.update(submissionId, updatedSubmission))
                .thenThrow(new DeadlinePassedException("It's over."));

        ResponseEntity<Submission> response = submissionController.submissionSubmissionIdPut(submissionId,
                updatedSubmission);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testUpdateSubmissionSuccess() throws Exception {
        Long submissionId = 123L;
        Submission updatedSubmission = new Submission();
        when(submissionService.update(submissionId, updatedSubmission))
                .thenReturn(new ResponseEntity<Submission>(HttpStatus.ACCEPTED));

        ResponseEntity<Submission> response = submissionController.submissionSubmissionIdPut(submissionId,
                updatedSubmission);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
    }

    @Test
    void testSubmissionGetResponse() {
        Long submittedBy = 123L;
        List<Long> authors = new ArrayList<>();
        authors.add(456L);
        String title = "Sample Title";
        List<String> keywords = new ArrayList<>();
        keywords.add("keyword1");
        Long trackId = 789L;
        Long eventId = 987L;
        PaperType type = PaperType.FULL_PAPER;
        ResponseEntity<List<Submission>> expectedResponse = ResponseEntity.ok(new ArrayList<>());
        when(submissionService.get(submittedBy, authors, title, keywords, trackId, eventId, type))
                .thenReturn(expectedResponse);
        ResponseEntity<List<Submission>> result = submissionController.submissionGet(submittedBy, authors, title,
                keywords, trackId, eventId, type);
        assertEquals(expectedResponse, result);
        verify(submissionService).get(submittedBy, authors, title, keywords, trackId, eventId, type);
    }
}






