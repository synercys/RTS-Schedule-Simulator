package synercys.rts.framework.event;

import synercys.rts.framework.Task;
import synercys.rts.framework.TaskSet;

import java.util.ArrayList;

/**
 * Created by jjs on 2/13/17.
 */
public class EventContainer {
    public static String SCHEDULING_POLICY_UNKNOWN = "unknown";
    public static String SCHEDULING_POLICY_FIXED_PRIORITY = "fixedPriority";
    public static String SCHEDULING_POLICY_TASKSHUFFLER = "taskShuffler";   // Randomization protocol for FP
    public static String SCHEDULING_POLICY_EDF = "edf";
    public static String SCHEDULING_POLICY_REORDER = "reorder"; // Randomization protocol for EDF

    public static final int SCHEDULER_EVENT = 0;
    public static final int INSTANT_EVENT = 1;

    private ArrayList<SchedulerIntervalEvent> schedulerEvents = new ArrayList<SchedulerIntervalEvent>();
    private ArrayList<TaskInstantEvent> taskInstantEvents = new ArrayList<TaskInstantEvent>();

    private TaskSet taskSet = new TaskSet();

    private String schedulingPolicy = SCHEDULING_POLICY_UNKNOWN;   // It is optional and does not affect any data in this class.

    public EventContainer(){}

    public void addNextEvent(int inEventType, long inTimestamp, int inEventTaskId, int inData, String inEventString)
    {
        if (inEventType == SCHEDULER_EVENT)
        {// inEventTaskId is 0 as from scheduler, inData is the Id of the task being scheduled.
            if (schedulerEvents.size() > 0) {
                schedulerEvents.get(schedulerEvents.size() - 1).setOrgEndTimestamp(inTimestamp);
            }
            schedulerEvents.add(new SchedulerIntervalEvent(inTimestamp, taskSet.getTaskById(inData), inEventString));

        } else if (inEventType == INSTANT_EVENT)
        {
            taskInstantEvents.add(new TaskInstantEvent(inTimestamp, taskSet.getTaskById(inEventTaskId), inData, inEventString));
        }
    }

    public void add(SchedulerIntervalEvent inSchedulerIntervalEvent) {
        schedulerEvents.add(inSchedulerIntervalEvent);
    }

    public void add (TaskInstantEvent inTaskInstantEvent) {
        taskInstantEvents.add(inTaskInstantEvent);
    }

    public void clearAll()
    {
        schedulerEvents.clear();
        taskInstantEvents.clear();
        taskSet.clear();
    }

    public void setTaskSet(TaskSet inTaskSet)
    {
        taskSet = inTaskSet;
    }

    public TaskSet getTaskSet()
    {
        return taskSet;
    }

    public ArrayList<SchedulerIntervalEvent> getSchedulerEvents() { return schedulerEvents; }
    public ArrayList<TaskInstantEvent> getTaskInstantEvents() { return taskInstantEvents; }

    public ArrayList<SchedulerIntervalEvent> getSchedulerEventsOfATask(Task inTask)
    {
        ArrayList resultArrayList = new ArrayList();
        for (SchedulerIntervalEvent thisEvent : schedulerEvents)
        {
            if (thisEvent.getTask() == inTask)
                resultArrayList.add(thisEvent);
        }
        return resultArrayList;
    }

    public ArrayList<TaskInstantEvent> getInstantEventsOfATask(Task inTask)
    {
        ArrayList<TaskInstantEvent> resultArrayList = new ArrayList();
        for (TaskInstantEvent thisEvent : taskInstantEvents)
        {
            if (thisEvent.getTask() == inTask)
            {
                resultArrayList.add(thisEvent);
            }
        }
        return resultArrayList;
    }

    public ArrayList getAllEvents()
    {
        ArrayList resultArrayList = new ArrayList();
        resultArrayList.addAll(schedulerEvents);
        resultArrayList.addAll(taskInstantEvents);
        return resultArrayList;
    }

    public ArrayList getAppAndSchedulerEvents()
    {
        ArrayList resultArrayList = new ArrayList();
        resultArrayList.addAll(schedulerEvents);
        resultArrayList.addAll(taskInstantEvents);
        return resultArrayList;
    }

    // This method returns the first matched event.
    public SchedulerIntervalEvent findSchedulerEventByTime(long inTimeStamp)
    {
        for (SchedulerIntervalEvent thisEvent : schedulerEvents)
        {
            if (thisEvent.contains(inTimeStamp))
                return thisEvent;
        }

        // If no event contains the designated time stamp, then return null.
        return null;
    }

    public ArrayList<SchedulerIntervalEvent> findSchedulerEventsByTimeWindow(int inBeginTimeStamp, int inEndTimeStamp)
    {
        ArrayList resultArrayList = new ArrayList();
        for (SchedulerIntervalEvent thisEvent : schedulerEvents)
        {
            if (isValueWithinRange(thisEvent.getOrgBeginTimestamp(), inBeginTimeStamp, inEndTimeStamp) ||
                    isValueWithinRange(thisEvent.getOrgEndTimestamp(), inBeginTimeStamp, inEndTimeStamp))
            {
                resultArrayList.add(thisEvent);
            }
        }
        return resultArrayList;
    }

