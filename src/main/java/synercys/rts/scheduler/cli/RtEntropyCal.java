package synercys.rts.scheduler.cli;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import synercys.rts.framework.TaskSet;
import synercys.rts.scheduler.*;
import synercys.rts.scheduler.entropy.ApproximateEntropyCalculator;
import synercys.rts.scheduler.entropy.ScheduleEntropyCalculatorInterface;
import synercys.rts.scheduler.entropy.ShannonScheduleEntropyCalculator;
import synercys.rts.scheduler.entropy.UpperApproximateEntropyCalculator;
import synercys.rts.scheduler.entropy.tester.MassScheduleEntropyTester;
import synercys.rts.scheduler.entropy.tester.ScheduleEntropyTester;
import synercys.rts.util.JsonLogLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "rtenc", versionProvider = synercys.rts.RtsConfig.class, header = "@|blue | RT Schedule Entropy Calculator | |@")
public class RtEntropyCal implements Callable {
    protected static final Logger loggerConsole = LogManager.getLogger("console");

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Print usage help and exit.")
    boolean usageHelpRequested;

    @CommandLine.Option(names = {"-V", "--version"}, versionHelp = true, description = "Print version information and exit.")
    boolean versionHelpRequested;

    @CommandLine.Option(names = {"-i", "--in"}, required = true, description = "One or more files that contain tasksets.")
    protected List<String> taskInputFileList = new ArrayList<>();

    @CommandLine.Option(names = {"-o", "--out"}, required = false, description = "A file path a prefix name for storing test results (the file extension is ignored).")
    String outputFilePrefixPath = "";

    @CommandLine.Option(names = {"-d", "--duration"}, required = false, description = "Simulation duration in 0.1ms (e.g., 10 is 1ms).")
    protected long simDuration = 0;

    @CommandLine.Option(names = {"-p", "--policy"}, required = true, description = "Scheduling policy (\"EDF\", \"RM\", \"TaskShuffler\" or \"ReOrder\").")
    protected String schedulingPolicy = "";

    @CommandLine.Option(names = {"-v", "--evar"}, required = false, description = "Enable execution time variation.")
    protected boolean optionExecutionVariation = false;

    @CommandLine.Option(names = {"-e", "--entropy"}, required = true, description = "Entropy algorithm (\"Shannon\", \"UApEn\" or \"ApEn\").")
    protected String entropyAlgorithm = "";

    @CommandLine.Option(names = {"-r", "--rounds"}, required = false, description = "The number of schedule rounds to be tested.")
    protected int optionRounds = 1;

    @CommandLine.Option(names = {"-c", "--case"}, required = false, description = "Test case (\"FULL_HP\").")
    String testCase = "";

    //protected TaskSet taskSet = null;

    public static void main(String... args) {
        /* A few command examples. Uncomment only one at a time to test it. */
        //args = new String[]{"-i", "sampleLogs\\60tasksets.txt", "-d", "1", "-t", "-c", "\"" + EvaluationCaseTester.TEST_CASE_SIM_DURATION + "\""};
        //args = new String[]{"-i", "sampleLogs/taskset1_zero_offset.txt", "-d", "1", "-p", "EDF", "-a", "2", "-o", "sampleLogs/test.xlsx"};
        //args = new String[]{"-h"};
        //-i ../../sampleLogs/60tasksets_zero_offset.txt  -d=20 -e

        CommandLine.call(new RtEntropyCal(), System.err, args);
    }

    @Override
    public Object call() throws Exception {
        /*===== Load tasksets =====*/
        TaskSetContainer taskSetContainer = new TaskSetContainer();
        for (String taskInputFile : taskInputFileList) {
            JsonLogLoader jsonLogLoader = new JsonLogLoader(taskInputFile);
            taskSetContainer.addTaskSets(((TaskSetContainer)jsonLogLoader.getResult()).getTaskSets());
        }

        if (testCase.equalsIgnoreCase("")) {
            TaskSet taskSet = taskSetContainer.getTaskSets().get(0);
            loggerConsole.info("No test case selected. Testing one task set.");
            loggerConsole.info(taskSet.toString());
            loggerConsole.info("");
            loggerConsole.info("Length of each schedule = {}", simDuration);
            loggerConsole.info("Rounds to estimate entropy = {}", optionRounds);

            ScheduleEntropyTester entropyTester = new ScheduleEntropyTester(taskSet, schedulingPolicy, entropyAlgorithm, optionExecutionVariation);
            double finalEntropy = entropyTester.run(simDuration, optionRounds);
            if (finalEntropy == -1) {
                loggerConsole.error("Unknown entropy calculator: {}", entropyAlgorithm);
                return null;
            }
            loggerConsole.info("{} entropy = {}", entropyAlgorithm, finalEntropy);
        } else {
            loggerConsole.info("Run mass test for the test case {}.", testCase);
            long startTime = System.currentTimeMillis();
            MassScheduleEntropyTester massScheduleEntropyTester = new MassScheduleEntropyTester(outputFilePrefixPath, taskSetContainer);
            massScheduleEntropyTester.setTestRounds(optionRounds);
            massScheduleEntropyTester.setSchedulingPolicy(schedulingPolicy);
            massScheduleEntropyTester.setEntropyAlgorithm(entropyAlgorithm);
            // TODO: set duration
            massScheduleEntropyTester.run(testCase);
            long estimatedTime = System.currentTimeMillis() - startTime;
            loggerConsole.info("Finished testing {} task sets. ({} ms)", taskSetContainer.size(), estimatedTime);
        }


        return null;
    }

}
