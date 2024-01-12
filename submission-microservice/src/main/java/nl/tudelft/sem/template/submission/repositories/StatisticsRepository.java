package nl.tudelft.sem.template.submission.repositories;

import nl.tudelft.sem.template.model.Statistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatisticsRepository extends JpaRepository<Statistics, Long> {
}
