package synercys.rts.scheduler.cli;

import cy.utility.Sys;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import synercys.rts.analysis.MassTester;
import synercys.rts.framework.TaskSet;
import synercys.rts.scheduler.*;
import synercys.rts.scheduler.entropy.*;
import synercys.rts.scheduler.entropy.tester.MassScheduleEntropyTester;
import synercys.rts.scheduler.entropy.tester.ScheduleEntropyTester;
import synercys.rts.util.JsonLogLoader;

import java.io.File;
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

    @CommandLine.Option(names = {"-i", "--in"}, required = false, description = "One or more files that contain tasksets.")
    protected List<String> taskInputFileList = new ArrayList<>();

    @CommandLine.Option(names = {"-o", "--out"}, required = false, description = "A file path a prefix name for storing test results (the file extension is ignored).")
    String outputFilePrefixPath = "";

    @CommandLine.Option(names = {"-d", "--duration"}, required = false, description = "Simulation duration in 0.1ms (e.g., 10 is 1ms).")
    protected long simDuration = 0;

    @CommandLine.Option(names = {"-p", "--policy"}, required = false, description = "Scheduling policy (\"--option\" for detailed options).")
    protected String schedulingPolicy = "";

    @CommandLine.Option(names = {"-v", "--evar"}, required = false, description = "Enable execution time variation.")
    protected boolean optionExecutionVariation = false;

    @CommandLine.Option(names = {"-e", "--entropy"}, required = false, description = "Entropy algorithm (\"--option\" for detailed options).")
    protected String entropyAlgorithm = "";

    @CommandLine.Option(names = {"-r", "--rounds"}, required = false, description = "The number of schedule rounds to be tested.")
    protected int optionRounds = 1;

    @CommandLine.Option(names = {"-c", "--case"}, required = false, description = "Test case (\"--option\" for detailed options).")
    String testCase = "";

    @CommandLine.Option(names = {"-n", "--num"}, required = false, description = "Limit the number of task sets to be tested.")
    protected int theNumberOfTestingTaskSets = -1;

    @CommandLine.Option(names = {"--options"}, required = false, description = "Show all option names.")
    protected boolean showOptionNames = false;


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

        if (showOptionNames) {
            loggerConsole.info("All supported options:");
            loggerConsole.info("Scheduling Algorithms = {}", SchedulerUtil.getSchedulerNames());
            loggerConsole.info("Entropy Algorithms = {}", EntropyCalculatorUtility.getEntropyNames());
            loggerConsole.info("Test Case = {}", MassTester.getTestCaseNames(MassScheduleEntropyTester.class));
            return null;
        }

        /*===== Load tasksets =====*/
        loggerConsole.info("==============================");
        loggerConsole.info("Loading task sets ...");
        loggerConsole.info("==============================");
        TaskSetContainer taskSetContainer = new TaskSetContainer();
        for (String taskInputFile : taskInputFileList) {
            ArrayList<String> taskInputFilePaths = new ArrayList<>();
            if (Sys.isFolderExisted(taskInputFile)) {
                loggerConsole.info("\"{}\" is a folder.", taskInputFile);
                loggerConsole.info("Loading all task set files in the folder.");
                ArrayList<File> taskSetFiles = Sys.getFilesByExtensionInFolder(taskInputFile, ".tasksets");
                for (File taskSetFile : taskSetFiles) {
                    taskInputFilePaths.add(taskSetFile.getPath());
                }
            } else {
                loggerConsole.info("\"{}\" is a file.", taskInputFile);
                taskInputFilePaths.add(taskInputFile);
            }

            for (String taskInputFilePath : taskInputFilePaths) {
                loggerConsole.info("- Loading task sets from \"{}\"...", taskInputFilePath);
                JsonLogLoader jsonLogLoader = new JsonLogLoader(taskInputFilePath);
                taskSetContainer.addTaskSets(((TaskSetContainer) jsonLogLoader.getResult()).getTaskSets());
            }
        }
        loggerConsole.info("{} task sets have been loaded.", taskSetContainer.size());


        loggerConsole.info("==============================");
        loggerConsole.info("Running test(s) ...");
        loggerConsole.info("==============================");
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

            /* Check if only a subset of task sets need to be tested (and remove all others). */
            if (theNumberOfTestingTaskSets > 0 && theNumberOfTestingTaskSets < taskSetContainer.size()) {
                loggerConsole.info("{}/{} task sets to be tested.", theNumberOfTestingTaskSets, taskSetContainer.size());
                int numOfTaskSetsToBeRemoved = taskSetContainer.size() - theNumberOfTestingTaskSets;
                for (int i=0; i<numOfTaskSetsToBeRemoved; i++) {
                    taskSetContainer.getTaskSets().remove(taskSetContainer.size()-1);
                }
            }

            long startTime = System.currentTimeMillis();
            MassScheduleEntropyTester massScheduleEntropyTester = new MassScheduleEntropyTester(outputFilePrefixPath, taskSetContainer);
            massScheduleEntropyTester.setParams(simDuration, schedulingPolicy, optionExecutionVariation);
            massScheduleEntropyTester.setTestRounds(optionRounds);
            massScheduleEntropyTester.setEntropyAlgorithm(entropyAlgorithm);
            massScheduleEntropyTester.run(testCase);
            long estimatedTime = System.currentTimeMillis() - startTime;
            loggerConsole.info("Finished testing {} task sets. ({} ms)", taskSetContainer.size(), estimatedTime);
        }


        return null;
    }

}
