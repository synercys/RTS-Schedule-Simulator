package synercys.rts.framework;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by jjs on 2/13/17.
 */
public class TaskSet {
    protected int id = 0;   // This field is intended for easily identifying individual tasksets in mass experiments.
    public HashMap<Integer, Task> tasks = new HashMap<Integer, Task>();

    public TaskSet() {}

    /**
     * This is a copy constructor. It does deep copy.
     * @param taskSet the taskset to be cloned.
     */
    public TaskSet(TaskSet taskSet) {
        id = taskSet.id;

        /* Clone tasks (a hashmap) */
        for (HashMap.Entry<Integer, Task> taskEntry : taskSet.tasks.entrySet()) {
            Task clonedTask = new Task(taskEntry.getValue());   // The task is also cloned by using a (deep copy) copy constructor.
            tasks.put((taskEntry.getKey()).intValue(), clonedTask);
        }
    }

    public Boolean addTask(Integer inTaskId, String inTitle, String inType, long inPeriod, long inDeadline, long inExecTime, int inPriority)
    {
        int newTaskId = 0;

        if (inTaskId == null) {
            newTaskId = getLargestTaskId() + 1;
        } else {
            newTaskId = inTaskId;
        }

        if (tasks.containsKey(newTaskId))
        {
            return false;
        }

        tasks.put(newTaskId,
                new Task(newTaskId, inTitle, inType, inPeriod, inDeadline, inExecTime, inPriority));

        return true;
    }

    //public Boolean addTask(int taskId, String taskTitle, int taskType, int taskPeriod, int taskComputationTime, int taskPriority)
    //{
    //    // When adding this task, assign deadline as equals period.
    //    return addTask(taskId, taskTitle, taskType, taskPeriod, taskPeriod, taskComputationTime, taskPriority);
    //}

    public void addTask(Task inTask) {
        tasks.put(inTask.getId(), inTask);
    }

    public Task addBlankTask() {
        int newTaskId = getLargestTaskId() + 1;

        addTask(newTaskId, "Task" + newTaskId, Task.TASK_TYPE_APP, 10000000, 10000000, 100000, 0);
        return getTaskById(newTaskId);
    }

    public Boolean removeTask(Task inTask) {
        if (inTask == null)
            return false;

        int thisTaskId = inTask.getId();
        if (getTaskById(thisTaskId) == null) {
            return false;
        }
        else {
            tasks.remove(thisTaskId);
            return true;
        }
    }

    public Task getTaskById(int searchId)
    {
        return tasks.get(searchId);
    }

    public ArrayList<Task> getTasksAsArray()
    {
        ArrayList<Task> resultTaskList = new ArrayList<Task>();
        ArrayList<Integer> taskIdList = new ArrayList<Integer>(tasks.keySet());
        Collections.sort(taskIdList);
        for (int thisTaskId : taskIdList)
        {
            resultTaskList.add(tasks.get(thisTaskId));
        }

//        return resultTaskList.toArray();
        return resultTaskList;
    }

    public ArrayList<Task> getAppTaskAsArraySortedByComputationTime()
    {
        // This method will return a new task array.
        return SortTasksByComputationTime(getAppTasksAsArray());
    }

    public ArrayList<Task> getAppTaskAsArraySortedByPeriod() {
        // This method will return a new task array.
        return SortTasksByPeriod(getAppTasksAsArray());
    }

    private ArrayList<Task> SortTasksByComputationTime(ArrayList<Task> inTaskArray)
    {
        if (inTaskArray.size() <= 1)
        { // If only one task is left in the array, then just return it.
            return new ArrayList<Task>(inTaskArray);
        }

        /* Find the task that has largest computation time. */
        Task LargestComputationTimeTask = null;
        Boolean firstLoop = true;
        for (Task thisTask : inTaskArray)
        {
            if (firstLoop == true)
            {
                LargestComputationTimeTask = thisTask;
                firstLoop = false;
                continue;
            }
            else
            {
                if (thisTask.getExecTime() > LargestComputationTimeTask.getExecTime())
                {
                    LargestComputationTimeTask = thisTask;
                }
            }
        }

        // Clone the input task array and pass it into next layer of recursive function (with largest task removed).
        ArrayList processingTaskArray = new ArrayList<Task>(inTaskArray);
        processingTaskArray.remove(LargestComputationTimeTask);

        // Get the rest of tasks sorted in the array.
        ArrayList<Task> resultTaskArray = SortTasksByComputationTime(processingTaskArray);

        // Add the largest computation time task in the array so that it is in ascending order.
        resultTaskArray.add(LargestComputationTimeTask);
        return resultTaskArray;

    }

