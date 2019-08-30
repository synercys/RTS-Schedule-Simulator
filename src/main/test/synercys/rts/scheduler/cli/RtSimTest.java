package synercys.rts.scheduler.cli;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RtSimTest {

    @Test
    void main() {
        String argString = "-i sampleLogs/5tasks.tasksets -d 10000 -p RM";
        RtSim.main(argString.split(" "));
    }
}