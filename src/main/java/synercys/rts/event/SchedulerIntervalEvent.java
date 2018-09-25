package synercys.rts.event;

import synercys.rts.framework.Task;

/**
 * Created by jjs on 2/13/17.
 */
public class SchedulerIntervalEvent extends IntervalEvent {
    public static int SCHEDULE_STATE_UNKNOWN = 0;
    public static int SCHEDULE_STATE_START = 1;
    public static int SCHEDULE_STATE_RESUME = 2;
    public static int SCHEDULE_STATE_SUSPEND = 3;
    public static int SCHEDULE_STATE_END = 4;

    private Task task = null;
    private int beginTimeScheduleState = SCHEDULE_STATE_UNKNOWN;
    private int endTimeScheduleState = SCHEDULE_STATE_UNKNOWN;

    public SchedulerIntervalEvent(long inTimeStamp, Task inTask, String inNote)
    {
        orgBeginTimestamp = inTimeStamp;
        scaledBeginTimestamp = inTimeStamp;

        orgEndTimestamp = orgBeginTimestamp;
        scaledEndTimestamp = orgEndTimestamp;

        task = inTask;
        note = inNote;

        // Since the end time is not specified, it is an incomplete event.
        eventCompleted = false;
    }

    public SchedulerIntervalEvent(long inBeginTimeStamp, long inEndTimeStamp, Task inTask, String inNote)
    {
        this(inBeginTimeStamp, inTask, inNote);
        orgEndTimestamp = inEndTimeStamp;
        scaledEndTimestamp = inEndTimeStamp;


        eventCompleted = true;
    }

    public void setScheduleStates(Integer inBeginTimeScheduleState, Integer inEndTimeScheduleState) {
        if (inBeginTimeScheduleState != null) {
            beginTimeScheduleState = inBeginTimeScheduleState;
        }
        if (inEndTimeScheduleState != null) {
            endTimeScheduleState = inEndTimeScheduleState;
        }
    }

    public int getBeginTimeScheduleState() {
        return beginTimeScheduleState;
    }

    public int getEndTimeScheduleState() {
        return endTimeScheduleState;
    }

    public Task getTask() { return task; }

    @Override
    public String toString() {
        return orgBeginTimestamp + "-" + orgEndTimestamp + ", " + task.getId() + ", " + "\"" + note + "\"";
    }
}
