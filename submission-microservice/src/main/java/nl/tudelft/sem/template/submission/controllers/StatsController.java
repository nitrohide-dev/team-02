package nl.tudelft.sem.template.submission.controllers;


import javassist.NotFoundException;
import nl.tudelft.sem.template.api.StatsApi;
import nl.tudelft.sem.template.model.Statistics;
import nl.tudelft.sem.template.submission.components.chain.DeadlinePassedException;
import nl.tudelft.sem.template.submission.repositories.StatisticsRepository;
import nl.tudelft.sem.template.submission.services.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;


@RestController
public class StatsController implements StatsApi {
    private final StatisticsRepository statisticsRepository;
    private final StatisticsService statisticsService;

    @Autowired
    public StatsController(StatisticsService statisticsService,
                           StatisticsRepository statisticsRepository) {
        this.statisticsService = statisticsService;
        this.statisticsRepository = statisticsRepository;
    }

    @Override
    public ResponseEntity<List<Statistics>> statsGet() {
        return ResponseEntity.of(Optional.of(statisticsRepository.findAll()));
    }


    @Override
    public ResponseEntity<Statistics> trackOrEventStatisticsGet(Long trackId) {
        try {
            Statistics output = statisticsService.getStatistics(trackId);
            return ResponseEntity.of(Optional.of(output));
        } catch (Exception e) {
            return handleException(e);
        }
    }

    private ResponseEntity<Statistics> handleException(Exception e) {
        if (e instanceof IllegalAccessException) {
            return ResponseEntity.status(401).build();
        } else if (e instanceof NotFoundException) {
            return ResponseEntity.status(404).build();
        } else if (e instanceof DeadlinePassedException) {
            return ResponseEntity.badRequest().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
