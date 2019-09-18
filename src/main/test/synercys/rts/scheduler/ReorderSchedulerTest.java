package synercys.rts.scheduler;

import org.junit.jupiter.api.Test;
import synercys.rts.framework.Task;
import synercys.rts.framework.TaskSet;

import static org.junit.jupiter.api.Assertions.*;

class ReorderSchedulerTest {

    @Test
    void computeTaskWCIB() {
        /* Test with the task set given in Example 1 in the REORDER paper. */
        TaskSet taskSet = new TaskSet();
        Task task1 = new Task(1, "", Task.TASK_TYPE_APP, 10, 10, 4, 1);
        Task task2 = new Task(2, "", Task.TASK_TYPE_APP, 20, 20, 1, 1);
        Task task3 = new Task(3, "", Task.TASK_TYPE_APP, 5, 5, 1, 1);
        Task task4 = new Task(4, "", Task.TASK_TYPE_APP, 12, 12, 2, 1);

        taskSet.addTask(task1);
        taskSet.addTask(task2);
        taskSet.addTask(task3);
        taskSet.addTask(task4);

        ReorderScheduler reorderScheduler = new ReorderScheduler(taskSet, false);

        // System.out.println(reorderScheduler.taskWCIB.get(task1));
        // System.out.println(reorderScheduler.taskWCIB.get(task2));
        // System.out.println(reorderScheduler.taskWCIB.get(task3));
        // System.out.println(reorderScheduler.taskWCIB.get(task4));

        assertEquals(1, reorderScheduler.taskWCIB.get(task1));
        assertEquals(-2, reorderScheduler.taskWCIB.get(task2));
        assertEquals(-2, reorderScheduler.taskWCIB.get(task3));
        assertEquals(-1, reorderScheduler.taskWCIB.get(task4));

        assertEquals(1, reorderScheduler.jobRIB.get(task1));
        assertEquals(-2, reorderScheduler.jobRIB.get(task2));
        assertEquals(-2, reorderScheduler.jobRIB.get(task3));
        assertEquals(-1, reorderScheduler.jobRIB.get(task4));
    }
}