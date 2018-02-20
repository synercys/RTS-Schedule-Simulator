package synercys.rts.event;

import synercys.rts.framework.Task;
import synercys.rts.framework.TaskSet;

import java.util.ArrayList;

/**
 * Created by jjs on 2/13/17.
 */
public class EventContainer {
    public static final int SCHEDULER_EVENT = 0;
    public static final int INSTANT_EVENT = 1;
    //public static final int HACKER_EVENT = 2;

    //private static final int PAINT_OFFSET_X = 100;
    //private static final int PAINT_OFFSET_Y = 150;//-75;//75;
    private static final double SCALE_X = 1.0;
    //private static final double SCALE_Y = 1.0;

    private ArrayList<SchedulerIntervalEvent> schedulerEvents = new ArrayList<SchedulerIntervalEvent>();
    private ArrayList<TaskInstantEvent> taskInstantEvents = new ArrayList<TaskInstantEvent>();
    //private ArrayList<HackerEvent> hackerEvents = new ArrayList<HackerEvent>();

    private TaskSet taskSet = new TaskSet();

    private long scaledEndTimestamp = 0;
    private long orgEndTimestamp = 0;

    public EventContainer(){}

    public void addNextEvent(int inEventType, long inTimestamp, int inEventTaskId, int inData, String inEventString)
    {
        if (inEventType == SCHEDULER_EVENT)
        {// inEventTaskId is 0 as from scheduler, inData is the Id of the task being scheduled.
            if (schedulerEvents.size() > 0) {
                schedulerEvents.get(schedulerEvents.size() - 1).setOrgEndTimestamp(inTimestamp);
            }
            schedulerEvents.add(new SchedulerIntervalEvent(inTimestamp, taskSet.getTaskById(inData), inEventString));

            // Assume that the added scheduler event is in order, the latest one should be the latest.
            // if (scaledEndTimestamp < inTimestampNs)
            orgEndTimestamp = inTimestamp;
            scaledEndTimestamp = inTimestamp;
        }
        //else if (inEventType == HACKER_EVENT)
        //{
        //    hackerEvents.addNextEvent(new HackerEvent(inTimestampNs, taskSet.getTaskById(inEventTaskId), inData, inEventString));
        //}
        else if (inEventType == INSTANT_EVENT)
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
        //hackerEvents.clear();
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
    //public  ArrayList<HackerEvent> getHackerEvents() { return hackerEvents; }

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

//    public ArrayList<HackerEvent> getLowHackerEvents()
//    {
//        ArrayList resultArrayList = new ArrayList();
//        for (HackerEvent currentEvent : hackerEvents)
//        {
//            if (currentEvent.getTaskId() == HackerEvent.lowHackerId)
//            {
//                resultArrayList.addNextEvent(currentEvent);
//            }
//        }
//        return resultArrayList;
//    }

    public ArrayList getAllEvents()
    {
        ArrayList resultArrayList = new ArrayList();
        resultArrayList.addAll(schedulerEvents);
        resultArrayList.addAll(taskInstantEvents);
        //resultArrayList.addAll(hackerEvents);
        return resultArrayList;
    }

    public ArrayList getAppAndSchedulerEvents()
    {
        ArrayList resultArrayList = new ArrayList();
        resultArrayList.addAll(schedulerEvents);
        resultArrayList.addAll(taskInstantEvents);
        return resultArrayList;
    }

    public long getScaledEndTimestamp()
    {
        return scaledEndTimestamp;
    }
    public long getOrgEndTimestamp() { return orgEndTimestamp; }

    public void applyHorizontalScale(int inScale)
    {
        for (SchedulerIntervalEvent currentEvent : schedulerEvents) {
            currentEvent.applyScaleX(inScale);
        }

        for (TaskInstantEvent currentEvent : taskInstantEvents)
        {
            currentEvent.applyScaleX(inScale);
        }

        //for (HackerEvent currentEvent : hackerEvents)
        //{
        //    currentEvent.applyScaleX(inScale);
        //}

        scaledEndTimestamp = orgEndTimestamp /inScale;
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
}
