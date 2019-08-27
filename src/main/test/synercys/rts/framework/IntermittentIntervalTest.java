package synercys.rts.framework;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IntermittentIntervalTest {

    @Test
    void cascadeInterval() {
        IntermittentInterval intermittentInterval = new IntermittentInterval();
        intermittentInterval.cascadeInterval(new Interval(-10, 10));
        intermittentInterval.cascadeInterval(new Interval(9, 10));   // [-10, 11)
        assertEquals(21, intermittentInterval.getTotalLength());

        intermittentInterval.cascadeInterval(new Interval(-10, -9));   // [-10, 12)
        assertEquals(22, intermittentInterval.getTotalLength());

        intermittentInterval.cascadeInterval(new Interval(-30, -20));  // {[-30, -20), [-10, 11)}
        assertEquals(32, intermittentInterval.getTotalLength());
        assertEquals(2, intermittentInterval.getIntervals().size());

        intermittentInterval.cascadeInterval(new Interval(-35, 15));   // {[-35, -35+50+32=47)}
        assertEquals(1, intermittentInterval.getIntervals().size());
        assertEquals(35+47, intermittentInterval.getTotalLength());
        assertEquals(-35, intermittentInterval.getBegin());
        assertEquals( 47, intermittentInterval.getEnd());
    }
}