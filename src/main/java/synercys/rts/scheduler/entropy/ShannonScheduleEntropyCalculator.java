package synercys.rts.scheduler.entropy;

import synercys.rts.framework.event.EventContainer;

import java.util.HashMap;
import java.util.Map;

public class ShannonScheduleEntropyCalculator implements ScheduleEntropyCalculatorInterface {
    public static String name = "Shannon";
    Map<String, Integer> scheduleOccurrenceMap = new HashMap<>();
    int totalScheduleCount = 0;
    long beginTimestamp;
    long length;

    public ShannonScheduleEntropyCalculator(long beginTimestamp, long length) {
        this.beginTimestamp = beginTimestamp;
        this.length = length;
    }

    @Override
    public void applyOneSchedule(EventContainer schedule) {
        String rawScheduleString = schedule.toRawScheduleString(beginTimestamp, length+beginTimestamp);
        if (!scheduleOccurrenceMap.containsKey(rawScheduleString)) {
            scheduleOccurrenceMap.put(rawScheduleString, 0);
        }
        int thisScheduleOccurrence = scheduleOccurrenceMap.get(rawScheduleString);
        scheduleOccurrenceMap.put(rawScheduleString, thisScheduleOccurrence+1);
        totalScheduleCount++;
    }

    @Override
    public double concludeEntropy() {
        double scheduleEntropy = 0;
        for (Integer scheduleOccurrence : scheduleOccurrenceMap.values()) {
            double scheduleOccurrenceProbability = scheduleOccurrence/(double)totalScheduleCount;
            if (scheduleOccurrenceProbability != 0)
                scheduleEntropy += (scheduleOccurrenceProbability * log2(scheduleOccurrenceProbability));
        }
        return -scheduleEntropy;
    }

    public static double log2(double x)
    {
        return Math.log(x) / Math.log(2);
    }
}
