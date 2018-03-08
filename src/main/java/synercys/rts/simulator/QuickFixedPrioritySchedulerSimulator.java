package synercys.rts.simulator;

import synercys.rts.event.SchedulerIntervalEvent;
import synercys.rts.framework.Job;
import synercys.rts.framework.Task;
import cy.utility.Umath;

import java.util.ArrayList;

/**
 * Created by cy on 3/13/2017.
 */
public class QuickFixedPrioritySchedulerSimulator extends SchedulerSimulator {

    private boolean executionTimeVariation = true;

    public void setExecutionTimeVariation(boolean val) {
        executionTimeVariation = val;
    }

    @Override
    public boolean runSim(long tickLimit) {
//        if (taskContainer.schedulabilityTest() == true)
//            ProgMsg.debugPutline("schedulable.");
//        else
//            ProgMsg.errPutline("not schedulable.");

        // Pre-schedule, turn tasks into jobs
        QuickFPSchedulerJobContainer simJobContainer = preSchedule(tickLimit);

        // Start simulating, the output schedule will be stored in
        simJobs(simJobContainer);
        return true;
    }

    @Override
    protected void setTaskSetHook() {
        assignPriority();
    }

    /**
     * Turn task set into jobs within designated end time.
     * @param tickLimit designated end time
     * @return a container that stores jobs to be simulated.
     */
    public QuickFPSchedulerJobContainer preSchedule(long tickLimit) {
        QuickFPSchedulerJobContainer resultSimJobs = new QuickFPSchedulerJobContainer();
        for (Task thisTask : taskSet.getTasksAsArray()) {
            if (thisTask.getTaskType() == Task.TASK_TYPE_IDLE)
                continue;

            if (thisTask.isSporadicTask() == true) {
                long thisOffset = thisTask.getInitialOffset();
                long thisInterArrival = thisTask.getPeriod();
                for (long tick = thisOffset; tick < tickLimit; tick += getVariedInterArrivalTime(thisInterArrival)) {
                    //resultSimJobs.addNextEvent( new SimJob(thisTask, tick, thisTask.getComputationTimeNs()) );
                    if (executionTimeVariation == true)
                        resultSimJobs.add(new Job(thisTask, tick, getDeviatedExecutionTime(thisTask)));
                    else
                        resultSimJobs.add(new Job(thisTask, tick, thisTask.getExecTime()));
                }
            } else {
                long thisPeriod = thisTask.getPeriod();
                long thisOffset = thisTask.getInitialOffset();
                for (long tick = thisOffset; tick < tickLimit; tick += thisPeriod) {
                    if (executionTimeVariation == true)
                        resultSimJobs.add(new Job(thisTask, tick, getDeviatedExecutionTime(thisTask)));
                    else
                        resultSimJobs.add(new Job(thisTask, tick, thisTask.getExecTime()));
                }
            }
        }
        return resultSimJobs;
    }

    public long getVariedInterArrivalTime(long minInterArrival) {
        long result = 0;
        while (result < minInterArrival) {
            result = Umath.getPoisson((minInterArrival/10)*1.2)*10;
        }
        return result;
    }

