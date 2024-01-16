package nl.tudelft.sem.template.submission.repositories;

import nl.tudelft.sem.template.model.PaperType;
import nl.tudelft.sem.template.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long>, JpaSpecificationExecutor<Submission> {
    @Query("select s from Submission s where "
            + "(?1 is null or s.submittedBy = ?1)"
            + " and ((?2) is null or exists (select a from s.authors a where a in (?2)))"
            + " and (?3 is null or lower(s.title) like concat('%', lower(?3), '%'))"
            + " and ((?4) is null or exists (select k from s.keywords k where k in (?4)))"
            + " and ((?5 is null or s.trackId = ?5))"
            + " and ((?6 is null or s.eventId = ?6))"
            + " and (?7 is null or s.type = ?7)")
    List<Submission> findAllMatching(Long submittedBy, List<Long> authors,
                                     String title, List<String> keywords, Long trackId,
                                     Long eventId, PaperType type) throws IllegalArgumentException;

}
