package synercys.rts.cli;

import picocli.CommandLine;
import synercys.rts.util.LogHandler;

import java.util.concurrent.Callable;

/**
 * Created by cy on 2/19/2018.
 */
@CommandLine.Command(name = "rtsim", header = "%n@|Simulate RT schedule|@")
public class RtSim implements Callable {
    protected String[] args;

    @CommandLine.Option(names = {"-i", "--in"}, required = true, description = "A file that contains task configurations.")
    String taskInputFile = "";

    @CommandLine.Option(names = {"-o", "--out"}, required = false, description = "A file for storing schedule simulation output.")
    String scheduleOutputFile = "";

    @CommandLine.Option(names = {"-d", "--duration"}, required = true, description = "Simulation duration in 10ms.")
    long simDuration = 0;


    public static void main(String... args) {
        //String[] testArgs = { "-n", "1"};
        String[] testArgs = {"-i", "D:\\myProgram\\Java\\RTS-Schedule-Simulator\\sampleLogs\\tasks.txt", "-d", "1000"};
        args = testArgs;
        CommandLine.call(new RtSim(), System.err, args);
    }

    @Override
    public Object call() throws Exception {
        System.out.println("Hello, " + taskInputFile);
        LogHandler logHandler = new LogHandler();
        logHandler.parseLog(taskInputFile);
        return null;
    }
}
