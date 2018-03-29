package synercys.rts.util;

import cy.utility.file.FileHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import synercys.rts.event.EventContainer;
import synercys.rts.framework.Task;
import synercys.rts.framework.TaskSet;
import synercys.rts.simulator.TaskSetContainer;
import synercys.rts.simulator.TaskSetGenerator;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by cy on 3/29/2018.
 */
public class JsonLogLoader extends FileHandler implements LogParser{
    final static String PARSER_VERSION = "12";

    Object result = null;   // An abstract object that supports all types of results.

    EventContainer eventContainer = null;

    // For tasksets type of data
    TaskSetContainer taskSetContainer = null;

    public JsonLogLoader(String filePath) {
        eventContainer = new EventContainer();
        taskSetContainer = new TaskSetContainer();
        openFile(filePath);
        parseLog(fileReader);
    }

    @Override
    public int getParserVersion() {
        return Integer.valueOf(PARSER_VERSION).intValue();
    }

    @Override
    public Boolean parseLog(BufferedReader fileReader) {
        JSONObject jsonRoot = new JSONObject(bufferedReaderToString(fileReader));
        String dataType = jsonRoot.getString(JsonLogStr.ROOT_DATA_TYPE);

        if (dataType.equalsIgnoreCase(JsonLogStr.DATA_TYPE_SINGLE_TASKSET) || dataType.equalsIgnoreCase(JsonLogStr.DATA_TYPE_TASKSETS)) {
            loadTaskSets(jsonRoot.getJSONObject(JsonLogStr.ROOT_DATA));
            eventContainer.setTaskSet(taskSetContainer.getTaskSets().get(0));
        } else if (dataType.equalsIgnoreCase(JsonLogStr.DATA_TYPE_RT_TASK_GEN_SETTINGS)) {
            loadRtTaskGenSettings(jsonRoot.getJSONObject(JsonLogStr.ROOT_DATA));
        } else {
            return false;
        }

        return true;
    }

    @Override
    public EventContainer getEventContainer() {
        return eventContainer;
    }

    protected String bufferedReaderToString(BufferedReader br) {
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

        return sb.toString();
    }

    protected TaskSet jsonToTaskSet(JSONObject jsonTaskSet) {
        TaskSet taskSet = new TaskSet();

        taskSet.setId(jsonTaskSet.getInt(JsonLogStr.TASKSET_ID));

        int taskSize = jsonTaskSet.getJSONArray(JsonLogStr.TASKSET_TASKS).length();
        for (int i=0; i<taskSize; i++) {
            JSONObject jsonTask = jsonTaskSet.getJSONArray(JsonLogStr.TASKSET_TASKS).getJSONObject(i);
            taskSet.addTask(
                    jsonTask.getInt(JsonLogStr.TASK_ID),
                    jsonTask.getString(JsonLogStr.TASK_NAME),
                    jsonTask.getInt(JsonLogStr.TASK_TYPE),
                    jsonTask.getLong(JsonLogStr.TASK_PERIOD),
                    jsonTask.getLong(JsonLogStr.TASK_DEADLINE),
                    jsonTask.getLong(JsonLogStr.TASK_WCET),
                    jsonTask.getInt(JsonLogStr.TASK_PRIORITY)
                    );
        }

        return taskSet;
    }


    /**
     * Load tasksets into taskContainer from given jsonData (in JsonLogStr.DATA_TYPE_TASKSETS type)
     * Note that it takes the first taskset for the eventContainer's taskset.
     * @param jsonData json format object corresponding to JsonLogStr.DATA_TYPE_TASKSETS type
     * @return taskSetContainer which is a class level variable
     */
    protected TaskSetContainer loadTaskSets(JSONObject jsonData) {
        //taskSetContainer = new TaskSetContainer();

        JSONArray jsonTaskSetArray = jsonData.getJSONArray(JsonLogStr.DATA_TASKSETS);
        int taskSetSize = jsonTaskSetArray.length();
        for (int i=0; i<taskSetSize; i++) {
            JSONObject jsonTaskSet = jsonTaskSetArray.getJSONObject(i);
            taskSetContainer.addTaskSet(jsonToTaskSet(jsonTaskSet));
        }

        eventContainer.setTaskSet(taskSetContainer.getTaskSets().get(0));
        result = taskSetContainer;

        return taskSetContainer;
    }

    protected ArrayList<TaskSetGenerator> loadRtTaskGenSettings(JSONObject jsonData) {
        ArrayList<TaskSetGenerator> taskSetGenerators = new ArrayList<>();

        JSONArray jsonGenConfigArray = jsonData.getJSONArray(JsonLogStr.DATA_TASK_GEN_SETTINGS);
        int configSize = jsonGenConfigArray.length();
        for (int i=0; i<configSize; i++) {
            TaskSetGenerator taskSetGenerator = new TaskSetGenerator();
            taskSetGenerators.add(taskSetGenerator);
            JSONObject jsonGenConfig = jsonGenConfigArray.getJSONObject(i);

            taskSetGenerator.setMaxHyperPeriod(jsonGenConfig.getInt("maxHyperPeriod"));
            taskSetGenerator.setMaxPeriod(jsonGenConfig.getInt("maxPeriod"));
            taskSetGenerator.setMinPeriod(jsonGenConfig.getInt("minPeriod"));
            taskSetGenerator.setMaxWcet(jsonGenConfig.getInt("maxWcet"));
            taskSetGenerator.setMinWcet(jsonGenConfig.getInt("minWcet"));
            taskSetGenerator.setMaxInitOffset(jsonGenConfig.getInt("maxInitOffset"));
            taskSetGenerator.setMinInitOffset(jsonGenConfig.getInt("minInitOffset"));
            taskSetGenerator.setMaxUtil(jsonGenConfig.getDouble("maxUtil"));
            taskSetGenerator.setMinUtil(jsonGenConfig.getDouble("minUtil"));
            taskSetGenerator.setNumTaskPerSet(jsonGenConfig.getInt("numTaskPerSet"));
            taskSetGenerator.setNumTaskSet(jsonGenConfig.getInt("numTaskSet"));
            taskSetGenerator.setGenerateFromHpDivisors(jsonGenConfig.getBoolean("generateFromHpDivisors"));
            taskSetGenerator.setNonHarmonicOnly(jsonGenConfig.getBoolean("nonHarmonicOnly"));
            taskSetGenerator.setNeedGenObserverTask(jsonGenConfig.getBoolean("needGenObserverTask"));
            taskSetGenerator.setMaxObservationRatio(jsonGenConfig.getDouble("maxObservationRatio"));
            taskSetGenerator.setMinObservationRatio(jsonGenConfig.getDouble("minObservationRatio"));
            taskSetGenerator.setObserverTaskPriority(jsonGenConfig.getInt("observerTaskPriority"));
            taskSetGenerator.setVictimTaskPriority(jsonGenConfig.getInt("victimTaskPriority"));
        }

        result = taskSetGenerators;
        return taskSetGenerators;
    }

    public TaskSetContainer getTaskSetContainer() {
        return taskSetContainer;
    }

    public Object getResult() {
        return result;
    }

    public static void main(String[] args) {
        JsonLogLoader jsonLogLoader = new JsonLogLoader("sampleLogs\\jsontask_out.txt");
        TaskSetContainer taskSetContainer = (TaskSetContainer) jsonLogLoader.getResult();
        System.out.println(taskSetContainer.getTaskSets().get(0).toString());
    }
}
