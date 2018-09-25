package synercys.rts.simulator;

import cy.utility.Umath;    // for poisson distributed randomization
import synercys.rts.event.EventContainer;
import synercys.rts.framework.Job;
import synercys.rts.framework.Task;
import synercys.rts.framework.TaskSet;
import cy.utility.ProgressUpdater;

import java.util.*;

/**
 * Created by jjs on 2/13/17.
 */
abstract class SchedulerSimulator {
    public static int DISTRIBUTION_MODE_FIXED = 0;
    public static int DISTRIBUTION_MODE_UNIFORM = 1;
    public static int DISTRIBUTION_MODE_GAUSSIAN = 2;

    public ProgressUpdater progressUpdater = new ProgressUpdater();

    protected TaskSet taskSet = null;

    double totalUtil = 0;
    ArrayList<Job> readyQueue = new ArrayList<>();
    ArrayList<Job> activeQueue = new ArrayList<>();
    // jobs that were released but haven't finished.
    long LCM = 1;
    int maximumInitialOffset = Integer.MIN_VALUE;
    long tick = 0;
    Job currentJob = null;
    Job lastJob = null;
    String inputFileName = null;
    long NUM_INVOC = 0;
    static boolean DEBUG = false; // ////////////////////////////////////////////////////////////////////////
    static boolean DEBUG_SCHLOG = false;// Added by CY
    static Random random = new Random();

    protected EventContainer simEventContainer = new EventContainer();

    /* runTimeVariation:
     * Runtime variation includes execution time and inter-arrival time variations.
     * False value disables runtime variation: execution time will always be WCETs and inter-arrival time will be the task's periods.
     */
    boolean runTimeVariation = true;

    abstract public EventContainer runSim(long tickLimit);
    abstract protected void setTaskSetHook();

    public SchedulerSimulator(TaskSet taskSet) {
        this.taskSet = taskSet;
    }

    public void setTaskSet(TaskSet inTaskSet)
    {
        taskSet = inTaskSet;

        // Remove current idle task if there is any because the simulator will create one later.
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

    long getVariedExecutionTime(Task task_i) {
        // Gaussian Distribution
        double stddev = 0.2;    // added by CY
        double gaussianFactor = random.nextGaussian();
        long deviatedExecutionTime = (long) (gaussianFactor * (task_i.getWcet() * stddev) + task_i.getWcet()*0.8);
        //System.out.println(task_i.getWcet() + " : " + String.valueOf(Long.min(deviatedExecutionTime, task_i.getWcet())));
        return Long.min(deviatedExecutionTime, task_i.getWcet());
    }

    long getVariedInterArrivalTime(long minInterArrival) {
        // Poisson Distribution
        long result = 0;
        while (result < minInterArrival) {
            result = Umath.getPoisson((minInterArrival/10)*1.2)*10;
        }
        return result;
        /* For reference: case of uniform distribution
         * double ScaleFactor = 0.2;
         * return (long)( ((Math.random()-0.5)*2) * (ScaleFactor*task_i.period) + task_i.period);
         */
    }

    // Insert the job to ready queue according to the priority. (the bigger the higher)
    void insertToReadyQueue(Job job_i) {
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

    public static int[][] multiply(int a[][], int b[][]) {
        int aRows = a.length, aColumns = a[0].length, bRows = b.length, bColumns = b[0].length;

        if (aColumns != bRows) {
            throw new IllegalArgumentException("A:Rows: " + aColumns
                    + " did not match B:Columns " + bRows + ".");
        }

        int[][] resultant = new int[aRows][bColumns];

        for (int i = 0; i < aRows; i++) { // aRow
            for (int j = 0; j < bColumns; j++) { // bColumn
                for (int k = 0; k < aColumns; k++) { // aColumn
                    resultant[i][j] += a[i][k] * b[k][j];
                }
            }
        }

        return resultant;
    }

    double calc_WCRT(Task task_i) {
        int numItr = 0;
        double Wi = task_i.getWcet();
        double prev_Wi = 0;

        ArrayList<Task> allTasks = taskSet.getAppTasksAsArray();
        int numTasks = allTasks.size();

        while (true) {
            double interference = 0;
            for (int i = 0; i < numTasks; i++) {
                Task task_hp = allTasks.get(i);
                if (task_hp.getPriority() >= task_i.getPriority())
                    continue;

                double Tj = task_hp.getPeriod();
                double Cj = task_hp.getWcet();

                interference += myCeil(Wi / Tj) * Cj;
            }

            Wi = task_i.getWcet() + interference;

            if (Double.compare(Wi, prev_Wi) == 0)
                return Wi;

            prev_Wi = Wi;

            numItr++;
            if (numItr > 1000 || Double.isInfinite(Wi) || Wi < 0)
                return Double.MAX_VALUE;
        }
    }

    static double myCeil(double val) {
        double diff = Math.ceil(val) - val;
        if (diff > 0.99999) {
            System.out.println("###" + (val) + "###\t\t " + Math.ceil(val));
            System.exit(-1);
        }
        return Math.ceil(val);
    }


    static double[][] getDistribution(LinkedList<Long> list) {
        double[][] distribution = null;
        Collections.sort(list);

        long lowest = list.get(0);
        long highest = list.get(list.size()-1);
        int numDistinct = (int) (highest - lowest + 1);
        int numAll = list.size();

        distribution = new double[numDistinct][numAll];
        for (int i=0; i<distribution.length; i++) {
            distribution[i][0] = lowest + i;
            distribution[i][1] = 0;
        }
        Iterator<Long> itr = list.iterator();
        while(itr.hasNext()) {
            long number = itr.next();
            int idx = (int)(number - lowest);
            distribution[idx][1]++;
        }

        for (int i=0; i<distribution.length; i++) {
            distribution[i][1] /= (double)numAll;
        }

        return distribution;
    }

    public EventContainer getSimEventContainer()
    {
        return simEventContainer;
    }
}
