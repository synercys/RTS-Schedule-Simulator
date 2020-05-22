package synercys.rts.scheduler;

import cy.utility.Umath;
import org.apache.commons.math3.distribution.LaplaceDistribution;
import synercys.rts.RtsConfig;
import synercys.rts.framework.Job;
import synercys.rts.framework.Task;
import synercys.rts.framework.TaskSet;

import java.util.HashMap;
import java.util.Random;

public class LaplaceScheduler extends EdfScheduler {
    HashMap<Task, LaplaceDistribution> taskLaplaceInterArrivalTimeGenerator = new HashMap<>();
    HashMap<Task, Long> taskMaxInterArrivalTime = new HashMap<>();

    HashMap<Task, Double> taskEpsilon = new HashMap<>();
    HashMap<Task, Double> taskJ = new HashMap<>();
    HashMap<Task, Double> taskSensitivity = new HashMap<>();


    // protected LaplaceDistribution laplaceDistribution;
    Random rand = new Random();
    int i=0;


    public LaplaceScheduler(TaskSet taskSet, boolean runTimeVariation) {
        super(taskSet, runTimeVariation);

        for (Task task : taskSet.getRunnableTasksAsArray()) {
            long maxInterArrivalTime = task.getPeriod()*3;
            taskMaxInterArrivalTime.put(task, maxInterArrivalTime);

            double mu = task.getPeriod() + (maxInterArrivalTime - task.getPeriod())/2.0; // location
            double beta = 100; // b is sometimes referred to as the diversity, is a scale parameter.
            taskLaplaceInterArrivalTimeGenerator.put(task, new LaplaceDistribution(mu, beta));
        }

        /* Add noisy tasks if needed */

    }

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
        long nextArrivalTime;
        if (taskLaplaceInterArrivalTimeGenerator.containsKey(task)) {
            long interArrivalTime = getLaplaceInterArrivalTime(task);
            nextArrivalTime = nextJobOfATask.get(task).releaseTime + interArrivalTime;
            // System.out.println(1.0/(interArrivalTime*RtsConfig.TIMESTAMP_UNIT_TO_S_MULTIPLIER));
            System.out.println(interArrivalTime*RtsConfig.TIMESTAMP_UNIT_TO_MS_MULTIPLIER);
        } else if (task.isSporadicTask()) {
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
        if (!taskLaplaceInterArrivalTimeGenerator.containsKey(task)) {
            return task.getPeriod();

        }

        long interArrivalTime = (long) taskLaplaceInterArrivalTimeGenerator.get(task).sample();
        if (interArrivalTime<task.getPeriod())
            interArrivalTime = task.getPeriod();
        else if (interArrivalTime>taskMaxInterArrivalTime.get(task))
            interArrivalTime = taskMaxInterArrivalTime.get(task);

        return interArrivalTime;
    }

    public void setMode(int mode) {
        if (mode <= 1) {   // 0 or 1 (or below)
        }
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
