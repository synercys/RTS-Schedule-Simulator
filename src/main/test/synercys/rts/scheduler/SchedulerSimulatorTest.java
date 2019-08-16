package synercys.rts.scheduler;

import org.junit.jupiter.api.Test;
import synercys.rts.framework.Task;
import synercys.rts.framework.event.EventContainer;
import synercys.rts.framework.event.SchedulerIntervalEvent;

import static org.junit.jupiter.api.Assertions.*;

class SchedulerSimulatorTest {

    @Test
    void getVariedExecutionTime() {
        long testWcet = 100;

        SchedulerSimulator schedulerSimulator = newEmptySchedulerSimulator();

        Task task = new Task();
        task.setWcet(testWcet);

        long variedExecutionTime;
        for (int i=0; i<100000; i++) {
            variedExecutionTime = schedulerSimulator.getVariedExecutionTime(task);
            //System.out.println(variedExecutionTime);
            assertTrue(variedExecutionTime>0 && variedExecutionTime<=testWcet);
        }
    }

    SchedulerSimulator newEmptySchedulerSimulator() {
        SchedulerSimulator schedulerSimulator = new SchedulerSimulator(null, true, null) {
            @Override
            public EventContainer runSim(long tickLimit) {
                return null;
            }

            @Override
            protected void setTaskSetHook() {

            }
        };
        return schedulerSimulator;
    }
}