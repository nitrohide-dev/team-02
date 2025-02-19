package nl.tudelft.sem.template.submission.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import javassist.NotFoundException;
import nl.tudelft.sem.template.api.SubmissionApi;
import nl.tudelft.sem.template.model.PaperType;
import nl.tudelft.sem.template.model.Submission;
import nl.tudelft.sem.template.submission.components.chain.DeadlinePassedException;
import nl.tudelft.sem.template.submission.components.chain.DuplicateSubmissionException;
import nl.tudelft.sem.template.submission.repositories.SubmissionRepository;
import nl.tudelft.sem.template.submission.services.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;


@RestController
public class SubmissionController implements SubmissionApi {
    private final SubmissionService submissionService;
    private final SubmissionRepository submissionRepository;

    /**
     * Submission controller constructor.
     *
     * @param submissionService    submission service
     * @param submissionRepository submission repository
     */
    @Autowired
    public SubmissionController(SubmissionService submissionService,
                                SubmissionRepository submissionRepository) {
        this.submissionService = submissionService;
        this.submissionRepository = submissionRepository;
    }

    /**
     * New submission.
     *
     * @param submissionData data of the submission (required)
     * @param file           the file to upload (required)
     * @return add a submission or return error status code if it fails
     */
    @Override
    public ResponseEntity<String> addSubmission(@RequestParam("submissionData") String submissionData,
                                                @RequestParam("file") MultipartFile file) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Submission submission = objectMapper.readValue(submissionData, Submission.class);
            if (!file.isEmpty() && file.getContentType().equals("text/plain")) {
                String filePath = saveFile(file);
                submission.setTextFilePath(filePath);
            } else {
                return ResponseEntity.badRequest().body(null);
            }
            return submissionService.add(submission);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (DeadlinePassedException e) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Saves the uploaded file.
     *
     * @param file the file that is being uploaded
     * @return the String of a directory of the text file
     */
    private String saveFile(MultipartFile file) {
        try {
            String directoryPath = "../../";

            String originalFilename = file.getOriginalFilename();
            String newFileName = UUID.randomUUID().toString() + "-" + originalFilename;
            String filePath = directoryPath + File.separator + newFileName;

            Path path = Paths.get(filePath);
            Files.copy(file.getInputStream(), path);

            return filePath;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Delete submission with a given id.
     *
     * @param submissionId Submission id to delete (required)
     * @return response ok if submission is deleted, error otherwise
     */
    @Override
    public ResponseEntity<Void> deleteSubmission(Long submissionId) {
        try {
            return submissionService.delete(submissionId);
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(401).build();
        } catch (NotFoundException e) {
            return ResponseEntity.status(404).build();
        } catch (DeadlinePassedException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Returns submission with a provided id.
     *
     * @param submissionId ID of submission to return (required)
     * @return submission if it is found for a given id, error otherwise
     */
    @Override
    public ResponseEntity<Submission> getSubmissionById(Long submissionId) {
        try {
            return submissionService.getById(submissionId);
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Returns list of submissions matching search criteria.
     *
     * @param submittedBy Filter by person who submitted (optional)
     * @param authors     Filter by author id (optional)
     * @param title       Filter by submission name (optional)
     * @param keywords    Filters by keywords (optional)
     * @param trackId     Filter by track id (optional)
     * @param eventId     Filter by event id (optional)
     * @param type        Filter by submission type (optional)
     * @return list of submissions. All submissions are returned if no criteria specified.
     */
    @Override
    public ResponseEntity<List<Submission>> submissionGet(Long submittedBy, List<Long> authors,
                                                          String title, List<String> keywords, Long trackId,
                                                          Long eventId, PaperType type) {

        return submissionService.get(submittedBy, authors, title,
                keywords, trackId, eventId, type);
    }

    /**
     * Updated submission with a provided id.
     *
     * @param submissionId     (required)
     * @param updateSubmission updated submission
     * @return response with updated submission if success, error otherwise
     */
    @Override
    public ResponseEntity<Submission> submissionSubmissionIdPut(Long submissionId,
                                                                Submission updateSubmission) {
        try {
            return submissionService.update(submissionId, updateSubmission);
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(401).build();
        } catch (NotFoundException e) {
            return ResponseEntity.status(404).build();
        } catch (DeadlinePassedException e) {
            return ResponseEntity.badRequest().build();
        } catch (DuplicateSubmissionException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
