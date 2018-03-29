package synercys.rts.util;

import cy.utility.file.FileHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import synercys.rts.RtsConfig;
import synercys.rts.framework.Task;
import synercys.rts.framework.TaskSet;
import synercys.rts.simulator.TaskSetContainer;

import java.util.ArrayList;

/**
 * Created by cy on 3/28/2018.
 */
public class JsonLogExporter extends FileHandler {
    public static final String WRITER_VERSION = "12";

    public JsonLogExporter(String filePath) {
        openToWriteFile(filePath);
    }

    public String getVersion() {
        return WRITER_VERSION;
    }

    public void exportSingleTaskSet(TaskSet inTaskSet) {
        JSONObject jsonRoot = new JSONObject();

        /* Basics */
        // root - formatVersion
        putVersion(jsonRoot);
        // root - dataType
        jsonRoot.put(JsonLogStr.ROOT_DATA_TYPE, JsonLogStr.DATA_TYPE_SINGLE_TASKSET);

        /* root - data */
        JSONObject jsonData = new JSONObject(); // This will be added to jsonRoot in the end.
        // root - data - tickUnitNs
        jsonData.put(JsonLogStr.TICK_UNIT, RtsConfig.TIMESTAMP_UNIT_NS);
        // root - data - taskSets (an array containing only one taskset)
        JSONArray jsonTaskSetArray = new JSONArray();
        jsonTaskSetArray.put(getJsonTaskSet(inTaskSet));
        jsonData.put(JsonLogStr.DATA_TASKSETS, jsonTaskSetArray);

        jsonRoot.put(JsonLogStr.ROOT_DATA, jsonData);

        writeString(jsonRoot.toString());
    }

    public void exportTaskSets(TaskSetContainer taskSetContainer) {
        JSONObject jsonRoot = new JSONObject();

        /* Basics */
        // root - formatVersion
        putVersion(jsonRoot);
        // root - dataType
        jsonRoot.put(JsonLogStr.ROOT_DATA_TYPE, JsonLogStr.DATA_TYPE_TASKSETS);

        /* root - data */
        JSONObject jsonData = new JSONObject(); // This will be added to jsonRoot in the end.
        // root - data - tickUnitNs
        jsonData.put(JsonLogStr.TICK_UNIT, RtsConfig.TIMESTAMP_UNIT_NS);
        // root - data - tasksets
        jsonData.put(JsonLogStr.DATA_TASKSETS, getJsonTaskSets(taskSetContainer.getTaskSets()));

        jsonRoot.put(JsonLogStr.ROOT_DATA, jsonData);

        writeString(jsonRoot.toString());
    }

    protected void putVersion(JSONObject inJson) {
        inJson.put(JsonLogStr.ROOT_FORMAT_VERSION, getVersion());
    }

    /**
     * Json example:
     * {
     *  "tasks": [
     *      {
     *          "id":1
     *          "type":"periodic"
     *          "period":100
     *          ...
     *      }, {
     *          "id":2
     *          "type":"sporadic"
     *          "period":200
     *          ...
     *      }
     *  ]
     * }
     * @param inTaskSet the task set to be converted to JSON format.
     * @return a JSON object that corresponds to the given task set.
     */
    protected JSONObject getJsonTaskSet(TaskSet inTaskSet) {
        JSONObject jsonTaskSet = new JSONObject();

        JSONArray jsonTaskArray = new JSONArray();
        for (Task task : inTaskSet.getTasksAsArray()) {
            JSONObject jsonTask = new JSONObject();
            jsonTask.put(JsonLogStr.TASK_ID, task.getId());
            jsonTask.put(JsonLogStr.TASK_NAME, task.getTitle());
            jsonTask.put(JsonLogStr.TASK_TYPE, task.getTaskType());
            jsonTask.put(JsonLogStr.TASK_ARRIVAL_TYPE, task.isSporadicTask()?JsonLogStr.TASK_ARRIVAL_TYPE_SPORADIC :JsonLogStr.TASK_ARRIVAL_TYPE_PERIODIC);
            jsonTask.put(JsonLogStr.TASK_PERIOD, task.getPeriod());
            jsonTask.put(JsonLogStr.TASK_DEADLINE, task.getDeadline());
            jsonTask.put(JsonLogStr.TASK_WCET, task.getWcet());
            jsonTask.put(JsonLogStr.TASK_PRIORITY, task.getPriority());
            jsonTask.put(JsonLogStr.TASK_PHASE, task.getInitialOffset());
            jsonTaskArray.put(jsonTask);
        }
        jsonTaskSet.put(JsonLogStr.TASKSET_TASKS, jsonTaskArray);
        jsonTaskSet.put(JsonLogStr.TASKSET_ID, inTaskSet.getId());

        return jsonTaskSet;
    }

    protected JSONArray getJsonTaskSets(ArrayList<TaskSet> inTaskSets) {
        //JSONObject jsonTaskSetsWrapper = new JSONObject();

        JSONArray jsonTaskSetArray = new JSONArray();
        for (TaskSet taskset : inTaskSets) {
            JSONObject jsonTaskSet = getJsonTaskSet(taskset);
            //jsonTaskSet.put(JsonLogStr.TASKSET_ID, taskset.getId());
            jsonTaskSetArray.put(jsonTaskSet);
        }

        return jsonTaskSetArray;
    }
}
