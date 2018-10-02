package synercys.rts.util;

/**
 * Created by cy on 3/29/2018.
 */
public class JsonLogStr {
    /* data type */
    public static String DATA_TYPE_RT_TASK_GEN_SETTINGS = "rtTaskGen settings";
    public static String DATA_TYPE_SINGLE_TASKSET = "single taskset";
    public static String DATA_TYPE_TASKSETS = "tasksets";
    public static String DATA_TYPE_RT_SIM_RAW_SCHEDULE = "rtSim raw schedule";

    /* key names */
    /* root */
    public static String ROOT_FORMAT_VERSION = "formatVersion";
    public static String ROOT_DATA_TYPE = "dataType";
    public static String ROOT_DATA = "data";

    /* data basics */
    public static String TICK_UNIT = "tickUnitInNs";

    /* tasksets */
    public static String DATA_TASKSETS = "tasksets";

    /* taskset */
    //public static String DATA_TASKSET = "taskset";
    public static String TASKSET_ID = "id";
    public static String TASKSET_TASKS = "tasks";

    /* task */
    public static String TASK_ID = "id";
    public static String TASK_NAME = "name";
    public static String TASK_TYPE = "type";    // type defined below
    public static String TASK_ARRIVAL_TYPE = "arrivalType"; // arrivalType defined below
    public static String TASK_PERIOD = "period";
    public static String TASK_DEADLINE = "deadline";
    public static String TASK_WCET = "wcet";
    public static String TASK_PRIORITY = "priority";
    public static String TASK_PHASE = "phase";

    /* taskArrival type */
    public static String TASK_ARRIVAL_TYPE_PERIODIC = "periodic";
    public static String TASK_ARRIVAL_TYPE_SPORADIC = "sporadic";

    /* RtTaskGen */
    public static String DATA_TASK_GEN_SETTINGS = "configs";

    /* rtSim */
    public static String DATA_RT_SIM_SCHEDULE_INTERVAL_EVENTS = "scheduleIntervalEvents";
    public static String DATA_RT_SIM_TASK_INSTANT_EVENTS = "taskInstantEvents";

    /* scheduleIntervalEvents (interval events) */
    public static String SCHEDULE_INTERVAL_EVENT_TASK_ID = "taskId";
    public static String SCHEDULE_INTERVAL_EVENT_BEGIN_TIME = "begin";
    public static String SCHEDULE_INTERVAL_EVENT_END_TIME = "end";
    public static String SCHEDULE_INTERVAL_EVENT_BEGIN_STATE = "beginState";    // state defined below
    public static String SCHEDULE_INTERVAL_EVENT_END_STATE = "endState";    // state defined below
    public static String SCHEDULE_INTERVAL_EVENT_NOTE = "note";

    /* beginState and endState (interval event state) */
    public static String SCHEDULE_INTERVAL_EVENT_STATE_UNKNOWN = "unknown";
    public static String SCHEDULE_INTERVAL_EVENT_STATE_START = "start";
    public static String SCHEDULE_INTERVAL_EVENT_STATE_RESUME = "resume";
    public static String SCHEDULE_INTERVAL_EVENT_STATE_SUSPEND = "suspend";
    public static String SCHEDULE_INTERVAL_EVENT_STATE_END = "end";

    /* taskInstantEvents */
    public static String TASK_INSTANT_EVENT_TASK_ID = "taskId";
    public static String TASK_INSTANT_EVENT_BEGIN = "begin";
    public static String TASK_INSTANT_EVENT_RECORD = "record";
    public static String TASK_INSTANT_EVENT_NOTE = "note";


}
