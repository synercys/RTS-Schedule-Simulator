package synercys.rts.simulator;

import synercys.rts.RtsConfig;
import synercys.rts.framework.Task;
import synercys.rts.framework.TaskSet;
import cy.utility.Umath;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by jjs on 2/13/17.
 */
public class TaskSetGenerator {
    //    private TaskSetContainer taskSetContainer = new TaskSetContainer();

    //static int resolution;

    static int minNumTasks;
    static int maxNumTasks;

    static long minPeriod;
    static long maxPeriod;

    static long maxHyperPeriod;

    static Boolean generateFromHpDivisors;

    static long minWcet;
    static long maxWcet;

    static long minInitOffset;
    static long maxInitOffset;

	/* Assume Period = Deadline */

    static double minUtil;
    static double maxUtil;

    static int numTaskPerSet;
    static int numTaskSet;

    static Boolean nonHarmonicOnly;

    static Boolean needGenObserverTask;
    static double maxObservationRatio;
    static double minObservationRatio;

    //static Boolean needGenBadObserverTask;

    static int observerTaskPriority;
    static int victimTaskPriority;



    static Random rand = new Random();

    public TaskSetGenerator() {

        maxHyperPeriod = (1001_000_000/ RtsConfig.TIMESTAMP_UNIT_NS)*3; // 3 sec

        /* When maxHyperPeriod is specified, only minPeriod will be checked while the check for maxPeriod will be skipped. */
        maxPeriod = 100_000_000/ RtsConfig.TIMESTAMP_UNIT_NS; // 100 ms
        minPeriod = 10_000_000/ RtsConfig.TIMESTAMP_UNIT_NS;   // 10 ms // org = 5ms

        maxWcet = 50_000_000/ RtsConfig.TIMESTAMP_UNIT_NS; // 50 ms // org=3 ms
        minWcet = 100_000/ RtsConfig.TIMESTAMP_UNIT_NS; // 0.1 ms

        maxInitOffset = maxHyperPeriod; // 0ms //10_000_000; // 10 ms
        minInitOffset = 0; // 0 ms

        maxUtil = 1;
        minUtil = 0.9;

        numTaskPerSet = 5;
        numTaskSet = 10;

        generateFromHpDivisors = false;

        nonHarmonicOnly = false;

        needGenObserverTask = false;
        maxObservationRatio = 999;
        minObservationRatio = 1.0;

        observerTaskPriority = 1;
        victimTaskPriority = 2;
    }

    public TaskSetContainer generate() {
        return generate(numTaskPerSet, numTaskSet);
    }

    public TaskSetContainer generate(int inNumTasksPerSet, int inNumTaskSet) {
        maxNumTasks = inNumTasksPerSet;
        minNumTasks = inNumTasksPerSet;

        TaskSetContainer resultTaskSetContainer = new TaskSetContainer();

        for (int i=0; i<inNumTaskSet; i++) {
            TaskSet thisTaskContainer;
            thisTaskContainer = gen();
            if (thisTaskContainer == null) {
                i--;
                continue;
            } else {
                resultTaskSetContainer.addTaskSet(thisTaskContainer);
            }
        }

        return resultTaskSetContainer;
    }

    public static int getRandom(int min, int max) {
        return rand.nextInt(max - min + 1) + min;
    }

//    public static void main(String[] args)
//    {
//		/* TODO: Genearate input in a given range of utilization*/
//        System.out.println("--Usage--");
//        System.out.println("java GenInput [minNumTasks] [maxNumTasks] [minPeriod] [maxPeriod] [minWcet] [maxWcet] [inputStartNo] [inputEndNo] [minUtil] [maxUtil]");
//
//
//        int startNo = Integer.parseInt(args[6]);
//        int endNo = Integer.parseInt(args[7]);
//
//        for (int i = startNo; i <= endNo; i++) {
//            if (gen(args, i) == false) {
//                i--;
//            } else {
//                System.out.println("Inputfile #" + i + " has been generated!");
//            }
//        }
//
//    }

