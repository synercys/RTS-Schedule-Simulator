package synercys.rts.scheduler.entropy;

import synercys.rts.framework.Task;
import synercys.rts.framework.TaskSet;
import synercys.rts.framework.event.EventContainer;
import synercys.rts.framework.event.SchedulerIntervalEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpperApproximateEntropyCalculator implements ScheduleEntropyCalculatorInterface {
    static public String name = EntropyCalculatorUtility.ENTROPY_UPPER_APPROXIMATE;
    TaskSet taskSet;
    List<Map<Task, Integer>> slotTaskOccurrences = new ArrayList<>();
    List<Double> slotEntropy = new ArrayList<>();
    double finalUASEntropy = 0.0;
    long beginTimestamp;
    long length;
    int totalScheduleCount = 0;
    boolean meanSlotEnabled = false;

    public UpperApproximateEntropyCalculator(TaskSet taskSet, long beginTimestamp, long length) {
        this.taskSet = taskSet;
        this.beginTimestamp = beginTimestamp;
        this.length = length;
        for (int i=0; i<length; i++) {
            Map<Task, Integer> thisSlot = new HashMap<>();
            for (Task task : taskSet.getAppTasksAsArray()) {
                thisSlot.put(task, 0);
            }
            slotTaskOccurrences.add(thisSlot);
            slotEntropy.add(0.0);
        }
    }

    public UpperApproximateEntropyCalculator(TaskSet taskSet, long beginTimestamp, long length, boolean meanSlotEnabled) {
        this(taskSet, beginTimestamp, length);
        this.meanSlotEnabled = meanSlotEnabled;
    }

    @Override
    public void applyOneSchedule(EventContainer schedule) {
        for (SchedulerIntervalEvent ei : schedule.getSchedulerEvents()) {
            if (ei.getTask().isIdleTaskType())
                continue;

            for (long i=ei.getOrgBeginTimestamp()-beginTimestamp; i<(ei.getOrgEndTimestamp()-beginTimestamp); i++) {
                Map<Task, Integer> thisSlot = slotTaskOccurrences.get((int)i);
                int occurrence = thisSlot.get(ei.getTask());
                thisSlot.put(ei.getTask(), occurrence+1);
            }
        }
        totalScheduleCount++;
    }

    @Override
    public double concludeEntropy() {
        finalUASEntropy = 0;
        for (int i=0; i<length; i++) {
            double thisSlotEntropy = computeSlotEntropy(slotTaskOccurrences.get(i), totalScheduleCount);
            slotEntropy.set(i, thisSlotEntropy);
            finalUASEntropy += thisSlotEntropy;
        }

        if (meanSlotEnabled) {
            finalUASEntropy /= length;
        }

        return finalUASEntropy;
    }

    protected double computeSlotEntropy(Map<Task, Integer> taskOccurrenceMap, int totalOccurrence) {
        double slotEntropy = 0.0;
        int accumulativeOccurrence = 0;
        /* Compute entropy for each task in this slot */
        for (Task task : taskSet.getAppTasksAsArray()) {
            int taskOccurrence = taskOccurrenceMap.get(task);
            accumulativeOccurrence += taskOccurrence;
            double taskOccurrenceProbability = taskOccurrence/(double)totalOccurrence;
            if (taskOccurrenceProbability != 0)
                slotEntropy += (taskOccurrenceProbability * log2(taskOccurrenceProbability));
        }

        /* Now let's compute the idle time entropy */
        int idleTimeOccurrence = totalOccurrence - accumulativeOccurrence;
        double idleOccurrenceProbability = idleTimeOccurrence/(double)totalOccurrence;
        if (idleOccurrenceProbability != 0)
            slotEntropy += idleOccurrenceProbability * log2(idleOccurrenceProbability);
        return -slotEntropy;
    }

    public static double log2(double x)
    {
        return Math.log(x) / Math.log(2);
    }

}
