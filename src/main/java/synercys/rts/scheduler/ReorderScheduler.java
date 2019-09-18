package synercys.rts.scheduler;

import synercys.rts.framework.Task;
import synercys.rts.framework.TaskSet;
import synercys.rts.framework.event.EventContainer;

import java.util.HashMap;
import java.util.Random;

public class ReorderScheduler extends EdfScheduler {

    protected boolean idleTimeShuffleEnabled = true;
    protected boolean fineGrainedShuffleEnabled = true;

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
        /* The following two calculation methods, one based on Spuri's WCRT analysis (with adding two +1 terms)
         * and the other based on the REORDER paper, both yield the same result.
         * REORDER calculation is more efficient as it takes the upper bound directly without needing a iteration loop. */

        // The following call uses traditional (Spuri's paper) WCRT analysis with minor modifications (two +1 terms)
        // return Math.max(task.getWcet(), calculateWorkloadUpToGivenInclusiveArrivalInstanceWithPriorityInversion(task, a)-a);

        // The following call uses the WCRT equation given in the REORDER paper.
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
            if ( (jTask==task) || (Dj>(t+Di)))
                continue;

            interference += Cj*Math.min(
                    Math.ceil((double)Di/Tj)+1,
                    1+Math.floor((t+Di-Dj)/Tj)+1);
        }
        return interference;
    }
}
