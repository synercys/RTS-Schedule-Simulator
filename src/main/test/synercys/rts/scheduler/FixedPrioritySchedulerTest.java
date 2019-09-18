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
        EventContainer eventContainer = fixedPriorityScheduler.runSim(20);

        assertEquals(4, eventContainer.getSchedulerEvents().size());
    }

}