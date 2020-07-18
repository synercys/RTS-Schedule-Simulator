package synercys.rts.scheduler;

import synercys.rts.framework.event.EventContainer;
import synercys.rts.framework.Job;
import synercys.rts.framework.TaskSet;

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
        Job nextJob = getNextJobInReadyQueue(tick);
        if (nextJob == null) {
            /* No job is active at this given tick point, so let's check who is the first job in the future. */
            nextJob = getEarliestArrivedHigherPriorityJob();
        }
        return nextJob;
    }

    /**
     * Get the highest priority job in the ready queue. Return null if the ready queue is empty.
     * @param tick current tick
     * @return the highest priority job in the ready queue or null if the ready queue is empty
     */
    protected Job getNextJobInReadyQueue(long tick) {
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
        return targetJob;
    }

    @Override
    protected long getPreemptingTick(Job runJob, long tick) {
        Job preemptingJob = getPreemptingJob(runJob, tick);
        if (preemptingJob == null)
            return -1;
        else
            return preemptingJob.releaseTime;
    }

    protected Job getPreemptingJob(Job runJob, long tick) {
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

    @Override
    protected void runJobExecutedHook(Job job, long tick, long executedTime) {

    }

    @Override
    protected void deadlineMissedHook(Job runJob) {

    }

    protected Job getEarliestArrivedHigherPriorityJob() {
        Job targetJob = null;
        long earliestNextReleaseTime = Long.MAX_VALUE;
        for (Job job: nextJobOfATask.values()) {
            if (job.releaseTime < earliestNextReleaseTime) {
                earliestNextReleaseTime = job.releaseTime;
                targetJob = job;
            } else if ((job.releaseTime == earliestNextReleaseTime) && (job.task.getPriority() > targetJob.task.getPriority())) {
                targetJob = job;
            }
        }
        return targetJob;
    }

}
