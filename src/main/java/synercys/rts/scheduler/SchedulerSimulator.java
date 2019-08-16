package synercys.rts.scheduler;

import cy.utility.Umath;    // for poisson distributed randomization
import synercys.rts.framework.event.EventContainer;
import synercys.rts.framework.Job;
import synercys.rts.framework.Task;
import synercys.rts.framework.TaskSet;
import cy.utility.ProgressUpdater;

import java.util.*;

/**
 * SchedulerSimulator.java
 * Purpose: A abstract class for schedulers.
 *
 * @author CY Chen (cchen140@illinois.edu)
 * @version 1.1 - 2018, 12/22
 * @version 1.0 - 2017, 2/13
 */
abstract class SchedulerSimulator {
    public static int DISTRIBUTION_MODE_FIXED = 0;
    public static int DISTRIBUTION_MODE_UNIFORM = 1;
    public static int DISTRIBUTION_MODE_GAUSSIAN = 2;

    public ProgressUpdater progressUpdater = new ProgressUpdater();

    protected TaskSet taskSet = null;

    protected ArrayList<Job> readyQueue = new ArrayList<>();
    protected ArrayList<Job> activeQueue = new ArrayList<>();
    protected long tick = 0;
    protected Job currentJob = null;
    protected Job lastJob = null;
    protected static boolean DEBUG = false;
    protected static Random random = new Random();

    protected EventContainer simEventContainer = new EventContainer();

    /* runTimeVariation:
     * Runtime variation includes execution time and inter-arrival time variations.
     * False value disables runtime variation: execution time will always be WCETs and inter-arrival time will be the task's periods.
     */
    protected boolean runTimeVariation = true;

    abstract public EventContainer runSim(long tickLimit);
    abstract protected void setTaskSetHook();

    public SchedulerSimulator(TaskSet taskSet, boolean runTimeVariation, String schedulingPolicy) {
        setTaskSet(taskSet);
        simEventContainer.setSchedulingPolicy(schedulingPolicy);
        this.runTimeVariation = runTimeVariation;
    }

    public void setTaskSet(TaskSet inTaskSet)
    {
        taskSet = inTaskSet;

        // Remove current idle task if there is any because the scheduler will create one later.
        //taskSet.removeIdleTask();
        //taskSet.clearSimData();

        setTaskSetHook(); // Note that the taskset includes idle task.

        // Clear previous event container if any
        simEventContainer.clearAll();
        simEventContainer.setTaskSet(taskSet);
    }

    class SimThread extends Thread
    {
        long simTickLength = 0;

        public SimThread(long inSimTickLength)
        {
            super();
            simTickLength = inSimTickLength;
        }

        public void run()
        {
            runSim(simTickLength);
        }

        public EventContainer getSimResult()
        {
            return simEventContainer;
        }
    }

    public void setRunTimeVariation(boolean val) {
        runTimeVariation = val;
    }

    protected long getVariedExecutionTime(Task task_i) {
        // Gaussian Distribution
        double stddev = 0.2;    // added by CY
        double gaussianFactor = random.nextGaussian();
        long wcet = task_i.getWcet();
        long deviatedExecutionTime = (long) (gaussianFactor * (wcet * stddev) + wcet*0.8);
        if (deviatedExecutionTime <= 0) {
            deviatedExecutionTime = 1;
        } else if (deviatedExecutionTime > wcet) {
            deviatedExecutionTime = wcet;
        }
        return deviatedExecutionTime;
    }

    protected long getVariedInterArrivalTime(Task task) {
        long minInterArrival = task.getPeriod();

        // Poisson Distribution
        long variedInterArrivalTime = 0;
        while (variedInterArrivalTime < minInterArrival) {
            variedInterArrivalTime = Umath.getPoisson((minInterArrival/10)*1.2)*10;
        }
        return variedInterArrivalTime;
        /* For reference: case of uniform distribution
         * double ScaleFactor = 0.2;
         * return (long)( ((Math.random()-0.5)*2) * (ScaleFactor*task_i.period) + task_i.period);
         */
    }

    // Insert the job to ready queue according to the priority. (the bigger the higher)
    protected void insertToReadyQueue(Job job_i) {
        int idxToInsert = -1;
        for (int j = 0; j < readyQueue.size(); j++) {
            Job job_j = readyQueue.get(j);
            if (job_i.task.getPriority() > job_j.task.getPriority()
                    || (job_i.task.getId() == job_j.task.getId() && job_i.seqNo > job_j.seqNo)) {
                idxToInsert = j;
                break;
            }
        }
        if (idxToInsert > -1)
            readyQueue.add(idxToInsert, job_i);
        else
            readyQueue.add(job_i);
    }

    public EventContainer getSimEventContainer()
    {
        return simEventContainer;
    }
}
