package synercys.rts.framework.event;

import synercys.rts.framework.Interval;
import synercys.rts.framework.Task;

import java.util.ArrayList;

/**
 * Created by CY on 5/26/2015.
 */
public class BusyIntervalEventContainer {
    ArrayList<BusyIntervalEvent> busyIntervals = new ArrayList<>();

    public BusyIntervalEventContainer() {}

    public BusyIntervalEventContainer(EventContainer inEventContainer) {
        createBusyIntervalsFromEvents(inEventContainer);
    }

    public BusyIntervalEventContainer(ArrayList<BusyIntervalEvent> inBusyIntervals) {
        busyIntervals.addAll( inBusyIntervals );
    }

    public void add(BusyIntervalEvent inBi) {
        busyIntervals.add(inBi);
    }

    /**
     * create corresponding busy intervals from a set of scheduler events (with or without idle events)
     * @param inEventContainer schedule events (with or without idle events)
     * @return true if at least a busy interval is created; false otherwise
     */
    public Boolean createBusyIntervalsFromEvents(EventContainer inEventContainer) {
        ArrayList<SchedulerIntervalEvent> schedulerEvents = inEventContainer.getSchedulerEvents();

        // Reset the variable.
        busyIntervals.clear();

        BusyIntervalEvent currentBi = new BusyIntervalEvent();
        Boolean firstEvent = true;
        for (SchedulerIntervalEvent currentEvent : schedulerEvents) {
            if (currentEvent.getTask().getTaskType().equalsIgnoreCase(Task.TASK_TYPE_IDLE)) {
                continue;
            }

            if (firstEvent) {
                firstEvent = false;
                currentBi.addIntervalEvent(currentEvent);
                busyIntervals.add(currentBi);
                continue;
            }

            if (currentEvent.getOrgBeginTimestamp() > currentBi.getOrgEndTimestamp()) {
                currentBi = new BusyIntervalEvent();
                busyIntervals.add(currentBi);
            } else if (currentEvent.getOrgBeginTimestamp() < currentBi.getOrgEndTimestamp()) {
                System.err.println("Schedule events are ordered when creating busy intervals.");
                throw new AssertionError();
            }

            currentBi.addIntervalEvent(currentEvent);
        }

        // return false if no busy interval is created from any scheduler event; return true otherwise
        return !firstEvent;
    }

    /* This is used to convert events from Zedboard log. */
    public Boolean createBusyIntervalsFromIntervalEvents(ArrayList<IntervalEvent> inEvents)
    {
        // Reset the variable.
        busyIntervals.clear();

        for (IntervalEvent thisEvent : inEvents)
        {
            long thisBeginTimeStamp = thisEvent.getOrgBeginTimestamp();
            long thisEndTimeStamp = thisEvent.getOrgEndTimestamp();
            BusyIntervalEvent thisBusyInterval = new BusyIntervalEvent(thisBeginTimeStamp, thisEndTimeStamp);
            busyIntervals.add(thisBusyInterval);
        }
        return true;
    }

    public ArrayList<BusyIntervalEvent> getBusyIntervals()
    {
        return busyIntervals;
    }

    public BusyIntervalEvent findBusyIntervalByTimeStamp(int inTimeStamp)
    {
        for (BusyIntervalEvent thisBusyInterval : busyIntervals)
        {
            if (thisBusyInterval.contains(inTimeStamp) == true)
            {
                return thisBusyInterval;
            }
        }

        // If the program reaches here, that means no interval contains the input time stamp.
        return null;
    }

    public ArrayList<BusyIntervalEvent> findBusyIntervalsBeforeTimeStamp(long inTimeStamp)
    {
        ArrayList<BusyIntervalEvent> resultBis = new ArrayList<>();
        for (BusyIntervalEvent thisBusyInterval : busyIntervals)
        {
            if (thisBusyInterval.getOrgBeginTimestamp() <= inTimeStamp)
            {
                resultBis.add(thisBusyInterval);
            }
        }
        return resultBis;
    }

    public ArrayList<BusyIntervalEvent> findBusyIntervalsBetweenTimeStamp(long inBegin, long inEnd) {
        ArrayList<BusyIntervalEvent> resultBis = new ArrayList<>();
        for (BusyIntervalEvent thisBusyInterval : busyIntervals)
        {
            long thisBegin = thisBusyInterval.getOrgBeginTimestamp();
            long thisEnd = thisBusyInterval.getOrgEndTimestamp();

            Interval thisInterval = new Interval(thisBegin, thisEnd);
            Interval inInterval = new Interval(inBegin, inEnd);

            if (thisInterval.intersect(inInterval) != null) {
                resultBis.add(thisBusyInterval);
            }
        }
        return resultBis;
    }

