package synercys.rts.scheduler;

import synercys.rts.framework.event.EventContainer;
import synercys.rts.framework.Job;
import synercys.rts.framework.Task;
import synercys.rts.framework.TaskSet;

import java.util.HashMap;

/**
 * EdfScheduler.java
 * Purpose: An implementation of the EDF scheduler and some utility functions.
 *
 * @author CY Chen (cchen140@illinois.edu)
 * @version 1.1 - 2019, 9/7
 * @version 1.0 - 2018, 12/21
 */
public class EdfScheduler extends AdvanceableSchedulerSimulator {

    public EdfScheduler(TaskSet taskSet, boolean runTimeVariation) {
        super(taskSet, runTimeVariation, EventContainer.SCHEDULING_POLICY_EDF);
    }

    @Override
    protected void setTaskSetHook() {
        calculateAndSetWCRT(taskSet);
    }

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

            if (job.absoluteDeadline < earliestDeadline) {
                earliestDeadline = job.absoluteDeadline;
                targetJob = job;
            } else if ((job.absoluteDeadline == earliestDeadline) && (job.task.getPeriod() < targetJob.task.getPeriod())) {
                // When two jobs have the same absolute deadline, we choose the one with the smallest period
                // (to make the behavior consistent.)
                targetJob = job;
            }
        }

        if (targetJob != null)
            return targetJob;

        /* No job is active at this given tick point, so let's check who is the first job in the future. */
        return getEarliestArrivedJobWithCloserDeadline();
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


    protected Job getEarliestArrivedJobWithCloserDeadline() {
        Job targetJob = null;
        long earliestNextReleaseTime = Long.MAX_VALUE;
        for (Job job: nextJobOfATask.values()) {
            if (job.releaseTime < earliestNextReleaseTime) {
                earliestNextReleaseTime = job.releaseTime;
                targetJob = job;
            } else if ((job.releaseTime == earliestNextReleaseTime) && (job.absoluteDeadline < targetJob.absoluteDeadline)) {
                targetJob = job;
            }
        }
        return targetJob;
    }


    /**
     * Calculate and set the worst case response time for all the tasks in the given task set
     * @param taskSet the task set in which tasks to be examined
     */
    public static void calculateAndSetWCRT(TaskSet taskSet) {
        long LCap = calculateLCap(taskSet);
        for (Task task : taskSet.getAppTasksAsArray()) {
            task.setWcrt(calculateTaskWCRT(taskSet, task, LCap));
        }
    }


    /**
     * Calculate L cap which is needed for calculating a task's WCRT.
     * L cap is the largest possible length of any busy interval for the given task set.
     * r^0 = sum(Cj)
     * Please also see the description for the recursive call.
     * @param taskSet the task set under examination
     * @return L cap
     */
    protected static long calculateLCap(TaskSet taskSet) {
        long r0 = 0;
        for (Task task : taskSet.getAppTasksAsArray()) {
            r0 += task.getWcet();
        }
        return calculateLCapRecursion(taskSet, r0);
    }


    /**
     * Recursion function for computing L cap.
     * L cap is the largest possible length of any busy interval for the given task set.
     * r^0 = sum(Cj), r^(k+1) = sum( ceil(r^k / Tj)*Cj )
     * It stops when r^k == r^{k+1}, and then cap(L) = r^k
     * @param taskSet the task set under examination
     * @param rCurrent L cap result from the last recursion level
     * @return L cap
     */
    protected static long calculateLCapRecursion(TaskSet taskSet, long rCurrent) {
        long rNext = 0;
        for (Task task : taskSet.getAppTasksAsArray()) {
            rNext += Math.ceil((double)rCurrent/task.getPeriod())*task.getWcet();
        }
        if (rNext == rCurrent) {
            return rCurrent;
        } else {
            return calculateLCapRecursion(taskSet, rNext);
        }
    }


    /**
     * Calculate the worst case response time (WCRT) of the given task.
     * @param taskSet the task set under examination
     * @param task task_i
     * @param LCap L cap (can be obtained by invoking calculateRCap())
     * @return WCRT of the given task_i
     */
    public static long calculateTaskWCRT(TaskSet taskSet, Task task, long LCap) {
        long wcrt = 0;
        long aMax = LCap - task.getWcet();
        for (long a=0; (a<aMax) || (a==0); a++) {
            // Think of t as the arrival time point here, iterating through all possible arrival time points.
            wcrt = Math.max(wcrt, calculateTaskResponseTimeReleasedAtGivenTimePoint(taskSet, task, a));
        }
        return wcrt;
    }


    /**
     * Compute the response time for task_i, assuming it's arriving at the given time point a.
     * R_i(t) = max{Ci, L_i(a) - a}
     * @param taskSet the task set under examination
     * @param task  task_i
     * @param a the assumed arrival time of the task_i
     * @return the response time of task_i arriving at time point a
     */
    protected static long calculateTaskResponseTimeReleasedAtGivenTimePoint(TaskSet taskSet, Task task, long a) {
        return Math.max(task.getWcet(), calculateWorkloadUpToGivenInclusiveArrivalInstance(taskSet, task, a)-a);
    }


    /**
     * Compute total workload from t=0 to the end of task_i's job arriving at time a.
     * L_i(a)
     * See the description for the recursion function for more details.
     * @param taskSet the task set under examination
     * @param task  task_i
     * @param a the assumed arrival time of the task_i
     * @return total workload Li(a)
     */
    protected static long calculateWorkloadUpToGivenInclusiveArrivalInstance(TaskSet taskSet, Task task, long a) {
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
        return calculateWorkloadUpToGivenInclusiveArrivalInstanceRecursion(taskSet, task, a, initialLi);
    }


    /**
     * Compute total workload from t=0 to the end of task_i's job arriving at time a.
     * L_i^(m+1)(a) = W_i(a, L_i^(m)(a))
     * It stops when Li^(m)==Li^(m+1)
     * Note that, except task_i, all tasks arrive at t=0 (task phase = 0 for all task_j).
     * @param taskSet the task set under examination
     * @param task  task_i
     * @param a the assumed arrival time of the task_i
     * @param currentLi
     * @return total workload Li(a)
     */
    protected static long calculateWorkloadUpToGivenInclusiveArrivalInstanceRecursion(TaskSet taskSet, Task task, long a, long currentLi) {
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

            higherPriorityWorkload += Cj*Math.min(
                    Math.ceil((double)currentLi/Tj),
                    1 +  Math.floor( (double)(a+Di-Dj)/Tj )
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
            return calculateWorkloadUpToGivenInclusiveArrivalInstanceRecursion(taskSet, task, a, totalWorkload);
        }
    }


    /* CAUTION: This is implemented based on REORDER for study only.
     * W_i(t) = [floor(t/Ti) + 1]*Ci + I_i(t)
     * Note: It is to compute the $L_i(a)$ term in Spuri's paper.
     */
    @Deprecated
    private static long REORDER_calculateWorkloadUpToGivenInclusiveArrivalInstance(TaskSet taskSet, Task task, long t) {
        return (long)(Math.floor((double)t/task.getPeriod())+1)*task.getWcet() + REORDER_calculateInterferenceForTaskAtGivenTimePoint(taskSet, task, t);
    }


    /* CAUTION: This is implemented based on REORDER for study only.
     * Given task i (task_index), compute the upper bound of the experienced interference:
     * I_i(t) = sum{ min[ceil(Di/Tj)+1, 1+floor[(t+Di-Dj)/Tj]+1] * Cj} | j~=i, Dj <= t+Di
     */
    @Deprecated
    private static long REORDER_calculateInterferenceForTaskAtGivenTimePoint(TaskSet taskSet, Task task, long t) {
        long Di = task.getDeadline();
        long interference = 0;
        for (Task jTask : taskSet.getAppTasksAsArray()) {
            long Dj = jTask.getDeadline();
            long Tj = jTask.getPeriod();
            long Cj = jTask.getWcet();
            if ( (jTask==task) || (Dj>(t+Di)))
                continue;

            interference += Cj*Math.min(
                    Math.ceil((double)Di/Tj)+1,
                    1+Math.floor((t+Di-Dj)/Tj)+1);
        }
        return interference;
    }

}
