package synercys.rts.scheduler.entropy.tester;

import synercys.rts.framework.TaskSet;
import synercys.rts.scheduler.AdvanceableSchedulerInterface;
import synercys.rts.scheduler.SchedulerUtil;
import synercys.rts.scheduler.entropy.ScheduleEntropyCalculatorInterface;

public class ScheduleEntropyTester {
    TaskSet taskSet;
    String schedulingPolicy = "";
    boolean executionVariation = false;
    ScheduleEntropyCalculatorInterface entropyCalculator;

    public ScheduleEntropyTester(TaskSet taskSet, String schedulingPolicy, ScheduleEntropyCalculatorInterface entropyCalculator) {
        this.taskSet = taskSet;
        this.schedulingPolicy = schedulingPolicy;
        this.entropyCalculator = entropyCalculator;
    }

    public double run(long simDuration, int rounds) {
        for (int i=0; i<rounds; i++) {
            AdvanceableSchedulerInterface scheduler = SchedulerUtil.getScheduler(schedulingPolicy, taskSet, executionVariation);
            entropyCalculator.applyOneSchedule( scheduler.runSim(simDuration) );
        }
        return entropyCalculator.concludeEntropy();
    }
}
