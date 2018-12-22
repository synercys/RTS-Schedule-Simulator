package synercys.rts.scheduler;

import synercys.rts.framework.event.EventContainer;
import synercys.rts.framework.Job;
import synercys.rts.framework.Task;
import synercys.rts.framework.TaskSet;
import java.util.HashMap;

/**
 * FixedPriorityScheduler.java
 * Purpose: A scheduler for the preemptive, fixed-priority real-time scheduler using the rate-monotonic (RM) priority assignment.
 *
 * @author CY Chen (cchen140@illinois.edu)
 * @version 1.1 - 2018, 12/21
 * @version 1.0 - 2018, 12/14
 */
public class FixedPriorityScheduler extends AdvanceableSchedulerSimulator {

    public FixedPriorityScheduler(TaskSet taskSet, boolean runTimeVariation) {
        super(taskSet, runTimeVariation, EventContainer.SCHEDULING_POLICY_FIXED_PRIORITY);
    }

    @Override
    protected void setTaskSetHook() {
        this.taskSet.assignPriorityRm();
    }

    @Override
    protected Job getNextJob(long tick) {
        Job targetJob = null;
        int highestActivePriority = 0;

        for (Job job : nextJobOfATask.values()) {
            if (job.releaseTime > tick)
                continue;
            if (job.task.getPriority() > highestActivePriority) {
                highestActivePriority = job.task.getPriority();
                targetJob = job;
            }
        }
        if (targetJob != null)
            return targetJob;

        /* No job is active at this given tick point, so let's check who is the first job in the future. */
        return getEarliestArrivedJob();
    }

    @Override
    protected Job getPreemptingJob(Job runJob) {
        /* Find if there is any job preempting the runJob. */
        long earliestPreemptingJobReleaseTime = Long.MAX_VALUE;
        Job earliestPreemptingJob = null;
        long runJobFinishTime = tick + runJob.remainingExecTime;
        for (Job job: nextJobOfATask.values()) {
            if (job == runJob)
                continue;

            if (job.releaseTime < runJobFinishTime) {
                if (job.task.getPriority() > runJob.task.getPriority()) {
                    if (job.releaseTime < earliestPreemptingJobReleaseTime) {
                        earliestPreemptingJobReleaseTime = job.releaseTime;
                        earliestPreemptingJob = job;
                    }
                }
            }
        }
        return earliestPreemptingJob;
    }

}
