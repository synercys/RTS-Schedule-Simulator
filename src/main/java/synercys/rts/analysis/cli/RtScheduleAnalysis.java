package synercys.rts.analysis.cli;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import synercys.rts.analysis.MassTester;
import synercys.rts.analysis.schedule.tester.MassScheduleTester;
import synercys.rts.scheduler.SchedulerUtil;
import synercys.rts.scheduler.TaskSetContainer;
import synercys.rts.util.JsonLogLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "rtsched",
        versionProvider = synercys.rts.RtsConfig.class,
        header = "@|blue | RT Schedule Analyzer | |@",
        mixinStandardHelpOptions = true)
public class RtScheduleAnalysis implements Callable {
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

    @CommandLine.Option(names = {"-d", "--duration"}, required = false, description = "Simulation duration (unit depends on the test cases, 0.1ms by default).")
    protected long simDuration = 0;

    @CommandLine.Option(names = {"-p", "--policy"}, required = false, description = "Scheduling policy (\"--options\" for detailed options). Default = \"RM\"")
    protected String schedulingPolicy = "";

    @CommandLine.Option(names = {"-v", "--evar"}, required = false, description = "Enable execution time variation.")
    protected boolean optionExecutionVariation = false;

    @CommandLine.Option(names = {"-c", "--case"}, required = false, description = "Test case (\"--options\" for detailed options).")
    String testCase = "";


    public static void main(String... args) {
        CommandLine cmd = new CommandLine(new RtScheduleAnalysis());

        if (args.length == 0) {
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
            loggerConsole.info("Test Case = {}", MassTester.getTestCaseNames(MassScheduleTester.class));
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

        if (!testCase.isEmpty()) {
            if (outputFilePrefixPath.isEmpty()) {
                loggerConsole.info("An output log folder path is required for a mass test.");
                return EXIT_CODE_NORMAL;
            }

            loggerConsole.info("Run mass test for the test case \"{}\".", testCase);
            MassScheduleTester massTester = new MassScheduleTester(outputFilePrefixPath, taskSetContainer);
            massTester.setParams(simDuration, schedulingPolicy, optionExecutionVariation);
            if (!massTester.run(testCase)) {
                return EXIT_CODE_PRINT_HELP;
            }
        }

        return EXIT_CODE_NORMAL;
    }

    static public void printRequiredOptions() {
        loggerConsole.info("Minimum Usage: rtsched [-i=<task set file/folder(s)>] [-d=<sim duration>] [-c=<test case name>] [-o=<log output path and prefix>]");
    }
}
