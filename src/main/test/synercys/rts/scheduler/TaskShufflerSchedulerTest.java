package synercys.rts.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import synercys.rts.framework.Task;
import synercys.rts.framework.TaskSet;

import static org.junit.jupiter.api.Assertions.*;

class TaskShufflerSchedulerTest {

    @Test
    void computeTaskMinInversionPriority() {
        /* Test example 2 given in the TaskShuffler paper. */
        TaskSet taskSet = new TaskSet();
        Task task0 = new Task(0, "", Task.TASK_TYPE_APP, 5, 5, 1, 5);
        Task task1 = new Task(1, "", Task.TASK_TYPE_APP, 8, 8, 3, 4);
        Task task2 = new Task(2, "", Task.TASK_TYPE_APP, 20, 20, 4, 3);
        Task task3 = new Task(3, "", Task.TASK_TYPE_APP, 40, 40, 2, 2);
        Task task4 = new Task(4, "", Task.TASK_TYPE_APP, 80, 80, 4, 1);

        taskSet.addTask(task0);
        taskSet.addTask(task1);
        taskSet.addTask(task2);
        taskSet.addTask(task3);
        taskSet.addTask(task4);

        TaskShufflerScheduler taskShufflerScheduler = new TaskShufflerScheduler(taskSet, false);

        /* check WCIB (each task's worst case inversion budget) */
        assertEquals(4, taskShufflerScheduler.taskWCIB.get(task0));
        assertEquals(2, taskShufflerScheduler.taskWCIB.get(task1));
        assertEquals(-1, taskShufflerScheduler.taskWCIB.get(task2));
        assertEquals(-1, taskShufflerScheduler.taskWCIB.get(task3));
        assertEquals(0, taskShufflerScheduler.taskWCIB.get(task4));

        /* check RIB (each task's RIB) */
        assertEquals(4, taskShufflerScheduler.jobRIB.get(task0));
        assertEquals(2, taskShufflerScheduler.jobRIB.get(task1));
        assertEquals(-1, taskShufflerScheduler.jobRIB.get(task2));
        assertEquals(-1, taskShufflerScheduler.jobRIB.get(task3));
        assertEquals(0, taskShufflerScheduler.jobRIB.get(task4));

        /* check M_i (each task's minimum inversion priority)*/
        assertEquals(task2.getPriority(), taskShufflerScheduler.taskM.get(task0));
        assertEquals(task2.getPriority(), taskShufflerScheduler.taskM.get(task1));
        assertEquals(task3.getPriority(), taskShufflerScheduler.taskM.get(task2));
        assertEquals(-1, taskShufflerScheduler.taskM.get(task3));
        assertEquals(-1, taskShufflerScheduler.taskM.get(task4));

    }


}