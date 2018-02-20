package synercys.rts.simulator;

import synercys.rts.framework.TaskSet;

import java.util.ArrayList;

/**
 * Created by jjs on 9/3/15.
 */
public class TaskSetContainer {
    ArrayList<TaskSet> taskSets = new ArrayList<>();

    public TaskSetContainer() {
    }

    public TaskSetContainer(ArrayList<TaskSet> taskContainers) {
        this.taskSets = taskContainers;
    }

    public ArrayList<TaskSet> getTaskSets() {
        return taskSets;
    }

    public void addTaskSet(TaskSet inTaskSet) {
        taskSets.add(inTaskSet);
    }

    public void setTaskSets(ArrayList<TaskSet> inTaskContainers) {
        taskSets.clear();
        taskSets.addAll( inTaskContainers );
    }

    public int getMostTaskCount() {
        int mostTaskCount = 0;
        for (TaskSet thisTasks : taskSets) {
            mostTaskCount = (thisTasks.size()>mostTaskCount) ? thisTasks.size() : mostTaskCount;
        }
        return mostTaskCount;
    }

    public int size() {
        return taskSets.size();
    }
}