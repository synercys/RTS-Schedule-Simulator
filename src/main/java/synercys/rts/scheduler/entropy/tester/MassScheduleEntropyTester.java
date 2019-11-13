package synercys.rts.scheduler.entropy.tester;

import cy.utility.file.FileHandler;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import synercys.rts.framework.TaskSet;
import synercys.rts.scheduler.TaskSetContainer;
import synercys.rts.scheduler.entropy.ApproximateEntropyCalculator;

import java.nio.file.Paths;

public class MassScheduleEntropyTester {
    private static final Logger loggerConsole = LogManager.getLogger("console");

    public static final String[] TEST_CASES = {"FULL_HP"};

    TaskSetContainer taskSetContainer = null;
    String schedulingPolicy = "";
    String entropyAlgorithm = "";
    boolean executionVariation = false;
    int testRounds = 0;

    String logFileFolderPath = "";
    String logFileNamePrefix = "";

    public MassScheduleEntropyTester(String logFilePath, TaskSetContainer taskSetContainer) {
        setLogFilePrefixPath(logFilePath);
        this.taskSetContainer = taskSetContainer;
    }

    public boolean run(String testCase) {
        if (testCase.equalsIgnoreCase(TEST_CASES[0])) {
            return runFullHyperPeriodTest();
        } else {
            loggerConsole.error("Test terminated: test case \"{}\" not found.", testCase);
            return false;
        }
    }

    /**
     * This test case uses these variables: taskSetContainer, schedulingPolicy, entropyAlgorithm, executionVariation, testRounds
     * @return true if the test is valid, false otherwise
     */
    protected boolean runFullHyperPeriodTest() {
        if (taskSetContainer==null || schedulingPolicy=="" || entropyAlgorithm=="" || testRounds==0) {
            loggerConsole.error("Some required variables are not set before running the test case {}.", TEST_CASES[0]);
            return false;
        }

        FileHandler fileTestConfig = openWriteLogFile("full_hyperperiod_config");
        fileTestConfig.writeString("Test Rounds = " + testRounds + "\n");
        /* TODO: write test config details to the fileTestConfig file. */

        FileHandler fileTestLog = openWriteLogFile("full_hyperperiod");

        // title row
        fileTestLog.writeString(
            "Task Set ID,"
            + "Raw Task Set ID,"
            + "The Number of Tasks,"
            + "Utilization,"
            + "Hyper Period,"
            + "Entropy"
        );
        fileTestLog.writeString("\n");

        int taskSetCount = 0;
        int totalNumberOfTaskSet = taskSetContainer.size();
        for (TaskSet taskSet : taskSetContainer.getTaskSets()) {
            taskSetCount++;

            loggerConsole.info("Testing TaskSet #{}\t{}/{} ...", taskSet.getId(), taskSetCount, totalNumberOfTaskSet);

            /* log header row */
            fileTestLog.writeString(taskSetCount + ",");
            fileTestLog.writeString(taskSet.getId() + ",");
            fileTestLog.writeString(taskSet.getRunnableTasksAsArray().size() + ",");
            fileTestLog.writeString(taskSet.getUtilization() + ",");
            fileTestLog.writeString(taskSet.calHyperPeriod() + ",");

            ScheduleEntropyTester entropyTester = new ScheduleEntropyTester(taskSet, schedulingPolicy, entropyAlgorithm, true);
            double finalEntropy = entropyTester.run(taskSet.calHyperPeriod(), testRounds);
            fileTestLog.writeString(finalEntropy + "\n");
            loggerConsole.info("\tDone: Entropy = {}", finalEntropy);
        }

        return true;
    }

    public boolean setLogFilePrefixPath(String logFilePrefixPath) {
        this.logFileFolderPath = FilenameUtils.getFullPath(logFilePrefixPath);
        this.logFileNamePrefix = FilenameUtils.getBaseName(logFilePrefixPath);

        if (!this.logFileNamePrefix.isEmpty()) {
            this.logFileNamePrefix += "_";
        }
        return true;
    }

    protected FileHandler openWriteLogFile(String fileNameSuffix) {
        FileHandler newLogFile = new FileHandler();
        newLogFile.openToWriteFile(Paths.get(logFileFolderPath, logFileNamePrefix + fileNameSuffix + ".csv").toString());
        return newLogFile;
    }

    public void setTaskSetContainer(TaskSetContainer taskSetContainer) {
        this.taskSetContainer = taskSetContainer;
    }

    public void setSchedulingPolicy(String schedulingPolicy) {
        this.schedulingPolicy = schedulingPolicy;
    }

    public void setEntropyAlgorithm(String entropyAlgorithm) {
        this.entropyAlgorithm = entropyAlgorithm;
    }

    public void setExecutionVariation(boolean executionVariation) {
        this.executionVariation = executionVariation;
    }

    public void setTestRounds(int testRounds) {
        this.testRounds = testRounds;
    }
}