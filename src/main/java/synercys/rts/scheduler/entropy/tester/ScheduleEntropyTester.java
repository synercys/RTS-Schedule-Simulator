package synercys.rts.scheduler.entropy.tester;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import synercys.rts.framework.Task;
import synercys.rts.framework.TaskSet;
import synercys.rts.scheduler.AdvanceableSchedulerInterface;
import synercys.rts.scheduler.SchedulerUtil;
import synercys.rts.scheduler.entropy.ScheduleEntropyCalculatorInterface;

import static java.lang.Math.abs;
import static synercys.rts.scheduler.entropy.EntropyCalculatorUtility.getEntropyCalculator;

public class ScheduleEntropyTester {
    private static final Logger loggerConsole = LogManager.getLogger("console");

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

        loggerConsole.info("Setting all task phases to zero.");
        setAllTaskPhaseZero();

        if (isAllTaskPhaseZero()) {
            simOffset = 0;
        } else {
            simOffset = SchedulerUtil.getScheduler(schedulingPolicy, taskSet, executionVariation).getSimDefaultOffset();
        }

        entropyCalculator = getEntropyCalculator(entropyAlgorithm, taskSet, simOffset, simDuration);
        if (entropyCalculator == null)
            return -1;

        loggerConsole.info("Sim Duration (for each round) = {}", simDuration);
        double currentEntropy = 0;
        double lastEntropy = -1;
        for (int i=0; i<rounds; i++) {
            scheduler = SchedulerUtil.getScheduler(schedulingPolicy, taskSet, executionVariation);
            entropyCalculator.applyOneSchedule( scheduler.runSimWithOffset(simOffset, simDuration) );

            /* Check the resulting schedule entropy every 20 loops and see if it's been covered (diff<0.01%). */
            if (i%20 == 18) {
                lastEntropy = entropyCalculator.concludeEntropy();
                loggerConsole.info("- [#{}] Testing Round {}/{} ... en={}", taskSet.getId(), i, rounds, String.format("%.5f",lastEntropy));
            } else if (i%20 == 19) {
                currentEntropy = entropyCalculator.concludeEntropy();
                double diffEntropy = abs((currentEntropy - lastEntropy) / lastEntropy);
                loggerConsole.info("- [#{}] Testing Round {}/{} ... en={}, diff={}%", taskSet.getId(), i, rounds, String.format("%.5f",currentEntropy), String.format("%.3f", diffEntropy*100.0));
                if ( (diffEntropy<0.0001) || (lastEntropy==0.0 && currentEntropy==0.0) ) // < 0.01%, based on TaskShuffler
                    break;
            }

        }
        return currentEntropy;
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
