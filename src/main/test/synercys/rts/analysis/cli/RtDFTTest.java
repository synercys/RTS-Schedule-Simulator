package synercys.rts.analysis.cli;

import org.junit.jupiter.api.Test;
import synercys.rts.scheduler.cli.RtSim;

import static org.junit.jupiter.api.Assertions.*;

class RtDFTTest {

    @Test
    void main() {
        String argString = "-i sampleLogs/5tasks.tasksets -d 10000 -c VARIED_SCHEDULE_LENGTH -o test";
        RtDFT.main(argString.split(" "));

    }
}