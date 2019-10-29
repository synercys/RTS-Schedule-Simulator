package synercys.rts.scheduler.entropy;

import synercys.rts.framework.event.EventContainer;

public class ApproximateEntropyCalculator implements ScheduleEntropyCalculatorInterface {
    @Override
    public void applyOneSchedule(EventContainer schedule) {

    }

    @Override
    public double concludeEntropy() {
        return 0;
    }
}
