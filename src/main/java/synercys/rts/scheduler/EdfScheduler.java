package synercys.rts.scheduler;

import synercys.rts.framework.event.EventContainer;
import synercys.rts.framework.Job;
import synercys.rts.framework.Task;
import synercys.rts.framework.TaskSet;

import java.util.HashMap;

/**
 * EdfScheduler.java
 * Purpose: An implementation of the EDF scheduler.
 *
 * @author CY Chen (cchen140@illinois.edu)
 * @version 1.0 - 2018, 12/21
 */
public class EdfScheduler extends AdvanceableSchedulerSimulator {

    public EdfScheduler(TaskSet taskSet, boolean runTimeVariation) {
        super(taskSet, runTimeVariation, EventContainer.SCHEDULING_POLICY_EDF);
    }

    @Override
    protected void setTaskSetHook() {}

    /**
     * This function returns the earliest due job in the run queue or the next future released job if no job
     * is active at the given time tick.
     * @param tick reference time tick
     * @return
     */
    @Override
    protected Job getNextJob(long tick) {
        long earliestDeadline = Long.MAX_VALUE;
        Job targetJob = null;
        for (Job job: nextJobOfATask.values()) {
            if (job.releaseTime > tick)
                continue;

            // TODO: Here, we don't explicitly deal with the case where two jobs have the same absolute deadline.
            if (job.absoluteDeadline < earliestDeadline) {
                earliestDeadline = job.absoluteDeadline;
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
                if (job.absoluteDeadline < runJob.absoluteDeadline) {
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
