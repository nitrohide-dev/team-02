package nl.tudelft.sem.template.submission.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.model.SubmissionStatus;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.Valid;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor

public class Submission {
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime updated;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime created;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private SubmissionStatus status;

    private Long submittedBy;
    private String title;

    @Valid
    private List<Long> authors;

    private String paperAbstract;

    @Valid
    private List<String> keywords;

    private org.springframework.core.io.Resource file;

    private String link;

    public Submission(String title) {
        this.title = title;
    }

    public String toString() {
        return "Submission{}";
    }
}
