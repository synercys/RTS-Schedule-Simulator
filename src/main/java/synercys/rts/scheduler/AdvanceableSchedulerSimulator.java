package synercys.rts.scheduler;

import synercys.rts.framework.Job;
import synercys.rts.framework.Task;
import synercys.rts.framework.TaskSet;
import synercys.rts.framework.event.EventContainer;
import synercys.rts.framework.event.SchedulerIntervalEvent;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * AdvanceableSchedulerSimulator.java
 * Purpose: An abstract scheduler class that implements some of the basic methods for an advanceable scheduler simulator.
 *          An advanceable scheduler simulator allows users to call "advance()" to simulate by steps (and thus allow
 *          flexible simulation duration), rather than using "runSim()" to complete simulation for a given, fixed duration
 *          at a time.
 *
 * @author CY Chen (cchen140@illinois.edu)
 * @version 1.0 - 2018, 12/21
 */
abstract class AdvanceableSchedulerSimulator extends SchedulerSimulator implements AdvanceableSchedulerInterface {
    // This map stores each task's next job instance, no matter it's arrived or not.
    protected HashMap<Task, Job> nextJobOfATask = new HashMap<>();

    public AdvanceableSchedulerSimulator(TaskSet taskSet, boolean runTimeVariation, String schedulingPolicy) {
        super(taskSet, runTimeVariation, schedulingPolicy);

        /* Initialize the first job of each task. */
        initializeFirstTaskJobs();
    }

    abstract protected Job getNextJob(long tick);
    abstract protected long getPreemptingTick(Job runJob, long tick);
    abstract protected void runJobExecutedHook(Job runJob, long tick, long executedTime);


    @Override
    public EventContainer runSim(long tickLimit) {
        tick = 0;

        while (tick <= tickLimit) {
            advance();
        }
        simEventContainer.trimEventsToTimeStamp(tickLimit);

        return simEventContainer;
    }

    
    @Override
    public EventContainer concludeSim() {
        simEventContainer.trimEventsToTimeStamp(tick);
        return simEventContainer;
    }


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

    protected Job updateTaskJob(Task task) {
        /* Determine next arrival time. */
        long nextArrivalTime;
        if (task.isSporadicTask()) {
            nextArrivalTime = nextJobOfATask.get(task).releaseTime + getVariedInterArrivalTime(task);
        } else {
            nextArrivalTime = nextJobOfATask.get(task).releaseTime + task.getPeriod();
        }

        /* Determine the execution time. */
        long executionTime;
        if (runTimeVariation == true) {
            executionTime = getVariedExecutionTime(task);
        } else {
            executionTime = task.getWcet();
        }

        Job newJob = new Job(task, nextArrivalTime, executionTime);
        nextJobOfATask.put(task, newJob);

        return newJob;
    }

    protected void initializeFirstTaskJobs() {
        /* Note that the first job of a sporadic task arrives at the initial offset time point.
        * It is based on the assumption (also the fact) that any task needs to run some initialization
        * when it first starts. */
        for (Task task: taskSet.getRunnableTasksAsArray()) {
            Job firstJob;
            if (runTimeVariation == true)
                firstJob = new Job(task, task.getInitialOffset(), getVariedExecutionTime(task));
            else
                firstJob = new Job(task, task.getInitialOffset(), task.getWcet());
            nextJobOfATask.put(task, firstJob);
        }
    }


    protected long runJobToNextSchedulingPoint(long tick, Job runJob) {
        /* Find if there is any job preempting the runJob. */
        long earliestPreemptingTick = getPreemptingTick(runJob, tick);

        // Check if any new job will preempt runJob.
        if (earliestPreemptingTick == -1) { // -1 indicates that the current job will not be preempted
            /* This job is finished. */
            long runJobFinishTime = tick + runJob.remainingExecTime;

            if (runJobFinishTime > runJob.absoluteDeadline) {
                throw new AssertionError("A job (" + runJob.task.toString() + ") missed its deadline: deadline=" + runJob.absoluteDeadline + ", finishedTime=" + runJobFinishTime);
            }

            runJob.remainingExecTime = 0;
            runJobExecutedHook(runJob, runJobFinishTime,runJobFinishTime - tick);

            /* Log the job interval. */
            SchedulerIntervalEvent currentJobEvent = new SchedulerIntervalEvent(tick, runJobFinishTime, runJob.task, "");
            if ( runJob.hasStarted == false ) { // Check this job's starting state.
                runJob.hasStarted = true;
                currentJobEvent.setScheduleStates(SchedulerIntervalEvent.SCHEDULE_STATE_START, SchedulerIntervalEvent.SCHEDULE_STATE_END);
            } else {
                currentJobEvent.setScheduleStates(SchedulerIntervalEvent.SCHEDULE_STATE_RESUME, SchedulerIntervalEvent.SCHEDULE_STATE_END);
            }
            simEventContainer.add(currentJobEvent);

            updateTaskJob(runJob.task);

            // No one will preempt runJob, so runJob is good to finish its job.
            return runJobFinishTime;
        } else {
            /* This job is preempted. */

            if (earliestPreemptingTick == tick) {
                throw new AssertionError("A job gets preempted at the same tick when it's being selected to execute.");
                //return earliestPreemptingJobReleaseTime;
            } else if (earliestPreemptingTick < tick) {
                throw new AssertionError("Next preempting tick is smaller than the present tick.");
            }

            // runJob will be preempted before it's finished, so update runJob's remaining execution time.
            runJob.remainingExecTime -= (earliestPreemptingTick - tick);
            runJobExecutedHook(runJob, earliestPreemptingTick, earliestPreemptingTick - tick);

            /* Log the job interval. */
            SchedulerIntervalEvent currentJobEvent = new SchedulerIntervalEvent(tick, earliestPreemptingTick, runJob.task, "");
            if ( runJob.hasStarted == false ) { // Check this job's starting state.
                runJob.hasStarted = true;
                currentJobEvent.setScheduleStates(SchedulerIntervalEvent.SCHEDULE_STATE_START, SchedulerIntervalEvent.SCHEDULE_STATE_SUSPEND);
            } else {
                currentJobEvent.setScheduleStates(SchedulerIntervalEvent.SCHEDULE_STATE_RESUME, SchedulerIntervalEvent.SCHEDULE_STATE_SUSPEND);
            }
            simEventContainer.add(currentJobEvent);

            return earliestPreemptingTick;
        }
    }

    protected ArrayList<Job> getAllReadyJobs(long tick) {
        ArrayList<Job> readyJobs = new ArrayList<>();
        for (Job job : nextJobOfATask.values()) {
            if (job.releaseTime > tick)
                continue;
            else {
                readyJobs.add(job);
            }
        }
        return readyJobs;
    }

    @Override
    public EventContainer getSimEventContainer()
    {
        return simEventContainer;
    }

}
