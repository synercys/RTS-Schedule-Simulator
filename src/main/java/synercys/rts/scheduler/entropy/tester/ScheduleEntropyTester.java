package synercys.rts.scheduler.entropy.tester;

import synercys.rts.framework.Task;
import synercys.rts.framework.TaskSet;
import synercys.rts.scheduler.AdvanceableSchedulerInterface;
import synercys.rts.scheduler.SchedulerUtil;
import synercys.rts.scheduler.entropy.ScheduleEntropyCalculatorInterface;
import static synercys.rts.scheduler.entropy.EntropyCalculatorUtility.getEntropyCalculator;

public class ScheduleEntropyTester {
    TaskSet taskSet;
    String schedulingPolicy;
    String entropyAlgorithm;
    boolean executionVariation;
    ScheduleEntropyCalculatorInterface entropyCalculator;

    public ScheduleEntropyTester(TaskSet taskSet, String schedulingPolicy, String entropyAlgorithm, boolean executionVariation) {
        this.taskSet = taskSet;
        this.schedulingPolicy = schedulingPolicy;
        this.entropyAlgorithm = entropyAlgorithm;
        this.executionVariation = executionVariation;
    }

    public double run(long simDuration, int rounds) {
        AdvanceableSchedulerInterface scheduler;
        long simOffset;

        //setAllTaskPhaseZero();

        if (isAllTaskPhaseZero()) {
            simOffset = 0;
        } else {
            simOffset = SchedulerUtil.getScheduler(schedulingPolicy, taskSet, executionVariation).getSimDefaultOffset();
        }

        entropyCalculator = getEntropyCalculator(entropyAlgorithm, taskSet, simOffset, simDuration);
        if (entropyCalculator == null)
            return -1;

        for (int i=0; i<rounds; i++) {
            scheduler = SchedulerUtil.getScheduler(schedulingPolicy, taskSet, executionVariation);
            entropyCalculator.applyOneSchedule( scheduler.runSimWithOffset(simOffset, simDuration) );
        }
        return entropyCalculator.concludeEntropy();
    }

    protected void setAllTaskPhaseZero() {
        for (Task task : taskSet.getRunnableTasksAsArray()) {
            task.setInitialOffset(0);
        }
    }

    protected boolean isAllTaskPhaseZero() {
        for (Task task : taskSet.getRunnableTasksAsArray()) {
            if (task.getInitialOffset() != 0)
                return false;
        }
        return true;
    }


}
