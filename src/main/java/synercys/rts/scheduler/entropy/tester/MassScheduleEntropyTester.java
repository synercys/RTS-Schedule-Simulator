package synercys.rts.scheduler.entropy.tester;

import cy.utility.Umath;
import cy.utility.file.FileHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import synercys.rts.analysis.MassTester;
import synercys.rts.framework.TaskSet;
import synercys.rts.scheduler.TaskSetContainer;
import static synercys.rts.scheduler.TaskSetGenerator.computeDefaultObserverAndVictimTaskPriorities;


public class MassScheduleEntropyTester extends MassTester {
    private static final Logger loggerConsole = LogManager.getLogger("console");

    public static final String TEST_CASES_FULL_HP = "FULL_HP";
    public static final String TEST_CASES_PARTIAL_HP_025 = "PARTIAL_HP_025";
    public static final String TEST_CASES_PARTIAL_HP_050 = "PARTIAL_HP_050";
    public static final String TEST_CASES_PARTIAL_HP_075 = "PARTIAL_HP_075";
    public static final String TEST_CASES_PARTIAL_HP_100 = "PARTIAL_HP_100";
    public static final String TEST_CASES_PARTIAL_HP_200 = "PARTIAL_HP_200";
    public static final String TEST_CASES_PARTIAL_HP_300 = "PARTIAL_HP_300";
    public static final String TEST_CASES_LCM = "LCM";

    String entropyAlgorithm = "";
    int testRounds = 0;


    public MassScheduleEntropyTester(String logFilePath, TaskSetContainer taskSetContainer) {
        super(logFilePath, taskSetContainer);
    }