    /* This function is used by the ScheduLeak experiment. */
    public ArrayList<BusyIntervalEvent> getObservableBusyIntervalsByTask(Task inTask) throws Exception {
        ArrayList<BusyIntervalEvent> observedBis = new ArrayList<>();
        for (BusyIntervalEvent thisBi : busyIntervals) {
            BusyIntervalEvent observedBi = null;
            Boolean isConstructingBi = false;
            for (SchedulerIntervalEvent thisEvent : thisBi.getSchedulerIntervalEvents()) {
                if (thisEvent.getTask() == inTask) {

                    // handling the previous constructing busy interval.
                    if (isConstructingBi == true) {
                        // It should be the end of an observable busy interval.
                        if (thisEvent.getBeginTimeScheduleState() != SchedulerIntervalEvent.SCHEDULE_STATE_RESUME)
                            throw new RuntimeException("Scheduling state inconsistent.");//AssertionError();

                        observedBis.add(observedBi);
                    } else {
                        if (thisEvent.getBeginTimeScheduleState() == SchedulerIntervalEvent.SCHEDULE_STATE_START) {
                            // Check whether we are postponed before we start.
                            long thisStartTime = thisEvent.getOrgBeginTimestamp();
                            long startTimeOffset = (thisStartTime - inTask.getInitialOffset()) % inTask.getPeriod();
                            if (startTimeOffset > 0) {
                                // We are indeed postponed.
                                // Create a busy interval with the postponed interval.
                                observedBis.add(new BusyIntervalEvent(thisStartTime - startTimeOffset, thisStartTime));
                            }
                        }
                    }

                    // Handling the situation after this schedule interval
                    if (thisEvent.getEndTimeScheduleState() == SchedulerIntervalEvent.SCHEDULE_STATE_SUSPEND) {
                        // We are preempted again.
                        observedBi = new BusyIntervalEvent();
                        isConstructingBi = true;
                    } else {
                        // The end of the observer task.
                        if (thisEvent.getEndTimeScheduleState() != SchedulerIntervalEvent.SCHEDULE_STATE_END)
                            throw new AssertionError();

                        isConstructingBi = false;
                    }

                } else if (isConstructingBi == true) {
                    observedBi.addIntervalEvent(thisEvent);
                }
            }
            if (isConstructingBi == true) {
                throw new AssertionError();
            }
        }

        return observedBis;
    }

    public long getEndTime()
    {
        long endTime = 0;
        for (BusyIntervalEvent thisBusyInterval : busyIntervals) {
            if (thisBusyInterval.getOrgEndTimestamp() > endTime) {
                endTime = thisBusyInterval.getOrgEndTimestamp();
            }
        }
        return endTime;
    }

    public long getBeginTime()
    {
        long beginTime = 0;
        Boolean firstLoop = true;
        for (BusyIntervalEvent thisBusyInterval : busyIntervals) {
            if (firstLoop == true) {
                beginTime = thisBusyInterval.getOrgBeginTimestamp();
                firstLoop = false;
            }

            beginTime = thisBusyInterval.getOrgBeginTimestamp() < beginTime ? thisBusyInterval.getOrgBeginTimestamp() : beginTime;
        }
        return beginTime;
    }

    public void removeBusyIntervalsBeforeTimeStamp(int inTimeStamp) {
        ArrayList<BusyIntervalEvent> biBeforeTimeStamp;
        biBeforeTimeStamp = findBusyIntervalsBeforeTimeStamp(inTimeStamp);
        for (BusyIntervalEvent thisBi : biBeforeTimeStamp) {
            busyIntervals.remove(thisBi);
        }
    }

    public void removeBusyIntervalsBeforeButExcludeTimeStamp(long inTimeStamp) {
        ArrayList<BusyIntervalEvent> biBeforeTimeStamp;
        biBeforeTimeStamp = findBusyIntervalsBeforeTimeStamp(inTimeStamp);
        for (BusyIntervalEvent thisBi : biBeforeTimeStamp) {
            if (thisBi.contains(inTimeStamp)) {
                continue;
            }
            busyIntervals.remove(thisBi);
        }
    }

    public void removeTheLastBusyInterval() {
        long lastBeginTime = 0;
        BusyIntervalEvent lastBi = null;
        for (BusyIntervalEvent thisBi : busyIntervals) {
            if (lastBeginTime < thisBi.getOrgBeginTimestamp()) {
                lastBeginTime = thisBi.getOrgBeginTimestamp();
                lastBi = thisBi;
            }
        }
        busyIntervals.remove(lastBi);
    }

    public int size() {
        return busyIntervals.size();
    }

    public String toBinaryString() {
        String outStr = "";
        long lastTimestamp = 0;
        boolean firstPass = true;
        for (BusyIntervalEvent thisBi : busyIntervals) {
            if (firstPass) {
                firstPass = false;
                lastTimestamp = thisBi.orgBeginTimestamp;
            }

            for (long i=lastTimestamp; i<thisBi.getOrgBeginTimestamp(); i++) {
                outStr += "0, ";
            }

            outStr += thisBi.toBinaryString() + ", ";
            lastTimestamp = thisBi.orgEndTimestamp;
        }

        if (outStr.length() != 0) {
            outStr = outStr.substring(0, outStr.length()-2);
        }
        return outStr;
    }

    public double[] toBinaryDouble() {
        ArrayList<Double> binarySchedule = new ArrayList<>();

        long lastTimestamp = 0;
        boolean firstPass = true;
        for (BusyIntervalEvent thisBi : busyIntervals) {
            if (firstPass) {
                firstPass = false;
                lastTimestamp = thisBi.orgBeginTimestamp;
            }

            for (long i=lastTimestamp; i<thisBi.getOrgBeginTimestamp(); i++) {
                binarySchedule.add(0.0);
            }

            for (long i=thisBi.orgBeginTimestamp; i<thisBi.getOrgEndTimestamp(); i++) {
                binarySchedule.add(1.0);
            }

            lastTimestamp = thisBi.orgEndTimestamp;
        }

        // convert the resulting ArrayList<Double> to primitive double[]
        double[] returnArray = new double[binarySchedule.size()];
        for (int i=0; i<binarySchedule.size(); i++) {
            returnArray[i] = binarySchedule.get(i).doubleValue();
        }

        return returnArray;
    }
}