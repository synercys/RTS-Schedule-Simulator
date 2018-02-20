package synercys.rts.event;

/**
 * Created by jjs on 2/13/17.
 */
public class InstantEvent extends Event {
    protected long orgTimestamp = 0;
    protected long scaledTimestamp = 0;

    protected String note = "";

    public InstantEvent(){}

    public long getOrgTimestamp() { return orgTimestamp; }
    public long getScaledTimestamp()
    {
        return scaledTimestamp;
    }
    public void applyScaleX(int inScaleX)
    {
        scaledTimestamp = orgTimestamp /inScaleX;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
