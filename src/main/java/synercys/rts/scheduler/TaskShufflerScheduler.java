package synercys.rts.scheduler;

import synercys.rts.framework.Job;
import synercys.rts.framework.Task;
import synercys.rts.framework.TaskSet;
import synercys.rts.framework.event.EventContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static synercys.rts.framework.TaskSet.myCeil;

public class TaskShufflerScheduler extends FixedPriorityScheduler {
    protected static String SHUFFLE_MODE_NORMAL = "normal";
    protected static String SHUFFLE_MODE_WITH_IDLE = "withIdle";
    protected static String SHUFFLE_MODE_FINE_GRAINED = "fineGrained";

    protected String shuffleMode = SHUFFLE_MODE_NORMAL;

    protected HashMap<Task, Long> taskWCIB = new HashMap<>(); // each task's worst case maximum inversion budget
    protected HashMap<Task, Long> jobRIB = new HashMap<>(); // each task's current job's remaining inversion budget
    protected HashMap<Task, Integer> taskM = new HashMap<>();   // each task's minimum inversion priority, M_i (TaskShuffler, Definition 3)

    Random rand = new Random();

    public TaskShufflerScheduler(TaskSet taskSet, boolean runTimeVariation) {
        super(taskSet, runTimeVariation);
        simEventContainer.setSchedulingPolicy(EventContainer.SCHEDULING_POLICY_TASKSHUFFLER);

        /* initialize taskWCIB and jobRIB */
        for (Task task : taskSet.getAppTasksAsArray()) {
            taskWCIB.put(task, computeTaskWCIB(task));
            jobRIB.put(task, taskWCIB.get(task));
        }
        /* initialize taskM */
        for (Task task : taskSet.getAppTasksAsArray()) {
            taskM.put(task, computeTaskMinInversionPriority(task));
        }
    }

