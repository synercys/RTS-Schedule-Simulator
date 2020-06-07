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
    public static final String TEST_CASES_DURATION = "DURATION";

    String entropyAlgorithm = "";
    int testRounds = 0;
    String testCase = "";


    public MassScheduleEntropyTester(String logFilePath, TaskSetContainer taskSetContainer) {
        super(logFilePath, taskSetContainer);
    }

    public boolean run(String testCase) {
        this.testCase = testCase;

        if (taskSetContainer==null || schedulingPolicy=="" || entropyAlgorithm=="" || testRounds==0) {
            loggerConsole.error("Some required variables are not set before running the test case {}.", testCase);
            return false;
        }

        FileHandler fileTestConfig = openLogFileToWrite(testCase + "_config", "txt");
        fileTestConfig.writeString("Test Case = " + testCase + "\n");
        fileTestConfig.writeString("Test Rounds = " + testRounds + "\n");
        fileTestConfig.writeString("Entropy Algorithm = " + entropyAlgorithm + "\n");
        fileTestConfig.writeString("Scheduling Algorithm = " + schedulingPolicy + "\n");
        if (testCase.equalsIgnoreCase(TEST_CASES_DURATION)) {
            fileTestConfig.writeString("Sim Duration = " + runDuration + "\n");
        }

        FileHandler fileTestLog = openLogFileToWrite(testCase, "csv");

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

            long testDuration = getTestCaseSimDuration(taskSet);

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

    protected long getTestCaseSimDuration(TaskSet taskSet) {
        if (testCase.equalsIgnoreCase(TEST_CASES_FULL_HP) || testCase.equalsIgnoreCase(TEST_CASES_PARTIAL_HP_100)) {
            return taskSet.calHyperPeriod();
        } else if (testCase.equalsIgnoreCase(TEST_CASES_PARTIAL_HP_025)) {
            return (long)(taskSet.calHyperPeriod()*0.25);
        }  else if (testCase.equalsIgnoreCase(TEST_CASES_PARTIAL_HP_050)) {
            return (long)(taskSet.calHyperPeriod()*0.5);
        }  else if (testCase.equalsIgnoreCase(TEST_CASES_PARTIAL_HP_075)) {
            return (long)(taskSet.calHyperPeriod()*0.75);
        }  else if (testCase.equalsIgnoreCase(TEST_CASES_PARTIAL_HP_200)) {
            return (long)(taskSet.calHyperPeriod()*2.0);
        }  else if (testCase.equalsIgnoreCase(TEST_CASES_PARTIAL_HP_300)) {
            return (long)(taskSet.calHyperPeriod()*3.0);
        } else if (testCase.equalsIgnoreCase(TEST_CASES_LCM)) {
            return computeScheduLeakAttackDuration(taskSet);
        } else if (testCase.equalsIgnoreCase(TEST_CASES_DURATION)) {
            return runDuration;
        } else {
            return runDuration;
        }
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