    /* The configurations are passed by global variables. */
    private TaskSet gen()
    {

//        minNumTasks = Integer.parseInt(args[0]);
//        maxNumTasks = Integer.parseInt(args[1]);
//        minPeriod = Integer.parseInt(args[2]);
//        maxPeriod= Integer.parseInt(args[3]);
//        minWcet = Integer.parseInt(args[4]);
//        maxWcet = Integer.parseInt(args[5]);
//        minUtil = Double.parseDouble(args[8]);
//        maxUtil = Double.parseDouble(args[9]);

        int taskSeq = 0;
        int failureCount = 0;

        TaskSet taskContainer = new TaskSet();
//        ArrayList<Task> allTasks= new ArrayList<Task>();

        int numTasks = getRandom(minNumTasks, maxNumTasks);


        // Is maxHyperPeriod enabled?
        ArrayList<Long> hyperPeriodFactors = null;
        if (generateFromHpDivisors == true) {
            hyperPeriodFactors = Umath.integerFactorization(maxHyperPeriod);
            //ProgMsg.debugPutline(hyperPeriodFactors.toString());
        }

        double randomUtil = getRandom((int)(minUtil*100), (int)(maxUtil*100))/100.0;
        ArrayList<Double> utilDistribution = getRandomUtilDistribution(numTasks, randomUtil);

        double total_util = 0;
        double last_total_util = 0;
        for (int i = 0; i < numTasks; i++)
        {
            Task task = new Task();
            task.setId(i+1);
            task.setTitle("APP" + String.valueOf(i+1));

            if (generateFromHpDivisors == true) {
                int tempPeriod = 1;

                if (nonHarmonicOnly == true) {
                    // By fixed the number of chosen factors, we can get nonharmoic task set.
                    tempPeriod = getRandomDivisor(hyperPeriodFactors, 3);
                } else {
                    tempPeriod = getRandomDivisor(hyperPeriodFactors);
                }

                if (tempPeriod<minPeriod) { // || tempPeriod>maxPeriod) {
                    i--;
                    continue;
                }

                if (taskContainer.containPeriod(tempPeriod) == true) {
                    // TODO: Bug to be solved: need to check whether the possible combinations are more than needs.
                    // If the possible combinations are smaller than needs, then it will be stuck here.
                    // Skip duplicated period.
                    i--;
                    continue;
                }

                task.setPeriod(tempPeriod);
                task.setDeadline(task.getPeriod());
            } else {
                int tempPeriod = getRandom((int)minPeriod, (int)maxPeriod);//TODO: maybe... 2^x 3^y 5^z
                //task.setPeriodNs(tempPeriod - tempPeriod % 50);

                // Round to 1ms.
                task.setPeriod(tempPeriod - tempPeriod % (int) (1 * RtsConfig.TIMESTAMP_MS_TO_UNIT_MULTIPLIER));
                //task.setPeriodNs(tempPeriod);
                task.setDeadline(task.getPeriod());
            }

            long tempComputationTime;
            tempComputationTime = task.getPeriod();
            tempComputationTime  = (long)(((double)tempComputationTime)*utilDistribution.get(i));
            //tempComputationTime = (int)(utilDistribution.get(i)*((double)task.getPeriodNs()));
            if (tempComputationTime< minWcet || tempComputationTime> maxWcet) {
                failureCount++;
                if (failureCount > 10) {
                    return null;
                } else {
                    i--;
                    continue;
                }
            } else {
                failureCount = 0;
            }

//            if (minWcet>task.getPeriodNs()) {
//                return null;
//            } else {
//                tempComputationTime = (int) getRandom(minWcet, Math.min(task.getPeriodNs(), maxWcet));
//            }

            // Round to 0.1ms (100us).
            //task.setComputationTimeNs(tempComputationTime - tempComputationTime % 100_000);
            task.setExecTime(tempComputationTime);

            last_total_util = total_util;
            total_util += ( task.getExecTime() / (double)(task.getPeriod()));

            task.setTaskType(Task.TASK_TYPE_APP);

            int tempInitialOffset = getRandom((int)minInitOffset, (int)Math.min(task.getPeriod(), maxInitOffset));
            // Round to 0.1ms (100us).
            //task.setInitialOffset(tempInitialOffset - tempInitialOffset % 100_000);
            task.setInitialOffset(tempInitialOffset);

            taskContainer.addTask(task);

            // Test for getting rid of harmonic periods.
            if (nonHarmonicOnly == true) {
                if (taskContainer.hasHarmonicPeriods() == true) {
                    taskContainer.removeTask(task);
                    i--;
                    total_util = last_total_util;
                    continue;
                }
            }
        }

        last_total_util = taskContainer.getUtilization();

        if (total_util>1)
            return null;

        if (total_util<minUtil || total_util>=maxUtil)
            return null;

        taskContainer.assignPriorityRm();

        if (taskContainer.schedulabilityTest() == false)
            return null;


        double observationRatio = 0;
        if (needGenObserverTask == true) {
            Task victim, observer;
            victim = taskContainer.getOneTaskByPriority(victimTaskPriority);
            observer = taskContainer.getOneTaskByPriority(observerTaskPriority);
            double gcd = Umath.gcd(victim.getPeriod(), observer.getPeriod());
            observationRatio = observer.getExecTime()/gcd;
            if ((observationRatio<minObservationRatio) || (observationRatio>maxObservationRatio)) {
                return null;
            }
        }

//        int[][] sl = new int[numTasks][numTasks];
//        for (int i=0; i<numTasks; i++)
//        {
//            for (int j=0; j<numTasks; j++)
//                sl[i][j] = 0;
//        }
//
//        for (int i=0; i<numTasks; i++)
//        {
//            for (int j=i+1; j<numTasks; j++)
//            {
//                if (rand.nextDouble()<=0.5)
//                    sl[i][j] = 1;
//            }
//        }

        taskContainer.addIdleTask();
        return taskContainer;



        //System.out.println("Total util = " + total_util);

//        FileWriter fw = null;
//        PrintWriter pw = null;
//        try
//        {
//            new File("input").mkdir();
//            fw = new FileWriter("input/input_" + outputFileIndex + ".txt");
//            pw = new PrintWriter(fw);
//            pw.println(numTasks);
//
//            for (int i = 0; i < numTasks; i++)
//            {
//                Task task = allTasks.get(i);
//                System.out.println(task.id+ " " + task.period + " " + task.execTime + " " + task.deadline);
//                pw.println(task.id+ " " + task.period + " " + task.execTime + " " + task.deadline);
//            }
//            System.out.println("Total Util = " + total_util);
//
//            for (int i=0; i<numTasks; i++)
//            {
//                for (int j=i+1; j<numTasks; j++)
//                {
//                    if (sl[i][j] == 1)
//                    {
//                        System.out.println(i + " " + j);
//                        pw.println(i + " " + j);
//                    }
//                }
//            }
//            pw.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return true;
    }

//    static long GCD(long a, long b) {
//        long Remainder;
//
//        while (b != 0) {
//            Remainder = a % b;
//            a = b;
//            b = Remainder;
//        }
//
//        return a;
//    }
//
//    static long LCM(long a, long b) {
//        return a * b / GCD(a, b);
//    }
//
//    public long calHyperPeriod(TaskContainer taskContainer) {
//        long hyperPeriod = 1;
//        for (Task thisTask : taskContainer.getAppTasksAsArray()) {
//            hyperPeriod = LCM(hyperPeriod, thisTask.getPeriodNs());
//        }
//        return hyperPeriod;
//    }
//
//    public Boolean schedulabilityTest(TaskContainer taskContainer) {
//        //int numTasks = taskContainer.getAppTasksAsArray().size();
//        for (Task thisTask : taskContainer.getAppTasksAsArray()) {
//            int thisWCRT = calc_WCRT(taskContainer, thisTask);
//            if (thisWCRT > thisTask.getDeadlineNs()) {
//                // unschedulable.
//                //ProgMsg.errPutline("%d > %d", thisWCRT, thisTask.getDeadlineNs());
//                return false;
//            } else {
//                //ProgMsg.sysPutLine("ok: %d < %d", thisWCRT, thisTask.getDeadlineNs());
//            }
//        }
//        return true;
//    }
//
//    // Code modified from Man-Ki's code
//    int calc_WCRT(TaskContainer taskContainer, Task task_i) {
//        int numItr = 0;
//        int Wi = task_i.getComputationTimeNs();
//        int prev_Wi = 0;
//
//        //int numTasks = taskContainer.getAppTasksAsArray().size();
//        while (true) {
//            int interference = 0;
//            for (Task thisTask : taskContainer.getAppTasksAsArray()) {
//                Task task_hp = thisTask;
//                if (task_hp.getPriority() <= task_i.getPriority())  // Priority: the bigger the higher
//                    continue;
//
//                int Tj = task_hp.getPeriodNs();
//                int Cj = task_hp.getComputationTimeNs();
//
//                interference += (int)myCeil((double)Wi / (double)Tj) * Cj;
//            }
//
//            Wi = task_i.getComputationTimeNs() + interference;
//
//            if (Integer.compare(Wi, prev_Wi) == 0)
//                return Wi;
//
//            prev_Wi = Wi;
//
//            numItr++;
//            if (numItr > 1000 || Wi < 0)
//                return Integer.MAX_VALUE;
//        }
//    }
//
//
//    // Code from Man-Ki
//    double myCeil(double val) {
//        double diff = Math.ceil(val) - val;
//        if (diff > 0.99999) {
//            ProgMsg.errPutline("###" + (val) + "###\t\t " + Math.ceil(val));
//            System.exit(-1);
//        }
//        return Math.ceil(val);
//    }
//
//    // The bigger the number the higher the priority
//    // When calling this function, the taskContainer should not contain idle task.
//    protected void assignPriority(TaskContainer taskContainer)
//    {
//        ArrayList<Task> allTasks = taskContainer.getTasksAsArray();
//        int numTasks = taskContainer.getAppTasksAsArray().size();
//
//        /* Assign priorities (RM) */
//        for (Task task_i : taskContainer.getAppTasksAsArray()) {
//            //Task task_i = allTasks.get(i);
//            int cnt = 1;    // 1 represents highest priority.
//            /* Get the priority by comparing other tasks. */
//            for (Task task_j : taskContainer.getAppTasksAsArray()) {
//                if (task_i.equals(task_j))
//                    continue;
//
//                //Task task_j = allTasks.get(j);
//                if (task_j.getPeriodNs() > task_i.getPeriodNs()
//                        || (task_j.getPeriodNs() == task_i.getPeriodNs() && task_j.getId() > task_i.getId())) {
//                    cnt++;
//                }
//            }
//            task_i.setPriority(cnt);
//        }
//    }

