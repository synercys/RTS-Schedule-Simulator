package synercys.rts.scheduler.cli;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RtEntropyCalTest {

    @Test
    void main() {
        //String argString = "-i sampleLogs/5tasks.tasksets -d 10000 -p taskshuffler -v";
        String argString = "-i experiments/tasksets/hp20.tasksets -d 20 -r 1000000 -p reorder -e Shannon";
        RtEntropyCal.main(argString.split(" "));
    }
}