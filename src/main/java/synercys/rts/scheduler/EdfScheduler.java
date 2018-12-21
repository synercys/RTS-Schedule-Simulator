package synercys.rts.scheduler;

import synercys.rts.event.EventContainer;
import synercys.rts.event.SchedulerIntervalEvent;
import synercys.rts.framework.Job;
import synercys.rts.framework.Task;
import synercys.rts.framework.TaskSet;

import java.util.HashMap;

/**
 * Created by jjs on 2/13/17.
 */
public class EdfScheduler extends SchedulerSimulator implements Advanceable{

    // This map stores each task's next job instance, no matter it's arrived or not.
    HashMap<Task, Job> nextJobOfATask = new HashMap<>();

    public EdfScheduler(TaskSet taskSet, boolean runTimeVariation) {
        super(taskSet, runTimeVariation);

        /* Initialize the first job of each task. */
        initializeFirstTaskJobs();
    }


    @Override
    public EventContainer runSim(long tickLimit) {
        tick = 0;

        while (tick <= tickLimit) {
            advance();
        }
        simEventContainer.trimEventsToTimeStamp(tickLimit);

        simEventContainer.setSchedulingPolicy(EventContainer.SCHEDULING_POLICY_EDF);
        return simEventContainer;
    }

    @Override
    protected void setTaskSetHook() {

    }

    private void initializeFirstTaskJobs() {
        for (Task task: taskSet.getRunnableTasksAsArray()) {
            Job firstJob;
            if (runTimeVariation == true)
                firstJob = new Job(task, task.getInitialOffset(), getVariedExecutionTime(task));
            else
                firstJob = new Job(task, task.getInitialOffset(), task.getWcet());
            nextJobOfATask.put(task, firstJob);
        }
    }

    /**
     * Get the next earliest due job.
     * Note that this function does not create any new job instance or update for the found EDF job.
     * @param tick reference time tick
     * @return a job that has arrived (before the given "tick" time) and has earliest deadline
     */
//    private Job getNextRunnableEdfJob(long tick) {
//        long earliestDeadline = Long.MAX_VALUE;
//        Job targetJob = null;
//        for (Job job: nextJobOfATask.values()) {
//            if (job.releaseTime > tick)
//                continue;
//
//            // TODO: Here, we don't explicitly deal with the case where two jobs have the same absolute deadline.
//            if (job.absoluteDeadline < earliestDeadline) {
//                earliestDeadline = job.absoluteDeadline;
//                targetJob = job;
//            }
//        }
//
//        return targetJob;
//    }

//    private long getNextValidSchedulingTick(long tick) {
//        long nextSchedulingTick = Long.MAX_VALUE;
//        for (Job job: nextJobOfATask.values()) {
//            if (job.releaseTime <= tick)
//                return tick;
//
//            if (job.releaseTime < )
//        }
//
//    }

    /**
     * Run simulation and advance to next scheduling point.
     */
    @Override
    public void advance() {
        Job currentJob = getNextJob(tick);

        // If it is a future job, then jump the tick first.
        if (currentJob.releaseTime > tick)
            tick = currentJob.releaseTime;

        // Run the job (and log the execution interval).
        tick = runJobToNextSchedulingPoint(tick, currentJob);
    }

    /**
     * This function returns the earliest due job in the run queue or the next future released job if no job
     * is active at the given time tick.
     * @param tick reference time tick
     * @return
     */
    private Job getNextJob(long tick) {
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
        long earliestNextReleaseTime = Long.MAX_VALUE;
        for (Job job: nextJobOfATask.values()) {
            if (job.releaseTime < earliestNextReleaseTime) {
                earliestNextReleaseTime = job.releaseTime;
                targetJob = job;
            }
        }

        return targetJob;
    }

    private long runJobToNextSchedulingPoint(long tick, Job runJob) {
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

        // Check if any new job will preempt runJob.
        if (earliestPreemptingJob == null) {
            /* This job is finished. */
            runJob.remainingExecTime = 0;

            /* Log the job interval. */
            SchedulerIntervalEvent currentJobEvent = new SchedulerIntervalEvent(tick, runJobFinishTime, runJob.task, "");
            if ( runJob.hasStarted == false ) { // Check this job's starting state.
                runJob.hasStarted = true;
                currentJobEvent.setScheduleStates(SchedulerIntervalEvent.SCHEDULE_STATE_START, SchedulerIntervalEvent.SCHEDULE_STATE_END);
            } else {
                currentJobEvent.setScheduleStates(SchedulerIntervalEvent.SCHEDULE_STATE_RESUME, SchedulerIntervalEvent.SCHEDULE_STATE_END);
            }
            simEventContainer.add(currentJobEvent);

            advanceToNextJob(runJob.task);

            // No one will preempt runJob, so runJob is good to finish its job.
            return runJobFinishTime;
        } else {
            /* This job is preempted. */
            // runJob will be preempted before it's finished, so update runJob's remaining execution time.
            runJob.remainingExecTime -= (earliestPreemptingJobReleaseTime - tick);

            /* Log the job interval. */
            SchedulerIntervalEvent currentJobEvent = new SchedulerIntervalEvent(tick, earliestPreemptingJobReleaseTime, runJob.task, "");
            if ( runJob.hasStarted == false ) { // Check this job's starting state.
                runJob.hasStarted = true;
                currentJobEvent.setScheduleStates(SchedulerIntervalEvent.SCHEDULE_STATE_START, SchedulerIntervalEvent.SCHEDULE_STATE_SUSPEND);
            } else {
                currentJobEvent.setScheduleStates(SchedulerIntervalEvent.SCHEDULE_STATE_RESUME, SchedulerIntervalEvent.SCHEDULE_STATE_SUSPEND);
            }
            simEventContainer.add(currentJobEvent);

            return earliestPreemptingJobReleaseTime;
        }
    }

    private Job advanceToNextJob(Task task) {
        long nextReleaseTime = nextJobOfATask.get(task).releaseTime + task.getPeriod();
        Job newJob;
        if (runTimeVariation == true)
            newJob = new Job(task, nextReleaseTime, getVariedExecutionTime(task));
        else
            newJob = new Job(task, nextReleaseTime, task.getWcet());
        nextJobOfATask.put(task, newJob);
        return newJob;
    }
}
