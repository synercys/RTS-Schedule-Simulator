package synercys.rts.framework.event;

import synercys.rts.framework.Task;

import java.util.ArrayList;

/**
 * Created by cy on 3/26/2017.
 */
public class StartTimeEventContainer {
    ArrayList<TaskInstantEvent> startTimeEvents = new ArrayList<>();

    public StartTimeEventContainer() {}

    public StartTimeEventContainer(StartTimeEventContainer inContainer)
    {
        // replicate the event array.
        startTimeEvents.addAll(inContainer.startTimeEvents);
    }

    public void add( TaskInstantEvent inEvent )
    {
        startTimeEvents.add(inEvent);
        sortTaskReleaseEventsByTime();
    }

    public TaskInstantEvent add(int releaseTime, Task inTask)
    {
        TaskInstantEvent thisEvent = new TaskInstantEvent( releaseTime, inTask, 0, "" );

        this.add(thisEvent);

        return thisEvent;
    }

    public void addAll( ArrayList<TaskInstantEvent> inEvents )
    {
        startTimeEvents.addAll(inEvents);
    }

    public void sortTaskReleaseEventsByTime()
    {
        ArrayList<TaskInstantEvent> sortedEvents = new ArrayList<>();
        for ( TaskInstantEvent thisEvent : startTimeEvents) {

            Boolean firstLoop = true;
            for ( TaskInstantEvent thisSortedEvent : sortedEvents ) {
                if ( firstLoop == true ) {
                    firstLoop = false;
                    sortedEvents.add( thisEvent );
                    continue;
                }

                // If the time is smaller (earlier), then insert to that
                if ( thisEvent.getOrgTimestamp() < thisSortedEvent.getOrgTimestamp() ) {
                    sortedEvents.add( sortedEvents.indexOf( thisSortedEvent ), thisEvent );
                    break; // Found place, so insert the event and break the loop to process next event.
                }
            }
        }
    }

    public int size()
    {
        return startTimeEvents.size();
    }

    public ArrayList<Task> getTasksOfEvents()
    {
        ArrayList<Task> resultTasks = new ArrayList<>();
        for ( TaskInstantEvent thisEvent : startTimeEvents) {
            resultTasks.add(thisEvent.getTask());
        }
        return resultTasks;
    }

    public TaskInstantEvent get(int index)
    {
        return startTimeEvents.get( index );
    }

    public void clear()
    {
        startTimeEvents.clear();
    }


    /* Find the first event after the designated time stamp. */
    public TaskInstantEvent getNextEvent( int inTimeStamp )
    {
        for ( TaskInstantEvent thisEvent : startTimeEvents) {
            if ( thisEvent.getOrgTimestamp() >= inTimeStamp )
                return thisEvent;
        }

        // If no event is after the designated time, then return null.
        return null;
    }

    public ArrayList<TaskInstantEvent> getEvents()
    {
        return startTimeEvents;
    }

    public ArrayList<TaskInstantEvent> getEventsOfTask(Task inTask) {
        ArrayList<TaskInstantEvent> resultEvents = new ArrayList<>();
        for ( TaskInstantEvent thisEvent : startTimeEvents) {
            if ( thisEvent.getTask() == inTask )
                resultEvents.add(thisEvent);
        }
        return resultEvents;
    }
}
