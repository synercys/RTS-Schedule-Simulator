package synercys.rts.analysis.dft.tester;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import synercys.rts.analysis.MassTester;
import synercys.rts.framework.TaskSet;
import synercys.rts.scheduler.TaskSetContainer;

public class MassScheduleDFTTester extends MassTester {
    public static final String TEST_CASES_VARIED_SCHEDULE_LENGTH = "VARIED_SCHEDULE_LENGTH";
    public static final String TEST_CASES_STFT = "STFT";
    public static final String TEST_CASES_STFT_SCHEDULEAK_LCM = "STFT_SCHEDULEAK_LCM";
    public static final String TEST_CASES_STFT_SCHEDULEAK_VICTIM_CUMULATIVE = "STFT_SCHEDULEAK_VICTIM_CUMULATIVE";

    private static final Logger loggerConsole = LogManager.getLogger("console");

    public MassScheduleDFTTester(String logFilePath, TaskSetContainer taskSetContainer) {
        super(logFilePath, taskSetContainer);
    }

    public boolean run(String testCase) {
        boolean status;
        switch (testCase) {
            case TEST_CASES_STFT:
                status = runSTFTTest();
                break;
            case TEST_CASES_STFT_SCHEDULEAK_LCM:
                status= runSTFTScheduLeakTest();
                break;
            case TEST_CASES_STFT_SCHEDULEAK_VICTIM_CUMULATIVE:
                status= runSTFTScheduLeakVictimCumulativeTest();
                break;
            case TEST_CASES_VARIED_SCHEDULE_LENGTH: default:
                status = runVariedScheduleLengthTest();
                break;
        }
        return status;
    }

    protected boolean runVariedScheduleLengthTest() {
        loggerConsole.info("Scheduler: {}", schedulingPolicy);

        int taskSetCount = 0;
        int totalNumberOfTaskSet = taskSetContainer.size();
        for (TaskSet taskSet : taskSetContainer.getTaskSets()) {
            taskSetCount++;
            loggerConsole.info("Testing TaskSet #{}\t{}/{} ...", taskSet.getId(), taskSetCount, totalNumberOfTaskSet);

            VariedScheduleLengthDFTTester tester = new VariedScheduleLengthDFTTester(taskSet, schedulingPolicy, executionVariation);
            tester.run(10);
            tester.exportAll(getLogFullPathFileName(String.valueOf(taskSet.getId())));
        }

        return true;
    }

    protected boolean runSTFTTest() {
        if (runDuration <= 0) {
            loggerConsole.error("Test aborted: duration is negative or zero.");
            return false;
        }

        TaskSet taskSet = taskSetContainer.getTaskSets().get(0);
        ScheduleSTFTTester stftTester = new ScheduleSTFTTester(taskSet, schedulingPolicy, executionVariation);
        stftTester.run(runDuration);
        stftTester.exportReport(getLogFullPathFileName());

        return true;
    }

    protected boolean runSTFTScheduLeakTest() {
        if (runDuration <= 0) {
            loggerConsole.error("Test aborted: iteration (duration) is negative or zero.");
            return false;
        }

        loggerConsole.info("------------------------------");
        loggerConsole.info("Start STFT test ...");
        loggerConsole.info("Set Scheduler: {}", schedulingPolicy);
        loggerConsole.info("Variation: {}", executionVariation);

        TaskSet taskSet = taskSetContainer.getTaskSets().get(0);
        ScheduleSTFTTester stftTester = new ScheduleSTFTTester(taskSet, schedulingPolicy, executionVariation);
        stftTester.runScheduLeakAttackDuration((int)runDuration);
        stftTester.exportReport(getLogFullPathFileName());

        loggerConsole.info("------------------------------");

        return true;
    }

    protected boolean runSTFTScheduLeakVictimCumulativeTest() {
        if (runDuration <= 0) {
            loggerConsole.error("Test aborted: sim factor is negative or zero.");
            return false;
        }

        loggerConsole.info("------------------------------");
        loggerConsole.info("Start STFT test ...");
        loggerConsole.info("Set Scheduler: {}", schedulingPolicy);
        loggerConsole.info("Variation: {}", executionVariation);

        TaskSet taskSet = taskSetContainer.getTaskSets().get(0);
        ScheduleSTFTTester stftTester = new ScheduleSTFTTester(taskSet, schedulingPolicy, executionVariation);
        stftTester.runScheduLeakVictimCumulativeSTFT((int)runDuration);

        loggerConsole.info("Export experiment results ...");
        stftTester.exportReport(getLogFullPathFileName());

        loggerConsole.info("------------------------------");

        return true;
    }
}