    public void simJobs(QuickFPSchedulerJobContainer jobContainer) {
        progressUpdater.setIsStarted(true);
        int orgNumOfJobsToSim = jobContainer.size();

        Task idleTask = taskSet.getIdleTask();
        Task currentRunTask = null;
        Job currentJob = null;
        Job nextJob;
        int currentTimeStamp = 0;

        Boolean anyJobRunning = false;
        while ( true ) {
            progressUpdater.setProgressPercent(1-((double)jobContainer.size()/(double)orgNumOfJobsToSim));

            if ( anyJobRunning == false ) {
                anyJobRunning = true;
                currentJob = jobContainer.popNextHighestPriorityJobByTime(currentTimeStamp);

                if ( (currentJob == null) && (jobContainer.size() == 0) ) {
                    break;
                } else if (currentJob == null) {
                    currentJob = jobContainer.popNextEarliestHighestPriorityJob();
//                        simEventContainer.addNextEvent(EventContainer.SCHEDULER_EVENT, (int) tick, 0, Task.IDLE_TASK_ID, "IDLE");
                    SchedulerIntervalEvent currentIdleEvent = new SchedulerIntervalEvent(currentTimeStamp, (int)currentJob.releaseTime, idleTask, "");
                    simEventContainer.add(currentIdleEvent);

                    currentTimeStamp = (int)currentJob.releaseTime;
                }


                currentRunTask = currentJob.task;
                if (currentTimeStamp > (int)currentJob.releaseTime) {
                    currentJob.releaseTime = (long)currentTimeStamp;
                }

                //if ( (int)currentJob.remainingExecTime == currentRunTask.getComputationTimeNs() ) {
//                if ( currentJob.hasStarted == false ) {
//                    currentJob.hasStarted = true;
//                    TaskInstantEvent thisReleaseEvent = new TaskInstantEvent((int) currentJob.releaseTime, currentRunTask, 0, "BEGIN");
//                    simEventContainer.add(thisReleaseEvent);
//                    //resultSchedulingEvents.addNextEvent(thisReleaseEvent);
//                    //bi.startTimesInference.addNextEvent(thisReleaseEvent);
//                }

                continue;
            }

            /* There is a job running. */

            // Get the job that will preempt current job before within the remaining computation time.
            nextJob = jobContainer.popNextEarliestHigherPriorityJobByTime(currentRunTask.getPriority(), (int) (currentTimeStamp + currentJob.remainingExecTime));

            if ( nextJob != null ) {
                // Current job is being preempted. Create and push the updated current job.
                SchedulerIntervalEvent currentJobEvent = new SchedulerIntervalEvent((int) currentJob.releaseTime, (int) nextJob.releaseTime, currentRunTask, "");

                // Check last task's scheduling states.
                if ( currentJob.hasStarted == false ) {
                    currentJob.hasStarted = true;
                    currentJobEvent.setScheduleStates(SchedulerIntervalEvent.SCHEDULE_STATE_START, SchedulerIntervalEvent.SCHEDULE_STATE_SUSPEND);
                } else {
                    currentJobEvent.setScheduleStates(SchedulerIntervalEvent.SCHEDULE_STATE_RESUME, SchedulerIntervalEvent.SCHEDULE_STATE_SUSPEND);
                }

                simEventContainer.add(currentJobEvent);

                currentJob.remainingExecTime -= (nextJob.releaseTime - currentTimeStamp);
                currentJob.releaseTime = nextJob.releaseTime;
                jobContainer.add(currentJob);

                currentJob = nextJob;
                currentRunTask = currentJob.task;
                currentTimeStamp = (int)currentJob.releaseTime;

            } else {

                // No next higher priority event, thus finish the last remaining job.
                SchedulerIntervalEvent currentJobEvent = new SchedulerIntervalEvent( currentTimeStamp, (int)(currentTimeStamp+currentJob.remainingExecTime), currentRunTask, "");
                // Check last task's scheduling states.
                if ( currentJob.hasStarted == false ) {
                    currentJob.hasStarted = true;
                    currentJobEvent.setScheduleStates(SchedulerIntervalEvent.SCHEDULE_STATE_START, SchedulerIntervalEvent.SCHEDULE_STATE_END);
                } else {
                    currentJobEvent.setScheduleStates(SchedulerIntervalEvent.SCHEDULE_STATE_RESUME, SchedulerIntervalEvent.SCHEDULE_STATE_END);
                }

                simEventContainer.add(currentJobEvent);

                anyJobRunning = false;
                currentTimeStamp = (int)(currentTimeStamp+currentJob.remainingExecTime);
            }

            continue;
        }

        progressUpdater.setIsFinished(true);
    }

    // The bigger the number the higher the priority
    // When calling this function, the taskContainer should not contain idle task.
    protected void assignPriority()
    {
        ArrayList<Task> allTasks = taskSet.getTasksAsArray();

        // Don't include the idle task.
        Task idleTask = taskSet.getIdleTask();
        allTasks.remove(idleTask);

        int numTasks = allTasks.size();

        /* Assign priorities (RM) */
        for (int i = 0; i < numTasks; i++) {
            Task task_i = allTasks.get(i);
            int cnt = 1;    // 1 represents highest priority.
            /* Get the priority by comparing other tasks. */
            for (int j = 0; j < numTasks; j++) {
                if (i == j)
                    continue;

                Task task_j = allTasks.get(j);
                if (task_j.getPeriod() > task_i.getPeriod()
                        || (task_j.getPeriod() == task_i.getPeriod() && task_j.getId() > task_i.getId())) {
                    cnt++;
                }
            }
            task_i.setPriority(cnt);
        }
    }

}
