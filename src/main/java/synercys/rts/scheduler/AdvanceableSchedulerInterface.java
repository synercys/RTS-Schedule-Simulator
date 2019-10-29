package synercys.rts.scheduler;

import synercys.rts.framework.event.EventContainer;

public interface AdvanceableSchedulerInterface {
    public EventContainer runSim(long tickLimit);
    public void advance();
    public EventContainer concludeSim();
    public EventContainer getSimEventContainer();
}
