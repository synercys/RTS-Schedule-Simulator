package synercys.rts.scheduler;

import org.apache.commons.math3.distribution.LaplaceDistribution;
import synercys.rts.RtsConfig;
import synercys.rts.framework.Job;
import synercys.rts.framework.Task;
import synercys.rts.framework.TaskSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class LaplaceScheduler extends EdfScheduler {
    HashMap<Task, LaplaceDistribution> taskLaplaceInterArrivalTimeGenerator = new HashMap<>();
    // HashMap<Task, Long> taskMaxInterArrivalTime = new HashMap<>();

    HashMap<Task, Double> taskEpsilon = new HashMap<>();
    HashMap<Task, Long> taskJ = new HashMap<>();
    HashMap<Task, Long> taskSensitivity = new HashMap<>();

    long globalSensitivity = 190*(long)RtsConfig.TIMESTAMP_MS_TO_UNIT_MULTIPLIER;    // 190ms
    long globalProtectionInstanceCount = 0;
    long globalAdmissibleUpperPeriod = 200*(long)RtsConfig.TIMESTAMP_MS_TO_UNIT_MULTIPLIER; // 5Hz
    long globalAdmissibleLowerPeriod = 10*(long)RtsConfig.TIMESTAMP_MS_TO_UNIT_MULTIPLIER;  // 100Hz


    // protected LaplaceDistribution laplaceDistribution;
    Random rand = new Random();
    int i=0;

    // By default J for each task will be calculated based on the default protection time 500ms.
    public LaplaceScheduler(TaskSet taskSet, boolean runTimeVariation, double epsilon) {
        this(taskSet, runTimeVariation, 500*(long)RtsConfig.TIMESTAMP_MS_TO_UNIT_MULTIPLIER, epsilon);
    }

    public LaplaceScheduler(TaskSet taskSet, boolean runTimeVariation, long protectionTime, double epsilon) {
        super(taskSet, runTimeVariation);

        this.assertOnDeadlineMiss = false;

        globalProtectionInstanceCount = calculateGlobalJByProtectionTime(taskSet, protectionTime);

        // long globalSensitivity = taskSet.getLargestPeriod() - taskSet.getSmallestPeriod();
        for (Task task : taskSet.getRunnableTasksAsArray()) {
            // long maxInterArrivalTime = task.getPeriod()*3;
            // taskMaxInterArrivalTime.put(task, maxInterArrivalTime);

            taskEpsilon.put(task, epsilon);
            taskSensitivity.put(task, globalSensitivity);
            taskJ.put(task, globalProtectionInstanceCount);
            // updateTaskJByDuration(task, 500*(long)RtsConfig.TIMESTAMP_MS_TO_UNIT_MULTIPLIER);
            updateTaskLaplaceNoise(task);

            if (task.getAdmissiblePeriodUpper() == 0) {
                // task.setAdmissiblePeriodUpper((long)(task.getPeriod()*1.2));
                task.setAdmissiblePeriodUpper(globalAdmissibleUpperPeriod);
            }

            if (task.getAdmissiblePeriodLower() == 0) {
                // task.setAdmissiblePeriodLower((long)(task.getPeriod()*0.8));
                task.setAdmissiblePeriodLower(globalAdmissibleLowerPeriod);
            }
        }

    }

    protected long calculateGlobalJByProtectionTime(TaskSet taskSet, long protectionTime) {
        long largestJ = 0;
        for (Task task : taskSet.getRunnableTasksAsArray()) {
            long thisJ = (long)Math.ceil((double)protectionTime/task.getPeriod());
            largestJ = (thisJ>largestJ) ? thisJ : largestJ;
        }
        return largestJ;
    }

    public void updateTaskJByDuration(Task task, long protectionTime) {
        taskJ.put(task, (long)Math.ceil((double)protectionTime/task.getPeriod()));
    }

    protected void updateTaskJByAbsoluteValue(Task task, long j) {
        taskJ.put(task, j);
    }

    public void updateTaskLaplaceNoise(Task task) {
        double mu = task.getPeriod(); // location
        if (taskEpsilon.get(task) > 0) {
            double beta = 2 * taskJ.get(task) * taskSensitivity.get(task) / taskEpsilon.get(task); // b is sometimes referred to as the diversity, is a scale parameter.
            taskLaplaceInterArrivalTimeGenerator.put(task, new LaplaceDistribution(mu, beta));
        } else {
            taskLaplaceInterArrivalTimeGenerator.put(task, null);
        }
    }

    public void updateTaskSetLaplaceNoiseByProtectionDuration(long protectionTime) {
        for (Task task : taskSet.getRunnableTasksAsArray()) {
            updateTaskJByDuration(task, protectionTime);
            updateTaskLaplaceNoise(task);
        }
    }

    public void updateTaskEpsilon(Task task, double epsilon) {
        taskEpsilon.put(task, epsilon);
    }

    // public void updateTaskSetLaplaceNoiseByEpsilon(Task task, double epsilon) {
    //     taskEpsilon.put(task, epsilon);
    //     updateTaskLaplaceNoise(task);
    // }

    @Override
    protected Job updateTaskJob(Task task) {

        /* Uncomment for testing randomly skipping jobs */
        // return updateTaskJob_intermittentJobSkip(task);

        /* Uncomment for testing gradually changing period (every 33 instances) */
        // return updateTaskJob_dynamicPeriod(task);

        /* Uncomment for testing dynamic phase */
        // return updateTaskJob_dynamicPhase(task);

        /* Uncomment for testing dynamic start times */
        // return updateTaskJob_dynamicStartTime(task);


        /* Determine next arrival time. */
        long interArrivalTime = getLaplaceInterArrivalTime(task);
        long nextArrivalTime = nextJobOfATask.get(task).releaseTime + interArrivalTime;
        // System.out.println(interArrivalTime*RtsConfig.TIMESTAMP_UNIT_TO_MS_MULTIPLIER);

        if (traceEnabled) {
            taskInterArrivalTimeTrace.get(task).add(interArrivalTime);
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

    private Job updateTaskJob_dynamicStartTime(Task task) {
        Job newJob = super.updateTaskJob(task);
        newJob.releaseTime += getRandomInt(0, (int)(task.getPeriod()-task.getWcet()));
        return newJob;
    }

    private Job updateTaskJob_dynamicPeriod(Task task) {
        i++;

        long orgPeriod = task.getPeriod();

        // Multiply the period by 1.2 every 20 instances
        // task.setPeriod((long)(orgPeriod*Math.pow(1.2, (int)(i/20))));

        // Increase 10Hz every 20 instances
        task.setPeriod((long)((1.0/(task.getFreq()-(i/20)*10))/RtsConfig.TIMESTAMP_UNIT_TO_S_MULTIPLIER));
        System.out.println(task.getPeriod());

        Job newJob = super.updateTaskJob(task);

        task.setPeriod(orgPeriod);

        return newJob;
    }

    private Job updateTaskJob_intermittentJobSkip(Task task) {
        Job newJob = super.updateTaskJob(task);
        while (getUniformRandomBoolean()) {
            // Skip current period and advance to next period
            newJob = super.updateTaskJob(task);
        }
        return newJob;
    }

    private Job updateTaskJob_dynamicPhase(Task task) {
        i++;

        Job newJob = super.updateTaskJob(task);

        if (i%20 == 0) {
            newJob.releaseTime += getRandomInt(1, (int) task.getPeriod()*5 - 1);
            newJob.absoluteDeadline = newJob.releaseTime + task.getPeriod();
        }

        return newJob;
    }

    protected long getLaplaceInterArrivalTime(Task task) {
        if (taskLaplaceInterArrivalTimeGenerator.get(task) == null)
            return task.getPeriod();

        long interArrivalTime;
        while (true) {
            interArrivalTime = (long) taskLaplaceInterArrivalTimeGenerator.get(task).sample();
            if (interArrivalTime>=task.getAdmissiblePeriodLower() && interArrivalTime<=task.getAdmissiblePeriodUpper())
                break;
        }
        return interArrivalTime;
    }

    @Override
    protected void deadlineMissedHook(Job runJob) {
        super.deadlineMissedHook(runJob);

    }

    protected int getUniformNoise() {
        return rand.nextInt(2);
    }

    protected boolean getUniformRandomBoolean() {
        if (rand.nextInt(2) == 1)
            return true;
        else
            return false;
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
