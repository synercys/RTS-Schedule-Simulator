package synercys.rts.util;

/**
 * Created by cy on 3/29/2018.
 */
public class JsonLogStr {
    /* data type */
    public static String DATA_TYPE_RT_TASK_GEN_SETTINGS = "rtTaskGen settings";
    public static String DATA_TYPE_SINGLE_TASKSET = "single taskset";
    public static String DATA_TYPE_TASKSETS = "tasksets";

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
    public static String TASK_TYPE = "type";
    public static String TASK_ARRIVAL_TYPE = "arrivalType";
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

}
