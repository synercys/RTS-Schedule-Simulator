package synercys.rts.scheduler.entropy.tester;

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
        for (int i=0; i<rounds; i++) {
            AdvanceableSchedulerInterface scheduler = SchedulerUtil.getScheduler(schedulingPolicy, taskSet, executionVariation);
            if (i==0) {
                entropyCalculator = getEntropyCalculator(entropyAlgorithm, taskSet, scheduler.getSimDefaultOffset(), simDuration);
                if (entropyCalculator == null)
                    return -1;
            }
            entropyCalculator.applyOneSchedule( scheduler.runSimWithDefaultOffset(simDuration) );
        }
        return entropyCalculator.concludeEntropy();
    }


}