    int getRandomDivisor(ArrayList<Long> inFactors, int numOfChosenFactors) {
        ArrayList<Long> factors = (ArrayList<Long>) inFactors.clone();
        int resultDivisor = 1;
        int randomLoopNum;

        if (numOfChosenFactors == 0) {
            randomLoopNum = getRandom(1, factors.size());
        } else {
            randomLoopNum = numOfChosenFactors;
        }

        for (int i=0; i<randomLoopNum; i++) {
            int thisIndex = getRandom(0, factors.size()-1);
            resultDivisor = resultDivisor * factors.get(thisIndex).intValue();
            factors.remove(thisIndex);
        }
        return resultDivisor;
    }

    int getRandomDivisor(ArrayList<Long> inFactors) {
        return getRandomDivisor(inFactors, 0);
    }


    /* Divide inMaxUtil into inMaxTaskNum pieces evenly, and then mess them up. */
    ArrayList<Double> getRandomUtilDistribution(int inMaxTaskNum, double inMaxUtil) {
        ArrayList<Double> resultUtilArray = new ArrayList<>();

        // Initialize the array with evenly dividing utilization.
        for (int i=0; i<inMaxTaskNum; i++) {
            resultUtilArray.add(inMaxUtil/(double)inMaxTaskNum);
        }

        /* Randomize the distribution. */
        double randUnit = inMaxUtil/100.0;
        for (int i=0; i<100; i++) {
            int indexA, indexB;
            indexA = getRandom(0, inMaxTaskNum-1);
            indexB = getRandom(0, inMaxTaskNum-1);

            if (indexA==indexB || resultUtilArray.get(indexB)<0.001) {
                //i--;
                continue;
            } else {
                resultUtilArray.set(indexA, resultUtilArray.get(indexA) + randUnit);
                resultUtilArray.set(indexB, resultUtilArray.get(indexB) - randUnit);
            }
        }

        return  resultUtilArray;
    }


