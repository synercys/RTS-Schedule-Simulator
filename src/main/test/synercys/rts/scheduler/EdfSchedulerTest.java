package synercys.rts.scheduler;

import org.junit.jupiter.api.Test;
import synercys.rts.framework.Task;
import synercys.rts.framework.TaskSet;

import static org.junit.jupiter.api.Assertions.*;

class EdfSchedulerTest {

    /**
     * Create a task set that is given in Spuri's paper (Table 1)
     * Paper link: https://hal.inria.fr/inria-00073920/file/RR-2772.pdf
     * @return a sample task set
     */
    TaskSet getExampleTaskSet() {
        TaskSet taskSet = new TaskSet();
        taskSet.addTask(1, "", Task.TASK_TYPE_APP, 4, 4, 1, 0);
        taskSet.addTask(2, "", Task.TASK_TYPE_APP, 6, 9, 2, 0);
        taskSet.addTask(3, "", Task.TASK_TYPE_APP, 8, 6, 2, 0);
        taskSet.addTask(4, "", Task.TASK_TYPE_APP, 16, 12, 2, 0);
        return taskSet;
    }

    @Test
    void calculateAndSetWCRT() {
        TaskSet taskSet = getExampleTaskSet();
        EdfScheduler.calculateAndSetWCRT(taskSet);

        /* Verify the WCRT calculation with the results given in Table 1 in Spuri's paper. */
        assertEquals(2, taskSet.getTaskById(1).getWcrt());
        assertEquals(7, taskSet.getTaskById(2).getWcrt());
        assertEquals(4, taskSet.getTaskById(3).getWcrt());
        assertEquals(10, taskSet.getTaskById(4).getWcrt());
    }

    @Test
    void calculateTaskResponseTimeReleasedAtGivenTimePoint() {
        TaskSet taskSet = getExampleTaskSet();

        /* Verify the response time calculation with the results given in Table 2 in Spuri's paper. */
        Task task3 = taskSet.getTaskById(3);
        assertEquals(3, EdfScheduler.calculateTaskResponseTimeReleasedAtGivenTimePoint(taskSet, task3, 0));
        assertEquals(2, EdfScheduler.calculateTaskResponseTimeReleasedAtGivenTimePoint(taskSet, task3, 2));
        assertEquals(2, EdfScheduler.calculateTaskResponseTimeReleasedAtGivenTimePoint(taskSet, task3, 3));
        assertEquals(2, EdfScheduler.calculateTaskResponseTimeReleasedAtGivenTimePoint(taskSet, task3, 6));
        assertEquals(2, EdfScheduler.calculateTaskResponseTimeReleasedAtGivenTimePoint(taskSet, task3, 8));
        assertEquals(4, EdfScheduler.calculateTaskResponseTimeReleasedAtGivenTimePoint(taskSet, task3, 9));
        assertEquals(4, EdfScheduler.calculateTaskResponseTimeReleasedAtGivenTimePoint(taskSet, task3, 10));
    }
}