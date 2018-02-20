package synercys.rts.event;

import java.util.ArrayList;

/**
 * Created by CY on 5/21/2015.
 */
public class BusyIntervalEvent extends IntervalEvent {
     ArrayList<SchedulerIntervalEvent> schedulerIntervalEvents = new ArrayList<>();

    public BusyIntervalEvent() {
        super();
    }

    public BusyIntervalEvent(long inBeginTimeStamp, long inEndTimeStamp)
    {
        super(inBeginTimeStamp, inEndTimeStamp);
    }

    public ArrayList<SchedulerIntervalEvent> getSchedulerIntervalEvents() {
        return schedulerIntervalEvents;
    }

    public Boolean addIntervalEvent(SchedulerIntervalEvent inEvent) {
        schedulerIntervalEvents.add(inEvent);

        updateBeginAndEndTimes();

        // TODO: We should check whether added interval would make a discontinued busy interval and return false if it does.
        return true;
    }

    public void updateBeginAndEndTimes() {
        Boolean firstLoop = true;
        for (SchedulerIntervalEvent thisEvent : schedulerIntervalEvents) {
            if (firstLoop) {
                orgBeginTimestamp = thisEvent.orgBeginTimestamp;
                orgEndTimestamp = thisEvent.orgEndTimestamp;
                firstLoop = false;
            } else {
                orgBeginTimestamp = thisEvent.orgBeginTimestamp<orgBeginTimestamp?thisEvent.orgBeginTimestamp:orgBeginTimestamp;
                orgEndTimestamp = thisEvent.orgEndTimestamp>orgEndTimestamp?thisEvent.orgEndTimestamp:orgEndTimestamp;
            }
        }

    }

    public ArrayList<BusyIntervalEvent> getBusyIntervalEventsFromHigherPriorityTasks(int inPriority) {
        ArrayList<BusyIntervalEvent> resultBis = new ArrayList<>();
        BusyIntervalEvent currentBusyInterval = null;
        Boolean aBusyIntervalFound = false;
        for (SchedulerIntervalEvent thisEvent : schedulerIntervalEvents) {
            if (thisEvent.getTask().getPriority() >= inPriority) {
                if (aBusyIntervalFound == true) {
                    // Add current busy interval to the last existing busy interval.
                    currentBusyInterval.addIntervalEvent(thisEvent);
                } else {
                    // New a busy interval since it is the beginning of the busy interval
                    currentBusyInterval = new BusyIntervalEvent(thisEvent.getOrgBeginTimestamp(), thisEvent.getOrgEndTimestamp());
                    currentBusyInterval.addIntervalEvent(thisEvent);
                    aBusyIntervalFound = true;
                }
            } else {
                resultBis.add(currentBusyInterval);
                aBusyIntervalFound = false;
            }
        }

        // Check if the last busy interval is not closed.
        if (aBusyIntervalFound == true) {
            resultBis.add(currentBusyInterval);
        }

        return resultBis;
    }
}