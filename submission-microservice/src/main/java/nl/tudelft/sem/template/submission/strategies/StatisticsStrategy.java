package nl.tudelft.sem.template.submission.strategies;

import javassist.NotFoundException;
import nl.tudelft.sem.template.model.Statistics;
import org.springframework.stereotype.Component;

@Component
public interface StatisticsStrategy {
    Statistics getStatistics(Long id) throws NotFoundException;
}
