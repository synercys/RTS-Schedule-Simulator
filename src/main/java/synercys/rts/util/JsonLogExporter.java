package synercys.rts.util;

import cy.utility.file.FileHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import synercys.rts.RtsConfig;
import synercys.rts.framework.event.EventContainer;
import synercys.rts.framework.event.SchedulerIntervalEvent;
import synercys.rts.framework.event.TaskInstantEvent;
import synercys.rts.framework.Task;
import synercys.rts.framework.TaskSet;
import synercys.rts.scheduler.TaskSetContainer;
import synercys.rts.scheduler.TaskSetGenerator;

import java.util.ArrayList;

/**
 * Created by cy on 3/28/2018.
 */
public class JsonLogExporter extends FileHandler {
    public static final String WRITER_VERSION = "15";

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

        writeString(jsonRoot.toString(4));
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

        writeString(jsonRoot.toString(4));
    }

    public void exportRawSchedule(EventContainer eventContainer) {
        JSONObject jsonRoot = new JSONObject();

        /* Basics */
        // root - formatVersion
        putVersion(jsonRoot);
        // root - dataType
        jsonRoot.put(JsonLogStr.ROOT_DATA_TYPE, JsonLogStr.DATA_TYPE_RT_SIM_RAW_SCHEDULE);

        /* root - data */
        JSONObject jsonData = new JSONObject(); // This will be added to jsonRoot in the end.
        // root - data - tickUnitNs
        jsonData.put(JsonLogStr.TICK_UNIT, RtsConfig.TIMESTAMP_UNIT_NS);

        // root - data - taskset
        jsonData.put(JsonLogStr.DATA_RT_SIM_TASKSET, getJsonTaskSet(eventContainer.getTaskSet()));

        // root - data - schedulingPolicy
        String schedulingPolicy = eventContainer.getSchedulingPolicy();
        if (schedulingPolicy == null) {
            schedulingPolicy = "";
        }
        if (schedulingPolicy.equalsIgnoreCase(EventContainer.SCHEDULING_POLICY_FIXED_PRIORITY)) {   // RM
            jsonData.put(JsonLogStr.DATA_RT_SIM_SCHEDULING_POLICY, JsonLogStr.SCHEDULING_POLICY_FIXED_PRIORITY);
        } else if (schedulingPolicy.equalsIgnoreCase(EventContainer.SCHEDULING_POLICY_EDF)){    // EDF
            jsonData.put(JsonLogStr.DATA_RT_SIM_SCHEDULING_POLICY, JsonLogStr.SCHEDULING_POLICY_EDF);
        } else {    // Unknown/Undefined
            jsonData.put(JsonLogStr.DATA_RT_SIM_SCHEDULING_POLICY, JsonLogStr.SCHEDULING_POLICY_UNKNOWN);
        }

        // root - data - scheduleIntervalEvents
        jsonData.put(JsonLogStr.DATA_RT_SIM_SCHEDULE_INTERVAL_EVENTS, getJsonScheduleIntervalEvents(eventContainer.getSchedulerEvents()));
        // root - data - taskInstantEvents
        jsonData.put(JsonLogStr.DATA_RT_SIM_TASK_INSTANT_EVENTS, getJsonTaskInstantEvents(eventContainer.getTaskInstantEvents()));

        jsonRoot.put(JsonLogStr.ROOT_DATA, jsonData);

        writeString(jsonRoot.toString(4));
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

    /**
     *  Json example:
     * {
     *      "taskId":1
     *      "beginState":"resume"
     *      "endState":"suspend"
     *      "begin":120
     *      "end":180
     *  }
     * @param inEvent a schedule interval event
     * @return a Json object that corresponds to the given schedule int4erval event
     */
    protected JSONObject getJsonScheduleIntervalEvent(SchedulerIntervalEvent inEvent) {
        JSONObject jsonEvent = new JSONObject();
        jsonEvent.put(JsonLogStr.SCHEDULE_INTERVAL_EVENT_TASK_ID, inEvent.getTask().getId());
        jsonEvent.put(JsonLogStr.SCHEDULE_INTERVAL_EVENT_BEGIN_STATE, getScheduleStateValueString(inEvent.getBeginTimeScheduleState()));
        jsonEvent.put(JsonLogStr.SCHEDULE_INTERVAL_EVENT_END_STATE, getScheduleStateValueString(inEvent.getEndTimeScheduleState()));
        jsonEvent.put(JsonLogStr.SCHEDULE_INTERVAL_EVENT_BEGIN_TIME, inEvent.getOrgBeginTimestamp());
        jsonEvent.put(JsonLogStr.SCHEDULE_INTERVAL_EVENT_END_TIME, inEvent.getOrgEndTimestamp());
        return jsonEvent;
    }

    /**
     *  Json example:
     * {
     *  "scheduleIntervalEvents": [
     *      {
     *          "taskId":1
     *          "beginState":"resume"
     *          "endState":"suspend"
     *          ...
     *      }, {
     *          "taskId":2
     *          "beginState":"start"
     *          "endState":"suspend"
     *          ...
     *      }
     *  ]
     * }
     * @param inEvents a list of schedule interval events
     * @return a JSON array that contains the given schedule interval events
     */
    protected JSONArray getJsonScheduleIntervalEvents(ArrayList<SchedulerIntervalEvent> inEvents) {
        JSONArray jsonArray = new JSONArray();
        for (SchedulerIntervalEvent event: inEvents) {
            jsonArray.put(getJsonScheduleIntervalEvent(event));
        }
        return jsonArray;
    }

    protected JSONObject getJsonTaskInstantEvent(TaskInstantEvent inEvent) {
        JSONObject jsonEvent = new JSONObject();
        jsonEvent.put(JsonLogStr.TASK_INSTANT_EVENT_TASK_ID, inEvent.getTask().getId());
        jsonEvent.put(JsonLogStr.TASK_INSTANT_EVENT_BEGIN, inEvent.getOrgTimestamp());
        jsonEvent.put(JsonLogStr.TASK_INSTANT_EVENT_RECORD, inEvent.getRecordData());
        jsonEvent.put(JsonLogStr.TASK_INSTANT_EVENT_NOTE, inEvent.getNote());
        return jsonEvent;
    }

    protected JSONArray getJsonTaskInstantEvents(ArrayList<TaskInstantEvent> inEvents) {
        JSONArray jsonArray = new JSONArray();
        for (TaskInstantEvent event: inEvents) {
            jsonArray.put(getJsonTaskInstantEvent(event));
        }
        return jsonArray;
    }

    public String getScheduleStateValueString(int inScheduleStateIndex) {
        /* copied from SchedulerIntervalEvent class:
        public static int SCHEDULE_STATE_UNKNOWN = 0;
        public static int SCHEDULE_STATE_START = 1;
        public static int SCHEDULE_STATE_RESUME = 2;
        public static int SCHEDULE_STATE_SUSPEND = 3;
        public static int SCHEDULE_STATE_END = 4;
        */
        if (SchedulerIntervalEvent.SCHEDULE_STATE_START == inScheduleStateIndex)
            return JsonLogStr.SCHEDULE_INTERVAL_EVENT_STATE_START;
        else if (SchedulerIntervalEvent.SCHEDULE_STATE_RESUME == inScheduleStateIndex)
            return JsonLogStr.SCHEDULE_INTERVAL_EVENT_STATE_RESUME;
        else if (SchedulerIntervalEvent.SCHEDULE_STATE_SUSPEND == inScheduleStateIndex)
            return JsonLogStr.SCHEDULE_INTERVAL_EVENT_STATE_SUSPEND;
        else if (SchedulerIntervalEvent.SCHEDULE_STATE_END == inScheduleStateIndex)
            return JsonLogStr.SCHEDULE_INTERVAL_EVENT_STATE_END;
        else //if (SchedulerIntervalEvent.SCHEDULE_STATE_UNKNOWN == inScheduleStateIndex)
            return JsonLogStr.SCHEDULE_INTERVAL_EVENT_STATE_UNKNOWN;

    }


    public void exportRtTaskGenDefaultSettings() {
        TaskSetGenerator taskSetGenerator = new TaskSetGenerator();

        JSONObject jsonRoot = new JSONObject();

        /* Basics */
        // root - formatVersion
        putVersion(jsonRoot);
        // root - dataType
        jsonRoot.put(JsonLogStr.ROOT_DATA_TYPE, JsonLogStr.DATA_TYPE_RT_TASK_GEN_SETTINGS);

        /* root - data */
        JSONObject jsonData = new JSONObject(); // This will be added to jsonRoot in the end.

        // root - data - tickUnitNs
        jsonData.put(JsonLogStr.TICK_UNIT, RtsConfig.TIMESTAMP_UNIT_NS);

        // root - data - configs
        JSONArray jsonConfigArrary = new JSONArray();

        JSONObject jsonConfig = new JSONObject();
        jsonConfig.put(JsonLogStr.TASK_GEN_SETTINGS_NUM_TASK_PER_TASKSET, taskSetGenerator.getNumTaskPerSet());
        jsonConfig.put(JsonLogStr.TASK_GEN_SETTINGS_NUM_TASKSET, taskSetGenerator.getNumTaskSet());
        jsonConfig.put(JsonLogStr.TASK_GEN_SETTINGS_MAX_HP, taskSetGenerator.getMaxHyperPeriod());
        jsonConfig.put(JsonLogStr.TASK_GEN_SETTINGS_MIN_PERIOD, taskSetGenerator.getMinPeriod());
        jsonConfig.put(JsonLogStr.TASK_GEN_SETTINGS_MAX_PERIOD, taskSetGenerator.getMaxPeriod());
        jsonConfig.put(JsonLogStr.TASK_GEN_SETTINGS_MIN_WCET, taskSetGenerator.getMinWcet());
        jsonConfig.put(JsonLogStr.TASK_GEN_SETTINGS_MAX_WCET, taskSetGenerator.getMaxWcet());
        jsonConfig.put(JsonLogStr.TASK_GEN_SETTINGS_MIN_OFFSET, taskSetGenerator.getMinInitOffset());
        jsonConfig.put(JsonLogStr.TASK_GEN_SETTINGS_MAX_OFFSET, taskSetGenerator.getMaxInitOffset());
        jsonConfig.put(JsonLogStr.TASK_GEN_SETTINGS_MIN_UTIL, taskSetGenerator.getMinUtil());
        jsonConfig.put(JsonLogStr.TASK_GEN_SETTINGS_MAX_UTIL, taskSetGenerator.getMaxUtil());
        jsonConfig.put(JsonLogStr.TASK_GEN_SETTINGS_FROM_HP_DIVISORS, taskSetGenerator.getGenerateFromHpDivisors());
        jsonConfig.put(JsonLogStr.TASK_GEN_SETTINGS_NON_HARMONIC_ONLY, taskSetGenerator.getNonHarmonicOnly());
        jsonConfig.put(JsonLogStr.TASK_GEN_SETTINGS_DISTINCT_PERIOD_ONLY, taskSetGenerator.getDistinctPeriodOnly());
        /* ScheduLeak */
        // jsonConfig.put(JsonLogStr.TASK_GEN_SETTINGS_SCHEDULEAK_OBSERVER, taskSetGenerator.getNeedGenObserverTask());
        // jsonConfig.put(JsonLogStr.TASK_GEN_SETTINGS_SCHEDULEAK_MAX_OB_RATIO, taskSetGenerator.getMaxObservationRatio());
        // jsonConfig.put(JsonLogStr.TASK_GEN_SETTINGS_SCHEDULEAK_MIN_OB_RATIO, taskSetGenerator.getMinObservationRatio());
        // jsonConfig.put(JsonLogStr.TASK_GEN_SETTINGS_SCHEDULEAK_OBSERVER_PRIORITY, taskSetGenerator.getObserverTaskPriority());
        // jsonConfig.put(JsonLogStr.TASK_GEN_SETTINGS_SCHEDULEAK_VICTIM_PRIORITY, taskSetGenerator.getVictimTaskPriority());

        jsonConfigArrary.put(jsonConfig);
        jsonData.put(JsonLogStr.DATA_TASK_GEN_SETTINGS, jsonConfigArrary);

        jsonRoot.put(JsonLogStr.ROOT_DATA, jsonData);
        writeString(jsonRoot.toString(4) + "\n");
    }
}
