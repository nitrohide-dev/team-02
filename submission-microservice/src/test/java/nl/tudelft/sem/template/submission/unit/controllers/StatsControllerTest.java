package nl.tudelft.sem.template.submission.unit.controllers;

import javassist.NotFoundException;
import nl.tudelft.sem.template.model.Statistics;
import nl.tudelft.sem.template.submission.Application;
import nl.tudelft.sem.template.submission.controllers.StatsController;
import nl.tudelft.sem.template.submission.repositories.StatisticsRepository;
import nl.tudelft.sem.template.submission.services.StatisticsService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)

public class StatsControllerTest {
    @MockBean
    private StatisticsService service;

    @MockBean
    private StatisticsRepository repository;

    @Autowired
    @InjectMocks
    private StatsController controller;

    private static Statistics statistics1;
    private static Statistics statistics2;

    @BeforeAll
    private static void generalSetup() {
        statistics1 = new Statistics();
        statistics1.setId(1L);
        statistics1.setTotalSubmissions(3L);
        statistics1.setAccepted(1L);
        statistics1.setOpen(1L);
        statistics1.setRejected(1L);

        statistics2 = new Statistics();
        statistics2.setId(2L);
        statistics2.setTotalSubmissions(2L);
        statistics2.setAccepted(1L);
        statistics2.setRejected(1L);
        statistics2.setAverageNumberOfAuthors(10L);
    }

    @BeforeEach
    void setup() throws NotFoundException, IllegalAccessException {
        when(service.getStatistics(1L, 1L)).thenReturn(statistics1);
        when(service.getStatistics(2L, 2L)).thenReturn(statistics2);
        when(service.getStatistics(1L, 0L)).thenThrow(NotFoundException.class);
        when(service.getStatistics(0L, 1L)).thenThrow(IllegalAccessException.class);

        when(repository.findAll()).thenReturn(List.of(statistics1, statistics2));
    }

    @Test
    void testStatsGet() {
        ResponseEntity<List<Statistics>> response = controller.statsGet();
        assertEquals(List.of(statistics1, statistics2), response.getBody());
    }

    @Test
    void testTrackStatistics() {
        ResponseEntity<Statistics> stats = controller.trackStatisticsGet(1L, 1L);
        assertEquals(statistics1, stats.getBody());
    }

    @Test
    void testEventStatistics() {
        ResponseEntity<Statistics> stats = controller.trackStatisticsGet(2L, 2L);
        assertEquals(statistics2, stats.getBody());
    }

    @Test
    void testNotFoundException() {
        ResponseEntity<Statistics> out = controller.trackStatisticsGet(0L, 1L);
        assertEquals(HttpStatus.NOT_FOUND, out.getStatusCode());
    }

    @Test
    void testIllegalAccessException() {
        ResponseEntity<Statistics> out = controller.trackStatisticsGet(1L, 0L);
        assertEquals(HttpStatus.UNAUTHORIZED, out.getStatusCode());
    }
}
