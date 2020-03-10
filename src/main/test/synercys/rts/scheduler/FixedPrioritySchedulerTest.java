package synercys.rts.scheduler;

import org.junit.jupiter.api.Test;
import synercys.rts.framework.Task;
import synercys.rts.framework.TaskSet;
import synercys.rts.framework.event.EventContainer;

import static org.junit.jupiter.api.Assertions.*;

class FixedPrioritySchedulerTest {

    @Test
    void scheduleCheck() {
        TaskSet taskSet = new TaskSet();
        taskSet.addTask(new Task(0, "", Task.TASK_TYPE_APP, 5, 5, 2, 1));

        FixedPriorityScheduler fixedPriorityScheduler = new FixedPriorityScheduler(taskSet, false);
        //fixedPriorityScheduler.setGenIdleTimeEvents(false);
        EventContainer eventContainer = fixedPriorityScheduler.runSim(20);

        assertEquals(8, eventContainer.getSchedulerEvents().size());    // This includes 4 idle time events
    }

}