    @Override
    protected Job getNextJob(long tick) {
        ArrayList<Job> candidateJobs = new ArrayList<>();
        Job nextJob = null;
        if (shuffleMode.equalsIgnoreCase(SHUFFLE_MODE_NORMAL)) {

            /* Step 1: get the highest priority task and check */
            Job topPriorityJob = getNextJobInReadyQueue(tick);
            if (topPriorityJob == null) {
                /* No job is active at this given tick point, so let's return the first arrived job in the future. */
                return getEarliestArrivedHigherPriorityJob();
            }

            if (getJobRIB(topPriorityJob) <= 0) {
                /* No priority inversion is allowed. */
                return topPriorityJob;
            }

            /* Step 1-a and 1-b: construct candidate list */
            ArrayList<Job> readyJobs = getAllReadyJobs(tick);
            if (readyJobs.size() == 1) {
                // There is only one job (the highest priority job) in the ready queue.
                return topPriorityJob;
            }

            int topPriorityWithZeroRIB = 0;
            Job topPriorityJobWithZeroRIB = findTopPriorityJobWithZeroRIB(readyJobs);
            if (topPriorityJobWithZeroRIB != null)
                topPriorityWithZeroRIB = topPriorityJobWithZeroRIB.task.getPriority();

            int topPriorityJobM = taskM.get(topPriorityJob.task);

            for (Job job : readyJobs) {
                int thisJobPriority = job.task.getPriority();
                if ((thisJobPriority>=topPriorityWithZeroRIB) && (thisJobPriority>=topPriorityJobM)) {
                    candidateJobs.add(job);
                }
            }

            /* Step 2: randomly pick one job from the list */
            int randomSelectionIndex = getRandomInt(0, candidateJobs.size()-1);
            nextJob = candidateJobs.get(randomSelectionIndex);
        } else if (shuffleMode.equalsIgnoreCase(SHUFFLE_MODE_WITH_IDLE)) {

        }
        return nextJob;
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

    protected Job findTopPriorityJobWithZeroRIB(ArrayList<Job> jobs) {
        Job topPriorityJobWithZeroRIB = null;
        for (Job job : jobs) {
            if (getJobRIB(job)<=0) {
                if (topPriorityJobWithZeroRIB == null)
                    topPriorityJobWithZeroRIB = job;
                else {
                    topPriorityJobWithZeroRIB = topPriorityJobWithZeroRIB.task.getPriority()>job.task.getPriority() ? topPriorityJobWithZeroRIB : job;
                }
            }
        }
        return topPriorityJobWithZeroRIB;
    }

    @Override
    protected long getPreemptingTick(Job runJob, long tick) {
        long preemptingTick = -1;
        if (shuffleMode.equalsIgnoreCase(SHUFFLE_MODE_NORMAL)) {
            /* a preempting point (or say scheduling point) occurs when:
             *  1. RIB of any job in current ready queue being priority-inversed becomes 0.
             *  2. Literally some higher priority jobs arrive (as the original FP scheduling)
             *  (3. when runJob itself is finished -- this is not preempting, thus is not handled here)
             */

            /* Check condition 1 -- RIB of jobs in the ready queue. */
            for (Job job : getAllReadyJobs(tick)) {
                if (job.task.getPriority()>runJob.task.getPriority()) {
                    if (getJobRIB(job) < runJob.remainingExecTime) {
                        if (preemptingTick == -1)
                            preemptingTick = tick + getJobRIB(job);
                        else
                            preemptingTick = preemptingTick<(tick+getJobRIB(job)) ? preemptingTick : (tick+getJobRIB(job));
                    }
                }
            }

            /* Now check if any job arrives before that RIB becomes 0 (if any) and after present tick.
             * Note that the jobs arrived before present tick do not preempt (except for RIB==0) the current job
             * as thy were chosen to be priority-inversed when the current job was selected to run.
             */
            long maxPreemptingTick = preemptingTick!=-1 ? preemptingTick : (tick+runJob.remainingExecTime);
            for (Job job: nextJobOfATask.values()) {
                if (job == runJob)
                    continue;

                if ((job.releaseTime>tick) && (job.releaseTime<maxPreemptingTick)) {
                    /* Here is a new arrival! */
                    if (preemptingTick==-1)
                        preemptingTick = job.releaseTime;
                    else
                        preemptingTick = preemptingTick<job.releaseTime ? preemptingTick : job.releaseTime;
                }
            }

        }
        return preemptingTick;
    }

    @Override
    protected void runJobExecutedHook(Job runJob, long tick, long executedTime) {
        //TODO: if it's idle task, then we don't decrease the RIB (check if it's correct)
        for (Job job : getAllReadyJobs(tick-executedTime)) {
            if (runJob == job)
                continue;

            if (job.task.getPriority()>runJob.task.getPriority())
                consumeJobRIB(job, executedTime);
        }
    }

    /* this is where a job is finished and RIB is refreshed. */
    @Override
    protected Job updateTaskJob(Task task) {
        refreshTaskJobRIB(task);
        return super.updateTaskJob(task);
    }

    /* This function is modified based on Man-Ki's original implementation of TaskShuffler. */
    protected long computeTaskWCIB(Task task) {
        long Wi = task.getDeadline();
        long interference = 0;
        for (Task hpTask : taskSet.getHigherPriorityTasks(task.getPriority())) {
            long Tj = hpTask.getPeriod();
            long Cj = hpTask.getWcet();
            interference += ( 1 + myCeil((double)Wi / Tj)) * Cj;
        }
        Wi = task.getWcet() + interference;

        return task.getDeadline() - Wi;
    }

    protected int computeTaskMinInversionPriority(Task task) {
        int maxMinInversionPriority = 0;
        for (Task iTask : taskSet.getAppTasksAsArray()) {
            if ((iTask == task) || (iTask.getPriority()>=task.getPriority()))
                continue;

            if (taskWCIB.get(iTask)<0)
                maxMinInversionPriority = iTask.getPriority()>maxMinInversionPriority ? iTask.getPriority() : maxMinInversionPriority;

        }
        if (maxMinInversionPriority == 0) {
            maxMinInversionPriority = taskSet.getLowestPriorityTask().getPriority();
        }
        return maxMinInversionPriority;
    }

    protected long getJobRIB(Job job) {
        return jobRIB.get(job.task);
    }

    protected void refreshTaskJobRIB(Task task) {
        jobRIB.put(task, taskWCIB.get(task));
    }

    protected long consumeJobRIB(Job job, long consumedBudget) {
        long updatedRIB = jobRIB.get(job.task) - consumedBudget;
        jobRIB.put(job.task, updatedRIB);
        return updatedRIB;
    }

    /**
     * generate a random integer between inclusiveMin and inclusiveMax, both bounds are inclusive.
     * @param inclusiveMin  the smallest possible number
     * @param inclusiveMax  the largest possible number
     * @return an integer between inclusiveMin and inclusiveMax, both bounds are inclusive
     */
    public int getRandomInt(int inclusiveMin, int inclusiveMax) {
        // nextInt generates a number between 0 (inclusive) and the given number (exclusive).
        return rand.nextInt(inclusiveMax - inclusiveMin + 1) + inclusiveMin;
    }
}
