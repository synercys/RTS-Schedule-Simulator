package synercys.rts.event;

/**
 * Created by jjs on 2/13/17.
 */
public class IntervalEvent extends Event {
    //public static final int EVENT_SCHEDULER = 0;
    protected long orgBeginTimestamp = 0;
    protected long orgEndTimestamp = 0;

    protected long scaledBeginTimestamp = 0;
    protected long scaledEndTimestamp = 0;

    public Boolean eventCompleted = true;

    protected String note = "";
    protected Boolean noteVisible = false;

    public IntervalEvent(){}

    public IntervalEvent(long inBeginTimeStamp, long inEndTimeStamp)
    {
        orgBeginTimestamp = inBeginTimeStamp;
        scaledBeginTimestamp = inBeginTimeStamp;

        orgEndTimestamp = inEndTimeStamp;
        scaledEndTimestamp = inEndTimeStamp;
    }

    public long getOrgBeginTimestamp() { return orgBeginTimestamp; }
    public long getScaledBeginTimestamp()
    {
        return scaledBeginTimestamp;
    }
    public void setOrgEndTimestamp(long inputTimeStamp)
    {
        orgEndTimestamp = inputTimeStamp;
        // TODO: scaled value should be handled separately.
        scaledEndTimestamp = orgEndTimestamp;
    }
    public long getScaledEndTimestamp() { return scaledEndTimestamp; }

    public long getOrgEndTimestamp()
    {
        return orgEndTimestamp;
    }

    public void applyScaleX(int inScaleX)
    {
        scaledBeginTimestamp = orgBeginTimestamp /inScaleX;
        scaledEndTimestamp = orgEndTimestamp /inScaleX;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Boolean getNoteVisible() {
        return noteVisible;
    }

    public void setNoteVisible(Boolean noteVisible) {
        this.noteVisible = noteVisible;
    }

    public Boolean contains(long inTimeStamp)
    {
        if ((orgBeginTimestamp <= inTimeStamp)
                && (orgEndTimestamp >= inTimeStamp))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public long getDuration()
    {
        return orgEndTimestamp - orgBeginTimestamp;
    }

    public long getScaledDuration() {
        return scaledEndTimestamp - scaledBeginTimestamp;
    }

    public void extendEnd(long inExtraLength) {
        orgEndTimestamp += inExtraLength;
        //TODO: How to deal with scaled timestamp here?
        scaledEndTimestamp = orgEndTimestamp;
    }
}
