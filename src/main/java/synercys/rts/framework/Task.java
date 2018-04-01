package synercys.rts.framework;

/**
 * Created by CY on 2/13/17.
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

    protected long wcet = 0;

    protected long execTime = 0;

    protected int priority = 0;

    protected long initialOffset = 0;

    /* Parameters for Simulation */
    protected long nextReleaseTime = 0;

    protected boolean isSporadicTask = false;

    public Task(){}

    public Task(int inTaskId, String inTitle, String inType, long inPeriod, long inDeadline, long inExecTime, int inPriority)
    {
        title = inTitle;
        id = inTaskId;
        taskType = inType;
        period = inPeriod;
        deadline = inDeadline;
        execTime = inExecTime;
        priority = inPriority;
    }

    //public Task(Task inTask) {
    //    cloneSettings(inTask);
    //}


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

    public long getExecTime() {
        return execTime;
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

    public void setExecTime(long execTime) {
        this.execTime = execTime;

        if (wcet == 0) {
            wcet = execTime;
        }
    }

    public long getWcet() {
        return wcet;
    }

    public void setWcet(long wcet) {
        this.wcet = wcet;
    }

    @Override
    public String toString() {
        return "Task-" + id +
                ": p=" + period +
                ", c=" + execTime +
                ", pri=" + priority +
                ", offset=" + initialOffset;
    }
}
