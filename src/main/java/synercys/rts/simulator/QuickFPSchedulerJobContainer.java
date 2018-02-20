package synercys.rts.simulator;

import synercys.rts.framework.Job;
import synercys.rts.framework.Task;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by CY on 8/19/2015.
 */
public class QuickFPSchedulerJobContainer {
    ArrayList<Job> jobs = new ArrayList<>();
    Stack<Job> jobStack = new Stack<>();

    public void add( Job inJob )
    {
        jobs.add(inJob);
    }

    public void sortJobsIntoStack() {
        Job nextJob = null;
        Stack<Job> reversedJobStack = new Stack<>();
        while ((nextJob=popNextEarliestHighestPriorityJob()) != null) {
            reversedJobStack.push(nextJob);
        }
        /* reverse the order from reversedJobStack */
        for (Job thisJob : reversedJobStack) {
            jobStack.push(thisJob);
        }
    }

    /* This method pops the earliest available job.
    * Use popNextHighestPriorityJobByTime() instead to determine reference time stamp. */
    public Job popNextEarliestHighestPriorityJob()
    {
        if ( jobs.size() == 0 )
            return null;

        Job nextHighJob = null;
        Boolean firstLoop = true;
        for ( Job thisJob : jobs ) {
            if ( firstLoop == true ) {
                firstLoop = false;
                nextHighJob = thisJob;
                continue;
            }

            // Note that bigger value in priority means higher priority.
            if ( (thisJob.releaseTime<nextHighJob.releaseTime)
                    || ( (thisJob.releaseTime==nextHighJob.releaseTime) && (thisJob.task.getPriority()>=nextHighJob.task.getPriority()) ) ) {
                nextHighJob = thisJob;
            }
        }
        jobs.remove(nextHighJob);
        return nextHighJob;
    }

    public Job popNextEarliestHigherPriorityJobByTime(int inPriority, int timeStamp)
    {
        if ( jobs.size() == 0 )
            return null;

        Job nextHighJob = null;
        Boolean firstLoop = true;
        for ( Job thisJob : jobs ) {
            // Skip the job that is later than the designated time.
            if ( (thisJob.releaseTime>timeStamp) || (thisJob.task.getPriority()<=inPriority) )
                continue;

            if ( firstLoop == true ) {
                firstLoop = false;
                nextHighJob = thisJob;
                continue;
            }

            // Among those jobs that have higher priority, find the earliest one.
            if ( thisJob.releaseTime<nextHighJob.releaseTime
                    || ( thisJob.releaseTime==nextHighJob.releaseTime && thisJob.task.getPriority()>nextHighJob.task.getPriority() )) {
                nextHighJob = thisJob;
            }
        }
        jobs.remove( nextHighJob );
        return nextHighJob;
    }

    public Job popNextHighestPriorityJobByTime( int timeStamp )
    {
        if ( jobs.size() == 0 )
            return null;

        Job nextHighJob = null;
        Boolean firstLoop = true;
        for ( Job thisJob : jobs ) {
            // Skip the job that is later than the designated time.
            if ( thisJob.releaseTime>timeStamp )
                 continue;

            if ( firstLoop == true ) {
                firstLoop = false;
                nextHighJob = thisJob;
                continue;
            }

            // Note that bigger value in priority means higher priority.
            if ( thisJob.task.getPriority() > nextHighJob.task.getPriority() ) {
                nextHighJob = thisJob;
            }
        }
        jobs.remove( nextHighJob );
        return nextHighJob;
    }

    public int size() {
        return jobs.size();
    }

    public ArrayList<Job> getJobs() {
        return jobs;
    }

    public ArrayList<Job> getTaskJobs(Task inTask) {
        ArrayList<Job> taskJobs = new ArrayList<>();
        for (Job thisJob : jobs) {
            if (thisJob.task == inTask) {
                taskJobs.add(thisJob);
            }
        }
        return taskJobs;
    }
}