    public Boolean isValueWithinRange(long inTargetValue, long inBegin, long inEnd)
    {
        return (inTargetValue>=inBegin && inTargetValue<=inEnd) ? true : false;
    }

    public void removeEventsBeforeButExcludeTimeStamp(long inTimeStamp) {
        ArrayList<SchedulerIntervalEvent> schedulerIntervalEventsToBeRemoved = new ArrayList<>();
        for (SchedulerIntervalEvent thisSchedulerEvent : schedulerEvents) {
            if (thisSchedulerEvent.getOrgEndTimestamp() < inTimeStamp) {
                schedulerIntervalEventsToBeRemoved.add(thisSchedulerEvent);
            }
        }
        schedulerEvents.removeAll(schedulerIntervalEventsToBeRemoved);

        ArrayList<TaskInstantEvent> taskInstantEventsToBeRemoved = new ArrayList<>();
        for (TaskInstantEvent thisInstantEvent : taskInstantEvents) {
            if (thisInstantEvent.getOrgTimestamp() < inTimeStamp) {
                taskInstantEventsToBeRemoved.add(thisInstantEvent);
            }
        }
        taskInstantEvents.removeAll(taskInstantEventsToBeRemoved);
    }

    public void removeSchedulerIntervalEventsAfterButExcludeTimeStamp(long inTimeStamp) {
        ArrayList<SchedulerIntervalEvent> schedulerIntervalEventsToBeRemoved = new ArrayList<>();
        for (SchedulerIntervalEvent thisSchedulerEvent : schedulerEvents) {
            if (thisSchedulerEvent.getOrgBeginTimestamp() > inTimeStamp) {
                schedulerIntervalEventsToBeRemoved.add(thisSchedulerEvent);
            }
        }
        schedulerEvents.removeAll(schedulerIntervalEventsToBeRemoved);
    }

    public void removeTaskInstantEventsAfterAndIncludeTimeStamp(long inTimeStamp) {
        ArrayList<TaskInstantEvent> taskInstantEventsToBeRemoved = new ArrayList<>();
        for (TaskInstantEvent thisInstantEvent : taskInstantEvents) {
            if (thisInstantEvent.getOrgTimestamp() >= inTimeStamp) {
                taskInstantEventsToBeRemoved.add(thisInstantEvent);
            }
        }
        taskInstantEvents.removeAll(taskInstantEventsToBeRemoved);
    }

    public void trimEventsToTimeStamp(long timeLimit) {
        /* Scheduler interval events */
        removeSchedulerIntervalEventsAfterButExcludeTimeStamp(timeLimit);
        if (schedulerEvents.size() > 0) {
            SchedulerIntervalEvent lastInterval = schedulerEvents.get(schedulerEvents.size() - 1);
            if (lastInterval.getOrgBeginTimestamp() == timeLimit) {
                // This means the last interval is [timeLimit, timeLimit) which corresponds to an empty interval,
                // so it doesn't make sense to keep it.
                schedulerEvents.remove(lastInterval);
            } else if (lastInterval.getOrgEndTimestamp() > timeLimit) {
                lastInterval.setOrgEndTimestamp(timeLimit);
            }
        }

        /* Task instant events */
        removeTaskInstantEventsAfterAndIncludeTimeStamp(timeLimit);
    }

    public String toRawScheduleString() {
        String outStr = "";
        long lastTimestamp = 0;
        boolean firstPass = true;
        for (SchedulerIntervalEvent thisEvent : schedulerEvents) {
            if (firstPass) {
                firstPass = false;
                lastTimestamp = thisEvent.orgBeginTimestamp;
            }

            for (long i=lastTimestamp; i<thisEvent.getOrgBeginTimestamp(); i++) {
                outStr += "0, ";
            }

            outStr += thisEvent.toRawScheduleString() + ", ";
            lastTimestamp = thisEvent.orgEndTimestamp;
        }

        if (outStr.length() != 0) {
            outStr = outStr.substring(0, outStr.length()-2);
        }
        return outStr;
    }

    public long getEndTimeStamp() {
        long endTimestamp = 0;
        for (SchedulerIntervalEvent event: schedulerEvents) {
            endTimestamp = (event.getOrgEndTimestamp() > endTimestamp) ? event.getOrgEndTimestamp() : endTimestamp;
        }
        for (TaskInstantEvent event: taskInstantEvents) {
            endTimestamp = (event.getOrgTimestamp() > endTimestamp) ? event.getOrgTimestamp() : endTimestamp;
        }
        return endTimestamp;
    }

    public String getSchedulingPolicy() {
        return schedulingPolicy;
    }

    public void setSchedulingPolicy(String schedulingPolicy) {
        this.schedulingPolicy = schedulingPolicy;
    }
}