    private ArrayList<Task> SortTasksByPeriod(ArrayList<Task> inTaskArray)
    {
        if (inTaskArray.size() <= 1)
        { // If only one task is left in the array, then just return it.
            return new ArrayList<Task>(inTaskArray);
        }

        /* Find the task that has largest period. */
        Task LargestPeriodTask = null;
        Boolean firstLoop = true;
        for (Task thisTask : inTaskArray)
        {
            if (firstLoop == true)
            {
                LargestPeriodTask = thisTask;
                firstLoop = false;
                continue;
            }
            else
            {
                if (thisTask.getPeriod() > LargestPeriodTask.getPeriod())
                {
                    LargestPeriodTask = thisTask;
                }
            }
        }

        // Clone the input task array and pass it into next layer of recursive function (with largest task removed).
        ArrayList processingTaskArray = new ArrayList<Task>(inTaskArray);
        processingTaskArray.remove(LargestPeriodTask);

        // Get the rest of tasks sorted in the array.
        ArrayList<Task> resultTaskArray = SortTasksByPeriod(processingTaskArray);

        // Add the largest period task in the array so that it is in ascending order.
        resultTaskArray.add(LargestPeriodTask);
        return resultTaskArray;

    }

    public ArrayList<Task> getAppTasksAsArray()
    {
        ArrayList<Task> appTasks = new ArrayList<Task>();
        for (Task thisTask: tasks.values())
        {
            if (thisTask.getTaskType().equalsIgnoreCase(Task.TASK_TYPE_APP))
            {
                appTasks.add(thisTask);
            }
        }
        return appTasks;
    }

    public void clear()
    {
        tasks.clear();
    }

    public int size() { return tasks.size(); }

    public ArrayList<Task> getHigherPriorityTasks(int inPriority)
    {
        ArrayList<Task> higherPriorityTask = new ArrayList<Task>();
        for (Task thisTask: tasks.values())
        {
            if ((thisTask.getTaskType().equalsIgnoreCase(Task.TASK_TYPE_APP)) && (thisTask.getPriority()>inPriority))
            {
                higherPriorityTask.add(thisTask);
            }
        }
        return higherPriorityTask;
    }

    public Task getTaskByName( String inName ) {
        for (Task thisTask : getTasksAsArray()) {
            if ( thisTask.getTitle().equalsIgnoreCase(inName) == true ) {
                return thisTask;
            }
        }

        // No task has been found.
        return null;
    }

    public Task getIdleTask() {
        for (Task thisTask : tasks.values()) {
            if (thisTask.getTaskType().equalsIgnoreCase(Task.TASK_TYPE_IDLE)) {
                return thisTask;
            }
        }
        return null;
    }

    public void addIdleTask() {
        if (getIdleTask() == null) {
            tasks.put(Task.IDLE_TASK_ID, new Task(Task.IDLE_TASK_ID, "IDLE", Task.TASK_TYPE_IDLE, 0, 0, 0, 0));
        }
    }

    public void removeIdleTask() {
        Task idleTask = getIdleTask();
        if (idleTask != null) {
            removeTask(idleTask);
        }
    }

    //public void clearSimData() {
    //    for (Task thisTask : getTasksAsArray()) {
    //        thisTask.clearSimData();
    //    }
    //}

    public int getLargestTaskId() {
        int largestId = 0;
        /* Search for the largest ID number. */
        for (int thisId : tasks.keySet()) {
            largestId = (largestId>thisId) ? largestId : thisId;
        }
        return largestId;
    }

    static long GCD(long a, long b) {
        long Remainder;

        while (b != 0) {
            Remainder = a % b;
            a = b;
            b = Remainder;
        }

        return a;
    }

    static long LCM(long a, long b) {
        return a * b / GCD(a, b);
    }

    public long calHyperPeriod() {
        long hyperPeriod = 1;
        for (Task thisTask : getAppTasksAsArray()) {
            hyperPeriod = LCM(hyperPeriod, thisTask.getPeriod());
        }
        return hyperPeriod;
    }

    public Boolean schedulabilityTest() {
        //int numTasks = taskContainer.getAppTasksAsArray().size();
        for (Task thisTask : getAppTasksAsArray()) {
            long thisWCRT = calc_WCRT(thisTask);
            if (thisWCRT > thisTask.getDeadline()) {
                // unschedulable.
                //ProgMsg.errPutline("%d > %d", thisWCRT, thisTask.getDeadlineNs());
                return false;
            } else {
                //ProgMsg.sysPutLine("ok: %d < %d", thisWCRT, thisTask.getDeadlineNs());
            }
        }
        return true;
    }

