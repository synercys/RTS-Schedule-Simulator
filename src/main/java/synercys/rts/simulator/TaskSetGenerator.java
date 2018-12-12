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

    int minNumTasks;
    int maxNumTasks;

    long minPeriod;
    long maxPeriod;

    long maxHyperPeriod;

    Boolean generateFromHpDivisors;

    long minWcet;
    long maxWcet;

    long minInitOffset;
    long maxInitOffset;

	/* Assume Period = Deadline */

    double minUtil;
    double maxUtil;

    int numTaskPerSet;
    int numTaskSet;

    Boolean nonHarmonicOnly;

    /*==== Dedicated to the research for schedule-based side-channel =====*/
    Boolean needGenObserverTask;
    double maxObservationRatio;
    double minObservationRatio;
    int observerTaskPriority;
    int victimTaskPriority;
    /*===== end =====*/

    Random rand = new Random();

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
        numTaskSet = 1;

        generateFromHpDivisors = false;

        nonHarmonicOnly = false;

        /*==== Dedicated to the research for schedule-based side-channel =====*/
        needGenObserverTask = false;
        maxObservationRatio = 999;
        minObservationRatio = 1.0;
        observerTaskPriority = 1;
        victimTaskPriority = 2;
        /*===== end =====*/
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
                thisTaskContainer.setId(i);
                resultTaskSetContainer.addTaskSet(thisTaskContainer);
            }
        }

        return resultTaskSetContainer;
    }

    public int getRandom(int min, int max) {
        return rand.nextInt(max - min + 1) + min;
    }

    /* The configurations are passed by global variables. */
    private TaskSet gen()
    {
        int failureCount = 0;

        TaskSet taskContainer = new TaskSet();

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
            task.setWcet(tempComputationTime);

            last_total_util = total_util;
            total_util += ( task.getWcet() / (double)(task.getPeriod()));

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


        /*==== Dedicated to the research for schedule-based side-channel =====*/
        double observationRatio = 0;
        if (needGenObserverTask == true) {
            Task victim, observer;
            victim = taskContainer.getOneTaskByPriority(victimTaskPriority);
            observer = taskContainer.getOneTaskByPriority(observerTaskPriority);
            double gcd = Umath.gcd(victim.getPeriod(), observer.getPeriod());
            observationRatio = observer.getWcet()/gcd;
            if ((observationRatio<minObservationRatio) || (observationRatio>maxObservationRatio)) {
                return null;
            }
        }
        /*===== end =====*/

        taskContainer.addIdleTask();
        return taskContainer;
    }

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
        this.minNumTasks = minNumTasks;
    }

    public int getMaxNumTasks() {
        return maxNumTasks;
    }

    public void setMaxNumTasks(int maxNumTasks) {
        this.maxNumTasks = maxNumTasks;
    }

    public long getMinPeriod() {
        return minPeriod;
    }

    public void setMinPeriod(long minPeriod) {
        this.minPeriod = minPeriod;
    }

    public long getMaxPeriod() {
        return maxPeriod;
    }

    public void setMaxPeriod(long maxPeriod) {
        this.maxPeriod = maxPeriod;
    }

    public long getMaxHyperPeriod() {
        return maxHyperPeriod;
    }

    public void setMaxHyperPeriod(long maxHyperPeriod) {
        this.maxHyperPeriod = maxHyperPeriod;
    }

    public Boolean getGenerateFromHpDivisors() {
        return generateFromHpDivisors;
    }

    public void setGenerateFromHpDivisors(Boolean generateFromHpDivisors) {
        this.generateFromHpDivisors = generateFromHpDivisors;
    }

    public long getMinWcet() {
        return minWcet;
    }

    public void setMinWcet(long minWcet) {
        this.minWcet = minWcet;
    }

    public long getMaxWcet() {
        return maxWcet;
    }

    public void setMaxWcet(long maxWcet) {
        this.maxWcet = maxWcet;
    }

    public long getMinInitOffset() {
        return minInitOffset;
    }

    public void setMinInitOffset(long minInitOffset) {
        this.minInitOffset = minInitOffset;
    }

    public long getMaxInitOffset() {
        return maxInitOffset;
    }

    public void setMaxInitOffset(long maxInitOffset) {
        this.maxInitOffset = maxInitOffset;
    }

    public double getMinUtil() {
        return minUtil;
    }

    public void setMinUtil(double minUtil) {
        this.minUtil = minUtil;
    }

    public double getMaxUtil() {
        return maxUtil;
    }

    public void setMaxUtil(double maxUtil) {
        this.maxUtil = maxUtil;
    }

    public int getNumTaskPerSet() {
        return numTaskPerSet;
    }

    public void setNumTaskPerSet(int numTaskPerSet) {
        this.numTaskPerSet = numTaskPerSet;
    }

    public int getNumTaskSet() {
        return numTaskSet;
    }

    public void setNumTaskSet(int numTaskSet) {
        this.numTaskSet = numTaskSet;
    }

    public Boolean getNonHarmonicOnly() {
        return nonHarmonicOnly;
    }

    public void setNonHarmonicOnly(Boolean nonHarmonicOnly) {
        this.nonHarmonicOnly = nonHarmonicOnly;
    }

    public Boolean getNeedGenObserverTask() {
        return needGenObserverTask;
    }

    public void setNeedGenObserverTask(Boolean needGenObserverTask) {
        this.needGenObserverTask = needGenObserverTask;
    }

    public double getMaxObservationRatio() {
        return maxObservationRatio;
    }

    public void setMaxObservationRatio(double maxObservationRatio) {
        this.maxObservationRatio = maxObservationRatio;
    }

    public double getMinObservationRatio() {
        return minObservationRatio;
    }

    public void setMinObservationRatio(double minObservationRatio) {
        this.minObservationRatio = minObservationRatio;
    }

    public int getObserverTaskPriority() {
        return observerTaskPriority;
    }

    public void setObserverTaskPriority(int observerTaskPriority) {
        this.observerTaskPriority = observerTaskPriority;
    }

    public int getVictimTaskPriority() {
        return victimTaskPriority;
    }

    public void setVictimTaskPriority(int victimTaskPriority) {
        this.victimTaskPriority = victimTaskPriority;
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
