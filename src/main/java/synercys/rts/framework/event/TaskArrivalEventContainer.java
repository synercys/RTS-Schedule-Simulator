package synercys.rts.framework.event;

import java.util.Collections;

/**
 * Created by CY on 7/29/2015.
 */
public class TaskArrivalEventContainer extends StartTimeEventContainer {
    public TaskArrivalEventContainer() {super();}

    public TaskArrivalEventContainer( TaskArrivalEventContainer inContainer )
    {
        super( inContainer );
    }

    @Override
    public void add(TaskInstantEvent inEvent) {
        super.add(inEvent);
        sortTaskReleaseEventsByTimePriority();
    }

    public void sortTaskReleaseEventsByTimePriority()
    {
        sortTaskReleaseEventsByTime();

        int beforeSwapping = 0;
        int afterSwapping = 0;
        do {
            int numOfEvents = startTimeEvents.size();
            beforeSwapping = startTimeEvents.hashCode();
            for (int loop=0; loop<(numOfEvents-1) ; loop++) {
                TaskInstantEvent thisEvent = startTimeEvents.get(loop);
                TaskInstantEvent nextEvent = startTimeEvents.get(loop+1);
                if ( thisEvent.getOrgTimestamp() == nextEvent.getOrgTimestamp() ) {
                    // This event and next event have the same arrival time, thus check priority in advance.
                    if ( nextEvent.getTask().getPriority() < thisEvent.getTask().getPriority() ) {
                        // Next event has higher priority, thus do swapping.
                        Collections.swap(startTimeEvents, loop, loop+1 );
                    }
                }
            }
            afterSwapping = startTimeEvents.hashCode();

            // If some elements are swapped, then the hash code would be different. Continue the process until nothing to swap.
        } while ( beforeSwapping != afterSwapping );
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

    /* Pop the first event after the designated time stamp. */
    public TaskInstantEvent popNextEvent( int inTimeStamp )
    {
        for ( TaskInstantEvent thisEvent : startTimeEvents) {
            if ( thisEvent.getOrgTimestamp() >= inTimeStamp ) {
                startTimeEvents.remove( thisEvent );
                return thisEvent;
            }

        }

        // If no event is after the designated time, then return null.
        return null;
    }
}