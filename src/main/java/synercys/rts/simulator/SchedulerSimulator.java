package synercys.rts.simulator;

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

    TaskSet taskSet = null;
    ArrayList<Task> allTasks = null;
    int numTasks = 0;
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


    /* Comment added by CY on 2015/05/13
    * Argument format:
    * One line indicates to one task.
    * In each line, specify task's values as follows:
    *   taskID, period, executionTime, deadline
    * */
//    public static void main(String[] args) {
//        if (args.length < 2) {
//            System.out.println("Usage: java RM inputfile tickLimit");
//            return;
//        }
//        RM rmfp = new RM();
//        rmfp.run(args[0], Long.parseLong(args[1]));
//    }

    abstract public boolean runSim(long tickLimit);
    abstract protected void setTaskSetHook();

    public void setTaskSet(TaskSet inTaskSet)
    {
        taskSet = inTaskSet;

        // Remove current idle task if there is any because the simulator will create one later.
        //taskSet.removeIdleTask();
        //taskSet.clearSimData();

        setTaskSetHook(); // Note that the task set includes idle task.

        // Clear previous event container if any
        simEventContainer.clearAll();
        simEventContainer.setTaskSet(taskSet);
    }

    class SimThread extends Thread
    {
        long simTickLength = 0;
        Boolean simResult = false;

        public SimThread(long inSimTickLength)
        {
            super();
            simTickLength = inSimTickLength;
        }

        public void run()
        {
            simResult = runSim(simTickLength);
        }

        public Boolean getSimResult()
        {
            return simResult;
        }
    }

    long getNextInterarrivalTime(Task task_i) {
        // http://en.wikipedia.org/wiki/Poisson_distribution#Generating_Poisson-distributed_random_variables
        // Example: poisson
                /*
                 * double lambda = task_i.period; double L =
                 * Math.exp(-(1/(double)lambda)); double p = 1.0; int k = 0; do { k++; p
                 * *= Math.random(); } while (p > L); return k - 1;
                 */
                /*
                 * //Case of exponential double lambda = 1/(double)task_i.period; double
                 * u=Math.random(); return (long)(Math.log(1-u)/(double)(-lambda));
                 */
        // Case of Fixed
        return task_i.getPeriod();

        // Case of Gaussian
        // double stddev = 0.2;
        // return (long) (random.nextGaussian() * (task_i.period * stddev) +
        // task_i.period);
        // Case of Uniform Distribution
                /*
                 * double ScaleFactor = 0.2; return (long)( ((Math.random()-0.5)*2) *
                 * (ScaleFactor*task_i.period) + task_i.period);
                 */
    }

    long newExecutionTime(Task task_i) {
        // Case of Fixed
        // return task_i.execTime;
        // Case of Gaussian
        double stddev = 0.2;
        return (long) (random.nextGaussian() * (task_i.getExecTime() * stddev) + task_i.getExecTime());
    }

    long getDeviatedExecutionTime(Task task_i) {
        // Case of Fixed
//        return task_i.getExecTime();

        // Case of Gaussian
        double stddev = 0.2;    // added by CY
        double gaussianFactor = random.nextGaussian();
        long deviatedExecutionTime = (long) (gaussianFactor * (task_i.getExecTime() * stddev) + task_i.getExecTime());
        //long deviatedExecutionTime = (long) ((gaussianFactor * stddev) + task_i.getComputationTimeNs());
        return deviatedExecutionTime;//(long) (random.nextGaussian() * (task_i.getComputationTimeNs() * stddev) + task_i.getComputationTimeNs());
        //return (long) ((random.nextGaussian() * stddev) + task_i.getComputationTimeNs());
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

//    boolean readModel(String inputFileName) {
//        allTasks = new ArrayList<SimTask>();
//
//        BufferedReader br = null;
//        String line;
//        StringTokenizer st = null;
//
//        try {
//            br = new BufferedReader(new FileReader(inputFileName));
//
//            while ((line = br.readLine()) != null) {
//                numTasks++;
//                st = new StringTokenizer(line);
//
//                Task task = new Task();
//                task.id = Integer.parseInt(st.nextToken());
//                task.period = Integer.parseInt(st.nextToken());
//                task.execTime = Integer.parseInt(st.nextToken());
//                task.deadline = Integer.parseInt(st.nextToken());
//                task.priority = -1;
//
//                totalUtil += (task.execTime / (double) task.period);
//
//                if (st.hasMoreTokens())
//                    task.initialOffset = Integer.parseInt(st.nextToken());
//                else
//                    task.initialOffset = 0;
//
//                task.nextReleaseTime = task.initialOffset;
//
//                if (task.initialOffset > maximumInitialOffset)
//                    maximumInitialOffset = task.initialOffset;
//
//                task.WCRT = Long.MIN_VALUE;
//                task.jobSeqNo = 0;
//                task.lastReleaseTime = -1;
//                task.lastFinishTime = 0;
//
//                LCM = LCM(LCM, task.period);
//
//                allTasks.addNextEvent(task);
//            }
//
//                        /* Assign priorities (RM) */
//            for (int i = 0; i < numTasks; i++) {
//                Task task_i = allTasks.get(i);
//                int cnt = 0;
//                for (int j = 0; j < numTasks; j++) {
//                    if (i == j)
//                        continue;
//
//                    Task task_j = allTasks.get(j);
//                    if (task_j.period < task_i.period
//                            || (task_j.period == task_i.period && task_j.id < task_i.id)) {
//                        cnt++;
//                    }
//                }
//                task_i.priority = cnt;
//                if (DEBUG)
//                    System.out.println(task_i);
//            }
//
//
//            for (int i = 0; i < numTasks; i++) {
//                NUM_INVOC += (LCM / allTasks.get(i).period);
//            }
//
//            br.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//
//        return true;
//    }

    static long GCD(long a, long b) {
        long Remainder;

        while (b != 0) {
            Remainder = a % b;
            a = b;
            b = Remainder;
        }

        return a;
    }

    static long LCM(long a, long b) {
        return a * b / GCD(a, b);
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
        double Wi = task_i.getExecTime();
        double prev_Wi = 0;

        while (true) {
            double interference = 0;
            for (int i = 0; i < numTasks; i++) {
                Task task_hp = allTasks.get(i);
                if (task_hp.getPriority() >= task_i.getPriority())
                    continue;

                double Tj = task_hp.getPeriod();
                double Cj = task_hp.getExecTime();

                interference += myCeil(Wi / Tj) * Cj;
            }

            Wi = task_i.getExecTime() + interference;

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
