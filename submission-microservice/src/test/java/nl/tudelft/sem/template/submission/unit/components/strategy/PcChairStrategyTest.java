package nl.tudelft.sem.template.submission.unit.components.strategy;

import javassist.NotFoundException;
import nl.tudelft.sem.template.model.Statistics;
import nl.tudelft.sem.template.model.Track;
import nl.tudelft.sem.template.submission.Application;
import nl.tudelft.sem.template.submission.components.strategy.PcChairStrategy;
import nl.tudelft.sem.template.submission.controllers.SubmissionController;
import nl.tudelft.sem.template.submission.repositories.StatisticsRepository;
import nl.tudelft.sem.template.submission.services.SubmissionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PcChairStrategyTest {
    @MockBean
    private SubmissionService submissionService;
    @MockBean
    private SubmissionController submissionController;
    @MockBean
    StatisticsRepository statisticsRepository;

    @Autowired
    @InjectMocks
    private PcChairStrategy pcChairStrategy;

    @Test
    void testGetStatistics_StatisticsExist_ReturnsStatistics() throws NotFoundException {
        long trackId = 1L;
        Statistics expectedStatistics = new Statistics();
        when(statisticsRepository.findById(trackId))
                .thenReturn(Optional.of(expectedStatistics));

        Track track = new Track();
        track.setId(trackId);

        Statistics result = pcChairStrategy.getStatistics(track);

        assertEquals(expectedStatistics, result);
        verify(statisticsRepository).findById(trackId);
    }

    @Test
    void testGetStatistics_StatisticsDoNotExist_ThrowsNotFoundException() {
        long trackId = 1L;
        when(statisticsRepository.findById(trackId))
                .thenReturn(Optional.empty());

        Track track = new Track();
        track.setId(trackId);

        assertThrows(NotFoundException.class, () -> pcChairStrategy.getStatistics(track));
        verify(statisticsRepository).findById(trackId);
    }

}