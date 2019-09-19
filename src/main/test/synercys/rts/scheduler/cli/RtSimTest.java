package synercys.rts.scheduler.cli;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RtSimTest {

    @Test
    void main() {
        String argString = "-i sampleLogs/5tasks.tasksets -d 10000 -p RM";
        RtSim.main(argString.split(" "));
    }

    @Test
    void testTaskShuffler() {
        String argString = "-i sampleLogs/5tasks.tasksets -d 10000 -p taskshuffler";
        RtSim.main(argString.split(" "));
    }

    @Test
    void testReorder() {
        String argString = "-i sampleLogs/5tasks.tasksets -d 10000 -p reorder";
        RtSim.main(argString.split(" "));
    }
}