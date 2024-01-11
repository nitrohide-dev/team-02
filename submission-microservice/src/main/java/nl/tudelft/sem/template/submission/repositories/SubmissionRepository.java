package nl.tudelft.sem.template.submission.repositories;

import nl.tudelft.sem.template.model.ReturnedSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SubmissionRepository extends JpaRepository<ReturnedSubmission, UUID> {
}
