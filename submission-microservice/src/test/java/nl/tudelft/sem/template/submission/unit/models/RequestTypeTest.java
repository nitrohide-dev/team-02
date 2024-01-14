package nl.tudelft.sem.template.submission.unit.models;

import nl.tudelft.sem.template.submission.models.RequestType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RequestTypeTest {

    @Test
    void testGetValue() {
        Assertions.assertEquals("review", RequestType.REVIEW.getValue());
        assertEquals("user", RequestType.USER.getValue());
    }

    @Test
    void testToString() {
        assertEquals("review", RequestType.REVIEW.toString());
        assertEquals("user", RequestType.USER.toString());
    }
}