    public boolean run(String testCase) {
        if (testCase.equalsIgnoreCase(TEST_CASES_FULL_HP) || testCase.equalsIgnoreCase(TEST_CASES_PARTIAL_HP_100)) {
            return runFullHyperPeriodTest();
        } else if (testCase.equalsIgnoreCase(TEST_CASES_PARTIAL_HP_025)) {
            return runPartialHyperPeriodTest(0.25);
        }  else if (testCase.equalsIgnoreCase(TEST_CASES_PARTIAL_HP_050)) {
            return runPartialHyperPeriodTest(0.5);
        }  else if (testCase.equalsIgnoreCase(TEST_CASES_PARTIAL_HP_075)) {
            return runPartialHyperPeriodTest(0.75);
        }  else if (testCase.equalsIgnoreCase(TEST_CASES_PARTIAL_HP_200)) {
            return runPartialHyperPeriodTest(2.0);
        }  else if (testCase.equalsIgnoreCase(TEST_CASES_PARTIAL_HP_300)) {
            return runPartialHyperPeriodTest(3.0);
        } else if (testCase.equalsIgnoreCase(TEST_CASES_LCM)) {
            return runLcmTest();
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
            loggerConsole.error("Some required variables are not set before running the test case {}.", TEST_CASES_FULL_HP);
            return false;
        }

        FileHandler fileTestConfig = openLogFileToWrite("full_hyperperiod_config", "txt");
        fileTestConfig.writeString("Test Case = " + TEST_CASES_FULL_HP + "\n");
        fileTestConfig.writeString("Test Rounds = " + testRounds + "\n");
        fileTestConfig.writeString("Entropy Algorithm = " + entropyAlgorithm + "\n");
        fileTestConfig.writeString("Scheduling Algorithm = " + schedulingPolicy + "\n");
        /* TODO: write test config details to the fileTestConfig file. */

        FileHandler fileTestLog = openLogFileToWrite("full_hyperperiod", "csv");

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

    /**
     * This test case uses these variables: taskSetContainer, schedulingPolicy, entropyAlgorithm, executionVariation, testRounds
     * @return true if the test is valid, false otherwise
     */
    protected boolean runPartialHyperPeriodTest(double hpProportion) {
        if (taskSetContainer==null || schedulingPolicy=="" || entropyAlgorithm=="" || testRounds==0) {
            loggerConsole.error("Some required variables are not set before running the test case {}.", "PARTIAL_HP_XXX");
            return false;
        }

        FileHandler fileTestConfig = openLogFileToWrite("paritial_hp_config", "txt");
        fileTestConfig.writeString("Test Case = " + "PARTIAL_HP_XXX" + "\n");
        fileTestConfig.writeString("Test HP Proportion = " + hpProportion + "\n");
        fileTestConfig.writeString("Test Rounds = " + testRounds + "\n");
        fileTestConfig.writeString("Entropy Algorithm = " + entropyAlgorithm + "\n");
        fileTestConfig.writeString("Scheduling Algorithm = " + schedulingPolicy + "\n");
        /* TODO: write test config details to the fileTestConfig file. */

        FileHandler fileTestLog = openLogFileToWrite("partial_hp", "csv");

        // title row
        fileTestLog.writeString(
                "Task Set ID,"
                        + "Raw Task Set ID,"
                        + "The Number of Tasks,"
                        + "Utilization,"
                        + "Hyper Period,"
                        + "Test Length,"
                        + "Entropy"
        );
        fileTestLog.writeString("\n");

        int taskSetCount = 0;
        int totalNumberOfTaskSet = taskSetContainer.size();
        for (TaskSet taskSet : taskSetContainer.getTaskSets()) {
            taskSetCount++;

            loggerConsole.info("Testing TaskSet #{}\t{}/{} ...", taskSet.getId(), taskSetCount, totalNumberOfTaskSet);

            long testDuration = (long)(taskSet.calHyperPeriod()*hpProportion);

            /* log header row */
            fileTestLog.writeString(taskSetCount + ",");
            fileTestLog.writeString(taskSet.getId() + ",");
            fileTestLog.writeString(taskSet.getRunnableTasksAsArray().size() + ",");
            fileTestLog.writeString(taskSet.getUtilization() + ",");
            fileTestLog.writeString(taskSet.calHyperPeriod() + ",");
            fileTestLog.writeString(testDuration + ",");

            ScheduleEntropyTester entropyTester = new ScheduleEntropyTester(taskSet, schedulingPolicy, entropyAlgorithm, true);
            double finalEntropy = entropyTester.run(testDuration, testRounds);
            fileTestLog.writeString(finalEntropy + "\n");
            loggerConsole.info("\tDone: Entropy = {}", finalEntropy);
        }

        return true;
    }

    /**
     * This test case uses these variables: taskSetContainer, schedulingPolicy, entropyAlgorithm, executionVariation, testRounds
     * @return true if the test is valid, false otherwise
     */
    protected boolean runLcmTest() {
        String testCaseName = TEST_CASES_LCM;
        String testCaseLogFilePrefix = "lcm";

        if (taskSetContainer==null || schedulingPolicy=="" || entropyAlgorithm=="" || testRounds==0) {
            loggerConsole.error("Some required variables are not set before running the test case {}.", testCaseName);
            return false;
        }

        FileHandler fileTestConfig = openLogFileToWrite(testCaseLogFilePrefix + "_config", "txt");
        fileTestConfig.writeString("Test Case = " + testCaseName + "\n");
        fileTestConfig.writeString("Test Rounds = " + testRounds + "\n");
        fileTestConfig.writeString("Entropy Algorithm = " + entropyAlgorithm + "\n");
        fileTestConfig.writeString("Scheduling Algorithm = " + schedulingPolicy + "\n");
        /* TODO: write test config details to the fileTestConfig file. */

        FileHandler fileTestLog = openLogFileToWrite(testCaseLogFilePrefix, "csv");

        // title row
        fileTestLog.writeString(
                "Task Set ID,"
                        + "Raw Task Set ID,"
                        + "The Number of Tasks,"
                        + "Utilization,"
                        + "Hyper Period,"
                        + "Test Length,"
                        + "Entropy"
        );
        fileTestLog.writeString("\n");

        int taskSetCount = 0;
        int totalNumberOfTaskSet = taskSetContainer.size();
        for (TaskSet taskSet : taskSetContainer.getTaskSets()) {
            taskSetCount++;

            loggerConsole.info("Testing TaskSet #{}\t{}/{} ...", taskSet.getId(), taskSetCount, totalNumberOfTaskSet);

            long testDuration = computeScheduLeakAttackDuration(taskSet);

            /* log header row */
            fileTestLog.writeString(taskSetCount + ",");
            fileTestLog.writeString(taskSet.getId() + ",");
            fileTestLog.writeString(taskSet.getRunnableTasksAsArray().size() + ",");
            fileTestLog.writeString(taskSet.getUtilization() + ",");
            fileTestLog.writeString(taskSet.calHyperPeriod() + ",");
            fileTestLog.writeString(testDuration + ",");

            ScheduleEntropyTester entropyTester = new ScheduleEntropyTester(taskSet, schedulingPolicy, entropyAlgorithm, true);
            double finalEntropy = entropyTester.run(testDuration, testRounds);

            fileTestLog.writeString(finalEntropy + "\n");
            loggerConsole.info("\tDone: Entropy = {}", finalEntropy);
        }

        return true;
    }

    protected long computeScheduLeakAttackDuration(TaskSet taskSet) {
        taskSet.assignPriorityRm();
        int observerVictimTaskPriorities[] = computeDefaultObserverAndVictimTaskPriorities(taskSet.getRunnableTasksAsArray().size());
        long po = taskSet.getOneTaskByPriority(observerVictimTaskPriorities[0]).getPeriod();
        long pv = taskSet.getOneTaskByPriority(observerVictimTaskPriorities[1]).getPeriod();
        return 10*Umath.lcm(po, pv);
    }

    public void setEntropyAlgorithm(String entropyAlgorithm) {
        this.entropyAlgorithm = entropyAlgorithm;
    }

    public void setTestRounds(int testRounds) {
        this.testRounds = testRounds;
    }
}