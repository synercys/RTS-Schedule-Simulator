package synercys.rts.framework;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class HistogramTest {

    Histogram histogram = new Histogram();

    @Test
    void touch() {
        Histogram histogram = new Histogram();

        /* enter 50 occurrences in the even number positions */
        for (int i=0; i<100; i+=2)
            histogram.touch(i);

        for (int i=0; i<100; i++) {
            if (i%2 == 0) {
                assertEquals(histogram.getValue(i), 1);
            } else {
                assertEquals(histogram.getValue(i), 0);
            }
        }

        /* enter another 100 occurrences at random positions (within 0-30) */
        for (int i=0; i<100; i++) {
            histogram.touch(new Random().nextInt(31)); // [0...30]);
        }

        double occurrenceSum = 0.0;
        for (int i=0; i<100; i++) {
            occurrenceSum += histogram.getValue(i);
        }

        assertEquals(occurrenceSum,150.0);

    }

    @Test
    void touchInterval() {
    }

    @Test
    void touchIntervals() {
    }

    @Test
    void getBegin() {
    }

    @Test
    void getEnd() {
    }

    @Test
    void getValue() {
        /* enter 50 occurrences at a specific position=50 */
        int position = 50;
        for (int i=0; i<100; i++)
            histogram.touch(position);

        for (int i=0; i<100; i++) {
            if (i == position)
                assertEquals(histogram.getValue(i), 100);
            else {
                assertEquals(histogram.getValue(i), 0);
            }
        }
    }

    @Test
    void getMostWeightedValue() {
    }

    @Test
    void getMostWeightedIntervals() {
    }

    @Test
    void getLeastWeightedIntervals() {
    }
}