    /* The following section is the automatically generated setters and getters. */

    public int getMinNumTasks() {
        return minNumTasks;
    }

    public void setMinNumTasks(int minNumTasks) {
        TaskSetGenerator.minNumTasks = minNumTasks;
    }

    public int getMaxNumTasks() {
        return maxNumTasks;
    }

    public void setMaxNumTasks(int maxNumTasks) {
        TaskSetGenerator.maxNumTasks = maxNumTasks;
    }

    public long getMinPeriod() {
        return minPeriod;
    }

    public void setMinPeriod(int minPeriod) {
        TaskSetGenerator.minPeriod = minPeriod;
    }

    public long getMaxPeriod() {
        return maxPeriod;
    }

    public void setMaxPeriod(int maxPeriod) {
        TaskSetGenerator.maxPeriod = maxPeriod;
    }

    public long getMaxHyperPeriod() {
        return maxHyperPeriod;
    }

    public void setMaxHyperPeriod(int maxHyperPeriod) {
        TaskSetGenerator.maxHyperPeriod = maxHyperPeriod;
    }

    public long getMinWcet() {
        return minWcet;
    }

    public void setMinWcet(int minWcet) {
        TaskSetGenerator.minWcet = minWcet;
    }

    public long getMaxWcet() {
        return maxWcet;
    }

