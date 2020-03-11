package synercys.rts.analysis.dft.tester;

import cy.utility.Class;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import synercys.rts.analysis.MassTester;
import synercys.rts.framework.TaskSet;
import synercys.rts.scheduler.TaskSetContainer;

public class MassScheduleDFTTester extends MassTester {
    public static final String TEST_CASES_VARIED_SCHEDULE_LENGTH = "VARIED_SCHEDULE_LENGTH";

    private static final Logger loggerConsole = LogManager.getLogger("console");

    public MassScheduleDFTTester(String logFilePath, TaskSetContainer taskSetContainer) {
        super(logFilePath, taskSetContainer);
    }

    public boolean run(String testCase) {
        switch (testCase) {
            case "TEST_CASES_VARIED_SCHEDULE_LENGTH": default:
                runVariedScheduleLengthTest();
                break;
        }
        return true;
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
}
