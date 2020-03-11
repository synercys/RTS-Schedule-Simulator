package synercys.rts.scheduler;

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

    int taskSetIdCounter;

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
    Boolean distinctPeriodOnly;
    Boolean rmSchedulabilityTest;

    /*==== Dedicated to the research for schedule-based side-channel =====*/
    Boolean needGenObserverTask;
    Boolean edfScheduleakObservationRatio;  // When enabled, observer/victim priorities are ignored.
    Boolean onlyScheduLeakFailsInEDF;           // Generate task sets with C_o > T_o - T_v only
    double maxObservationRatio;
    double minObservationRatio;
    int observerTaskPriority;
    int victimTaskPriority;
    boolean needGenHarmonicObserverTask;    // If it's true, at least one task having harmonic period with the observer will be generated.
    /*===== end =====*/

    Random rand = new Random();

    public TaskSetGenerator() {

        taskSetIdCounter = 0;

        maxHyperPeriod = (1001_000_000/ RtsConfig.TIMESTAMP_UNIT_NS)*3; // 3 sec

        /* When maxHyperPeriod is specified, only minPeriod will be checked while the check for maxPeriod will be skipped. */
        maxPeriod = 100_000_000/ RtsConfig.TIMESTAMP_UNIT_NS; // 100 ms
        minPeriod = 10_000_000/ RtsConfig.TIMESTAMP_UNIT_NS;   // 10 ms // org = 5ms

        maxWcet = 50_000_000/ RtsConfig.TIMESTAMP_UNIT_NS; // 50 ms // org=3 ms
        minWcet = 100_000/ RtsConfig.TIMESTAMP_UNIT_NS; // 0.1 ms

        maxInitOffset = maxHyperPeriod; // 0ms //10_000_000; // 10 ms
        minInitOffset = 0; // 0 ms

        maxUtil = 0.9;
        minUtil = 0.1;

        numTaskPerSet = 5;
        numTaskSet = 1;

        generateFromHpDivisors = false;

        nonHarmonicOnly = false;

        distinctPeriodOnly = true;

        rmSchedulabilityTest = true;

        /*==== Dedicated to the research for schedule-based side-channel =====*/
        needGenObserverTask = false;
        needGenHarmonicObserverTask = false;
        edfScheduleakObservationRatio = false;
        onlyScheduLeakFailsInEDF = false;
        maxObservationRatio = 999;
        minObservationRatio = 1.0;
        observerTaskPriority = -1;  // -1 indicates that it's not being set
        victimTaskPriority = -1;    // -1 indicates that it's not being set
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
                thisTaskContainer.setId(taskSetIdCounter);
                taskSetIdCounter++;
                resultTaskSetContainer.addTaskSet(thisTaskContainer);
            }
        }

        return resultTaskSetContainer;
    }

    /**
     * generate a random integer between inclusiveMin and inclusiveMax, both bounds are inclusive.
     * @param inclusiveMin  the smallest possible number
     * @param inclusiveMax  the largest possible number
     * @return an integer between inclusiveMin and inclusiveMax, both bounds are inclusive
     */
    public int getRandom(int inclusiveMin, int inclusiveMax) {
        // nextInt generates a number between 0 (inclusive) and the given number (exclusive).
        return rand.nextInt(inclusiveMax - inclusiveMin + 1) + inclusiveMin;
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

        double randomUtil;
        do {
            randomUtil = getRandom((int) (minUtil * 100), (int) (maxUtil * 100)) / 100.0;
        } while (randomUtil == 0);
        ArrayList<Double> utilDistribution = getRandomUtilDistributionByUUniFast(numTasks, randomUtil);

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

            if (distinctPeriodOnly) {
                boolean invalidTask = false;
                for (Task addedTask : taskContainer.getTasksAsArray()) {
                    if (addedTask.getPeriod() == task.getPeriod()) {
                        invalidTask = true;
                        break;
                    }
                }
                if (invalidTask) {
                    i--;
                    continue;
                }
            }

            long tempComputationTime = (long)(((double)task.getPeriod())*utilDistribution.get(i));
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

        if (generateFromHpDivisors) {
            if (taskContainer.calHyperPeriod() != maxHyperPeriod)
                return null;
        }

        taskContainer.assignPriorityRm();

        if (rmSchedulabilityTest) {
            if (taskContainer.schedulabilityTest() == false)
                return null;
        }


        /*==== Dedicated to the research for scheduler-based side-channel =====*/
        if (needGenObserverTask == true) {
            Task victim, observer;

            if (observerTaskPriority==-1 || victimTaskPriority==-1) {
                int observerVictimTaskPriorities[] = computeDefaultObserverAndVictimTaskPriorities(numTasks);
                observerTaskPriority = observerVictimTaskPriorities[0];
                victimTaskPriority = observerVictimTaskPriorities[1];
            }

            observer = taskContainer.getOneTaskByPriority(observerTaskPriority);
            //victim = taskContainer.getOneTaskByPriority(victimTaskPriority); // Victim will be assigned later

            /* Need at least a harmonic task with the observer? */
            // We say at least one because there might be other harmonic tasks existed but we don't know.
            if (needGenHarmonicObserverTask == true) {

                /* Get and check the observer's period's factors first. */
                ArrayList<Long> observerPeriodFactors = Umath.integerFactorization(observer.getPeriod());
                if (observerPeriodFactors.size() == 1) {
                    // The observer's period is a prime number.
                    // We'll skip this task set as no smaller harmonic period can be generated.
                    return null;
                }

                int genHarmonicObserverTaskFailureCount = 0;
                while (true) {

                    if (genHarmonicObserverTaskFailureCount > 10) {
                        // This might be too hard to generate one that fulfills the task generation requirements, so we quit.
                        return null;
                    }

                    // Randomly pick one task that has period smaller than the observer.
                    int chosenHarmonicTaskPriority = getRandom(observerTaskPriority + 1, numTasks);
                    Task harmonicTask = taskContainer.getOneTaskByPriority(chosenHarmonicTaskPriority);

                    // period
                    long newHarmonicPeriod = getRandomDivisor(observerPeriodFactors);

                    if (newHarmonicPeriod<minPeriod || newHarmonicPeriod>maxPeriod) {
                        genHarmonicObserverTaskFailureCount++;
                        continue;
                    }

                    if (distinctPeriodOnly) {
                        boolean invalidHarmonicTask = false;
                        for (Task thisTask : taskContainer.getTasksAsArray()) {
                            if (thisTask == harmonicTask)
                                continue;

                            if (thisTask.getPeriod() == newHarmonicPeriod) {
                                invalidHarmonicTask = true;
                                break;
                            }
                        }
                        if (invalidHarmonicTask == true) {
                            genHarmonicObserverTaskFailureCount++;
                            continue;
                        }
                    }

                    // Wcet
                    double thisTaskUtil = (double) harmonicTask.getWcet() / harmonicTask.getPeriod();
                    long newWcet = (long) ((double) newHarmonicPeriod * thisTaskUtil);
                    if (newWcet < minWcet || newWcet > maxWcet) {
                        genHarmonicObserverTaskFailureCount++;
                        continue;
                    }

                    // task phase
                    long newTaskPhase = getRandom((int) minInitOffset, (int) Math.min(newHarmonicPeriod, maxInitOffset));

                    harmonicTask.setPeriod(newHarmonicPeriod);
                    harmonicTask.setDeadline(newHarmonicPeriod);
                    harmonicTask.setWcet(newWcet);
                    harmonicTask.setInitialOffset(newTaskPhase);

                    /* Reassign task priorities and do schedulability test. */
                    taskContainer.assignPriorityRm();

                    if (rmSchedulabilityTest) {
                        if (taskContainer.schedulabilityTest() == false)
                            return null;
                    }

                    break;
                }

            }

            victim = taskContainer.getOneTaskByPriority(victimTaskPriority);


            long po = observer.getPeriod();
            long pv = victim.getPeriod();
            double gcd = Umath.gcd(pv, po);
            if (edfScheduleakObservationRatio) {
                /* Enforce coverage ratio for EDF */
                double observationRatio = (double) Math.min((po - pv), observer.getWcet()) / gcd;
                if ((observationRatio < minObservationRatio) || (observationRatio > maxObservationRatio)) {
                    return null;
                }
            } else {
                /* Enforce coverage ratio for RM */
                double observationRatio = observer.getWcet() / gcd;
                if ((observationRatio < minObservationRatio) || (observationRatio > maxObservationRatio)) {
                    return null;
                }
            }

            if (onlyScheduLeakFailsInEDF) {
                // See the DyPS paper for details.
                if (!(observer.getWcet() > (po - pv))) {
                    return null;
                }
            }

        }
        /*===== end of needGenObserverTask =====*/

        reassignedTaskIdsBasedOnPriorities(taskContainer);

        taskContainer.addIdleTask();
        return taskContainer;
    }


    /**
     * Compute the priorities of the observer and the victim tasks in a task set with numTasks tasks.
     * @param numTasks the number of tasks in a task set to be considered.
     * @return an int array storing the priorities for the observer (the first element) and victim (the second element).
     */
    static public int[] computeDefaultObserverAndVictimTaskPriorities(int numTasks) {
        int result[] = new int[2];
        /*
         * Task priority starts from 1. A small number represents a low priority.
         * observerTaskPriority = floor(numOfTasks/3) + 1
         * victimTaskPriority = numOfTasks - floor(numOfTasks/3)
         * */
        switch (numTasks) {
            case 3:
                result[0] = 1; //observerTaskPriority = 1;
                result[1] = 3; //victimTaskPriority = 3;
                break;
            default:
                result[0] = (int) Math.floor((double) numTasks / 3.0) + 1; //observerTaskPriority = (int) Math.floor((double) numTasks / 3.0) + 1;
                result[1] = numTasks - (int) Math.floor((double) numTasks / 3); //victimTaskPriority = numTasks - (int) Math.floor((double) numTasks / 3);
                break;
            //case 5: observerTaskPriority = 2; victimTaskPriority = 4; break;
            //case 7: observerTaskPriority = 3; victimTaskPriority = 5; break;
            //case 9: observerTaskPriority = 4; victimTaskPriority = 6; break;
            //case 11: observerTaskPriority = 4; victimTaskPriority = 8; break;
        }
        return result;
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


    ArrayList<Double> getRandomUtilDistributionByUUniFast(int inMaxTaskNum, double inMaxUtil) {
        ArrayList<Double> resultUtilArray = new ArrayList<>();
        double sum = inMaxUtil;
        double nextSum;
        for (int i=1; i<=inMaxTaskNum-1; i++)
        {
            double thisTaskUtil;
            do {
                nextSum = sum * Math.pow(Math.random(), 1.0/(inMaxTaskNum-i));
                thisTaskUtil = sum - nextSum;
            } while (thisTaskUtil==0.0 || nextSum==0.0);
            resultUtilArray.add(thisTaskUtil);
            sum = nextSum;
        }
        resultUtilArray.add(sum);

        return  resultUtilArray;
    }

    public void reassignedTaskIdsBasedOnPriorities(TaskSet taskSet) {
        int maxPriority = taskSet.getHighestPriorityTask().getPriority();
        int minPriority = taskSet.getLowestPriorityTask().getPriority();
        int idCounter = 1;
        for (int priority=minPriority; priority<=maxPriority; priority++) {
            ArrayList<Task> thisPriorityTasks = taskSet.getTasksByPriority(priority);
            for (Task thisTask : thisPriorityTasks) {
                thisTask.setId(idCounter);
                idCounter++;
            }
        }
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

    public Boolean getEdfScheduleakObservationRatio() {
        return edfScheduleakObservationRatio;
    }

    public void setEdfScheduleakObservationRatio(Boolean edfScheduleakObservationRatio) {
        this.edfScheduleakObservationRatio = edfScheduleakObservationRatio;
    }

    public Boolean getDistinctPeriodOnly() {
        return distinctPeriodOnly;
    }

    public void setDistinctPeriodOnly(Boolean distinctPeriodOnly) {
        this.distinctPeriodOnly = distinctPeriodOnly;
    }

    public boolean isNeedGenHarmonicObserverTask() {
        return needGenHarmonicObserverTask;
    }

    public void setNeedGenHarmonicObserverTask(boolean needGenHarmonicObserverTask) {
        this.needGenHarmonicObserverTask = needGenHarmonicObserverTask;
    }

    public Boolean getOnlyScheduLeakFailsInEDF() {
        return onlyScheduLeakFailsInEDF;
    }

    public void setOnlyScheduLeakFailsInEDF(Boolean onlyScheduLeakFailsInEDF) {
        this.onlyScheduLeakFailsInEDF = onlyScheduLeakFailsInEDF;
    }

    public Boolean getRmSchedulabilityTest() {
        return rmSchedulabilityTest;
    }

    public void setRmSchedulabilityTest(Boolean rmSchedulabilityTest) {
        this.rmSchedulabilityTest = rmSchedulabilityTest;
    }

    public int getTaskSetIdCounter() {
        return taskSetIdCounter;
    }

    public void setTaskSetIdCounter(int taskSetIdCounter) {
        this.taskSetIdCounter = taskSetIdCounter;
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