    public void setMaxWcet(int maxWcet) {
        TaskSetGenerator.maxWcet = maxWcet;
    }

    public long getMinInitOffset() {
        return minInitOffset;
    }

    public void setMinInitOffset(int minInitOffset) {
        TaskSetGenerator.minInitOffset = minInitOffset;
    }

    public long getMaxInitOffset() {
        return maxInitOffset;
    }

    public void setMaxInitOffset(int maxInitOffset) {
        TaskSetGenerator.maxInitOffset = maxInitOffset;
    }

    public double getMinUtil() {
        return minUtil;
    }

    public void setMinUtil(double minUtil) {
        TaskSetGenerator.minUtil = minUtil;
    }

    public double getMaxUtil() {
        return maxUtil;
    }

    public void setMaxUtil(double maxUtil) {
        TaskSetGenerator.maxUtil = maxUtil;
    }

    public int getNumTaskPerSet() {
        return numTaskPerSet;
    }

    public void setNumTaskPerSet(int numTaskPerSet) {
        TaskSetGenerator.numTaskPerSet = numTaskPerSet;
    }

    public int getNumTaskSet() {
        return numTaskSet;
    }

    public void setNumTaskSet(int numTaskSet) {
        TaskSetGenerator.numTaskSet = numTaskSet;
    }

    public static Boolean getGenerateFromHpDivisors() {
        return generateFromHpDivisors;
    }

    public static Boolean getNeedGenObserverTask() {
        return needGenObserverTask;
    }

    public static void setNeedGenObserverTask(Boolean needGenObserverTask) {
        TaskSetGenerator.needGenObserverTask = needGenObserverTask;
    }

    public static void setMaxObservationRatio(double maxObservationRatio) {
        TaskSetGenerator.maxObservationRatio = maxObservationRatio;
    }

    public static void setMinObservationRatio(double minObservationRatio) {
        TaskSetGenerator.minObservationRatio = minObservationRatio;
    }

    public static int getObserverTaskPriority() {
        return observerTaskPriority;
    }

    public static void setObserverTaskPriority(int observerTaskPriority) {
        TaskSetGenerator.observerTaskPriority = observerTaskPriority;
    }

    public static int getVictimTaskPriority() {
        return victimTaskPriority;
    }

    public static void setVictimTaskPriority(int victimTaskPriority) {
        TaskSetGenerator.victimTaskPriority = victimTaskPriority;
    }

    public static void setGenerateFromHpDivisors(Boolean generateFromHpDivisors) {
        TaskSetGenerator.generateFromHpDivisors = generateFromHpDivisors;
    }

    public static Boolean getNonHarmonicOnly() {
        return nonHarmonicOnly;
    }

    public static void setNonHarmonicOnly(Boolean nonHarmonicOnly) {
        TaskSetGenerator.nonHarmonicOnly = nonHarmonicOnly;
    }

    public String toCommentString() {
        String outputStr = "";

        outputStr += "## Task set parameters:\r\n";
        outputStr += "# num of tasks per set = " + numTaskPerSet + "\r\n";
        outputStr += "# util = " + minUtil*100 + "%% - " + maxUtil*100 + "%%\r\n";
        outputStr += "# exe = " + minWcet * RtsConfig.TIMESTAMP_UNIT_TO_MS_MULTIPLIER + "ms - " + maxWcet * RtsConfig.TIMESTAMP_UNIT_TO_MS_MULTIPLIER + "ms\r\n";
        outputStr += "# offset = " + minInitOffset* RtsConfig.TIMESTAMP_UNIT_TO_MS_MULTIPLIER + "ms - " + maxInitOffset* RtsConfig.TIMESTAMP_UNIT_TO_MS_MULTIPLIER + "ms\r\n";
        outputStr += "# period = " + minPeriod* RtsConfig.TIMESTAMP_UNIT_TO_MS_MULTIPLIER + "ms - " + maxPeriod* RtsConfig.TIMESTAMP_UNIT_TO_MS_MULTIPLIER + "ms\r\n";
        outputStr += "#  - Is tasks generated based on HP upper bound? " + generateFromHpDivisors + "\r\n";
        outputStr += "#  --- If yes, hyper-period upper bound = " + maxHyperPeriod* RtsConfig.TIMESTAMP_UNIT_TO_MS_MULTIPLIER + "ms \r\n";

        return outputStr;
    }
}
