package synercys.rts.analysis.cli;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import synercys.rts.analysis.dft.tester.MassScheduleDFTTester;
import synercys.rts.analysis.dft.tester.ScheduleDFTTester;
import synercys.rts.framework.TaskSet;
import synercys.rts.scheduler.SchedulerUtil;
import synercys.rts.scheduler.TaskSetContainer;
import synercys.rts.util.JsonLogLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "rtdft",
        versionProvider = synercys.rts.RtsConfig.class,
        header = "@|blue | RT DFT Analyzer | |@",
        mixinStandardHelpOptions = true)
public class RtDFT implements Callable {
    protected static final Logger loggerConsole = LogManager.getLogger("console");

    static final int EXIT_CODE_NORMAL = 0;
    static final int EXIT_CODE_PRINT_HELP = 1;
    static final int EXIT_CODE_PRINT_REQUIRED_OPTIONS = 2;

    @CommandLine.Option(names = {"--options"}, required = false, description = "Show all option names.")
    protected boolean optionShowOptionNames = false;

    @CommandLine.Option(names = {"-i", "--in"}, required = false, description = "One or more files that contain tasksets.")
    protected List<String> taskInputFileList = new ArrayList<>();

    @CommandLine.Option(names = {"-o", "--out"}, required = false, description = "A file path a prefix name for storing test results (the file extension is ignored).")
    String outputFilePrefixPath = "";

    @CommandLine.Option(names = {"-d", "--duration"}, required = false, description = "Simulation duration in 0.1ms (e.g., 10 is 1ms).")
    protected long simDuration = 0;

    @CommandLine.Option(names = {"-p", "--policy"}, required = false, description = "Scheduling policy (\"--option\" for detailed options). Default = \"RM\"")
    protected String schedulingPolicy = "";

    @CommandLine.Option(names = {"-v", "--evar"}, required = false, description = "Enable execution time variation.")
    protected boolean optionExecutionVariation = false;

    @CommandLine.Option(names = {"-c", "--case"}, required = false, description = "Test case (\"--option\" for detailed options).")
    String testCase = "";


    public static void main(String... args) {
        CommandLine cmd = new CommandLine(new RtDFT());

        if (args.length == 1) {
            cmd.usage(cmd.getOut()); // Print help message
            System.exit(0);
        }

        int exitCode = cmd.execute(args);
        switch (exitCode) {
            case EXIT_CODE_PRINT_HELP:
                cmd.usage(cmd.getOut()); // Print help message
                break;
            case EXIT_CODE_PRINT_REQUIRED_OPTIONS:
                printRequiredOptions();
                break;
        }
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {

        if (optionShowOptionNames) {
            loggerConsole.info("All supported options:");
            loggerConsole.info("Scheduling Algorithms = {}", SchedulerUtil.getSchedulerNames());
            loggerConsole.info("Test Case = {}", MassScheduleDFTTester.getTestCaseNames());
            return EXIT_CODE_NORMAL;
        }

        /*===== Load tasksets =====*/
        if (taskInputFileList.size() == 0) {
            loggerConsole.info("Input files were not specified. Please assign files with \"-i\".");
            return EXIT_CODE_PRINT_REQUIRED_OPTIONS;
        }
        loggerConsole.info("");
        loggerConsole.info("==============================");
        loggerConsole.info("Loading task sets ...");
        loggerConsole.info("==============================");
        TaskSetContainer taskSetContainer = JsonLogLoader.loadTaskSetsFromPathList(taskInputFileList);
        if (taskSetContainer.size() == 0) {
            loggerConsole.info("No task set has been found in the specified file(s)/folder(s).");
            return EXIT_CODE_NORMAL;
        } else {
            loggerConsole.info("{} task set(s) have been loaded.", taskSetContainer.size());
        }

        /*===== Run tests =====*/
        loggerConsole.info("");
        loggerConsole.info("==============================");
        loggerConsole.info("Running test(s) ...");
        loggerConsole.info("==============================");
        if (testCase.isEmpty()) {
            TaskSet taskSet = taskSetContainer.getTaskSets().get(0);
            loggerConsole.info("No test case selected. Testing one task set.");
            loggerConsole.info(taskSet.toString());

            if (simDuration == 0) {
                loggerConsole.info("Sim duration was not specified. Please assign duration with \"-d\".");
                return EXIT_CODE_PRINT_REQUIRED_OPTIONS;
            }
            loggerConsole.info("Sim duration = {}", simDuration);

            ScheduleDFTTester dftTester = new ScheduleDFTTester(taskSet, schedulingPolicy, optionExecutionVariation);
            dftTester.run(simDuration);

            loggerConsole.info("");
            loggerConsole.info("------------------------------");
            loggerConsole.info("DFT Results");
            loggerConsole.info("------------------------------");
            loggerConsole.info("Peak Frequencies (first 10):");
            ArrayList<Double> peakFrequencies = dftTester.getReport().getPeakFrequencies();
            for (int i=0; i<10; i++) {
                double thisPeakFreq = peakFrequencies.get(i);
                double thisPeakFreqMag = dftTester.getReport().getFreqSpectrumAmplitudeMap().get(thisPeakFreq);
                loggerConsole.info(String.format("    [%d] %.2f Hz \t(%.2f)", i+1, thisPeakFreq, thisPeakFreqMag));
            }

            if (!outputFilePrefixPath.isEmpty()) {
                loggerConsole.info("Storing DFT analysis results into files.");
                dftTester.exportReport(outputFilePrefixPath);
            }

        } else {
            if (outputFilePrefixPath.isEmpty()) {
                loggerConsole.info("An output log folder path is required for a mass test.");
                return EXIT_CODE_NORMAL;
            }

            loggerConsole.info("Run mass test for the test case \"{}\".", testCase);
            MassScheduleDFTTester massTester = new MassScheduleDFTTester(outputFilePrefixPath, taskSetContainer);
            massTester.setParams(simDuration, schedulingPolicy, optionExecutionVariation);
            if (!massTester.run(testCase)) {
                return EXIT_CODE_PRINT_HELP;
            }
        }

        return EXIT_CODE_NORMAL;
    }

    static public void printRequiredOptions() {
        loggerConsole.info("Minimum Usage: rtdft [-i=<task set file/folder(s)>] [-d=<sim duration>]");
    }
}
