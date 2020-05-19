package synercys.rts.analysis;

import synercys.rts.framework.TaskSet;
import synercys.rts.scheduler.AdvanceableSchedulerInterface;
import synercys.rts.scheduler.SchedulerUtil;

public abstract class Tester {
    // private static final Logger loggerConsole = LogManager.getLogger("console");

    protected AdvanceableSchedulerInterface scheduler;
    protected TaskSet taskSet;
    protected Report report;

    public Tester(TaskSet taskSet, String schedulingPolicy, boolean executionVariation) {
        this.taskSet = taskSet;
        scheduler = SchedulerUtil.getScheduler(schedulingPolicy, taskSet, executionVariation);
    }

    public abstract Report run(long simDuration);
}
