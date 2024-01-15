package nl.tudelft.sem.template.submission.unit.controllers;

import javassist.NotFoundException;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SubmissionControllerTest {
    @Mock
    private SubmissionService submissionService;

    @Mock
    private SubmissionRepository submissionRepository;

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
    void testAddSubmission() throws DeadlinePassedException, IllegalAccessException {
        when(submissionService.add(any(Submission.class))).thenReturn(ResponseEntity.ok("Submission Added"));
        ResponseEntity<String> response = submissionController.addSubmission(submissionData, mockFile);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Submission Added", response.getBody());
    }

    @Test
    void testDeleteSubmission() throws NotFoundException, IllegalAccessException, DeadlinePassedException {
        UUID submissionId = UUID.randomUUID();
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
    void testDeleteSubmissionNotFoundException() throws NotFoundException, IllegalAccessException, DeadlinePassedException {
        UUID submissionId = UUID.randomUUID();
        when(submissionService.delete(submissionId)).thenThrow(new NotFoundException("Not found"));
        ResponseEntity<Void> response = submissionController.deleteSubmission(submissionId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testUpdateSubmission_NotFoundException() throws NotFoundException, IllegalAccessException, DeadlinePassedException {
        UUID submissionId = UUID.randomUUID();
        Submission updatedSubmission = new Submission();
        when(submissionService.update(submissionId, updatedSubmission))
                .thenThrow(new NotFoundException("Not Found"));

        ResponseEntity<Submission> response = submissionController.submissionSubmissionIdPut(submissionId,
                updatedSubmission);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testDeleteSubmissionNoPermission() throws NotFoundException, DeadlinePassedException, IllegalAccessException {
        UUID submissionId = UUID.randomUUID();
        when(submissionService.delete(submissionId)).thenThrow(IllegalAccessException.class);
        ResponseEntity<Void> response = submissionController.deleteSubmission(submissionId);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

}