    // Code modified from Man-Ki's code (compute worst case response time)
    public long calc_WCRT(Task task_i) {
        int numItr = 0;
        long Wi = task_i.getExecTime();
        long prev_Wi = 0;

        //int numTasks = taskContainer.getAppTasksAsArray().size();
        while (true) {
            int interference = 0;
            for (Task thisTask : getAppTasksAsArray()) {
                Task task_hp = thisTask;
                if (task_hp.getPriority() <= task_i.getPriority())  // Priority: the bigger the higher
                    continue;

                long Tj = task_hp.getPeriod();
                long Cj = task_hp.getExecTime();

                interference += (int)myCeil((double)Wi / (double)Tj) * Cj;
            }

            Wi = task_i.getExecTime() + interference;

            if (Long.compare(Wi, prev_Wi) == 0)
                return Wi;

            prev_Wi = Wi;

            numItr++;
            if (numItr > 1000 || Wi < 0)
                return Integer.MAX_VALUE;
        }
    }


    // Code from Man-Ki
    double myCeil(double val) {
        double diff = Math.ceil(val) - val;
        if (diff > 0.99999) {
            //ProgMsg.errPutline("###" + (val) + "###\t\t " + Math.ceil(val));
            System.exit(-1);
        }
        return Math.ceil(val);
    }

    // The bigger the number the higher the priority
    // When calling this function, the taskContainer should not contain idle task.
    public void assignPriorityRm()
    {
        ArrayList<Task> allTasks = getTasksAsArray();
        int numTasks = getAppTasksAsArray().size();

        /* Assign priorities (RM) */
        for (Task task_i : getAppTasksAsArray()) {
            //Task task_i = allTasks.get(i);
            int cnt = 1;    // 1 represents highest priority.
            /* Get the priority by comparing other tasks. */
            for (Task task_j : getAppTasksAsArray()) {
                if (task_i.equals(task_j))
                    continue;

                //Task task_j = allTasks.get(j);
                if (task_j.getPeriod() > task_i.getPeriod()
                        || (task_j.getPeriod() == task_i.getPeriod() && task_j.getId() > task_i.getId())) {
                    cnt++;
                }
            }
            task_i.setPriority(cnt);
        }
    }

    public Boolean containPeriod(int inPeriod) {
        for (Task thisTask: tasks.values())
        {
            if (thisTask.getPeriod() == inPeriod)
                return true;
        }
        return false;
    }

    public long getLargestPeriod() {
        long largestPeriod = 0;
        for (Task thisTask: tasks.values()) {
            largestPeriod = (thisTask.getPeriod() > largestPeriod) ? thisTask.getPeriod() : largestPeriod;
        }
        return largestPeriod;
    }

    public Boolean hasHarmonicPeriods() {
        for (Task taskA: getAppTasksAsArray()) {
            for (Task taskB: getAppTasksAsArray()) {
                if (taskA == taskB)
                    continue;

                if (taskA.getPeriod() % taskB.getPeriod() == 0)
                    return true;
            }
        }
        // No harmonic periods.
        return false;
    }

    public double getUtilization() {
        double resultUtil = 0;
        for (Task thisTask : getAppTasksAsArray()) {
            resultUtil += ((double)thisTask.getExecTime())/((double)thisTask.getPeriod());
        }
        return resultUtil;
    }

    public Task getLowestPriorityTask() {
        Task lowestPriorityTask = null;
        Boolean firstLoop = true;
        for (Task thisTask : getAppTasksAsArray()) {
            if (firstLoop) {
                lowestPriorityTask = thisTask;
                firstLoop = false;
            }

            // Note: the smaller the lower priority
            lowestPriorityTask = thisTask.getPriority()<lowestPriorityTask.getPriority() ? thisTask : lowestPriorityTask;
        }
        return lowestPriorityTask;
    }

    public Task getHighestPriorityTask() {
        Task highestPriorityTask = null;
        Boolean firstLoop = true;
        for (Task thisTask : getAppTasksAsArray()) {
            if (firstLoop) {
                highestPriorityTask = thisTask;
                firstLoop = false;
            }

            // Note: the bigger the higher priority
            highestPriorityTask = thisTask.getPriority()>highestPriorityTask.getPriority() ? thisTask : highestPriorityTask;
        }
        return highestPriorityTask;
    }

    public Task getOneTaskByPriority(int inPriority) {
        for (Task thisTask : tasks.values()) {
            if (thisTask.getPriority() == inPriority) {
                return thisTask;
            }
        }
        return null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        String outStr = "TaskSet(" + getUtilization() + "):\r\n";
        for (int i=getLowestPriorityTask().getPriority(); i<=getHighestPriorityTask().getPriority(); i++) {
        //for (Task thisTask : tasks.values()) {
            outStr += "\t" + getOneTaskByPriority(i).toString() + "\r\n";
        }
        return outStr;
    }
}
