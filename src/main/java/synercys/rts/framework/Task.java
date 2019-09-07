package synercys.rts.framework;

import synercys.rts.RtsConfig;
import java.util.Formatter; // for formatting String

/**
 * Task.java
 *
 * @author CY Chen (cchen140@illinois.edu)
 * @version 1.1 - 2019, 9/7
 * @version 1.0 - 2017, 2/13
 */
public class Task {
    public static String TASK_TYPE_UNKNOWN = "UNKNOWN";
    public static String TASK_TYPE_SYS = "SYS";
    public static String TASK_TYPE_APP = "APP";
    public static String TASK_TYPE_IDLE = "IDLE";

    public static int IDLE_TASK_ID = 0;

    /* Fundamental Task Parameters */
    protected int id = 0;
    private String taskType = TASK_TYPE_UNKNOWN;

    private String title = "";

    protected long period = 0;
    protected long deadline = 0;

    protected long wcet = 0;    // the worst case execution time
    protected long wcrt = 0;    // the worst case response time

    //protected long execTime = 0;

    protected int priority = 0;

    protected long initialOffset = 0;

    /* Parameters for Simulation */
    protected long nextReleaseTime = 0;

    protected boolean isSporadicTask = false;

    public Task(){}

    public Task(int inTaskId, String inTitle, String inType, long inPeriod, long inDeadline, long inWCET, int inPriority)
    {
        title = inTitle;
        id = inTaskId;
        taskType = inType;
        period = inPeriod;
        deadline = inDeadline;
        //execTime = inExecTime;
        wcet = inWCET;
        priority = inPriority;
    }

    /**
     * This is a copy constructor. It does deep copy.
     * @param task the task to be cloned.
     */
    public Task(Task task) {
        id = task.id;
        taskType = task.taskType;
        title = task.title;
        period = task.period;
        deadline = task.deadline;
        wcet = task.wcet;
        //execTime = task.execTime;
        priority = task.priority;
        initialOffset = task.initialOffset;
        nextReleaseTime = task.nextReleaseTime;
        isSporadicTask = task.isSporadicTask;
    }

    public boolean isSporadicTask() {
        return isSporadicTask;
    }

    public void setSporadicTask(boolean sporadicTask) {
        isSporadicTask = sporadicTask;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId()
    {
        return id;
    }

    public long getPeriod() {
        return period;
    }

    public String getTitle()
    {
        return title;
    }

    public String getTaskType()
    {
        return taskType;
    }

    public long getDeadline() { return deadline; }

    public int getPriority() { return priority; }

    public void setPriority(int inPriority)
    {
        priority = inPriority;
    }

    public long getInitialOffset() {
        return initialOffset;
    }

    public void setInitialOffset(long initialOffset) {
        this.initialOffset = initialOffset;
    }

    public long getNextReleaseTime() {
        return nextReleaseTime;
    }

    public void setNextReleaseTime(long nextReleaseTime) {
        this.nextReleaseTime = nextReleaseTime;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    public long getWcet() {
        return wcet;
    }

    public void setWcet(long wcet) {
        this.wcet = wcet;
    }

    public long getWcrt() {
        return wcrt;
    }

    public void setWcrt(long wcrt) {
        this.wcrt = wcrt;
    }

    @Override
    public String toString() {
        return "Task-" + id +
                ": p=" + period +
                ", c=" + wcet +
                ", pri=" + priority +
                ", offset=" + initialOffset +
                //", f=" + 1 / ((double)period*RtsConfig.TIMESTAMP_UNIT_TO_S_MULTIPLIER);
                (new Formatter()).format(", f=%.2f", 1 / ((double)period*RtsConfig.TIMESTAMP_UNIT_TO_S_MULTIPLIER)).toString();
    }
}
