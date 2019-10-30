package synercys.rts.scheduler.cli;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import synercys.rts.framework.TaskSet;
import synercys.rts.scheduler.*;
import synercys.rts.scheduler.entropy.ApproximateEntropyCalculator;
import synercys.rts.scheduler.entropy.ScheduleEntropyCalculatorInterface;
import synercys.rts.scheduler.entropy.ShannonScheduleEntropyCalculator;
import synercys.rts.scheduler.entropy.UASEntropyCalculator;
import synercys.rts.scheduler.entropy.tester.ScheduleEntropyTester;
import synercys.rts.util.JsonLogLoader;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "rtenc", versionProvider = synercys.rts.RtsConfig.class, header = "@|blue | RT Schedule Entropy Calculator | |@")
public class RtEntropyCal implements Callable {
    protected static final Logger loggerConsole = LogManager.getLogger("console");

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Print usage help and exit.")
    boolean usageHelpRequested;

    @CommandLine.Option(names = {"-V", "--version"}, versionHelp = true, description = "Print version information and exit.")
    boolean versionHelpRequested;

    @CommandLine.Option(names = {"-i", "--in"}, required = true, description = "A file that contains taskset parameters.")
    protected String taskInputFile = "";

    @CommandLine.Option(names = {"-d", "--duration"}, required = true, description = "Simulation duration in 0.1ms (e.g., 10 is 1ms).")
    protected long simDuration = 0;

    @CommandLine.Option(names = {"-p", "--policy"}, required = true, description = "Scheduling policy (\"EDF\", \"RM\", \"TaskShuffler\" or \"ReOrder\").")
    protected String schedulingPolicy = "";

    @CommandLine.Option(names = {"-v", "--evar"}, required = false, description = "Enable execution time variation.")
    protected boolean optionExecutionVariation = false;

    @CommandLine.Option(names = {"-e", "--entropy"}, required = true, description = "Entropy algorithm (\"Shannon\", \"UASE\" or \"ApEn\").")
    protected String entropyAlgorithm = "";

    @CommandLine.Option(names = {"-r", "--rounds"}, required = false, description = "The number of schedule rounds to be tested.")
    protected int optionRounds = 1;

    protected TaskSet taskSet = null;

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
        if (importTaskSet() == false) {
            loggerConsole.error("Failed to import the taskset.");
            return null;
        }

        loggerConsole.info("Length of each schedule = {}", simDuration);
        loggerConsole.info("Rounds to estimate entropy = {}", optionRounds);

        ScheduleEntropyCalculatorInterface entropyCalculator = getEntropyCalculator();
        ScheduleEntropyTester entropyTester = new ScheduleEntropyTester(taskSet, schedulingPolicy, entropyCalculator);
        double finalEntropy = entropyTester.run(simDuration, optionRounds);
        loggerConsole.info("{} entropy = {}", entropyAlgorithm, finalEntropy);
        return null;
    }

    protected boolean importTaskSet() {
        JsonLogLoader jsonLogLoader = new JsonLogLoader(taskInputFile);
        try {
            TaskSetContainer taskSetContainer = (TaskSetContainer) jsonLogLoader.getResult();
            taskSet = taskSetContainer.getTaskSets().get(0);
        } catch (Exception e) {
            //loggerConsole.error(e);
            return false;
        }
        loggerConsole.info(taskSet.toString());
        return true;
    }


    protected ScheduleEntropyCalculatorInterface getEntropyCalculator() {
        if (entropyAlgorithm.equalsIgnoreCase("Shannon"))
            return new ShannonScheduleEntropyCalculator(0, simDuration);
        else if (entropyAlgorithm.equalsIgnoreCase("UASE"))
            return new UASEntropyCalculator(taskSet, 0, simDuration);
        else // if (entropyAlgorithm.equalsIgnoreCase("ApEn"))
            return new ApproximateEntropyCalculator(0, simDuration);
    }
}
