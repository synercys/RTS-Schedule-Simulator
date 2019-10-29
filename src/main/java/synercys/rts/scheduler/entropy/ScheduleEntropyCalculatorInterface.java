package synercys.rts.scheduler.entropy;

import synercys.rts.framework.TaskSet;
import synercys.rts.framework.event.EventContainer;

public interface ScheduleEntropyCalculatorInterface {
    public void applyOneSchedule(EventContainer schedule);
    public double concludeEntropy();
}
