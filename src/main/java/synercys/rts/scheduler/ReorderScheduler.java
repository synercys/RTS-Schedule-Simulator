package synercys.rts.scheduler;

import synercys.rts.framework.Job;
import synercys.rts.framework.Task;
import synercys.rts.framework.TaskSet;
import synercys.rts.framework.event.EventContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class ReorderScheduler extends EdfScheduler {

    protected boolean idleTimeShuffleEnabled = true;
    protected boolean fineGrainedShuffleEnabled = true;
    protected boolean unusedTimeReclamationEnabled = true;

    protected HashMap<Task, Long> taskWCIB = new HashMap<>(); // each task's worst case maximum inversion budget
    protected HashMap<Task, Long> jobRIB = new HashMap<>(); // each task's current job's remaining inversion budget

    Random rand = new Random();

    public ReorderScheduler(TaskSet taskSet, boolean runTimeVariation) {
        super(taskSet, runTimeVariation);
        simEventContainer.setSchedulingPolicy(EventContainer.SCHEDULING_POLICY_REORDER);

        /* initialize taskWCIB and jobRIB */
        long LCap = calculateLCap(taskSet);
        for (Task task : taskSet.getAppTasksAsArray()) {
            taskWCIB.put(task, computeTaskWCIB(task, LCap));
            jobRIB.put(task, taskWCIB.get(task));
        }

    }


    @Override
    protected Job getNextJob(long tick) {
        ArrayList<Job> candidateJobs = new ArrayList<>();
        Job nextJob = null;

        /* Step 1: check the top priority job and construct candidate list. */
        Job topPriorityJob = getNextJobInReadyQueue(tick);
        if (topPriorityJob == null) {
            /* No job is active at this given tick point, so let's move to the first arrived job in the future. */
            topPriorityJob = getEarliestArrivedJobWithCloserDeadline();
            tick = topPriorityJob.releaseTime;
        }

        if (getJobRIB(topPriorityJob) <= 0) {
            /* No priority inversion is allowed. */
            return topPriorityJob;
        }

        ArrayList<Job> readyJobs = getAllReadyJobs(tick);
        if (idleTimeShuffleEnabled == false) {
            if (readyJobs.size() == 1) {
                // There is only one job (the highest priority job) in the ready queue.
                return topPriorityJob;
            }
        }

        long topPriorityJobM = computeCurrentJobM(topPriorityJob, tick);
        if (topPriorityJobM == -1) {
            candidateJobs.addAll(readyJobs);
        } else {
            for (Job job : readyJobs) {
                if (job.absoluteDeadline<=topPriorityJobM)
                    candidateJobs.add(job);
            }
        }

        if (idleTimeShuffleEnabled) {
            if (topPriorityJobM==-1) {
                /* All jobs in the ready queue are open to priority inversion, so let's add the idle job to the candidate list. */
                long smallestRIB = -1;
                for (Job job : readyJobs) {
                    if (smallestRIB == -1)
                        smallestRIB = jobRIB.get(job.task);
                    else
                        smallestRIB = smallestRIB<jobRIB.get(job.task) ? smallestRIB : jobRIB.get(job.task);
                }
                // Making the remaining execution time as smallestRIB+1 ensures that the idle job will be preempted.
                Job dummyIdleTaskJob = new Job(taskSet.getIdleTask(), tick, smallestRIB+1);
                dummyIdleTaskJob.hasStarted = true;
                candidateJobs.add(dummyIdleTaskJob);
            }
        }

        /* Step 2: randomly pick one job from the list */
        int randomSelectionIndex = getRandomInt(0, candidateJobs.size()-1);
        nextJob = candidateJobs.get(randomSelectionIndex);

        return nextJob;

    }

    @Override
    protected long getPreemptingTick(Job runJob, long tick) {
        long preemptingTick = -1;

        /* a preempting point (or say scheduling point) occurs when:
         *  1. RIB of any job in current ready queue being priority-inversed becomes 0.
         *  2. Literally some jobs arrive (as the original EDF scheduling)
         *  (3. when runJob itself is finished -- this is not preempting, thus is not handled here)
         */

        /* Check condition 1 -- RIB of jobs in the ready queue. */
        for (Job job : getAllReadyJobs(tick)) {
            if (job == runJob)
                continue;

            if ((job.absoluteDeadline < runJob.absoluteDeadline) || (runJob.task.getTaskType().equalsIgnoreCase(Task.TASK_TYPE_IDLE))) { // < is based on equation 2 in the REORDER paper
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

        return preemptingTick;
    }

    @Override
    protected void runJobExecutedHook(Job runJob, long tick, long executedTime) {
        for (Job job : getAllReadyJobs(tick-executedTime)) {
            if (runJob == job)
                continue;

            if ((job.absoluteDeadline<runJob.absoluteDeadline) || (runJob.task.getTaskType().equalsIgnoreCase(Task.TASK_TYPE_IDLE)))
                consumeJobRIB(job, executedTime);
        }
    }

    /* this is where a job is finished and RIB is refreshed. */
    @Override
    protected Job updateTaskJob(Task task) {
        refreshTaskJobRIB(task);
        return super.updateTaskJob(task);
    }

    protected long getJobRIB(Job job) {
        return jobRIB.get(job.task);
    }

    protected void refreshTaskJobRIB(Task task) {
        jobRIB.put(task, taskWCIB.get(task));
    }


    /**
     * Compute the minimum inversion deadline for the given job. It is used to exclude jobs from priority inversion.
     * No job has a higher deadline than jobM_i of the given job can be scheduled as long as this job_i has an unfinished job.
     * @param job   the job_i
     * @param tick  present tick
     * @return  the minimum inversion deadline for job_i. -1 indicates that nothing needs to be excluded from priority inversion.
     */
    protected long computeCurrentJobM(Job job, long tick) {
        ArrayList<Job> readyJobs = getAllReadyJobs(tick);
        long jobM = -1;
        for (Job jJob : readyJobs) {
            if (jJob == job)
                continue;

            if ((jJob.absoluteDeadline>job.absoluteDeadline) && (getJobRIB(jJob)<=0)) { // TODO: Fix the equation at the top of page 4 with <= 0
                if (jobM == -1)
                    jobM = jJob.absoluteDeadline;
                else
                    jobM = jobM < jJob.absoluteDeadline ? jobM : jJob.absoluteDeadline;
            }
        }
        return jobM;    // Note that jobM might be -1, which indicates that
    }


    /* WCIB_i = D_i - R'_i
     * where R'_i is the +1 version of the WCRT
     */
    protected long computeTaskWCIB(Task task, long LCap) {
        return task.getDeadline() - calculateTaskWCRTWithPriorityInversion(task, LCap);
    }

    /**
     * Calculate the worst case response time (WCRT) when priority inversion is considered of the given task.
     * @param task task_i
     * @param LCap L cap (can be obtained by invoking calculateRCap())
     * @return WCRT with priority inversion for the given task_i
     */
    protected long calculateTaskWCRTWithPriorityInversion(Task task, long LCap) {
        long wcrt = 0;
        long aMax = LCap - task.getWcet();
        for (long a=0; (a<aMax) || (a==0); a++) {
            // Think of t as the arrival time point here, iterating through all possible arrival time points.
            wcrt = Math.max(wcrt, calculateTaskResponseTimeReleasedAtGivenTimePointWithPriorityInversion(task, a));
        }
        return wcrt;
    }


    /**
     * Compute the response time (with priority inversion considered) for task_i, assuming it's arriving at the given time point a.
     * R'_i(t) = max{Ci, L'_i(a) - a}
     * @param task  task_i
     * @param a the assumed arrival time of the task_i
     * @return the response time of task_i arriving at time point a
     */
    protected long calculateTaskResponseTimeReleasedAtGivenTimePointWithPriorityInversion(Task task, long a) {
        // The following call uses traditional (Spuri's paper) WCRT analysis with minor modifications (two +1 terms)
        // This does not consider the interference from lower priority jobs (Tj>Ti)
        // return Math.max(task.getWcet(), calculateWorkloadUpToGivenInclusiveArrivalInstanceWithPriorityInversion(task, a)-a);

        // The following call uses the WCRT equation given in the REORDER paper.
        // ** Update 2019, 9/19: this call now explicitly includes the interference from lower priority jobs (Tj>Ti)
        return Math.max(task.getWcet(), REORDER_calculateWorkloadUpToGivenInclusiveArrivalInstance(task, a)-a);
    }


    /**
     * Compute total workload (with priority inversion considered) from t=0 to the end of task_i's job arriving at time a.
     * L'_i(a)
     * See the description for the recursion function for more details.
     * @param task  task_i
     * @param a the assumed arrival time of the task_i
     * @return total workload Li(a)
     */
    protected long calculateWorkloadUpToGivenInclusiveArrivalInstanceWithPriorityInversion(Task task, long a) {
        long initialLi = 0;
        long Di = task.getDeadline();
        long Ti = task.getPeriod();
        long Ci = task.getWcet();
        for (Task jTask : taskSet.getAppTasksAsArray()) {
            long Dj = jTask.getDeadline();
            long Cj = jTask.getWcet();
            if ( (jTask==task) || (Dj>a+Di) )
                continue;

            initialLi += Cj;
        }
        initialLi += (a%Ti==0?1:0)*Ci;
        return calculateWorkloadUpToGivenInclusiveArrivalInstanceRecursionWithPriorityInversion(task, a, initialLi);
    }


    /**
     * Compute total workload (with priority inversion considered) from t=0 to the end of task_i's job arriving at time a.
     * L'_i^(m+1)(a) = W'_i(a, L'_i^(m)(a))
     * It stops when L'i^(m)==L'i^(m+1)
     * Note that, except task_i, all tasks arrive at t=0 (task phase = 0 for all task_j).
     * @param task  task_i
     * @param a the assumed arrival time of the task_i
     * @param currentLi
     * @return total workload Li(a)
     */
    protected long calculateWorkloadUpToGivenInclusiveArrivalInstanceRecursionWithPriorityInversion(Task task, long a, long currentLi) {
        long higherPriorityWorkload = 0;
        long Di = task.getDeadline();
        long Ti = task.getPeriod();
        long Ci = task.getWcet();
        for (Task jTask : taskSet.getAppTasksAsArray()) {
            long Tj = jTask.getPeriod();
            long Dj = jTask.getDeadline();
            long Cj = jTask.getWcet();

            if ( (task==jTask) || (Dj>(a+Di)) )
                continue;

            /* Below is the original code from typical WCRT analysis in EDF as a reference.
             * ==== code ====
             * In REORDER, we add +1 in both conditions to consider the extra delay caused by the priority inversion.
             * higherPriorityWorkload += Cj*Math.min(
             *       Math.ceil((double)currentLi/Tj),
             *       1 +  Math.floor( (double)(a+Di-Dj)/Tj )
             * );
             * ==== end ====
             */
            higherPriorityWorkload += Cj*Math.min(
                    Math.ceil((double)currentLi/Tj) + 1,    // *** +1
                    1 +  Math.floor( (double)(a+Di-Dj)/Tj ) + 1 // *** +1
            );
        }

        long sia = a - (long)Math.floor(a/Ti)*Ti; // The arrival time of the first instance of the task_i
        long totalWorkload = 0;
        if (currentLi>sia) {
            totalWorkload = higherPriorityWorkload + Ci * (long) Math.min(
                    Math.ceil((double) (currentLi - sia) / Ti),
                    1 + Math.floor((double) a / Ti)
            );
        } else {
            totalWorkload = higherPriorityWorkload;
        }

        if (totalWorkload == currentLi) {
            return currentLi;
        } else {
            return calculateWorkloadUpToGivenInclusiveArrivalInstanceRecursionWithPriorityInversion(task, a, totalWorkload);
        }
    }


    /* CAUTION: This is implemented based on the REORDER paper.
     * W_i(t) = [floor(t/Ti) + 1]*Ci + I_i(t)
     * Note: It is to compute the $L_i(a)$ term in Spuri's paper.
     */
    protected long REORDER_calculateWorkloadUpToGivenInclusiveArrivalInstance(Task task, long t) {
        return (long)(Math.floor((double)t/task.getPeriod())+1)*task.getWcet() + REORDER_calculateInterferenceForTaskAtGivenTimePoint(task, t);
    }


    /* CAUTION: This is implemented based on the REORDER paper.
     * Given task i (task_index), compute the upper bound of the experienced interference:
     * I_i(t) = sum{ min[ceil(Di/Tj)+1, 1+floor[(t+Di-Dj)/Tj]+1] * Cj} | j~=i, Dj <= t+Di
     */
    protected long REORDER_calculateInterferenceForTaskAtGivenTimePoint(Task task, long t) {
        long Di = task.getDeadline();
        long interference = 0;
        for (Task jTask : taskSet.getAppTasksAsArray()) {
            long Dj = jTask.getDeadline();
            long Tj = jTask.getPeriod();
            long Cj = jTask.getWcet();
            if (jTask==task)
                continue;

            if (Dj > (t+Di))
                interference += Cj;
            else {
                interference += Cj * (1 + Math.floor((double) (t + Di - Dj) / Tj) + 1);
            }
        }
        return interference;
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
